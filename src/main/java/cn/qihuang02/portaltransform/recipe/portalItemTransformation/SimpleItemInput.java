package cn.qihuang02.portaltransform.recipe.portalItemTransformation;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public record SimpleItemInput(
        ItemStack item
) implements RecipeInput {
    @Override
    public @NotNull ItemStack getItem(int index) {
        return index == 0 ? item : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return item.isEmpty();
    }

}
