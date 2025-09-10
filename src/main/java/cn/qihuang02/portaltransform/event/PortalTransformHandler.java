package cn.qihuang02.portaltransform.event;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.ItemTransformRecipe;
import cn.qihuang02.portaltransform.recipe.Recipes;
import cn.qihuang02.portaltransform.util.InventoryUtil;
import cn.qihuang02.portaltransform.util.NbtUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = PortalTransform.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PortalTransformHandler {
    @SubscribeEvent
    public static void onEntityTravel(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (entity instanceof ItemEntity itemEntity) {
            handleItem(event, itemEntity, serverLevel);
        }
    }

    private static void handleItem(EntityTravelToDimensionEvent event, ItemEntity itemEntity, ServerLevel level) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty() || NbtUtil.hasNoPortalTransform(stack)) {
            if (NbtUtil.hasNoPortalTransform(stack)) {
                event.setCanceled(true);
            }
            return;
        }
        Optional<ItemTransformRecipe> recipeOpt = level.getRecipeManager()
                .getRecipeFor(Recipes.PORTAL_ITEM_TRANSFORM_TYPE.get(), new SimpleContainer(stack), level);
        recipeOpt.ifPresent(recipe -> {
            event.setCanceled(true);
            ItemStack output = recipe.getResultItem(level.registryAccess()).copyWithCount(stack.getCount());
            NbtUtil.setNoPortalTransform(output);
            ItemStack remaining = InventoryUtil.tryPlaceInNearbyInv(level, itemEntity.blockPosition(), output);
            if (remaining.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(remaining);
            }
        });
    }
}
