package orm.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.Document;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;

/**
 * @author jzb 2019-02-14
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
public class SilkCarRecord implements Serializable {
    @EqualsAndHashCode.Include
    @ToString.Include
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
    private SilkCar silkCar;
    @Getter
    @Setter
    @Convert(converter = InitSilkAttributeConverter.class)
    @Column
    @Size(min = 1)
    @NotNull
    private Collection<InitSilk> initSilks;

    @Embeddable
    public static class InitSilk implements Serializable {
        @Getter
        @Setter
        @Column
        @NotBlank
        private String id;
        @Getter
        @Setter
        @Column
        @NotBlank
        private String code;
        @Getter
        @Setter
        @Column
        @Min(1)
        private int row;
        @Getter
        @Setter
        @Column
        @Min(1)
        private int col;
    }

    /**
     * @author jzb 2019-02-14
     */
    public static class InitSilkAttributeConverter implements AttributeConverter<String, Document> {
        @Override
        public Document convertToDatabaseColumn(String s) {
            return null;
        }

        @Override
        public String convertToEntityAttribute(Document document) {
            return null;
        }
    }
}
