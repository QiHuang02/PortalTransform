package cn.qihuang02.portaltransform.event;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.component.Components;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Biomes;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Weather;
import cn.qihuang02.portaltransform.recipe.ItemTransformRecipe;
import cn.qihuang02.portaltransform.recipe.Recipes;
import cn.qihuang02.portaltransform.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(
        modid = PortalTransform.MODID,
        bus = EventBusSubscriber.Bus.FORGE)
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
                .ifPresent(match -> processTransformation(event, itemEntity, serverLevel, match));
    }

    private static Optional<RecipeMatch> getValidRecipeForContext(ItemEntity itemEntity, ServerLevel level, ResourceKey<Level> targetDimKey) {
        return findItemRecipe(itemEntity, level)
                .flatMap(result -> createMatch(result, itemEntity, level, targetDimKey));
    }

    private static Optional<RecipeMatch> createMatch(RecipeResult recipeResult, ItemEntity itemEntity, ServerLevel level, ResourceKey<Level> targetDimKey) {
        ItemTransformRecipe recipe = recipeResult.recipe();
        BlockPos itemPos = itemEntity.blockPosition();

        if (!matchesItemDimensionRequirements(recipe, level.dimension(), targetDimKey) ||
                !matchesWeather(recipe, level) ||
                !matchesBiome(recipe, level, itemPos) ||
                !matchesHeight(recipe, itemPos.getY()) ||
                !matchesTime(recipe, level) ||
                !matchesItemData(recipe, itemEntity.getItem())) {
            return Optional.empty();
        }

        return Optional.of(new RecipeMatch(recipeResult));
    }

    private static void processTransformation(EntityTravelToDimensionEvent event, ItemEntity itemEntity, ServerLevel level, RecipeMatch match) {
        ItemTransformRecipe recipe = match.recipeResult().recipe();
        float chance = recipe.transformChance();

        event.setCanceled(true);

        if (level.random.nextFloat() < chance) {
            transformItem(itemEntity, level, match, event.getDimension());
        } else {
            itemEntity.discard();
        }
    }

    private static boolean hasNoPortalTransformComponent(@NotNull ItemEntity itemEntity) {
        return Components.hasNoPortalTransform(itemEntity.getItem());
    }

    private static Optional<RecipeResult> findItemRecipe(@NotNull ItemEntity itemEntity, ServerLevel currentLevel) {
        ItemStack inputStack = itemEntity.getItem();
        if (inputStack.isEmpty()) {
            return Optional.empty();
        }

        RecipeManager recipeManager = currentLevel.getRecipeManager();
        return recipeManager.getRecipeFor(
                Recipes.PORTAL_ITEM_TRANSFORM_TYPE.get(),
                new SimpleContainer(inputStack),
                currentLevel
        ).map(recipe -> new RecipeResult(recipe, recipe.getId()));
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

    private static boolean matchesBiome(@NotNull ItemTransformRecipe recipe, @NotNull ServerLevel level, @NotNull BlockPos pos) {
        Optional<Biomes> requiredBiomes = recipe.getBiomes();

        if (requiredBiomes.isEmpty()) {
            return true;
        }

        Holder<Biome> biomeHolder = level.getBiome(pos);
        return biomeHolder.unwrapKey()
                .map(requiredBiomes.get()::contains)
                .orElse(false);
    }

    private static boolean matchesHeight(@NotNull ItemTransformRecipe recipe, int yLevel) {
        return recipe.getHeightRequirement()
                .map(requirement -> requirement.matches(yLevel))
                .orElse(true);
    }

    private static boolean matchesTime(@NotNull ItemTransformRecipe recipe, @NotNull ServerLevel level) {
        return recipe.getTimeRequirement()
                .map(condition -> condition.matches(level))
                .orElse(true);
    }

    private static boolean matchesItemData(@NotNull ItemTransformRecipe recipe, @NotNull ItemStack stack) {
        return recipe.getItemDataPredicate()
                .map(predicate -> predicate.matches(stack))
                .orElse(true);
    }

    private static void transformItem(ItemEntity itemEntity, ServerLevel level, RecipeMatch match, ResourceKey<Level> targetDimKey) {
        Objects.requireNonNull(itemEntity, "ItemEntity cannot be null");
        Objects.requireNonNull(level, "Level cannot be null");
        Objects.requireNonNull(match, "Recipe match cannot be null");

        ItemTransformRecipe recipe = match.recipeResult().recipe();
        ResourceLocation recipeId = match.recipeResult().id();

        BlockPos spawnPos = itemEntity.blockPosition();
        Vec3 pos = itemEntity.position();
        Vec3 motion = itemEntity.getDeltaMovement();
        int originalInputCount = itemEntity.getItem().getCount();
        ItemStack inputCopy = itemEntity.getItem().copy();
        RandomSource random = level.random;

        ItemStack recipeResult = recipe.getResultItem(level.registryAccess());
        if (recipeResult.isEmpty()) {
            LOGGER.debug("Item Recipe {} resulted in an empty output stack!", recipe);
            itemEntity.discard();
            return;
        }

        // Only copy and modify count if needed
        ItemStack outputStack;
        if (recipeResult.getCount() == originalInputCount) {
            outputStack = recipeResult.copy();
        } else {
            outputStack = recipeResult.copyWithCount(originalInputCount);
        }
        ItemStack producedStack = outputStack.copy();

        ItemStack remainingOutput = InventoryUtil.tryPlaceInNearbyInv(level, spawnPos, outputStack);

        if (remainingOutput.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(remainingOutput);
        }

        // Process byproducts
        List<ItemStack> producedByproducts = spawnByproducts(level, pos, motion, recipe, originalInputCount, random);

        PortalItemTransformedEvent transformedEvent = new PortalItemTransformedEvent(
                level,
                targetDimKey,
                itemEntity,
                pos,
                inputCopy,
                producedStack,
                remainingOutput,
                producedByproducts,
                recipe,
                recipeId
        );

        MinecraftForge.EVENT_BUS.post(transformedEvent);

        if (ModList.get().isLoaded("kubejs")) {
            cn.qihuang02.portaltransform.compat.kubejs.event.PortalTransformKubeEvents.postItemTransformed(transformedEvent);
        }
    }

    private static List<ItemStack> spawnByproducts(ServerLevel level, Vec3 pos, Vec3 motion, ItemTransformRecipe recipe, int originalInputCount, RandomSource random) {
        Optional<List<Byproducts>> byproductsOpt = recipe.getByproducts();
        if (byproductsOpt.isEmpty()) {
            return Collections.emptyList();
        }

        HashMap<ItemStack, Integer> byproductCounts = new HashMap<>();
        for (Byproducts definition : byproductsOpt.get()) {
            for (int i = 0; i < originalInputCount; i++) {
                definition.getResult(random).ifPresent(byproductStack -> {
                    ItemStack existingKey = byproductCounts.keySet().stream()
                            .filter(stack -> ItemStack.isSameItemSameTags(stack, byproductStack))
                            .findFirst()
                            .orElse(null);

                    if (existingKey != null) {
                        byproductCounts.put(existingKey, byproductCounts.get(existingKey) + byproductStack.getCount());
                    } else {
                        byproductCounts.put(byproductStack.copy(), byproductStack.getCount());
                    }
                });
            }
        }

        if (byproductCounts.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemStack> producedStacks = new ArrayList<>(byproductCounts.size());
        byproductCounts.forEach((stack, totalCount) -> {
            ItemStack spawnStack = stack.copyWithCount(totalCount);
            spawnItemByproduct(level, pos, motion, spawnStack, random);
            producedStacks.add(spawnStack.copy());
        });

        return producedStacks;
    }

    private static void spawnItemByproduct(ServerLevel level, Vec3 pos, Vec3 motion, @NotNull ItemStack byproductStack, RandomSource random) {
        BlockPos spawnPos = BlockPos.containing(pos);

        ItemStack remainingByproduct = InventoryUtil.tryPlaceInNearbyInv(level, spawnPos, byproductStack);

        if (remainingByproduct.isEmpty()) {
            return;
        }

        int maxStackSize = byproductStack.getMaxStackSize();
        int total = byproductStack.getCount();

        Components.markNoPortalTransform(byproductStack);

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

    private record RecipeResult(ItemTransformRecipe recipe, ResourceLocation id) {
    }

    private record RecipeMatch(RecipeResult recipeResult) {
    }
}
