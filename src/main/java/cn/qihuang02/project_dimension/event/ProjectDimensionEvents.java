package cn.qihuang02.project_dimension.event;

import cn.qihuang02.project_dimension.ProjectDimension;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolManager;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = ProjectDimension.MODID)
public final class ProjectDimensionEvents {
    @SubscribeEvent
    public static void onDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                DimensionSymbolManager.DIMENSION_SYMBOL_REGISTRY_KEY,
                DimensionSymbolVector.CODEC,
                DimensionSymbolVector.CODEC
        );
    }
}
