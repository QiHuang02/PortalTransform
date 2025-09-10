package cn.qihuang02.portaltransform.util;

import cn.qihuang02.portaltransform.config.PTConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class InventoryUtil {
    public static @NotNull ItemStack tryPlaceInNearbyInv(ServerLevel level, BlockPos pos, @NotNull ItemStack stack) {
        if (stack.isEmpty() || !PTConfig.COMMON.autoInsertIntoChests.get()) {
            return stack;
        }

        int radius = PTConfig.COMMON.chestSearchRadius.get();
        ItemStack remaining = stack.copy();

        for (BlockPos currentPos : BlockPos.betweenClosed(
                pos.offset(-radius, -radius, -radius),
                pos.offset(radius, radius, radius))) {
            if (remaining.isEmpty()) {
                return ItemStack.EMPTY;
            }

            try {
                BlockEntity blockEntity = level.getBlockEntity(currentPos);
                if (blockEntity != null) {
                    var capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                    if (capability.isPresent()) {
                        IItemHandler handler = capability.orElse(null);
                        if (handler != null) {
                            remaining = ItemHandlerHelper.insertItem(handler, remaining, false);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return remaining;
    }
}
