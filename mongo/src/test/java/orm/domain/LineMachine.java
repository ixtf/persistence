package orm.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author jzb 2019-02-14
 */
@Entity
public class LineMachine implements Serializable {
    @Getter
    @Setter
    @Id
    private String id;
    @Getter
    @Setter
    @ToString.Include
    @Column
    private Line line;
    /**
     * 机台位号
     */
    @Getter
    @Setter
    @ToString.Include
    @Column
    private int item;
}
