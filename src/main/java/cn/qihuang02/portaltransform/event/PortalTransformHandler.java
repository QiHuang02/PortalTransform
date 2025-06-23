package cn.qihuang02.portaltransform.event;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.component.Components;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Weather;
import cn.qihuang02.portaltransform.recipe.ItemTransformRecipe;
import cn.qihuang02.portaltransform.recipe.Recipes;
import cn.qihuang02.portaltransform.recipe.SimpleItemInput;
import cn.qihuang02.portaltransform.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

@EventBusSubscriber(
        modid = PortalTransform.MODID,
        bus = EventBusSubscriber.Bus.GAME)
public class PortalTransformHandler {
    private static final Logger LOGGER = PortalTransform.LOGGER;

    @SubscribeEvent
    public static void onEntityTravelToDimension(@NotNull EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        if (level.isClientSide() || entity.isRemoved()) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (entity instanceof ItemEntity itemEntity) {
            handleItemTransformation(event, itemEntity, serverLevel);
        }
    }

    // --- Item Transformation Logic ---
    private static void handleItemTransformation(EntityTravelToDimensionEvent event, ItemEntity itemEntity, ServerLevel serverLevel) {
        if (hasNoPortalTransformComponent(itemEntity) || itemEntity.getItem().isEmpty()) {
            if (hasNoPortalTransformComponent(itemEntity)) event.setCanceled(true);
            return;
        }

        getValidRecipeForContext(itemEntity, serverLevel, event.getDimension())
                .ifPresent(holder -> processTransformation(event, itemEntity, serverLevel, holder));
    }

    private static Optional<RecipeHolder<ItemTransformRecipe>> getValidRecipeForContext(ItemEntity itemEntity, ServerLevel level, ResourceKey<Level> targetDimKey) {
        return findItemRecipe(itemEntity, level)
                .filter(holder -> {
                    ItemTransformRecipe recipe = holder.value();
                    return matchesItemDimensionRequirements(recipe, level.dimension(), targetDimKey) &&
                            matchesWeather(recipe, level);
                });
    }

    private static void processTransformation(EntityTravelToDimensionEvent event, ItemEntity itemEntity, ServerLevel level, RecipeHolder<ItemTransformRecipe> holder) {
        ItemTransformRecipe recipe = holder.value();
        float chance = recipe.transformChance();

        event.setCanceled(true);

        if (level.random.nextFloat() < chance) {
            transformItem(itemEntity, level, recipe);
        } else {
            itemEntity.discard();
        }
    }

    private static boolean hasNoPortalTransformComponent(@NotNull ItemEntity itemEntity) {
        DataComponentType<Boolean> componentType = Components.NO_PORTAL_TRANSFORM.get();
        return Boolean.TRUE.equals(itemEntity.getItem().get(componentType));
    }

    private static Optional<RecipeHolder<ItemTransformRecipe>> findItemRecipe(@NotNull ItemEntity itemEntity, ServerLevel currentLevel) {
        ItemStack inputStack = itemEntity.getItem();
        if (inputStack.isEmpty()) {
            return Optional.empty();
        }

        RecipeManager recipeManager = currentLevel.getRecipeManager();
        return recipeManager.getRecipeFor(
                Recipes.PORTAL_ITEM_TRANSFORM_TYPE.get(),
                new SimpleItemInput(inputStack),
                currentLevel
        );
    }

    private static boolean matchesItemDimensionRequirements(@NotNull ItemTransformRecipe recipe, ResourceKey<Level> currentDimKey, ResourceKey<Level> targetDimKey) {
        return matchesDimension(recipe.getCurrent(), currentDimKey) &&
                matchesDimension(recipe.getTarget(), targetDimKey);
    }

    private static boolean matchesDimension(@NotNull Optional<ResourceKey<Level>> requiredDim, @NotNull ResourceKey<Level> actualDim) {
        return requiredDim.map(actualDim::equals).orElse(true);
    }

    private static boolean matchesWeather(@NotNull ItemTransformRecipe recipe, ServerLevel serverLevel) {
        Optional<Weather> weatherOpt = recipe.getWeather();

        if (weatherOpt.isEmpty()) {
            return true;
        }

        Weather weather = weatherOpt.get();

        if (weather == Weather.ANY) {
            return true;
        }

        boolean isThundering = serverLevel.isThundering();
        boolean isRaining = serverLevel.isRaining();

        return switch (weather) {
            case CLEAR -> !isThundering && !isRaining;
            case RAIN -> isRaining && !isThundering;
            case THUNDER -> isThundering;
            default -> true;
        };
    }

    private static void transformItem(ItemEntity itemEntity, ServerLevel level, ItemTransformRecipe recipe) {
        Objects.requireNonNull(itemEntity, "ItemEntity cannot be null");
        Objects.requireNonNull(level, "Level cannot be null");
        Objects.requireNonNull(recipe, "Recipe cannot be null");

        BlockPos spawnPos = itemEntity.blockPosition();
        Vec3 pos = itemEntity.position();
        Vec3 motion = itemEntity.getDeltaMovement();
        int originalInputCount = itemEntity.getItem().getCount();
        RandomSource random = level.random;

        ItemStack outputStack = recipe.getResultItem(level.registryAccess()).copy();
        if (!outputStack.isEmpty()) {
            outputStack.setCount(originalInputCount);

            ItemStack remainingOutput = InventoryUtil.tryPlaceInNearbyInv(level, spawnPos, outputStack);

            if (remainingOutput.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(remainingOutput);
            }
        } else {
            LOGGER.debug("Item Recipe {} resulted in an empty output stack!", recipe);
            itemEntity.discard();
            return;
        }

        recipe.getByproducts().ifPresent(byproducts -> {
            for (Byproducts definition : byproducts) {
                for (int i = 0; i < originalInputCount; i++) {
                    definition.getResult(random).ifPresent(byproductStack -> spawnItemByproduct(level, pos, motion, byproductStack, random));
                }
            }
        });
    }

    private static void spawnItemByproduct(ServerLevel level, Vec3 pos, Vec3 motion, @NotNull ItemStack byproductStack, RandomSource random) {
        BlockPos spawnPos = BlockPos.containing(pos);

        ItemStack remainingByproduct = InventoryUtil.tryPlaceInNearbyInv(level, spawnPos, byproductStack);

        if (remainingByproduct.isEmpty()) {
            return;
        }

        int maxStackSize = byproductStack.getMaxStackSize();
        int total = byproductStack.getCount();

        DataComponentType<Boolean> componentType = Components.NO_PORTAL_TRANSFORM.get();
        byproductStack.set(componentType, true);

        while (total > 0) {
            int spawnCount = Math.min(total, maxStackSize);
            total -= spawnCount;
            ItemStack partStack = byproductStack.copyWithCount(spawnCount);
            ItemEntity entity = new ItemEntity(level, pos.x(), pos.y(), pos.z(), partStack);
            entity.setDeltaMovement(calculateSpreadMotion(motion, random));
            level.addFreshEntity(entity);
        }
    }

    private static @NotNull Vec3 calculateSpreadMotion(@NotNull Vec3 baseMotion, @NotNull RandomSource random) {
        return baseMotion.add(
                (random.nextFloat() - 0.5) * 0.1,
                0.1,
                (random.nextFloat() - 0.5) * 0.1
        );
    }
}
