package cn.qihuang02.portaltransform.util;

import cn.qihuang02.portaltransform.config.PTConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class InventoryUtil {
    @Contract("_, _, _ -> param3")
    public static @NotNull ItemStack tryPlaceInNearbyInv(ServerLevel level, BlockPos pos, @NotNull ItemStack stack) {
        if (stack.isEmpty() || !PTConfig.COMMON.autoInsertIntoChests.get()) {
            return stack;
        }

        int radius = PTConfig.COMMON.chestSearchRadius.get();
        ItemStack remainingStack = stack.copy();

        // Use BlockPos.betweenClosed for more efficient iteration
        for (BlockPos currentPos : BlockPos.betweenClosed(
                pos.offset(-radius, -radius, -radius), 
                pos.offset(radius, radius, radius))) {
            
            if (remainingStack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            try {
                BlockEntity blockEntity = level.getBlockEntity(currentPos);
                if (blockEntity != null) {
                    LazyOptional<IItemHandler> capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                    if (capability.isPresent()) {
                        IItemHandler handler = capability.orElseThrow(IllegalStateException::new);
                        remainingStack = ItemHandlerHelper.insertItem(handler, remainingStack, false);
                    }
                }
            } catch (Exception e) {
                // Log error and continue with next position
                // Avoid crashing the entire operation for one bad container
                continue;
            }
        }
        return remainingStack;
    }
}
