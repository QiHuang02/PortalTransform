package cn.qihuang02.portaltransform;

import cn.qihuang02.portaltransform.component.Components;
import cn.qihuang02.portaltransform.recipe.Recipes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PortalTransform.MODID)
public class PortalTransform
{
    public static final String MODID = "portaltransform";
    public static final Logger LOGGER = LogManager.getLogger();

    public PortalTransform(IEventBus modEventBus)
    {
        Components.register(modEventBus);
        Recipes.register(modEventBus);

    }

    public static ResourceLocation getRL(String path) {
        return ResourceLocation.fromNamespaceAndPath(PortalTransform.MODID, path);
    }
}
