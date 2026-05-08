package cn.qihuang02.project_dimension.event;

import cn.qihuang02.project_dimension.ProjectDimension;
import cn.qihuang02.project_dimension.command.SymbolDebugCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = ProjectDimension.MODID)
public final class GameEvents {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SymbolDebugCommand.register(event.getDispatcher());
    }
}
