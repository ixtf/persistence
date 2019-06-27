package orm.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author jzb 2019-06-21
 */
@Embeddable
public class WorkshopEmbeddable implements Serializable {
    @Getter
    @Setter
    @Id
    private String id;
    @Getter
    @Setter
    @Column
    private String name;
    @Getter
    @Setter
    @Column(name = "corporation")
    private CorporationEmbeddable corporation;
}
