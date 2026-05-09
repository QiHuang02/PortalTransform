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
                                entry("life", 6),
                                entry("form", 4),
                                entry("flame", 1),
                                entry("void", -2),
                                entry("soul", 1),
                                entry("phase", 0),
                                entry("bloom", 1)
                        ));
                register(context,
                        Level.NETHER,
                        linkedSymbols(
                                entry("life", -4),
                                entry("form", -1),
                                entry("flame", 8),
                                entry("void", 3),
                                entry("soul", 0),
                                entry("phase", -2),
                                entry("cinder", 2)
                        ));
                register(context,
                        Level.END,
                        linkedSymbols(
                                entry("life", -3),
                                entry("form", 2),
                                entry("flame", -2),
                                entry("void", 7),
                                entry("soul", 3),
                                entry("phase", 6),
                                entry("entropy", 1)
                        ));
            });

    public PTDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(PortalTransform.MODID));
    }

    private static void register(@NotNull BootstrapContext<DimensionSymbolValues> context,
                                 ResourceKey<Level> dimension,
                                 Map<ResourceLocation, Integer> symbols) {
        context.register(key(dimension), new DimensionSymbolValues(dimension, symbols));
    }

    private static @NotNull ResourceKey<DimensionSymbolValues> key(@NotNull ResourceKey<Level> dimension) {
        return ResourceKey.create(
                PTRegistries.DIMENSION_SYMBOL_VALUES,
                PortalTransform.getRL(dimension.location().getPath())
        );
    }


    @SafeVarargs
    private static @NotNull Map<ResourceLocation, Integer> linkedSymbols(Map.Entry<ResourceLocation, Integer> @NotNull ... entries) {
        Map<ResourceLocation, Integer> symbols = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Integer> entry : entries) {
            symbols.put(entry.getKey(), entry.getValue());
        }
        return symbols;
    }

    @Contract("_, _ -> new")
    private static Map.@NotNull @Unmodifiable Entry<ResourceLocation, Integer> entry(String path, int value) {
        return Map.entry(PortalTransform.getRL(path), value);
    }
}
