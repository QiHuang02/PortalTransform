package cn.qihuang02.project_dimension;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ProjectDimension.MODID)
public class ProjectDimension {
    public static final String MODID = "project_dimension";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ProjectDimension(IEventBus modEventBus, ModContainer modContainer) {

    }
}
