package cn.qihuang02.portaltransform.recipe.custom;

import cn.qihuang02.portaltransform.recipe.Recipes;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record PortalTransformRecipe(
        Ingredient inputIngredient,
        Optional<ResourceKey<Level>> currentDimension,
        Optional<ResourceKey<Level>> targetDimension,
        ItemStack result,
        Optional<List<Byproducts>> byproducts
) implements Recipe<SimpleItemInput> {
    private static final int MAX_BYPRODUCT_TYPES = 9;
    private static final String ERROR_EMPTY_RESULT = "Recipe result byproduct cannot be empty";
    private static final String ERROR_TOO_MANY_BYPRODUCTS = "Recipe cannot have more than %d byproduct types, found %d";

    /**
     * 验证配方数据的有效性 (用于 Codec)。
     *
     * @param recipe 待验证的配方实例
     * @return 如果有效，返回包含配方的 DataResult.success；否则返回 DataResult.error。
     */
    private static DataResult<PortalTransformRecipe> validate(PortalTransformRecipe recipe) {
        if (recipe.result.isEmpty()) {
            return DataResult.error(() -> ERROR_EMPTY_RESULT);
        }

        List<Byproducts> byproducts = recipe.byproducts().orElseGet(Collections::emptyList);
        if (byproducts.size() > MAX_BYPRODUCT_TYPES) {
            return DataResult.error(() -> String.format(ERROR_TOO_MANY_BYPRODUCTS, MAX_BYPRODUCT_TYPES, byproducts.size()));
        }

        for (int i = 0; i < byproducts.size(); i++) {
            String error = Byproducts.validate(byproducts.get(i), i);
            if (error != null) {
                return DataResult.error(() -> error);
            }
        }
        return DataResult.success(recipe);
    }

    @Override
    public boolean matches(SimpleItemInput input, @NotNull Level level) {
        return inputIngredient.test(input.getItem(0));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SimpleItemInput input, HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(this.inputIngredient);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Recipes.PORTAL_TRANSFORM_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return Recipes.PORTAL_TRANSFORM_TYPE.get();
    }

    public Optional<List<Byproducts>> getByproducts() {
        return byproducts.map(ArrayList::new);
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    public Optional<ResourceKey<Level>> getCurrentDimension() {
        return currentDimension;
    }

    public Optional<ResourceKey<Level>> getTargetDimension() {
        return targetDimension;
    }

    public static class Serializer implements RecipeSerializer<PortalTransformRecipe> {
        public static final StreamCodec<RegistryFriendlyByteBuf, PortalTransformRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, PortalTransformRecipe::inputIngredient,
                ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)), PortalTransformRecipe::currentDimension,
                ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)), PortalTransformRecipe::targetDimension,
                ItemStack.STREAM_CODEC, PortalTransformRecipe::result,
                ByteBufCodecs.optional(ByteBufCodecs.collection(ArrayList::new, Byproducts.STREAM_CODEC)), PortalTransformRecipe::byproducts,
                PortalTransformRecipe::new
        );

        private static final MapCodec<PortalTransformRecipe> BASE_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(PortalTransformRecipe::inputIngredient),
                        ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("current_dimension").forGetter(PortalTransformRecipe::currentDimension),
                        ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("target_dimension").forGetter(PortalTransformRecipe::targetDimension),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(PortalTransformRecipe::result),
                        Byproducts.CODEC.codec().listOf().optionalFieldOf("byproducts").forGetter(PortalTransformRecipe::byproducts)
                ).apply(instance, PortalTransformRecipe::new)
        );

        public static final MapCodec<PortalTransformRecipe> CODEC = BASE_CODEC
                .flatXmap(PortalTransformRecipe::validate,
                        PortalTransformRecipe::validate
                );

        public @NotNull MapCodec<PortalTransformRecipe> codec() {
            return CODEC;
        }

        public @NotNull StreamCodec<RegistryFriendlyByteBuf, PortalTransformRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
