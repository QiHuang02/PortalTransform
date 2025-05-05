package cn.qihuang02.portaltransform.compat.kubejs;

import cn.qihuang02.portaltransform.compat.kubejs.binding.ByproductsBinding;
import cn.qihuang02.portaltransform.compat.kubejs.components.ByproductsComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.LevelComponent;
import cn.qihuang02.portaltransform.compat.kubejs.recipe.PortalTransformKubeRecipe;
import cn.qihuang02.portaltransform.compat.kubejs.schema.PortalTransformRecipeSchema;
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
    }

    @Override
    public void registerRecipeFactories(RecipeFactoryRegistry registry) {
        registry.register(PortalTransformKubeRecipe.FACTORY);
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(Recipes.PORTAL_TRANSFORM_TYPE.getId(), PortalTransformRecipeSchema.PORTAL_TRANSFORM);
    }
}
