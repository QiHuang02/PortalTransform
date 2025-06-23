package cn.qihuang02.portaltransform;

import cn.qihuang02.portaltransform.component.Components;
import cn.qihuang02.portaltransform.config.PTConfig;
import cn.qihuang02.portaltransform.recipe.Recipes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Mod(PortalTransform.MODID)
public class PortalTransform {
    public static final String MODID = "portaltransform";
    public static final Logger LOGGER = LogManager.getLogger();

    public PortalTransform(IEventBus modEventBus, ModContainer modContainer) {
        Components.register(modEventBus);
        Recipes.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, PTConfig.COMMON_SPEC);
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation getRL(String path) {
        return ResourceLocation.fromNamespaceAndPath(PortalTransform.MODID, path);
    }
}
