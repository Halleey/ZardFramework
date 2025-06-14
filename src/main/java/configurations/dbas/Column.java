package configurations.dbas;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String value() default "";

    boolean required() default false;

    BlobType blobType () default BlobType.NONE;

}

