package cn.qihuang02.portaltransform;

import cn.qihuang02.portaltransform.component.Components;
import cn.qihuang02.portaltransform.config.PTConfig;
import cn.qihuang02.portaltransform.recipe.Recipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Mod(PortalTransform.MODID)
public class PortalTransform {
    public static final String MODID = "portaltransform";
    public static final Logger LOGGER = LogManager.getLogger();

    public PortalTransform() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Components.register(modEventBus);
        Recipes.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PTConfig.COMMON_SPEC);
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation getRL(String path) {
        return new ResourceLocation(PortalTransform.MODID, path);
    }
}
