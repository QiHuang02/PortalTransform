package cn.qihuang02.project_dimension.command;

import com.mojang.brigadier.CommandDispatcher;
import cn.qihuang02.project_dimension.symbol.SymbolContext;
import cn.qihuang02.project_dimension.symbol.resolver.CompoundSymbolResolver;
import cn.qihuang02.project_dimension.symbol.resolver.SymbolContextFactory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.stream.Collectors;

public final class SymbolDebugCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("project_dimension")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("symbols")
                        .then(Commands.literal("here")
                                .then(Commands.argument("target_dimension", ResourceLocationArgument.id())
                                        .executes(context -> describeHere(
                                                context.getSource(),
                                                ResourceLocationArgument.getId(context, "target_dimension")
                                        ))))));
    }

    private static int describeHere(CommandSourceStack source, ResourceLocation targetDimensionId) {
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());
        ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, targetDimensionId);
        SymbolContext symbols = SymbolContextFactory.create(level, pos, targetDimension);

        source.sendSuccess(() -> Component.literal("源维度基础：" + symbols.sourceBase().toShortString()), false);
        source.sendSuccess(() -> Component.literal("目标维度基础：" + symbols.targetBase().toShortString()), false);
        source.sendSuccess(() -> Component.literal("源侧环境修正：" + symbols.environmentModifier().toShortString()), false);
        source.sendSuccess(() -> Component.literal("源侧有效象征：" + symbols.effectiveSource().toShortString()), false);
        source.sendSuccess(() -> Component.literal("目标有效象征：" + symbols.effectiveTarget().toShortString()), false);
        source.sendSuccess(() -> Component.literal("路径差值 delta：" + symbols.delta().toShortString()), false);
        source.sendSuccess(() -> Component.literal("复合象征：" + formatCompounds(CompoundSymbolResolver.resolve(symbols.delta()))), false);
        return 1;
    }

    private static String formatCompounds(Map<String, Integer> compounds) {
        return compounds.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
    }
}
