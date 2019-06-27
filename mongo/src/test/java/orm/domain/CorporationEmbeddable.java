package orm.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author jzb 2019-06-21
 */
@Embeddable
public class CorporationEmbeddable implements Serializable {
    @Getter
    @Setter
    @Id
    private String id;
    @Getter
    @Setter
    @Column(unique = true)
    @NotBlank
    private String code;
}
