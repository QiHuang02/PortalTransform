package cn.qihuang02.project_dimension.symbol;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface DimensionSymbolPreset {
    ResourceKey<Level> dimension();

    DimensionSymbolVector symbols();

    default DimensionSymbolDefinition definition() {
        return new DimensionSymbolDefinition(dimension(), symbols());
    }
}
