package cn.qihuang02.project_dimension.symbol;

public record SymbolContext(
        DimensionSymbolVector sourceBase,
        DimensionSymbolVector targetBase,
        DimensionSymbolVector environmentModifier,
        DimensionSymbolVector effectiveSource,
        DimensionSymbolVector effectiveTarget,
        DimensionSymbolVector delta
) {
}
