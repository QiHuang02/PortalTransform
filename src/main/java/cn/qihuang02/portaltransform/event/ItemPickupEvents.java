package cn.qihuang02.portaltransform.event;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.component.Components;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = PortalTransform.MODID, bus = EventBusSubscriber.Bus.FORGE)
public class ItemPickupEvents {
    @SubscribeEvent
    public static void onItemPickup(@NotNull EntityItemPickupEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemEntity itemEntity = event.getItem();
        ItemStack stack = itemEntity.getItem();

        if (Components.hasNoPortalTransform(stack)) {
            Components.clearNoPortalTransform(stack);
            player.containerMenu.broadcastChanges();
        }
    }
}
