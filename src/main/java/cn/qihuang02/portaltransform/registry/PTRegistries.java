package cn.qihuang02.portaltransform.registry;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.symbol.DimensionSymbolValues;
import cn.qihuang02.portaltransform.symbol.environment.EnvironmentModifier;
import cn.qihuang02.portaltransform.symbol.symbols.IDimensionSymbol;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * PortalTransform 自定义注册表定义。
 */
public final class PTRegistries {
    public static final ResourceKey<Registry<IDimensionSymbol>> DIMENSION_SYMBOL =
            ResourceKey.createRegistryKey(PortalTransform.getRL("dimension_symbol"));
    public static final ResourceKey<Registry<DimensionSymbolValues>> DIMENSION_SYMBOL_VALUES =
            ResourceKey.createRegistryKey(PortalTransform.getRL("dimension_symbol_values"));
    public static final ResourceKey<Registry<EnvironmentModifier>> ENVIRONMENT_MODIFIERS =
            ResourceKey.createRegistryKey(PortalTransform.getRL("environment_modifiers"));

    public static void configureDimensionSymbolRegistry(@NotNull RegistryBuilder<IDimensionSymbol> builder) {
        builder.sync(true);
    }
}
