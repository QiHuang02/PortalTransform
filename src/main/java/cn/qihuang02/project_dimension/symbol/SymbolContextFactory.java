package cn.qihuang02.project_dimension.symbol;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class SymbolContextFactory {
    private SymbolContextFactory() {
    }

    public static SymbolContext create(ServerLevel sourceLevel, BlockPos pos, ResourceKey<Level> targetDimension) {
        DimensionSymbolVector sourceBase = DimensionSymbolManager.INSTANCE.getBase(sourceLevel.dimension());
        DimensionSymbolVector targetBase = DimensionSymbolManager.INSTANCE.getBase(targetDimension);
        DimensionSymbolVector environmentModifier = EnvironmentSymbolResolver.resolve(sourceLevel, pos);
        DimensionSymbolVector effectiveSource = sourceBase.plus(environmentModifier).clamp();
        DimensionSymbolVector effectiveTarget = targetBase.clamp();
        DimensionSymbolVector delta = effectiveTarget.minus(effectiveSource);
        return new SymbolContext(sourceBase, targetBase, environmentModifier, effectiveSource, effectiveTarget, delta);
    }
}
