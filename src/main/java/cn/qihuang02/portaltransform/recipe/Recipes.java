package cn.qihuang02.portaltransform.recipe;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.portalItemTransformation.PortalItemTransformationRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Recipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, PortalTransform.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, PortalTransform.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PortalItemTransformationRecipe>> PORTAL_ITEM_TRANSFORMATION_SERIALIZER =
            RECIPE_SERIALIZERS.register("portal_item_transformation", PortalItemTransformationRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<PortalItemTransformationRecipe>> PORTAL_ITEM_TRANSFORMATION_TYPE =
            RECIPE_TYPES.register("portal_item_transformation", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "item_transformation";
                }
            });

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}
