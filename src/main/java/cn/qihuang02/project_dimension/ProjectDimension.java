package cn.qihuang02.project_dimension;

import cn.qihuang02.project_dimension.command.SymbolDebugCommand;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(ProjectDimension.MODID)
public class ProjectDimension {
    public static final String MODID = "project_dimension";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ProjectDimension() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                SymbolDebugCommand.register(event.getDispatcher()));
    }
}
