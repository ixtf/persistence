package orm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;
import lombok.*;
import org.bson.Document;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

import static java.util.stream.Collectors.*;

/**
 * @author jzb 2018-07-27
 */
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(indexes = {
        @Index(name = "_doffingType", columnList = "doffingType"),
        @Index(name = "_workshop", columnList = "workshop"),
        @Index(name = "_line", columnList = "line"),
        @Index(name = "_row", columnList = "row"),
        @Index(name = "_col", columnList = "col"),
})
public class DoffingSpec implements EntityLoggable {
    @ToString.Include
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    @Id
    @NotBlank
    private String id;
    @ToString.Include
    @Getter
    @Setter
    @Column
    @NotBlank
    private String name;
    @Getter
    @Setter
    @Column
    @NotNull
    private DoffingType doffingType;
    @Getter
    @Setter
    @Column
    private Workshop workshop;
    @Getter
    @Setter
    @Column
    private Line line;
    @Getter
    @Setter
    @Column
    private int row;
    @Getter
    @Setter
    @Column
    private int col;
    @Getter
    @Setter
    @Column
    private int lineMachineCount;
    @Getter
    @Setter
    @Column
    private int spindleNum;
    @Getter
    @Setter
    @Column
    private Set<CheckSpec> checkSpecs;
    @Getter
    @Setter
    @Column
    @Convert(converter = LineMachineSpecsSetConverter.class)
    private Set<Set<LineMachineSpec>> lineMachineSpecs;

    @JsonIgnore
    @Getter
    @Setter
    @Column
    @NotNull
    private Operator creator;
    @JsonIgnore
    @Getter
    @Setter
    @Column(name = "cdt")
    @NotNull
    private Date createDateTime;
    @JsonIgnore
    @Getter
    @Setter
    @Column
    private Operator modifier;
    @JsonIgnore
    @Getter
    @Setter
    @Column(name = "mdt")
    private Date modifyDateTime;
    @JsonIgnore
    @Getter
    @Setter
    @Column
    private boolean deleted;

    @PrePersist
    public void generateName() {
        if (J.isBlank(name)) {
            final String s = Optional.ofNullable(workshop)
                    .map(Workshop::getCode)
                    .orElseGet(() -> line.getName());
            name = s + "_" + doffingType + "_" + row + "X" + col;
        }
    }

    public void checkValid() {
        final int silkCount = lineMachineCount * spindleNum;
        final int cap = row * col * 2;
        if (silkCount > cap) {
            throw new RuntimeException("容量超出!");
        }
        if (checkSpecs.size() != lineMachineCount) {
            throw new RuntimeException("容量超出!");
        }
        lineMachineSpecs.forEach(set -> {
            if (set.size() != lineMachineCount) {
                throw new RuntimeException("容量超出!");
            }
            set.forEach(lineMachineSpec -> {
                final int size = lineMachineSpec.items.size();
                if (size != spindleNum) {
                    throw new RuntimeException("容量超出!");
                }
            });
        });
    }

    @NoArgsConstructor
    @Data
    @Embeddable
    public static class CheckSpec implements Serializable, Comparable<CheckSpec> {
        private int orderBy;
        private Set<Position> positions;

        synchronized public CheckSpec addPosition(SilkCarSideType sideType, int row, int col) {
            final Position position = new Position();
            position.setSideType(sideType);
            position.setRow(row);
            position.setCol(col);
            if (positions == null) {
                positions = Sets.newHashSet(position);
            } else {
                positions.add(position);
            }
            return this;
        }

        @Override
        public int compareTo(CheckSpec o) {
            return ComparisonChain.start()
                    .compare(orderBy, o.orderBy)
                    .result();
        }
    }

    @NoArgsConstructor
    @Data
    @Embeddable
    public static class Position implements Serializable {
        @ToString.Include
        private SilkCarSideType sideType;
        @ToString.Include
        private int row;
        @ToString.Include
        private int col;
    }

    @NoArgsConstructor
    @Data
    @Embeddable
    public static class LineMachineSpec implements Serializable, Comparable<LineMachineSpec> {
        private int orderBy;
        private Set<PositionAndSpindle> items;

        public static LineMachineSpec from(Document document) {
            final LineMachineSpec lineMachineSpec = new LineMachineSpec();
            lineMachineSpec.orderBy = document.getInteger("orderBy");
            lineMachineSpec.items = document.getList("item", List.class, List.of())
                    .parallelStream()
                    .map(Document.class::cast)
                    .map(PositionAndSpindle::from)
                    .collect(toSet());
            return lineMachineSpec;
        }

        synchronized public LineMachineSpec addItem(SilkCarSideType sideType, int row, int col, int spindle) {
            final PositionAndSpindle item = new PositionAndSpindle();
            item.setSideType(sideType);
            item.setRow(row);
            item.setCol(col);
            item.setSpindle(spindle);
            if (items == null) {
                items = Sets.newHashSet(item);
            } else {
                items.add(item);
            }
            return this;
        }

        @Override
        public int compareTo(LineMachineSpec o) {
            return ComparisonChain.start()
                    .compare(orderBy, o.orderBy)
                    .result();
        }

        public Document toDocument() {
            final Document ret = new Document().append("orderBy", orderBy);
            final List<Document> itemDocs = items.stream().map(PositionAndSpindle::toDocument).collect(toList());
            return ret.append("items", itemDocs);
        }
    }

    @NoArgsConstructor
    @Data
    @Embeddable
    public static class PositionAndSpindle implements Serializable {
        @ToString.Include
        private SilkCarSideType sideType;
        @ToString.Include
        private int row;
        @ToString.Include
        private int col;
        @ToString.Include
        private int spindle;

        public static PositionAndSpindle from(Document document) {
            final PositionAndSpindle positionAndSpindle = new PositionAndSpindle();
            positionAndSpindle.sideType = SilkCarSideType.valueOf(document.getString("sideType"));
            positionAndSpindle.row = document.getInteger("row");
            positionAndSpindle.col = document.getInteger("col");
            positionAndSpindle.spindle = document.getInteger("spindle");
            return positionAndSpindle;
        }

        public Document toDocument() {
            return new Document()
                    .append("sideType", sideType.name())
                    .append("row", row)
                    .append("col", col)
                    .append("spindle", spindle);
        }
    }

    public static class LineMachineSpecsSetConverter implements AttributeConverter<Set<Set<LineMachineSpec>>, List> {
        @Override
        public List convertToDatabaseColumn(Set<Set<LineMachineSpec>> attribute) {
            return attribute.parallelStream().map(it -> it.parallelStream()
                    .map(LineMachineSpec::toDocument)
                    .collect(toList())
            ).collect(toList());
        }

        @Override
        public Set<Set<LineMachineSpec>> convertToEntityAttribute(List dbData) {
            final List<List<Document>> lists = dbData;
            return lists.parallelStream().map(it -> it.parallelStream()
                    .map(LineMachineSpec::from)
                    .collect(toCollection(TreeSet::new))
            ).collect(toSet());
        }
    }

}
