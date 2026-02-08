package cn.qihuang02.portaltransform.compat.kubejs.schema;

import cn.qihuang02.portaltransform.compat.kubejs.components.BiomesComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.ByproductsComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.DimensionsComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.HeightComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.ItemPredicateComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.TimeComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.WeatherComponent;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Biomes;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Dimensions;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Height;
import cn.qihuang02.portaltransform.recipe.ItemTransform.TimeCondition;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Weather;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.advancements.critereon.ItemPredicate;

public interface PortalItemTransformRecipeSchema {
    RecipeKey<InputItem> INPUT = ItemComponents.INPUT.key("input");
    RecipeKey<OutputItem> RESULT = ItemComponents.OUTPUT.key("result");

    RecipeKey<Dimensions> DIMENSIONS = DimensionsComponent.INSTANCE.key("dimensions")
            .optional(Dimensions.empty())
            .alt("dimension");

    RecipeKey<Weather> WEATHER = WeatherComponent.INSTANCE.key("weather")
            .optional(Weather.ANY);

    RecipeKey<Biomes> BIOMES = BiomesComponent.INSTANCE.key("biomes")
            .defaultOptional()
            .alt("biome");

    RecipeKey<Height> HEIGHT = HeightComponent.INSTANCE.key("height")
            .defaultOptional()
            .alt("y_level")
            .alt("y");

    RecipeKey<TimeCondition> TIME = TimeComponent.INSTANCE.key("time")
            .defaultOptional();

    RecipeKey<ItemPredicate> ITEM_PREDICATE = ItemPredicateComponent.INSTANCE.key("item_predicate")
            .defaultOptional()
            .alt("itemPredicate")
            .alt("itemData");

    RecipeKey<Byproducts[]> BYPRODUCTS = ByproductsComponent.ARRAY.key("byproducts")
            .defaultOptional();

    RecipeKey<Float> TRANSFORM_CHANCE = NumberComponent.ANY_FLOAT.key("transform_chance")
            .optional(1.0F)
            .alt("chance");

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
    ).uniqueOutputId(RESULT)
            .constructor(INPUT, RESULT)
            .constructor(INPUT, RESULT, BYPRODUCTS);
}
