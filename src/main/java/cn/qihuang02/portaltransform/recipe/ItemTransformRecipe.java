package cn.qihuang02.portaltransform.recipe;

import cn.qihuang02.portaltransform.recipe.itemtransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.itemtransform.CountRange;
import cn.qihuang02.portaltransform.recipe.itemtransform.Dimensions;
import cn.qihuang02.portaltransform.recipe.itemtransform.Weather;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemTransformRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack result;
    private final List<Byproducts> byproducts;
    private final float transformChance;
    private final Dimensions dimensions;
    private final Weather weather;

    public ItemTransformRecipe(ResourceLocation id, Ingredient input, ItemStack result,
                               List<Byproducts> byproducts, float transformChance,
                               Dimensions dimensions, Weather weather) {
        this.id = id;
        this.input = input;
        this.result = result;
        this.byproducts = byproducts;
        this.transformChance = transformChance;
        this.dimensions = dimensions;
        this.weather = weather;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        return input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(SimpleContainer container, net.minecraft.core.RegistryAccess access) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess access) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input);
    }

    public List<Byproducts> getByproducts() {
        return Collections.unmodifiableList(byproducts);
    }

    public float getTransformChance() {
        return transformChance;
    }

    public java.util.Optional<ResourceKey<Level>> getCurrent() {
        return dimensions == null ? java.util.Optional.empty() : dimensions.current();
    }

    public java.util.Optional<ResourceKey<Level>> getTarget() {
        return dimensions == null ? java.util.Optional.empty() : dimensions.target();
    }

    public java.util.Optional<Weather> getWeather() {
        return java.util.Optional.ofNullable(weather);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Recipes.PORTAL_ITEM_TRANSFORM_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return Recipes.PORTAL_ITEM_TRANSFORM_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<ItemTransformRecipe> {
        @Override
        public ItemTransformRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            List<Byproducts> byproducts = new ArrayList<>();
            if (GsonHelper.isArrayNode(json, "byproducts")) {
                JsonArray array = GsonHelper.getAsJsonArray(json, "byproducts");
                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    ItemStack byproductStack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(obj, "byproduct"));
                    float chance = GsonHelper.getAsFloat(obj, "chance");
                    JsonObject countsObj = GsonHelper.getAsJsonObject(obj, "counts");
                    int min = GsonHelper.getAsInt(countsObj, "min");
                    int max = GsonHelper.getAsInt(countsObj, "max");
                    byproducts.add(new Byproducts(byproductStack, chance, new CountRange(min, max)));
                }
            }
            float transformChance = GsonHelper.getAsFloat(json, "transform_chance", 1.0F);

            Dimensions dims = null;
            if (GsonHelper.isObjectNode(json, "dimensions")) {
                JsonObject dimObj = GsonHelper.getAsJsonObject(json, "dimensions");
                ResourceKey<Level> current = null;
                ResourceKey<Level> target = null;
                if (GsonHelper.isStringValue(dimObj, "current")) {
                    ResourceLocation loc = new ResourceLocation(GsonHelper.getAsString(dimObj, "current"));
                    current = ResourceKey.create(Registries.DIMENSION, loc);
                }
                if (GsonHelper.isStringValue(dimObj, "target")) {
                    ResourceLocation loc = new ResourceLocation(GsonHelper.getAsString(dimObj, "target"));
                    target = ResourceKey.create(Registries.DIMENSION, loc);
                }
                dims = new Dimensions(current, target);
            }

            Weather weather = null;
            if (GsonHelper.isStringValue(json, "weather")) {
                weather = Weather.fromName(GsonHelper.getAsString(json, "weather"));
            }

            return new ItemTransformRecipe(id, input, output, byproducts, transformChance, dims, weather);
        }

        @Override
        public ItemTransformRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            ItemStack output = buf.readItem();
            int size = buf.readVarInt();
            List<Byproducts> byproducts = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                ItemStack stack = buf.readItem();
                float chance = buf.readFloat();
                int min = buf.readVarInt();
                int max = buf.readVarInt();
                byproducts.add(new Byproducts(stack, chance, new CountRange(min, max)));
            }
            float transformChance = buf.readFloat();

            Dimensions dims = null;
            if (buf.readBoolean()) {
                ResourceKey<Level> current = null;
                ResourceKey<Level> target = null;
                if (buf.readBoolean()) {
                    current = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
                }
                if (buf.readBoolean()) {
                    target = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
                }
                dims = new Dimensions(current, target);
            }

            Weather weather = null;
            if (buf.readBoolean()) {
                weather = buf.readEnum(Weather.class);
            }

            return new ItemTransformRecipe(id, input, output, byproducts, transformChance, dims, weather);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ItemTransformRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.byproducts.size());
            for (Byproducts bp : recipe.byproducts) {
                buf.writeItem(bp.getByproduct());
                buf.writeFloat(bp.getChance());
                buf.writeVarInt(bp.getCounts().getMin());
                buf.writeVarInt(bp.getCounts().getMax());
            }
            buf.writeFloat(recipe.transformChance);

            if (recipe.dimensions != null) {
                buf.writeBoolean(true);
                buf.writeBoolean(recipe.dimensions.current().isPresent());
                recipe.dimensions.current().ifPresent(dim -> buf.writeResourceLocation(dim.location()));
                buf.writeBoolean(recipe.dimensions.target().isPresent());
                recipe.dimensions.target().ifPresent(dim -> buf.writeResourceLocation(dim.location()));
            } else {
                buf.writeBoolean(false);
            }

            if (recipe.weather != null) {
                buf.writeBoolean(true);
                buf.writeEnum(recipe.weather);
            } else {
                buf.writeBoolean(false);
            }
        }
    }
}
