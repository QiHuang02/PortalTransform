package cn.qihuang02.portaltransform.event;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.util.NbtUtil;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Removes the portal-transform guard tag when players pick up items
 * so they can be processed again in future portal trips.
 */
@Mod.EventBusSubscriber(modid = PortalTransform.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemPickupEvents {
    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        ItemEntity itemEntity = event.getItem();
        ItemStack stack = itemEntity.getItem();
        if (NbtUtil.hasNoPortalTransform(stack)) {
            NbtUtil.clearNoPortalTransform(stack);
            player.containerMenu.broadcastChanges();
        }
    }
}
