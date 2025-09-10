package cn.qihuang02.portaltransform.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;

/**
 * KubeJS integration plugin for PortalTransform.
 */
public class PortalTransformKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.namespace("portaltransform").register("item_transform", ItemTransformRecipeJS.SCHEMA);
    }
}
