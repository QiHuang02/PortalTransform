package cn.qihuang02.portaltransform.registry;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.symbol.DimensionSymbolValues;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.NotNull;

/**
 * 自定义 datapack 注册表入口。
 */
@EventBusSubscriber(modid = PortalTransform.MODID)
public final class PTDatapackRegistries {
    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.@NotNull NewRegistry event) {
        event.dataPackRegistry(
                PTRegistries.DIMENSION_SYMBOL_VALUES,
                DimensionSymbolValues.CODEC,
                DimensionSymbolValues.CODEC
        );
    }
}
