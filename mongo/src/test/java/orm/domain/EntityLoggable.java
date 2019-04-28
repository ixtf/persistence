package orm.domain;

import com.github.ixtf.persistence.IEntity;

import java.util.Date;

/**
 * @author jzb 2019-02-14
 */
public interface EntityLoggable extends IEntity {

    Operator getCreator();

    void setCreator(Operator operator);

    Date getCreateDateTime();

    void setCreateDateTime(Date date);

    Operator getModifier();

    void setModifier(Operator operator);

    Date getModifyDateTime();

    void setModifyDateTime(Date date);

    default void _log(Operator operator) {
        _log(operator, new Date());
    }

    default void _log(Operator operator, Date date) {
        if (getCreator() == null) {
            setCreator(operator);
            setCreateDateTime(date);
        } else {
            setModifier(operator);
            setModifyDateTime(date);
        }
    }
}
