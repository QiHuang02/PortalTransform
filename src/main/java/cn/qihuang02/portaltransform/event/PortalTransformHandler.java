package cn.qihuang02.portaltransform.event;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.component.PTComponents;
import cn.qihuang02.portaltransform.recipe.PTRecipes;
import cn.qihuang02.portaltransform.recipe.custom.Byproducts;
import cn.qihuang02.portaltransform.recipe.custom.PortalTransformRecipe;
import cn.qihuang02.portaltransform.recipe.custom.SimpleItemInput;
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

import java.util.*;

@EventBusSubscriber(
        modid = PortalTransform.MODID,
        bus = EventBusSubscriber.Bus.GAME)
public class PortalTransformHandler {
    private static final Random RANDOM = new Random();
    private static final Map<ItemStack, Optional<RecipeHolder<PortalTransformRecipe>>> RECIPE_CACHE =
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
                    event.setCanceled(true);
                    transforming(itemEntity, serverLevel, holder.value());
                });
    }

    private static boolean hasNoPortalTransform(ItemEntity itemEntity) {
        return Boolean.TRUE.equals(
                itemEntity.getItem().get(PTComponents.NO_PORTAL_TRANSFORM.get())
        );
    }

    /**
     * 根据物品输入查找匹配的 PortalTransformRecipe。
     *
     * @param itemEntity   正在传送的物品实体。
     * @param currentLevel 物品实体当前所在的维度。
     * @return 如果找到基于物品输入的配方，则返回包含 RecipeHolder 的 Optional；否则返回 Optional.empty()。
     */
    private static Optional<RecipeHolder<PortalTransformRecipe>> findRecipeByInput(
            ItemEntity itemEntity,
            ServerLevel currentLevel
    ) {
        ItemStack inputStack = itemEntity.getItem();

        return RECIPE_CACHE.computeIfAbsent(inputStack, stack -> {
            RecipeManager recipeManager = currentLevel.getRecipeManager();
            SimpleItemInput recipeInput = new SimpleItemInput(inputStack);
            return recipeManager.getRecipeFor(
                    PTRecipes.PORTAL_TRANSFORM_TYPE.get(),
                    recipeInput,
                    currentLevel
            );
        });
    }

    private static boolean matchesDimensionRequirements(
            PortalTransformRecipe recipe,
            ResourceKey<Level> currentDimKey,
            ResourceKey<Level> targetDimKey
    ) {
        return matchesDimension(recipe.getCurrentDimension(), currentDimKey) &&
                matchesDimension(recipe.getTargetDimension(), targetDimKey);
    }

    private static boolean matchesDimension(Optional<ResourceKey<Level>> requiredDim, ResourceKey<Level> actualDim) {
        return requiredDim.map(actualDim::equals).orElse(true);
    }

    /**
     * 根据匹配的配方执行实际的物品转换和副产品生成。
     *
     * @param itemEntity 被转换的原始 ItemEntity。
     * @param level      转换发生的维度。
     * @param recipe     定义转换的匹配 PortalTransformRecipe。
     */
    private static void transforming(ItemEntity itemEntity, ServerLevel level, PortalTransformRecipe recipe) {
        Objects.requireNonNull(itemEntity, "ItemEntity cannot be null");
        Objects.requireNonNull(level, "Level cannot be null");
        Objects.requireNonNull(recipe, "Recipe cannot be null");

        Vec3 pos = itemEntity.position();
        Vec3 motion = itemEntity.getDeltaMovement();
        int originalInputCount = itemEntity.getItem().getCount();

        ItemStack outputStack = recipe.getResultItem(level.registryAccess());
        outputStack.setCount(originalInputCount);
        itemEntity.setItem(outputStack);

        recipe.getByproducts().ifPresent(byproducts -> {
            for (Byproducts definition : byproducts) {
                if (definition.counts().min() <= 0 ||
                        definition.counts().max() < definition.counts().min() ||
                        definition.byproduct().isEmpty()
                ) {
                    continue;
                }

                for (int index = 0; index < originalInputCount; index++) {
                    if (RANDOM.nextFloat() < definition.chance()) {
                        int countToSpawn = definition.counts().min();
                        if (definition.counts().max() > definition.counts().min()) {
                            countToSpawn = RANDOM.nextInt(definition.counts().min(), definition.counts().max() + 1);
                        }

                        if (countToSpawn > 0) {
                            ItemStack byproductStack = definition.byproduct().copyWithCount(countToSpawn);
                            spawnByproduct(level, pos, motion, byproductStack);
                        }
                    }
                }
            }
        });
    }

    private static void spawnByproduct(ServerLevel level, Vec3 pos, Vec3 motion, ItemStack byproductStack) {
        int maxStackSize = byproductStack.getMaxStackSize();
        int total = byproductStack.getCount();

        byproductStack.set(PTComponents.NO_PORTAL_TRANSFORM.get(), true);

        while (total > 0) {
            int spawnCount = Math.min(total, maxStackSize);
            total -= spawnCount;

            ItemStack partStack = byproductStack.copyWithCount(spawnCount);
            ItemEntity entity = new ItemEntity(level, pos.x(), pos.y(), pos.z(), partStack);
            entity.setDeltaMovement(calculateSpreadMotion(motion, level.random));
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
