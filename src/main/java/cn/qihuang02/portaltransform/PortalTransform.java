package cn.qihuang02.portaltransform;

import cn.qihuang02.portaltransform.config.PTConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(PortalTransform.MODID)
public class PortalTransform {

    public static final String MODID = "portaltransform";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PortalTransform() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Recipes.register(modEventBus); // TODO: implement recipe registration

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PTConfig.COMMON_SPEC);
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation getRL(String path) {
        return ResourceLocation.fromNamespaceAndPath(PortalTransform.MODID, path);
    }
}
