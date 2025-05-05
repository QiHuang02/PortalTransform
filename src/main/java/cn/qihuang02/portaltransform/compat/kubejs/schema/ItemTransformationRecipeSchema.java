package cn.qihuang02.portaltransform.compat.kubejs.schema;

import cn.qihuang02.portaltransform.compat.kubejs.components.ByproductsComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.LevelComponent;
import cn.qihuang02.portaltransform.compat.kubejs.recipe.PortalTransformKubeRecipe;
import cn.qihuang02.portaltransform.recipe.itemTransformation.Byproducts;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.List;

public interface ItemTransformationRecipeSchema {
    RecipeKey<Ingredient> INPUT = IngredientComponent.INGREDIENT
            .inputKey("input");
    RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK
            .outputKey("result");
    RecipeKey<ResourceKey<Level>> CURRENT_DIMENSION = LevelComponent.DIMENSION
            .otherKey("current_dimension").defaultOptional();
    RecipeKey<ResourceKey<Level>> TARGET_DIMENSION = LevelComponent.DIMENSION
            .otherKey("target_dimension").defaultOptional();
    RecipeKey<List<Byproducts>> BYPRODUCTS = ByproductsComponent.LIST
            .otherKey("byproducts").defaultOptional();

    RecipeSchema PORTAL_TRANSFORM = new RecipeSchema(
            INPUT,
            RESULT,
            CURRENT_DIMENSION,
            TARGET_DIMENSION,
            BYPRODUCTS
    ).factory(PortalTransformKubeRecipe.FACTORY);
}
