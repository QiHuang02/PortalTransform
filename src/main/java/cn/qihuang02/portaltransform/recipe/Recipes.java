package cn.qihuang02.portaltransform.recipe;

import cn.qihuang02.portaltransform.PortalTransform;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Recipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, PortalTransform.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, PortalTransform.MODID);

    public static final RegistryObject<RecipeSerializer<ItemTransformRecipe>> PORTAL_ITEM_TRANSFORM_SERIALIZER =
            RECIPE_SERIALIZERS.register("item_transform", ItemTransformRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<ItemTransformRecipe>> PORTAL_ITEM_TRANSFORM_TYPE =
            RECIPE_TYPES.register("item_transform", () -> new RecipeType<>() {});

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}
