package com.github.ixtf.persistence.lucene;

/**
 * @author jzb 2018-08-24
 */

import com.github.ixtf.japp.core.J;
import com.github.ixtf.persistence.IEntity;
import com.github.ixtf.persistence.IEntityLoggable;
import com.github.ixtf.persistence.IOperator;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseLucene<T extends IEntity> {
    @Getter
    protected final Class<T> entityClass;
    protected final IndexWriter indexWriter;
    protected final DirectoryTaxonomyWriter taxoWriter;
    protected final FacetsConfig facetsConfig;

    @SneakyThrows
    protected BaseLucene(Path indexPath, Path taxoPath) {
        final ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        entityClass = (Class<T>) parameterizedType.getActualTypeArguments()[0];
        indexWriter = new IndexWriter(FSDirectory.open(indexPath), new IndexWriterConfig(new SmartChineseAnalyzer()));
        taxoWriter = new DirectoryTaxonomyWriter(FSDirectory.open(taxoPath));
        facetsConfig = facetsConfig();
    }

    protected abstract FacetsConfig facetsConfig();

    protected abstract Document document(T entity);

    @SneakyThrows
    public IndexReader indexReader() {
        return DirectoryReader.open(indexWriter);
    }

    public DirectoryTaxonomyReader taxoReader() throws IOException {
        return new DirectoryTaxonomyReader(taxoWriter);
    }

    public void index(T entity) throws Exception {
        final Term term = new Term("id", entity.getId());
        if (entity.isDeleted()) {
            indexWriter.deleteDocuments(term);
        } else {
            indexWriter.updateDocument(term, facetsConfig.build(taxoWriter, document(entity)));
        }
        indexWriter.commit();
        taxoWriter.commit();
    }

    public void delete(String id) throws IOException {
        final Term term = new Term("id", id);
        indexWriter.deleteDocuments(term);
        indexWriter.commit();
        taxoWriter.commit();
    }

    protected Document id(String id) {
        Validate.notBlank(id);
        final Document doc = new Document();
        doc.add(new StringField("id", id, Store.YES));
        return doc;
    }

    protected Document add(Document doc, String fieldName, boolean b) {
        doc.add(new IntPoint(fieldName, b ? 1 : 0));
        return doc;
    }

    protected BooleanQuery.Builder add(BooleanQuery.Builder builder, String fieldName, boolean b) {
        builder.add(IntPoint.newExactQuery(fieldName, b ? 1 : 0), BooleanClause.Occur.MUST);
        return builder;
    }

    protected Document add(Document doc, String fieldName, Enum e) {
        return add(doc, fieldName, e.name());
    }

    protected BooleanQuery.Builder add(BooleanQuery.Builder builder, String fieldName, Enum e) {
        Optional.ofNullable(e).map(Enum::name).ifPresent(it -> add(builder, fieldName, it));
        return builder;
    }

    protected Document add(Document doc, String fieldName, String s) {
        Optional.ofNullable(s).filter(J::nonBlank).ifPresent(it -> doc.add(new StringField(fieldName, s, Store.NO)));
        return doc;
    }

    protected BooleanQuery.Builder add(BooleanQuery.Builder builder, String fieldName, String s) {
        Optional.ofNullable(s).filter(J::nonBlank)
                .map(it -> new TermQuery(new Term(fieldName, s)))
                .ifPresent(it -> builder.add(it, BooleanClause.Occur.MUST));
        return builder;
    }

    protected BooleanQuery.Builder add(BooleanQuery.Builder builder, String fieldName, Collection<String> ss) {
        if (J.nonEmpty(ss)) {
            final BooleanQuery.Builder subBuilder = new BooleanQuery.Builder();
            ss.stream().filter(J::nonBlank).forEach(it ->
                    subBuilder.add(new TermQuery(new Term(fieldName, it)), BooleanClause.Occur.SHOULD)
            );
            builder.add(subBuilder.build(), BooleanClause.Occur.MUST);
        }
        return builder;
    }

    protected Document add(Document doc, String fieldName, IOperator operator) {
        Optional.ofNullable(operator)
                .map(IOperator::getId)
                .ifPresent(it -> doc.add(new StringField(fieldName, it, Store.NO)));
        return doc;
    }

    protected Document add(Document doc, String fieldName, Date date) {
        Optional.ofNullable(date).map(Date::getTime).ifPresent(it -> {
            doc.add(new LongPoint(fieldName, it));
            doc.add(new NumericDocValuesField(fieldName, it));
        });
        return doc;
    }

    protected Document add(Document doc, IEntityLoggable entity) {
        add(doc, "creator", entity.getCreator());
        add(doc, "createDateTime", entity.getCreateDateTime());
        add(doc, "modifier", entity.getModifier());
        add(doc, "modifyDateTime", entity.getModifyDateTime());
        return doc;
    }

    public Collection<String> query(Query query) throws IOException {
        @Cleanup final IndexReader indexReader = indexReader();
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
        return baseQuery(searcher, topDocs);
    }

    public Collection<String> baseQuery(Query query, Sort sort) throws IOException {
        @Cleanup final IndexReader indexReader = indexReader();
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE, sort);
        return baseQuery(searcher, topDocs);
    }

    private Collection<String> baseQuery(IndexSearcher searcher, TopDocs topDocs) {
        return Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public Pair<Long, Collection<String>> baseQuery(Query query, int first, int pageSize) {
        @Cleanup final IndexReader indexReader = indexReader();
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(query, first + pageSize);
        return baseQuery(searcher, topDocs, first);
    }

    @SneakyThrows
    public Pair<Long, Collection<String>> baseQuery(Query query, int first, int pageSize, Sort sort) {
        @Cleanup final IndexReader indexReader = indexReader();
        final IndexSearcher searcher = new IndexSearcher(indexReader);
        final TopDocs topDocs = searcher.search(query, first + pageSize, sort);
        return baseQuery(searcher, topDocs, first);
    }

    private Pair<Long, Collection<String>> baseQuery(IndexSearcher searcher, TopDocs topDocs, int first) {
        if (topDocs.totalHits < 1) {
            return Pair.of(topDocs.totalHits, Collections.EMPTY_LIST);
        }
        final List<String> ids = Arrays.stream(topDocs.scoreDocs)
                .skip(first)
                .map(scoreDoc -> toDocument(searcher, scoreDoc))
                .map(it -> it.get("id"))
                .collect(Collectors.toList());
        return Pair.of(topDocs.totalHits, ids);
    }

    @SneakyThrows
    private Document toDocument(IndexSearcher searcher, ScoreDoc scoreDoc) {
        return searcher.doc(scoreDoc.doc);
    }

    public void close() throws IOException {
        taxoWriter.close();
        indexWriter.close();
    }
}
