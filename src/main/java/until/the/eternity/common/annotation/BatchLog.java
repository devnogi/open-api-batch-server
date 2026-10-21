package until.the.eternity.common.annotation;

import java.lang.annotation.*;
import until.the.eternity.batchlog.domain.enums.BatchType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BatchLog {

    BatchType type();
}
