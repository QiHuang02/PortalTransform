package cn.qihuang02.portaltransform.datagen;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.registry.PTRegistries;
import cn.qihuang02.portaltransform.symbol.DimensionSymbolValues;
import cn.qihuang02.portaltransform.symbol.environment.EnvironmentModifier;
import cn.qihuang02.portaltransform.symbol.environment.EnvironmentModifier.*;
import cn.qihuang02.portaltransform.symbol.environment.WeatherCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 生成默认维度象征值 datapack 条目，避免手写 JSON 与代码定义漂移。
 */
public class PTDatapackProvider extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(PTRegistries.DIMENSION_SYMBOL_VALUES, context -> {
                register(context,
                        Level.OVERWORLD,
                        linkedSymbols(
                                entry("life", 6.0F),
                                entry("form", 4.0F),
                                entry("flame", 1.0F),
                                entry("void", -2.0F),
                                entry("soul", 1.0F),
                                entry("phase", 0.0F),
                                entry("bloom", 1.0F)
                        ));
                register(context,
                        Level.NETHER,
                        linkedSymbols(
                                entry("life", -4.0F),
                                entry("form", -1.0F),
                                entry("flame", 8.0F),
                                entry("void", 3.0F),
                                entry("soul", 0.0F),
                                entry("phase", -2.0F),
                                entry("cinder", 2.0F)
                        ));
                register(context,
                        Level.END,
                        linkedSymbols(
                                entry("life", -3.0F),
                                entry("form", 2.0F),
                                entry("flame", -2.0F),
                                entry("void", 7.0F),
                                entry("soul", 3.0F),
                                entry("phase", 6.0F),
                                entry("entropy", 1.0F)
                        ));
            })
            .add(PTRegistries.ENVIRONMENT_MODIFIERS, PTDatapackProvider::bootstrapEnvironmentModifiers);

    public PTDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(PortalTransform.MODID));
    }

    private static void register(@NotNull BootstrapContext<DimensionSymbolValues> context,
                                 ResourceKey<Level> dimension,
                                 Map<ResourceLocation, Float> symbols) {
        context.register(key(dimension), new DimensionSymbolValues(dimension, symbols));
    }

    private static @NotNull ResourceKey<DimensionSymbolValues> key(@NotNull ResourceKey<Level> dimension) {
        return ResourceKey.create(
                PTRegistries.DIMENSION_SYMBOL_VALUES,
                PortalTransform.getRL(dimension.location().getPath())
        );
    }


    @SafeVarargs
    private static @NotNull Map<ResourceLocation, Float> linkedSymbols(Map.Entry<ResourceLocation, Float> @NotNull ... entries) {
        Map<ResourceLocation, Float> symbols = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Float> entry : entries) {
            symbols.put(entry.getKey(), entry.getValue());
        }
        return symbols;
    }

    @Contract("_, _ -> new")
    private static Map.@NotNull @Unmodifiable Entry<ResourceLocation, Float> entry(String path, float value) {
        return Map.entry(PortalTransform.getRL(path), value);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 环境修正 bootstrap
    // ─────────────────────────────────────────────────────────────────────────────

    private static void bootstrapEnvironmentModifiers(@NotNull BootstrapContext<EnvironmentModifier> context) {
        // 天气修正（3 条）
        context.register(envKey("weather_clear"), new WeatherModifier(
                WeatherCondition.CLEAR,
                linkedSymbols(entry("life", 1.0F), entry("flame", 0.5F))
        ));
        context.register(envKey("weather_rain"), new WeatherModifier(
                WeatherCondition.RAIN,
                linkedSymbols(entry("life", 2.0F), entry("flame", -1.0F))
        ));
        context.register(envKey("weather_thunder"), new WeatherModifier(
                WeatherCondition.THUNDER,
                linkedSymbols(entry("void", 2.0F), entry("soul", 1.5F), entry("flame", -0.5F))
        ));

        // 时间修正（4 条）
        context.register(envKey("time_dawn"), new TimeModifier(
                22000, 23999,
                linkedSymbols(entry("life", 1.0F), entry("phase", 0.5F))
        ));
        context.register(envKey("time_day"), new TimeModifier(
                0, 12000,
                linkedSymbols(entry("life", 0.5F), entry("flame", 0.5F))
        ));
        context.register(envKey("time_dusk"), new TimeModifier(
                12000, 13000,
                linkedSymbols(entry("soul", 0.5F), entry("phase", 1.0F))
        ));
        context.register(envKey("time_night"), new TimeModifier(
                13000, 22000,
                linkedSymbols(entry("void", 1.0F), entry("soul", 1.0F))
        ));

        // 高度修正（2 条）
        context.register(envKey("height_deep"), new HeightModifier(
                OptionalInt.empty(), OptionalInt.of(0),
                linkedSymbols(entry("void", 1.5F), entry("form", -0.5F))
        ));
        context.register(envKey("height_sky"), new HeightModifier(
                OptionalInt.of(192), OptionalInt.empty(),
                linkedSymbols(entry("phase", 1.5F), entry("void", -0.5F))
        ));

        // 群系修正（3 条）
        context.register(envKey("biome_hot"), new BiomeModifier(
                Optional.empty(),
                Optional.of(Tags.Biomes.IS_HOT_OVERWORLD),
                linkedSymbols(entry("flame", 1.5F), entry("life", -0.5F))
        ));
        context.register(envKey("biome_cold"), new BiomeModifier(
                Optional.empty(),
                Optional.of(Tags.Biomes.IS_COLD_OVERWORLD),
                linkedSymbols(entry("flame", -1.0F), entry("form", 1.0F))
        ));
        context.register(envKey("biome_forest"), new BiomeModifier(
                Optional.empty(),
                Optional.of(Tags.Biomes.IS_FOREST),
                linkedSymbols(entry("life", 1.5F), entry("bloom", 1.0F))
        ));
    }

    private static @NotNull ResourceKey<EnvironmentModifier> envKey(String path) {
        return ResourceKey.create(PTRegistries.ENVIRONMENT_MODIFIERS, PortalTransform.getRL(path));
    }
}
