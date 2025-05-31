package cn.qihuang02.portaltransform.compat.kubejs;

import cn.qihuang02.portaltransform.compat.kubejs.binding.ByproductsBinding;
import cn.qihuang02.portaltransform.compat.kubejs.components.ByproductsComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.DimensionsComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.LevelComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.WeatherComponent;
import cn.qihuang02.portaltransform.compat.kubejs.recipe.ItemTransformKubeRecipe;
import cn.qihuang02.portaltransform.compat.kubejs.schema.PortalItemTransformRecipeSchema;
import cn.qihuang02.portaltransform.recipe.Recipes;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import org.jetbrains.annotations.NotNull;

public class KubeJSPlugin implements dev.latvian.mods.kubejs.plugin.KubeJSPlugin {

    @Override
    public void registerBindings(@NotNull BindingRegistry bindings) {
        if (bindings.type().isServer()) {
            bindings.add("Byproduct", ByproductsBinding.class);
        }
    }

    @Override
    public void registerRecipeComponents(@NotNull RecipeComponentFactoryRegistry registry) {
        registry.register(LevelComponent.DIMENSION);
        registry.register(ByproductsComponent.BYPRODUCT);
        registry.register(DimensionsComponent.DIMENSIONS);
        registry.register(WeatherComponent.WEATHER);
    }

    @Override
    public void registerRecipeFactories(@NotNull RecipeFactoryRegistry registry) {
        registry.register(ItemTransformKubeRecipe.FACTORY);
    }

    @Override
    public void registerRecipeSchemas(@NotNull RecipeSchemaRegistry registry) {
        registry.register(Recipes.PORTAL_ITEM_TRANSFORM_TYPE.getId(), PortalItemTransformRecipeSchema.PORTAL_TRANSFORM);
    }
}
