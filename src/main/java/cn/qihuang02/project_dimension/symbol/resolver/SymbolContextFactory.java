package cn.qihuang02.project_dimension.symbol.resolver;

import cn.qihuang02.project_dimension.symbol.DimensionSymbolManager;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;
import cn.qihuang02.project_dimension.symbol.SymbolContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class SymbolContextFactory {
    public static SymbolContext create(ServerLevel sourceLevel, BlockPos pos, ResourceKey<Level> targetDimension) {
        Registry<DimensionSymbolVector> registry = sourceLevel.registryAccess()
                .registryOrThrow(DimensionSymbolManager.DIMENSION_SYMBOL_REGISTRY_KEY);

        DimensionSymbolVector sourceBase = DimensionSymbolManager.getBase(registry, sourceLevel.dimension());
        DimensionSymbolVector targetBase = DimensionSymbolManager.getBase(registry, targetDimension);
        DimensionSymbolVector environmentModifier = EnvironmentSymbolResolver.resolve(sourceLevel, pos);
        DimensionSymbolVector effectiveSource = sourceBase.plus(environmentModifier).clamp();
        DimensionSymbolVector effectiveTarget = targetBase.clamp();
        DimensionSymbolVector delta = effectiveTarget.minus(effectiveSource);
        return new SymbolContext(sourceBase, targetBase, environmentModifier, effectiveSource, effectiveTarget, delta);
    }
}
