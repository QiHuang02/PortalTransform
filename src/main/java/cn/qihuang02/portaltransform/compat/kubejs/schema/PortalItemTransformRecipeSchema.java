package cn.qihuang02.portaltransform.compat.kubejs.schema;

import cn.qihuang02.portaltransform.compat.kubejs.components.*;
import cn.qihuang02.portaltransform.compat.kubejs.recipe.ItemTransformKubeRecipe;
import cn.qihuang02.portaltransform.recipe.ItemTransform.*;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.advancements.critereon.ItemPredicate;
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

    RecipeKey<Biomes> BIOMES = BiomesComponent.BIOMES
            .otherKey("biomes").defaultOptional().functionNames(List.of("biomes", "biome"));

    RecipeKey<Height> HEIGHT = HeightComponent.HEIGHT
            .otherKey("height").defaultOptional().functionNames(List.of("height", "y_level", "y"));

    RecipeKey<TimeCondition> TIME = TimeComponent.TIME
            .otherKey("time").defaultOptional().functionNames(List.of("time"));

    RecipeKey<ItemPredicate> ITEM_PREDICATE = ItemPredicateComponent.ITEM_PREDICATE
            .otherKey("item_predicate").defaultOptional().functionNames(List.of("item_predicate", "itemPredicate", "itemData"));

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
            BIOMES,
            HEIGHT,
            TIME,
            ITEM_PREDICATE,
            TRANSFORM_CHANCE
    ).factory(ItemTransformKubeRecipe.FACTORY);
}
