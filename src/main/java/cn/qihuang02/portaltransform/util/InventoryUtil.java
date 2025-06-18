package cn.qihuang02.portaltransform.util;

import cn.qihuang02.portaltransform.config.PTConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class InventoryUtil {
    public static ItemStack tryPlaceInNearbyInv(ServerLevel level, BlockPos pos, @NotNull ItemStack stack) {
        if (stack.isEmpty() || !PTConfig.COMMON.autoInsertInttoChests.get()) {
            return stack;
        }

        int radius = PTConfig.COMMON.chestSearchRadius.get();
        ItemStack remainingStack = stack.copy();

        for (BlockPos currentPos : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
            if (remainingStack.isEmpty()) {
                break;
            }

            BlockEntity blockEntity = level.getBlockEntity(currentPos);
            if (blockEntity != null) {
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, currentPos, null);
                if (handler != null) {
                    remainingStack = ItemHandlerHelper.insertItem(handler, remainingStack, false);
                }
            }
        }
        return remainingStack;
    }
}
