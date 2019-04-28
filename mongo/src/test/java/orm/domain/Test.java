package orm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ixtf.persistence.IEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Collection;

/**
 * @author jzb 2019-03-01
 */
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
public class Test implements IEntity {
    @Getter
    @Setter
    @Id
    private String id;
    @Getter
    @Setter
    @Column
    private Collection<String> strings;
    @JsonIgnore
    @Getter
    @Setter
    @Column
    private boolean deleted;
}
