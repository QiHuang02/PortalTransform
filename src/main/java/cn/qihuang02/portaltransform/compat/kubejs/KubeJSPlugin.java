package cn.qihuang02.portaltransform.compat.kubejs;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.compat.kubejs.binding.ByproductsBinding;
import cn.qihuang02.portaltransform.compat.kubejs.components.BiomesComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.ByproductsComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.DimensionsComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.HeightComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.ItemPredicateComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.TimeComponent;
import cn.qihuang02.portaltransform.compat.kubejs.components.WeatherComponent;
import cn.qihuang02.portaltransform.compat.kubejs.event.PortalTransformKubeEvents;
import cn.qihuang02.portaltransform.compat.kubejs.schema.PortalItemTransformRecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistryEvent;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.rhino.util.HideFromJS;

@HideFromJS
public class KubeJSPlugin extends dev.latvian.mods.kubejs.KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        if (event.getType().isServer()) {
            event.add("Byproduct", ByproductsBinding.class);
        }
    }

    @Override
    public void registerEvents() {
        PortalTransformKubeEvents.GROUP.register();
    }

    @Override
    public void registerRecipeComponents(RecipeComponentFactoryRegistryEvent event) {
        event.register(ByproductsComponent.TYPE_ID, ByproductsComponent.INSTANCE);
        event.register(DimensionsComponent.TYPE_ID, DimensionsComponent.INSTANCE);
        event.register(BiomesComponent.TYPE_ID, BiomesComponent.INSTANCE);
        event.register(WeatherComponent.TYPE_ID, WeatherComponent.INSTANCE);
        event.register(HeightComponent.TYPE_ID, HeightComponent.INSTANCE);
        event.register(TimeComponent.TYPE_ID, TimeComponent.INSTANCE);
        event.register(ItemPredicateComponent.TYPE_ID, ItemPredicateComponent.INSTANCE);
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.register(PortalTransform.getRL("item_transform"), PortalItemTransformRecipeSchema.PORTAL_TRANSFORM);
    }
}
