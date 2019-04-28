package orm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author jzb 2019-02-14
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
public class PackageBox implements EntityLoggable, Serializable {
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
    private String code;
    @Getter
    @Setter
    @Column
    @Min(1)
    @NotNull
    private BigDecimal grossWeight;
    @Getter
    @Setter
    @Column
    @Min(1)
    @NotNull
    private BigDecimal netWeight;
    @Getter
    @Setter
    @Column
    @Min(1)
    @NotNull
    private int silkCount;
    @Getter
    @Setter
    @Column
    private List<Silk> silks;
    @Getter
    @Setter
    @Column
    @NotNull
    private Operator creator;
    @Getter
    @Setter
    @Column(name = "cdt")
    @NotNull
    private Date createDateTime;
    @Getter
    @Setter
    @Column
    private Operator modifier;
    @Getter
    @Setter
    @Column(name = "mdt")
    private Date modifyDateTime;
    @JsonIgnore
    @Getter
    @Setter
    @Column
    private boolean deleted;
}
