package cn.qihuang02.portaltransform.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

/**
 * KubeJS recipe wrapper for the Portal Transform item_transform recipe.
 */
public class ItemTransformRecipeJS extends RecipeJS {
    public static final RecipeSchema SCHEMA = new RecipeSchema(ItemTransformRecipeJS.class, ItemTransformRecipeJS::new);
}
