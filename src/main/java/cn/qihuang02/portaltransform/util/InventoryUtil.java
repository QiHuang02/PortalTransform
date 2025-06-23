package cn.qihuang02.portaltransform.util;

import cn.qihuang02.portaltransform.config.PTConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class InventoryUtil {
    @Contract("_, _, _ -> param3")
    public static @NotNull ItemStack tryPlaceInNearbyInv(ServerLevel level, BlockPos pos, @NotNull ItemStack stack) {
        if (stack.isEmpty() || !PTConfig.COMMON.autoInsertInttoChests.get()) {
            return stack;
        }

        int radius = PTConfig.COMMON.chestSearchRadius.get();
        ItemStack remainingStack = stack.copy();

        BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();

        for (int x = pos.getX() - radius; x <= pos.getX() + radius; x++) {
            for (int y = pos.getY() - radius; y <= pos.getY() + radius; y++) {
                for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; z++) {
                    currentPos.set(x, y, z);

                    if (remainingStack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }

                    BlockEntity blockEntity = level.getBlockEntity(currentPos);
                    if (blockEntity != null) {
                        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, currentPos.immutable(), null);
                        if (handler != null) {
                            remainingStack = ItemHandlerHelper.insertItem(handler, remainingStack, false);
                        }
                    }
                }
            }
        }
        return remainingStack;
    }
}
