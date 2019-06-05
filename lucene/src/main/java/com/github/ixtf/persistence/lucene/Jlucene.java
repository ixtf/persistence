package com.github.ixtf.persistence.lucene;

import com.github.ixtf.japp.core.J;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;

import java.util.Collection;
import java.util.Optional;

/**
 * @author jzb 2019-05-29
 */
public class Jlucene {

    public static BooleanQuery.Builder add(BooleanQuery.Builder builder, String fieldName, boolean b) {
        builder.add(IntPoint.newExactQuery(fieldName, b ? 1 : 0), BooleanClause.Occur.MUST);
        return builder;
    }

    protected BooleanQuery.Builder add(BooleanQuery.Builder builder, String fieldName, Enum e) {
        Optional.ofNullable(e).map(Enum::name).ifPresent(it -> add(builder, fieldName, it));
        return builder;
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
}
