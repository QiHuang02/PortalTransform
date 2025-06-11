package cn.qihuang02.portaltransform;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(PortalTransform.MODID)
public class PortalTransform {

    public static final String MODID = "portaltransform";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PortalTransform() {

    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation getRL(String path) {
        return ResourceLocation.fromNamespaceAndPath(PortalTransform.MODID, path);
    }
}
