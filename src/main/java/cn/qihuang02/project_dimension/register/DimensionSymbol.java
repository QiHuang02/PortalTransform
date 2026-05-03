package cn.qihuang02.project_dimension.register;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DimensionSymbol {
    String value();

    String displayName();

    boolean compound() default false;

    int color() default 0xFFFFFF;
}
