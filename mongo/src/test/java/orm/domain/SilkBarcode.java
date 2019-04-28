package orm.domain;

import com.google.common.base.Strings;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * 落次编码，数值类型全部36进制
 * 生成规则：
 * 4位日期        数值类型： 初始2018-11-11，相差天数，36进制
 * 5位落次        数值类型： 落次流水号，按天自增，36进制
 * <p>
 * 丝锭条码
 * 2位锭号        数值类型，10进制
 *
 * @author jzb 2018-08-08
 */
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class SilkBarcode implements Serializable {
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
    private String code;
    /**
     * 条码生成日期
     */
    @Getter
    @Setter
    @ToString.Include
    @Column
    @NotBlank
    private Date codeDate;
    /**
     * 条码生成落次，自增
     */
    @Getter
    @Setter
    @Column
    private long codeDoffingNum;
    @Getter
    @Setter
    @Column
    private LineMachine lineMachine;
    /**
     * 人工输入落次
     */
    @Getter
    @Setter
    @ToString.Include
    @Column
    private String doffingNum;

    public String generateSilkCode(int spindle) {
        final String spindleCode = spindleCode(spindle);
        return String.join("", getCode(), spindleCode, workshopCode()).toUpperCase();
    }

    private String spindleCode(int spindle) {
        final String s = Integer.toString(spindle);
        return Strings.padStart(s, 2, '0');
    }

    private String workshopCode() {
        final LineMachine lineMachine = getLineMachine();
        final Line line = lineMachine.getLine();
        final Workshop workshop = line.getWorkshop();
        return workshop.getCode();
    }

}
