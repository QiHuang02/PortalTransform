package cn.qihuang02.portaltransform.event;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.component.Components;
import cn.qihuang02.portaltransform.recipe.Recipes;
import cn.qihuang02.portaltransform.recipe.itemTransformation.Byproducts;
import cn.qihuang02.portaltransform.recipe.itemTransformation.ItemTransformationRecipe;
import cn.qihuang02.portaltransform.recipe.itemTransformation.SimpleItemInput;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;

@EventBusSubscriber(
        modid = PortalTransform.MODID,
        bus = EventBusSubscriber.Bus.GAME)
public class ItemTransformationHandler {
    private static final Map<ItemStack, Optional<RecipeHolder<ItemTransformationRecipe>>> RECIPE_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        if (entity instanceof ItemEntity itemEntity) {
            if (hasNoPortalTransform(itemEntity)) {
                event.setCanceled(true);
                return;
            }
        }

        if (!(entity instanceof ItemEntity itemEntity) ||
                level.isClientSide() ||
                itemEntity.getItem().isEmpty()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ResourceKey<Level> currentDimKey = serverLevel.dimension();
        ResourceKey<Level> targetDimKey = event.getDimension();

        findRecipeByInput(itemEntity, serverLevel)
                .filter(holder -> matchesDimensionRequirements(holder.value(), currentDimKey, targetDimKey))
                .ifPresent(holder -> {
                    ItemTransformationRecipe recipe = holder.value();
                    float chance = recipe.transformChance();

                    if (serverLevel.random.nextFloat() < chance) {
                        event.setCanceled(true);
                        transforming(itemEntity, serverLevel, recipe);
                    } else {
                        event.setCanceled(true);
                        itemEntity.discard();
                    }
                });
    }

    private static boolean hasNoPortalTransform(ItemEntity itemEntity) {
        return Boolean.TRUE.equals(
                itemEntity.getItem().get(Components.NO_PORTAL_TRANSFORM.get())
        );
    }

    /**
     * 根据物品输入查找匹配的 ItemTransformationRecipe。
     *
     * @param itemEntity   正在传送的物品实体。
     * @param currentLevel 物品实体当前所在的维度。
     * @return 如果找到基于物品输入的配方，则返回包含 RecipeHolder 的 Optional；否则返回 Optional.empty()。
     */
    private static Optional<RecipeHolder<ItemTransformationRecipe>> findRecipeByInput(
            ItemEntity itemEntity,
            ServerLevel currentLevel
    ) {
        ItemStack inputStack = itemEntity.getItem();

        return RECIPE_CACHE.computeIfAbsent(inputStack, stack -> {
            RecipeManager recipeManager = currentLevel.getRecipeManager();
            SimpleItemInput recipeInput = new SimpleItemInput(inputStack);
            return recipeManager.getRecipeFor(
                    Recipes.ITEM_TRANSFORMATION_TYPE.get(),
                    recipeInput,
                    currentLevel
            );
        });
    }

    private static boolean matchesDimensionRequirements(
            ItemTransformationRecipe recipe,
            ResourceKey<Level> currentDimKey,
            ResourceKey<Level> targetDimKey
    ) {
        return matchesDimension(recipe.getCurrentDimension(), currentDimKey) &&
                matchesDimension(recipe.getTargetDimension(), targetDimKey);
    }

    private static boolean matchesDimension(@NotNull Optional<ResourceKey<Level>> requiredDim, @NotNull ResourceKey<Level> actualDim) {
        return requiredDim.map(actualDim::equals).orElse(true);
    }

    /**
     * 根据匹配的配方执行实际的物品转换和副产品生成。
     *
     * @param itemEntity 被转换的原始 ItemEntity。
     * @param level      转换发生的维度。
     * @param recipe     定义转换的匹配 PortalTransformRecipe。
     */
    private static void transforming(ItemEntity itemEntity, ServerLevel level, ItemTransformationRecipe recipe) {
        Objects.requireNonNull(itemEntity, "ItemEntity cannot be null");
        Objects.requireNonNull(level, "Level cannot be null");
        Objects.requireNonNull(recipe, "Recipe cannot be null");

        Vec3 pos = itemEntity.position();
        Vec3 motion = itemEntity.getDeltaMovement();
        int originalInputCount = itemEntity.getItem().getCount();

        ItemStack outputStack = recipe.getResultItem(level.registryAccess());
        if (!outputStack.isEmpty()) {
            outputStack.setCount(originalInputCount);
            itemEntity.setItem(outputStack);
        } else {
            PortalTransform.LOGGER.warn("Recipe {} resulted in an empty output stack!", recipe);
            itemEntity.discard();
            return;
        }

        RandomSource random = level.random;

        recipe.getByproducts().ifPresent(byproducts -> {
            for (Byproducts definition : byproducts) {
                if (
                        !definition.counts().isValid() ||
                                definition.byproduct().isEmpty() ||
                                definition.chance() <= 0.0F
                ) {
                    continue;
                }

                for (int index = 0; index < originalInputCount; index++) {
                    if (random.nextFloat() < definition.chance()) {
                        int countToSpawn = definition.counts().min();
                        if (definition.counts().max() > definition.counts().min()) {
                            countToSpawn = random.nextInt(definition.counts().min(), definition.counts().max() + 1);
                        }

                        if (countToSpawn > 0) {
                            ItemStack byproductStack = definition.byproduct().copyWithCount(countToSpawn);
                            spawnByproduct(level, pos, motion, byproductStack, random);
                        }
                    }
                }
            }
        });
    }

    private static void spawnByproduct(ServerLevel level, Vec3 pos, Vec3 motion, ItemStack byproductStack, RandomSource random) {
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

    private static Vec3 calculateSpreadMotion(Vec3 baseMotion, RandomSource random) {
        return baseMotion.add(
                (random.nextFloat() - 0.5) * 0.1,
                0.1,
                (random.nextFloat() - 0.5) * 0.1
        );
    }
}
