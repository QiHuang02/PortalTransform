package cn.qihuang02.project_dimension.symbol;

import cn.qihuang02.project_dimension.symbol.defaults.EndDimensionSymbol;
import cn.qihuang02.project_dimension.symbol.defaults.NetherDimensionSymbol;
import cn.qihuang02.project_dimension.symbol.defaults.OverworldDimensionSymbol;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DimensionSymbolPresetRegistry {
    private static final List<DimensionSymbolPreset> BUILTIN_PRESETS = List.of(
            new OverworldDimensionSymbol(),
            new NetherDimensionSymbol(),
            new EndDimensionSymbol()
    );

    private DimensionSymbolPresetRegistry() {
    }

    public static List<DimensionSymbolPreset> builtins() {
        return BUILTIN_PRESETS;
    }

    public static Map<ResourceKey<Level>, DimensionSymbolVector> defaultSymbols() {
        Map<ResourceKey<Level>, DimensionSymbolVector> defaults = new LinkedHashMap<>();
        for (DimensionSymbolPreset preset : BUILTIN_PRESETS) {
            ResourceKey<Level> dimension = preset.dimension();
            if (defaults.put(dimension, preset.symbols().clamp()) != null) {
                throw new IllegalStateException("重复注册维度象征预设：" + dimension.location());
            }
        }
        return Map.copyOf(defaults);
    }
}
