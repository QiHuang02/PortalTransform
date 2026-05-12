package cn.qihuang02.portaltransform.symbol.environment;

import cn.qihuang02.portaltransform.registry.PTRegistries;
import cn.qihuang02.portaltransform.symbol.DimensionSymbolVector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 被动环境修正解析服务。
 * <p>无状态，接收 level + pos，返回环境修正向量。</p>
 */
public final class PassiveEnvironmentResolver {
    /**
     * 被动环境修正的单象征最大绝对值上限。
     * <p>劣化嬗变使用此硬编码值；仪式嬗变由仪式核心方块属性决定，不走此常量。</p>
     */
    private static final float PASSIVE_MODIFIER_CAP = 5.0F;

    /**
     * 解析指定位置的被动环境修正向量。
     *
     * @param level 服务端世界
     * @param pos   方块坐标
     * @return 环境修正向量（已 clamp）
     */
    public static @NotNull DimensionSymbolVector resolve(@NotNull ServerLevel level, @NotNull BlockPos pos) {
        Map<ResourceLocation, Float> accumulated = new LinkedHashMap<>();

        // 从 datapack 注册表获取所有环境修正条目
        var registry = level.registryAccess().registryOrThrow(PTRegistries.ENVIRONMENT_MODIFIERS);

        for (var entry : registry.entrySet()) {
            EnvironmentModifier modifier = entry.getValue();
            if (shouldApply(modifier, level, pos)) {
                // 叠加修正值
                for (Map.Entry<ResourceLocation, Float> mod : modifier.modifiers().entrySet()) {
                    accumulated.merge(mod.getKey(), mod.getValue(), Float::sum);
                }
            }
        }

        // per-symbol clamp 到 ±passiveCap
        DimensionSymbolVector vector = new DimensionSymbolVector(accumulated);
        return vector.clamp(-PASSIVE_MODIFIER_CAP, PASSIVE_MODIFIER_CAP);
    }

    /**
     * 判断修正条目是否在当前环境下生效。
     */
    private static boolean shouldApply(
            @NotNull EnvironmentModifier modifier,
            @NotNull ServerLevel level,
            @NotNull BlockPos pos
    ) {
        return switch (modifier) {
            case EnvironmentModifier.WeatherModifier weather -> weather.condition().matches(level);
            case EnvironmentModifier.TimeModifier time -> {
                long dayTime = level.getDayTime() % 24000L;
                yield dayTime >= time.minTick() && dayTime <= time.maxTick();
            }
            case EnvironmentModifier.BiomeModifier biome -> {
                Holder<Biome> currentBiome = level.getBiome(pos);
                boolean matchesBiome = biome.biome()
                        .map(currentBiome::is)
                        .orElse(false);
                boolean matchesTag = biome.biomeTag()
                        .map(currentBiome::is)
                        .orElse(false);
                yield matchesBiome || matchesTag;
            }
            case EnvironmentModifier.HeightModifier height -> {
                int y = pos.getY();
                boolean aboveMin = height.minY().isEmpty() || y >= height.minY().getAsInt();
                boolean belowMax = height.maxY().isEmpty() || y <= height.maxY().getAsInt();
                yield aboveMin && belowMax;
            }
        };
    }
}
