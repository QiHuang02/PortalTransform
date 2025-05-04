package cn.qihuang02.portaltransform;

import cn.qihuang02.portaltransform.component.PTComponents;
import cn.qihuang02.portaltransform.recipe.PTRecipes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PortalTransform.MODID)
public class PortalTransform
{
    public static final String MODID = "portaltransform";
    public static final Logger LOGGER = LogManager.getLogger();

    public PortalTransform(IEventBus modEventBus)
    {
        PTComponents.register(modEventBus);
        PTRecipes.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation getRL(String path) {
        return ResourceLocation.fromNamespaceAndPath(PortalTransform.MODID, path);
    }
}
