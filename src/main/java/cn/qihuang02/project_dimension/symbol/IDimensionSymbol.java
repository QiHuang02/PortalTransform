package cn.qihuang02.project_dimension.symbol;

import cn.qihuang02.project_dimension.register.DimensionSymbol;

public interface IDimensionSymbol {
    int resolve(DimensionSymbolVector vector);

    default String key() {
        DimensionSymbol annotation = getClass().getAnnotation(DimensionSymbol.class);
        if (annotation == null) {
            throw new IllegalStateException(getClass().getName() + " is missing the @DimensionSymbol annotation");
        }
        return annotation.value();
    }

    default String displayName() {
        DimensionSymbol annotation = getClass().getAnnotation(DimensionSymbol.class);
        if (annotation == null) {
            throw new IllegalStateException(getClass().getName() + " is missing the @DimensionSymbol annotation");
        }
        return annotation.displayName();
    }

    default boolean compound() {
        DimensionSymbol annotation = getClass().getAnnotation(DimensionSymbol.class);
        if (annotation == null) {
            throw new IllegalStateException(getClass().getName() + " is missing the @DimensionSymbol annotation");
        }
        return annotation.compound();
    }
}
