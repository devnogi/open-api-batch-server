package until.the.eternity.common.annotation;

import until.the.eternity.batchlog.domain.enums.BatchType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BatchLog {

    BatchType type();
}
