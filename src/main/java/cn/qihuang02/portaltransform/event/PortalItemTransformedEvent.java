package cn.qihuang02.portaltransform.event;

import cn.qihuang02.portaltransform.recipe.ItemTransformRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Fired when an {@link ItemEntity} successfully transforms via a portal item transform recipe.
 */
public class PortalItemTransformedEvent extends Event {
    private final ServerLevel level;
    private final ResourceKey<Level> targetDimension;
    private final ItemEntity itemEntity;
    private final Vec3 position;
    private final ItemStack originalStack;
    private final ItemStack resultStack;
    private final ItemStack remainingStack;
    private final List<ItemStack> byproducts;
    private final ItemTransformRecipe recipe;
    private final ResourceLocation recipeId;

    public PortalItemTransformedEvent(@NotNull ServerLevel level,
                                      @NotNull ResourceKey<Level> targetDimension,
                                      @NotNull ItemEntity itemEntity,
                                      @NotNull Vec3 position,
                                      @NotNull ItemStack originalStack,
                                      @NotNull ItemStack resultStack,
                                      @NotNull ItemStack remainingStack,
                                      @NotNull List<ItemStack> byproducts,
                                      @NotNull ItemTransformRecipe recipe,
                                      @NotNull ResourceLocation recipeId) {
        this.level = Objects.requireNonNull(level, "level");
        this.targetDimension = Objects.requireNonNull(targetDimension, "targetDimension");
        this.itemEntity = Objects.requireNonNull(itemEntity, "itemEntity");
        this.position = Objects.requireNonNull(position, "position");
        this.originalStack = originalStack.copy();
        this.resultStack = resultStack.copy();
        this.remainingStack = remainingStack.copy();
        this.byproducts = List.copyOf(byproducts.stream().map(ItemStack::copy).toList());
        this.recipe = Objects.requireNonNull(recipe, "recipe");
        this.recipeId = Objects.requireNonNull(recipeId, "recipeId");
    }

    public ServerLevel getLevel() {
        return level;
    }

    public ResourceKey<Level> getCurrentDimension() {
        return level.dimension();
    }

    public ResourceKey<Level> getTargetDimension() {
        return targetDimension;
    }

    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    public Vec3 getPosition() {
        return position;
    }

    public BlockPos getBlockPos() {
        return BlockPos.containing(position);
    }

    public ItemStack getOriginalStack() {
        return originalStack.copy();
    }

    public ItemStack getResultStack() {
        return resultStack.copy();
    }

    public ItemStack getRemainingStack() {
        return remainingStack.copy();
    }

    public boolean wasInsertedIntoContainer() {
        return remainingStack.isEmpty();
    }

    public List<ItemStack> getByproducts() {
        return byproducts.stream().map(ItemStack::copy).toList();
    }

    public ItemTransformRecipe getRecipe() {
        return recipe;
    }

    public ResourceLocation getRecipeId() {
        return recipeId;
    }
}
