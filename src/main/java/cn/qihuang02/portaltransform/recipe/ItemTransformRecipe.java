package cn.qihuang02.portaltransform.recipe;

import cn.qihuang02.portaltransform.recipe.itemtransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.itemtransform.CountRange;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
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

    public ItemTransformRecipe(ResourceLocation id, Ingredient input, ItemStack result, List<Byproducts> byproducts, float transformChance) {
        this.id = id;
        this.input = input;
        this.result = result;
        this.byproducts = byproducts;
        this.transformChance = transformChance;
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
            return new ItemTransformRecipe(id, input, output, byproducts, transformChance);
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
            return new ItemTransformRecipe(id, input, output, byproducts, transformChance);
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
        }
    }
}
