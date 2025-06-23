package cn.qihuang02.portaltransform.recipe;

import cn.qihuang02.portaltransform.PortalTransform;
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

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ItemTransformRecipe>> PORTAL_ITEM_TRANSFORM_SERIALIZER =
            RECIPE_SERIALIZERS.register("item_transform", ItemTransformRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ItemTransformRecipe>> PORTAL_ITEM_TRANSFORM_TYPE =
            RECIPE_TYPES.register("item_transform", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return PortalTransform.getRL("item_transform").toString();
                }
            });

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}
