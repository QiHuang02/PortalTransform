package cn.qihuang02.portaltransform.datagen;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.registry.PTRegistries;
import cn.qihuang02.portaltransform.symbol.DimensionSymbolValues;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedHashMap;
import java.util.Map;
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
            });

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
}
