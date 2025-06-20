package cn.qihuang02.portaltransform.compat.kubejs.schema;

import cn.qihuang02.portaltransform.compat.kubejs.components.ByproductsComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.DimensionsComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.WeatherComponent;
import cn.qihuang02.portaltransform.compat.kubejs.recipe.ItemTransformKubeRecipe;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Dimensions;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Weather;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public interface PortalItemTransformRecipeSchema {
    RecipeKey<Ingredient> INPUT = IngredientComponent.INGREDIENT
            .inputKey("input");
    RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK
            .outputKey("result");

    RecipeKey<Dimensions> DIMENSIONS = DimensionsComponent.DIMENSIONS
            .otherKey("dimensions").optional(Dimensions.empty()).functionNames(List.of("dimensions"));

    RecipeKey<Weather> WEATHER = WeatherComponent.WEATHER
            .otherKey("weather").optional(Weather.ANY).functionNames(List.of("weather"));

    RecipeKey<List<Byproducts>> BYPRODUCTS = ByproductsComponent.LIST
            .otherKey("byproducts").defaultOptional().functionNames(List.of("byproducts"));

    RecipeKey<Float> TRANSFORM_CHANCE = NumberComponent.FLOAT
            .otherKey("transform_chance").optional(1.0F).functionNames(List.of("chance"));

    RecipeSchema PORTAL_TRANSFORM = new RecipeSchema(
            INPUT,
            RESULT,
            BYPRODUCTS,
            DIMENSIONS,
            WEATHER,
            TRANSFORM_CHANCE
    ).factory(ItemTransformKubeRecipe.FACTORY);
}
