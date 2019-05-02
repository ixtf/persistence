package orm;

import com.github.ixtf.persistence.IEntity;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

/**
 * @author jzb 2019-02-14
 */
public class LineListener {
    @PostPersist
    @PostUpdate
    private void test(IEntity line) {
        System.out.println("@PostPersist @PostUpdate LineListener Test");
    }

}
