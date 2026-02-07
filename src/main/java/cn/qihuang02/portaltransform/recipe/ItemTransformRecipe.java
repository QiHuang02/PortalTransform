package cn.qihuang02.portaltransform.recipe;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Biomes;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Dimensions;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Height;
import cn.qihuang02.portaltransform.recipe.ItemTransform.TimeCondition;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Weather;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ItemTransformRecipe implements Recipe<SimpleContainer> {
    public static final int MAX_BYPRODUCT_TYPES = 9;
    public static final String ERROR_EMPTY_INPUT = "Recipe input ingredient cannot be empty";
    private static final String ERROR_EMPTY_RESULT = "Recipe result byproduct cannot be empty";
    private static final String ERROR_TOO_MANY_BYPRODUCTS = "Recipe cannot have more than %d byproduct types, found %d";

    private final ResourceLocation id;
    private final Ingredient inputIngredient;
    private final ItemStack result;
    private final Optional<List<Byproducts>> byproducts;
    private final Optional<Dimensions> dimensions;
    private final Optional<Weather> weather;
    private final Optional<Biomes> biomes;
    private final Optional<Height> height;
    private final Optional<TimeCondition> time;
    private final Optional<ItemPredicate> itemPredicate;
    private final float transformChance;

    public ItemTransformRecipe(ResourceLocation id,
                               Ingredient inputIngredient,
                               ItemStack result,
                               Optional<List<Byproducts>> byproducts,
                               Optional<Dimensions> dimensions,
                               Optional<Weather> weather,
                               Optional<Biomes> biomes,
                               Optional<Height> height,
                               Optional<TimeCondition> time,
                               Optional<ItemPredicate> itemPredicate,
                               float transformChance) {
        this.id = id;
        this.inputIngredient = inputIngredient;
        this.result = result.copy();
        this.byproducts = byproducts.map(list -> List.copyOf(list.stream().map(Byproducts::copy).toList()));
        this.dimensions = dimensions;
        this.weather = weather;
        this.biomes = biomes;
        this.height = height;
        this.time = time;
        this.itemPredicate = itemPredicate;
        this.transformChance = transformChance;
        validate();
    }

    private void validate() {
        if (inputIngredient.isEmpty()) {
            throw new IllegalArgumentException(ERROR_EMPTY_INPUT);
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException(ERROR_EMPTY_RESULT);
        }

        if (transformChance < 0.0F || transformChance > 1.0F) {
            throw new IllegalArgumentException("Transform chance must be between 0.0 and 1.0");
        }

        List<Byproducts> byproductList = byproducts.orElse(Collections.emptyList());
        if (byproductList.size() > MAX_BYPRODUCT_TYPES) {
            throw new IllegalArgumentException(String.format(ERROR_TOO_MANY_BYPRODUCTS, MAX_BYPRODUCT_TYPES, byproductList.size()));
        }
    }

    @Override
    public boolean matches(@NotNull SimpleContainer container, @NotNull Level level) {
        return inputIngredient.test(container.getItem(0));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SimpleContainer container, @NotNull RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, this.inputIngredient);
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Recipes.PORTAL_ITEM_TRANSFORM_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return Recipes.PORTAL_ITEM_TRANSFORM_TYPE.get();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    public Optional<List<Byproducts>> getByproducts() {
        return byproducts.map(list -> list.stream().map(Byproducts::copy).toList());
    }

    public Optional<ResourceKey<Level>> getCurrent() {
        return dimensions.flatMap(Dimensions::current);
    }

    public Optional<ResourceKey<Level>> getTarget() {
        return dimensions.flatMap(Dimensions::target);
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

    public float transformChance() {
        return transformChance;
    }

    public Ingredient inputIngredient() {
        return inputIngredient;
    }

    public ItemStack result() {
        return result;
    }

    public static class Serializer implements RecipeSerializer<ItemTransformRecipe> {
        @Override
        public ItemTransformRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            Optional<List<Byproducts>> byproducts = parseByproducts(json);
            Optional<Dimensions> dimensions = parseDimensions(json);
            Optional<Weather> weather = parseWeather(json);
            Optional<Biomes> biomes = parseBiomes(json);
            Optional<Height> height = parseHeight(json);
            Optional<TimeCondition> time = parseTime(json);
            Optional<ItemPredicate> predicate = parseItemPredicate(json);
            float chance = GsonHelper.getAsFloat(json, "transform_chance", 1.0F);

            return new ItemTransformRecipe(recipeId, input, result, byproducts, dimensions, weather, biomes, height, time, predicate, chance);
        }

        @Override
        public ItemTransformRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            Optional<List<Byproducts>> byproducts = readOptionalList(buf, Byproducts::fromNetwork);
            Optional<Dimensions> dimensions = readOptional(buf, Dimensions::fromNetwork);
            Optional<Weather> weather = readOptional(buf, Weather::fromNetwork);
            Optional<Biomes> biomes = readOptional(buf, Biomes::fromNetwork);
            Optional<Height> height = readOptional(buf, Height::fromNetwork);
            Optional<TimeCondition> time = readOptional(buf, TimeCondition::fromNetwork);
            Optional<ItemPredicate> predicate = readOptional(buf, Serializer::readItemPredicate);
            float chance = buf.readFloat();
            return new ItemTransformRecipe(recipeId, input, result, byproducts, dimensions, weather, biomes, height, time, predicate, chance);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, ItemTransformRecipe recipe) {
            recipe.inputIngredient().toNetwork(buf);
            buf.writeItem(recipe.result());
            writeOptionalList(buf, recipe.byproducts, (buffer, value) -> value.toNetwork(buffer));
            writeOptional(buf, recipe.dimensions, (buffer, value) -> value.toNetwork(buffer));
            writeOptional(buf, recipe.weather, (buffer, value) -> value.toNetwork(buffer));
            writeOptional(buf, recipe.biomes, (buffer, value) -> value.toNetwork(buffer));
            writeOptional(buf, recipe.height, (buffer, value) -> value.toNetwork(buffer));
            writeOptional(buf, recipe.time, (buffer, value) -> value.toNetwork(buffer));
            writeOptional(buf, recipe.itemPredicate, Serializer::writeItemPredicate);
            buf.writeFloat(recipe.transformChance);
        }

        private static Optional<List<Byproducts>> parseByproducts(JsonObject json) {
            if (!json.has("byproducts")) {
                return Optional.empty();
            }

            JsonArray array = GsonHelper.getAsJsonArray(json, "byproducts");
            if (array.size() == 0) {
                return Optional.empty();
            }

            List<Byproducts> list = new ArrayList<>(array.size());
            for (JsonElement element : array) {
                list.add(Byproducts.fromJson(GsonHelper.convertToJsonObject(element, "byproduct")));
            }
            return Optional.of(list);
        }

        private static Optional<Dimensions> parseDimensions(JsonObject json) {
            if (!json.has("dimensions")) {
                return Optional.empty();
            }

            JsonElement element = json.get("dimensions");
            if (element.isJsonArray()) {
                List<ResourceKey<Level>> dims = new ArrayList<>();
                JsonArray array = element.getAsJsonArray();
                for (JsonElement entry : array) {
                    dims.add(parseLevel(entry));
                }
                return Optional.of(new Dimensions(dims));
            }
            JsonObject obj = GsonHelper.convertToJsonObject(element, "dimensions");
            List<ResourceKey<Level>> dims = new ArrayList<>();
            if (obj.has("current")) {
                dims.add(parseLevel(obj.get("current")));
            }
            if (obj.has("target")) {
                dims.add(parseLevel(obj.get("target")));
            }
            return Optional.of(new Dimensions(dims));
        }

        private static Optional<Weather> parseWeather(JsonObject json) {
            if (!json.has("weather")) {
                return Optional.empty();
            }
            String value = GsonHelper.getAsString(json, "weather");
            Weather weather = Weather.fromName(value);
            if (weather == null) {
                throw new JsonParseException("Unknown weather value: " + value);
            }
            return Optional.of(weather);
        }

        private static Optional<Biomes> parseBiomes(JsonObject json) {
            if (!json.has("biomes")) {
                return Optional.empty();
            }
            JsonArray array = GsonHelper.getAsJsonArray(json, "biomes");
            if (array.size() == 0) {
                return Optional.empty();
            }
            List<ResourceKey<net.minecraft.world.level.biome.Biome>> keys = new ArrayList<>(array.size());
            for (JsonElement element : array) {
                ResourceLocation id = parseResourceLocation(element, "biome");
                keys.add(ResourceKey.create(net.minecraft.core.registries.Registries.BIOME, id));
            }
            return Optional.of(new Biomes(keys));
        }

        private static Optional<Height> parseHeight(JsonObject json) {
            if (!json.has("height")) {
                return Optional.empty();
            }
            JsonObject obj = GsonHelper.getAsJsonObject(json, "height");
            Optional<Integer> min = obj.has("min") ? Optional.of(GsonHelper.getAsInt(obj, "min")) : Optional.empty();
            Optional<Integer> max = obj.has("max") ? Optional.of(GsonHelper.getAsInt(obj, "max")) : Optional.empty();
            return Optional.of(new Height(min, max));
        }

        private static Optional<TimeCondition> parseTime(JsonObject json) {
            if (!json.has("time") || json.get("time").isJsonNull()) {
                return Optional.empty();
            }

            JsonElement element = json.get("time");
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                return Optional.of(parseKeywordTime(element.getAsString()));
            }
            if (element.isJsonArray()) {
                return Optional.of(parseRangeTime(element.getAsJsonArray()));
            }
            if (element.isJsonObject()) {
                return Optional.of(parseLegacyTimeObject(element.getAsJsonObject()));
            }
            throw new JsonParseException("Invalid time condition format. Expected keyword string, [start, end] array, or legacy object.");
        }

        // 兼容旧版 object 写法，同时支持新版字符串/数组语义。
        private static TimeCondition parseLegacyTimeObject(JsonObject obj) {
            if (!obj.has("mode") && obj.has("start") && obj.has("end")) {
                return TimeCondition.range(GsonHelper.getAsInt(obj, "start"), GsonHelper.getAsInt(obj, "end"));
            }

            String modeName = GsonHelper.getAsString(obj, "mode", "any").toLowerCase(Locale.ROOT);
            return switch (modeName) {
                case "any" -> TimeCondition.any();
                case "day" -> TimeCondition.day();
                case "night" -> TimeCondition.night();
                case "noon" -> TimeCondition.noon();
                case "midnight" -> TimeCondition.midnight();
                case "range" -> {
                    if (!obj.has("start") || !obj.has("end")) {
                        throw new JsonParseException("Time mode 'range' requires both start and end.");
                    }
                    yield TimeCondition.range(GsonHelper.getAsInt(obj, "start"), GsonHelper.getAsInt(obj, "end"));
                }
                default -> throw new JsonParseException("Unknown time mode: " + modeName);
            };
        }

        private static TimeCondition parseKeywordTime(String value) {
            return TimeCondition.Keyword.byName(value.trim().toLowerCase(Locale.ROOT))
                    .map(TimeCondition::keyword)
                    .orElseThrow(() -> new JsonParseException("Unknown time keyword: " + value));
        }

        private static TimeCondition parseRangeTime(JsonArray array) {
            if (array.size() != 2) {
                throw new JsonParseException("Time range must be [start, end].");
            }
            int start = GsonHelper.convertToInt(array.get(0), "time[0]");
            int end = GsonHelper.convertToInt(array.get(1), "time[1]");
            return TimeCondition.range(start, end);
        }

        private static Optional<ItemPredicate> parseItemPredicate(JsonObject json) {
            if (!json.has("item_predicate")) {
                return Optional.empty();
            }
            JsonElement element = json.get("item_predicate");
            return Optional.of(ItemPredicate.fromJson(element));
        }

        private static ResourceKey<Level> parseLevel(JsonElement element) {
            ResourceLocation id = parseResourceLocation(element, "dimension");
            return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id);
        }

        private static ResourceLocation parseResourceLocation(JsonElement element, String name) {
            String value = GsonHelper.convertToString(element, name);
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (id == null) {
                throw new JsonParseException("Invalid " + name + " id: " + value);
            }
            return id;
        }

        private static <T> void writeOptional(FriendlyByteBuf buf, Optional<T> optional, BiConsumer<FriendlyByteBuf, T> writer) {
            buf.writeBoolean(optional.isPresent());
            optional.ifPresent(value -> writer.accept(buf, value));
        }

        private static <T> Optional<T> readOptional(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> reader) {
            return buf.readBoolean() ? Optional.of(reader.apply(buf)) : Optional.empty();
        }

        private static <T> void writeOptionalList(FriendlyByteBuf buf, Optional<List<T>> optional, BiConsumer<FriendlyByteBuf, T> writer) {
            buf.writeBoolean(optional.isPresent());
            if (optional.isPresent()) {
                List<T> list = optional.get();
                buf.writeVarInt(list.size());
                for (T element : list) {
                    writer.accept(buf, element);
                }
            }
        }

        private static <T> Optional<List<T>> readOptionalList(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> reader) {
            if (!buf.readBoolean()) {
                return Optional.empty();
            }
            int size = buf.readVarInt();
            List<T> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(reader.apply(buf));
            }
            return Optional.of(list);
        }

        private static void writeItemPredicate(FriendlyByteBuf buf, ItemPredicate predicate) {
            buf.writeUtf(predicate.serializeToJson().toString());
        }

        private static ItemPredicate readItemPredicate(FriendlyByteBuf buf) {
            String json = buf.readUtf();
            return ItemPredicate.fromJson(JsonParser.parseString(json));
        }
    }
}
