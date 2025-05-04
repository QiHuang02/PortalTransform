package cn.qihuang02.portaltransform.event;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.component.PTComponents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(modid = PortalTransform.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ItemPickupEvents {
    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (event.getPlayer().level().isClientSide()) return;

        Player player = event.getPlayer();
        ItemEntity itemEntity = event.getItemEntity();
        ItemStack stack = itemEntity.getItem();

        if (stack.has(PTComponents.NO_PORTAL_TRANSFORM.get())) {
            stack.remove(PTComponents.NO_PORTAL_TRANSFORM.get());
        }

        if (!player.level().isClientSide()) {
            player.containerMenu.broadcastChanges();
        }
    }
}
