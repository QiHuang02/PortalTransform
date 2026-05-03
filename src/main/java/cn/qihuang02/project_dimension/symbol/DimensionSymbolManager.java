package cn.qihuang02.project_dimension.symbol;

import cn.qihuang02.project_dimension.ProjectDimension;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class DimensionSymbolManager {
    public static final ResourceKey<Registry<DimensionSymbolVector>> DIMENSION_SYMBOL_REGISTRY_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(ProjectDimension.MODID, "dimension_symbols")
            );

    @NotNull
    public static DimensionSymbolVector getBase(Registry<DimensionSymbolVector> registry, ResourceKey<Level> dimension) {
        DimensionSymbolVector vector = registry.get(dimension.location());
        if (vector == null) {
            ProjectDimension.LOGGER.debug("Dimension {} has no declared symbol vector, using zero vector", dimension.location());
            return DimensionSymbolVector.ZERO;
        }
        return vector;
    }
}
