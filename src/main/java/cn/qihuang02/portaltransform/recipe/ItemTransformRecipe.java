package cn.qihuang02.portaltransform.recipe;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Biomes;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Dimensions;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Height;
import cn.qihuang02.portaltransform.recipe.ItemTransform.TimeCondition;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Weather;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
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

public record ItemTransformRecipe(
        Ingredient inputIngredient,
        ItemStack result,
        Optional<List<Byproducts>> byproducts,
        Optional<Dimensions> dimensions,
        Optional<Weather> weather,
        Optional<Biomes> biomes,
        Optional<Height> height,
        Optional<TimeCondition> time,
        Optional<ItemPredicate> itemPredicate,
        float transformChance
) implements Recipe<SimpleItemInput> {
    private static final int MAX_BYPRODUCT_TYPES = 9;
    private static final String ERROR_EMPTY_RESULT = "Recipe result byproduct cannot be empty";
    private static final String ERROR_TOO_MANY_BYPRODUCTS = "Recipe cannot have more than %d byproduct types, found %d";
    public static final String ERROR_EMPTY_INPUT = "Recipe input ingredient cannot be empty";

    /**
     * 验证配方数据的有效性 (用于 Codec)。
     *
     * @param recipe 待验证的配方实例
     * @return 如果有效，返回包含配方的 DataResult.success；否则返回 DataResult.error。
     */
    private static DataResult<ItemTransformRecipe> validate(@NotNull ItemTransformRecipe recipe) {
        if (recipe.inputIngredient.isEmpty()) {
            return DataResult.error(() -> ERROR_EMPTY_INPUT);
        }

        if (recipe.result.isEmpty()) {
            return DataResult.error(() -> ERROR_EMPTY_RESULT);
        }

        List<Byproducts> byproducts = recipe.byproducts().orElseGet(Collections::emptyList);
        if (byproducts.size() > MAX_BYPRODUCT_TYPES) {
            return DataResult.error(() -> String.format(ERROR_TOO_MANY_BYPRODUCTS, MAX_BYPRODUCT_TYPES, byproducts.size()));
        }

        return DataResult.success(recipe);
    }

    @Override
    public boolean matches(@NotNull SimpleItemInput input, @NotNull Level level) {
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
        return Recipes.PORTAL_ITEM_TRANSFORM_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return Recipes.PORTAL_ITEM_TRANSFORM_TYPE.get();
    }

    public Optional<List<Byproducts>> getByproducts() {
        return byproducts.map(ArrayList::new);
    }

    public Optional<ResourceKey<Level>> getCurrent() {
        return dimensions().flatMap(Dimensions::current);
    }

    public Optional<ResourceKey<Level>> getTarget() {
        return dimensions().flatMap(Dimensions::target);
    }

    public Optional<Weather> getWeather() {
        return weather;
    }

    public Optional<Biomes> getBiomes() {
        return biomes;
    }

    public Optional<Height> getHeightRequirement() {
        return height;
    }

    public Optional<TimeCondition> getTimeRequirement() {
        return time;
    }

    public Optional<ItemPredicate> getItemDataPredicate() {
        return itemPredicate;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    public static class Serializer implements RecipeSerializer<ItemTransformRecipe> {
        private static final StreamCodec<RegistryFriendlyByteBuf, Optional<List<Byproducts>>> OPTIONAL_BYPRODUCTS_STREAM_CODEC =
                ByteBufCodecs.optional(ByteBufCodecs.collection(ArrayList::new, Byproducts.STREAM_CODEC));
        private static final StreamCodec<RegistryFriendlyByteBuf, Optional<Dimensions>> OPTIONAL_DIMENSIONS_STREAM_CODEC =
                ByteBufCodecs.optional(Dimensions.STREAM_CODEC);
        private static final StreamCodec<RegistryFriendlyByteBuf, Optional<Biomes>> OPTIONAL_BIOMES_STREAM_CODEC =
                ByteBufCodecs.optional(Biomes.STREAM_CODEC);
        private static final StreamCodec<RegistryFriendlyByteBuf, ItemPredicate> ITEM_PREDICATE_STREAM_CODEC =
                ByteBufCodecs.fromCodecWithRegistries(ItemPredicate.CODEC);

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemTransformRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::encode, Serializer::decode);

        private static void encode(RegistryFriendlyByteBuf buf, ItemTransformRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.inputIngredient());
            ItemStack.STREAM_CODEC.encode(buf, recipe.result());
            OPTIONAL_BYPRODUCTS_STREAM_CODEC.encode(buf, recipe.byproducts());
            OPTIONAL_DIMENSIONS_STREAM_CODEC.encode(buf, recipe.dimensions());
            encodeWeather(buf, recipe.weather());
            OPTIONAL_BIOMES_STREAM_CODEC.encode(buf, recipe.biomes());
            encodeOptionalHeight(buf, recipe.height());
            encodeOptionalTime(buf, recipe.time());
            encodeOptionalItemPredicate(buf, recipe.itemPredicate());
            ByteBufCodecs.FLOAT.encode(buf, recipe.transformChance());
        }

        private static ItemTransformRecipe decode(RegistryFriendlyByteBuf buf) {
            Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            Optional<List<Byproducts>> byproducts = OPTIONAL_BYPRODUCTS_STREAM_CODEC.decode(buf);
            Optional<Dimensions> dimensions = OPTIONAL_DIMENSIONS_STREAM_CODEC.decode(buf);
            Optional<Weather> weather = decodeWeather(buf);
            Optional<Biomes> biomes = OPTIONAL_BIOMES_STREAM_CODEC.decode(buf);
            Optional<Height> height = decodeOptionalHeight(buf);
            Optional<TimeCondition> time = decodeOptionalTime(buf);
            Optional<ItemPredicate> predicate = decodeOptionalItemPredicate(buf);
            float chance = ByteBufCodecs.FLOAT.decode(buf);
            return new ItemTransformRecipe(input, result, byproducts, dimensions, weather, biomes, height, time, predicate, chance);
        }

        private static void encodeOptionalHeight(RegistryFriendlyByteBuf buf, Optional<Height> height) {
            buf.writeBoolean(height.isPresent());
            height.ifPresent(value -> Height.STREAM_CODEC.encode(buf, value));
        }

        private static Optional<Height> decodeOptionalHeight(RegistryFriendlyByteBuf buf) {
            if (!buf.readBoolean()) {
                return Optional.empty();
            }
            return Optional.of(Height.STREAM_CODEC.decode(buf));
        }

        private static void encodeOptionalTime(RegistryFriendlyByteBuf buf, Optional<TimeCondition> time) {
            buf.writeBoolean(time.isPresent());
            time.ifPresent(value -> TimeCondition.STREAM_CODEC.encode(buf, value));
        }

        private static Optional<TimeCondition> decodeOptionalTime(RegistryFriendlyByteBuf buf) {
            if (!buf.readBoolean()) {
                return Optional.empty();
            }
            return Optional.of(TimeCondition.STREAM_CODEC.decode(buf));
        }

        private static void encodeOptionalItemPredicate(RegistryFriendlyByteBuf buf, Optional<ItemPredicate> predicate) {
            buf.writeBoolean(predicate.isPresent());
            predicate.ifPresent(value -> ITEM_PREDICATE_STREAM_CODEC.encode(buf, value));
        }

        private static Optional<ItemPredicate> decodeOptionalItemPredicate(RegistryFriendlyByteBuf buf) {
            if (!buf.readBoolean()) {
                return Optional.empty();
            }
            return Optional.of(ITEM_PREDICATE_STREAM_CODEC.decode(buf));
        }

        private static void encodeWeather(RegistryFriendlyByteBuf buf, Optional<Weather> weather) {
            buf.writeBoolean(weather.isPresent());
            weather.ifPresent(value -> Weather.STREAM_CODEC.encode(buf, value));
        }

        private static Optional<Weather> decodeWeather(RegistryFriendlyByteBuf buf) {
            if (!buf.readBoolean()) {
                return Optional.empty();
            }
            return Optional.of(Weather.STREAM_CODEC.decode(buf));
        }

        private static final MapCodec<ItemTransformRecipe> BASE_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(ItemTransformRecipe::inputIngredient),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ItemTransformRecipe::result),
                        Byproducts.CODEC.codec().listOf().optionalFieldOf("byproducts").forGetter(ItemTransformRecipe::byproducts),
                        Dimensions.CODEC.optionalFieldOf("dimensions").forGetter(ItemTransformRecipe::dimensions),
                        Weather.CODEC.optionalFieldOf("weather").forGetter(ItemTransformRecipe::weather),
                        Biomes.CODEC.optionalFieldOf("biomes").forGetter(ItemTransformRecipe::biomes),
                        Height.CODEC.optionalFieldOf("height").forGetter(ItemTransformRecipe::height),
                        TimeCondition.CODEC.optionalFieldOf("time").forGetter(ItemTransformRecipe::time),
                        ItemPredicate.CODEC.optionalFieldOf("item_predicate").forGetter(ItemTransformRecipe::itemPredicate),
                        Codec.floatRange(0.0F, 1.0F).optionalFieldOf("transform_chance", 1.0F).forGetter(ItemTransformRecipe::transformChance)
                ).apply(instance, ItemTransformRecipe::new)
        );

        public static final MapCodec<ItemTransformRecipe> CODEC = BASE_CODEC
                .flatXmap(ItemTransformRecipe::validate,
                        ItemTransformRecipe::validate
                );

        public @NotNull MapCodec<ItemTransformRecipe> codec() {
            return CODEC;
        }

        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ItemTransformRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
