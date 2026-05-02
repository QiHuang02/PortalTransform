package cn.qihuang02.project_dimension.symbol.resolver;

import cn.qihuang02.project_dimension.register.DimensionSymbolRegistry;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;
import cn.qihuang02.project_dimension.symbol.IDimensionSymbol;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CompoundSymbolResolver {

    public static Map<String, Integer> resolve(DimensionSymbolVector vector) {
        Map<String, Integer> compounds = new LinkedHashMap<>();
        for (IDimensionSymbol symbol : DimensionSymbolRegistry.compounds()) {
            compounds.put(symbol.key(), symbol.resolve(vector));
        }
        return Map.copyOf(compounds);
    }
}
