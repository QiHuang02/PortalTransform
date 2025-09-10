package cn.qihuang02.portaltransform.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;

/**
 * KubeJS integration plugin for PortalTransform.
 */
public class PortalTransformKubeJSPlugin extends KubeJSPlugin {
    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.namespace("portaltransform").register("item_transform", ItemTransformRecipeJS.SCHEMA);
    }
}
