package cn.qihuang02.portaltransform.event;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.ItemTransformRecipe;
import cn.qihuang02.portaltransform.recipe.Recipes;
import cn.qihuang02.portaltransform.recipe.itemtransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.itemtransform.Weather;
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

import java.util.HashMap;
import java.util.Map;
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
            if (recipe.getCurrent().isPresent() && !recipe.getCurrent().get().equals(level.dimension())) {
                return;
            }
            if (recipe.getTarget().isPresent() && !recipe.getTarget().get().equals(event.getDimension())) {
                return;
            }
            if (recipe.getWeather().isPresent() && !matchesWeather(recipe.getWeather().get(), level)) {
                return;
            }

            event.setCanceled(true);
            if (level.random.nextFloat() > recipe.getTransformChance()) {
                itemEntity.discard();
                return;
            }

            int originalCount = stack.getCount();
            ItemStack output = recipe.getResultItem(level.registryAccess()).copyWithCount(originalCount);
            NbtUtil.setNoPortalTransform(output);
            ItemStack remaining = InventoryUtil.tryPlaceInNearbyInv(level, itemEntity.blockPosition(), output);
            if (remaining.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(remaining);
            }

            Map<ItemStack, Integer> byproductCounts = new HashMap<>();
            for (Byproducts definition : recipe.getByproducts()) {
                for (int i = 0; i < originalCount; i++) {
                    definition.getResult(level.random).ifPresent(byStack -> {
                        ItemStack key = byproductCounts.keySet().stream()
                                .filter(s -> ItemStack.isSameItemSameTags(s, byStack))
                                .findFirst().orElse(null);
                        if (key != null) {
                            byproductCounts.put(key, byproductCounts.get(key) + byStack.getCount());
                        } else {
                            byproductCounts.put(byStack.copy(), byStack.getCount());
                        }
                    });
                }
            }

            byproductCounts.forEach((bpStack, count) -> {
                ItemStack spawnStack = bpStack.copyWithCount(count);
                NbtUtil.setNoPortalTransform(spawnStack);
                ItemStack leftover = InventoryUtil.tryPlaceInNearbyInv(level, itemEntity.blockPosition(), spawnStack);
                if (!leftover.isEmpty()) {
                    ItemEntity extra = new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), leftover);
                    level.addFreshEntity(extra);
                }
            });
        });
    }

    private static boolean matchesWeather(Weather weather, Level level) {
        return switch (weather) {
            case CLEAR -> !level.isRaining() && !level.isThundering();
            case RAIN -> level.isRaining() && !level.isThundering();
            case THUNDER -> level.isThundering();
            case ANY -> true;
        };
    }
}
