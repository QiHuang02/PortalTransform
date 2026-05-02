package cn.qihuang02.project_dimension;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = ProjectDimension.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ProjectDimension.MODID, value = Dist.CLIENT)
public class ProjectDimensionClient {
    public ProjectDimensionClient(ModContainer container) {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ProjectDimension.LOGGER.info("HELLO FROM CLIENT SETUP");
        ProjectDimension.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
