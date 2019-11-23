package orm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.persistence.IEntity;
import lombok.*;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 公司
 *
 * @author jzb 2018-06-21
 */
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Cacheable
@Entity
public class Corporation implements IEntity {
    @ToString.Include
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    @Id
    @NotBlank
    private String id;
    /**
     * 编码，最好和 SAP 编码统一
     */
    @ToString.Include
    @Getter
    @Setter
    @Column(unique = true)
    @NotBlank
    private String code;
    @JsonIgnore
    @Getter
    @Setter
    @Column
    @Size(min = 2, max = 2)
    @NotBlank
    private String packageCode;
    @ToString.Include
    @Getter
    @Setter
    @Column
    @NotBlank
    private String name;
    @JsonIgnore
    @Getter
    @Setter
    @Column
    private boolean deleted;

}
