package orm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import orm.LineListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 线别
 *
 * @author jzb 2018-06-22
 */
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(LineListener.class)
@Entity
public class Line implements EntityLoggable {
    @ToString.Include
    @EqualsAndHashCode.Include
    @Getter
    @Setter
    @Id
    @NotBlank
    private String id;
    @Getter
    @Setter
    @Column
    @NotNull
    private Workshop workshop;
    @Getter
    @Setter
    @Column
    private Workshop.WorkshopEmbeddable workshopEmbeddable;
    @ToString.Include
    @Getter
    @Setter
    @Column
    @NotBlank
    private String name;
    @Getter
    @Setter
    @Column
    private DoffingType doffingType;

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

    //    public Flowable<LineMachine> lineMachines() {
//        final LineMachineRepository lineMachineRepository = Jvertx.getProxy(LineMachineRepository.class);
//        return lineMachineRepository.listBy(this);
//    }
    @PostPersist
    @PostUpdate
    private void test() {
        System.out.println("@PrePersist @PreUpdate Test");
    }

}
