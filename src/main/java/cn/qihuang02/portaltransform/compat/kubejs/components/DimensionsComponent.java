package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Dimensions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.JsonIO;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

@HideFromJS
public class DimensionsComponent implements RecipeComponent<Dimensions> {
    public static final String TYPE_ID = "portaltransform:dimensions";
    public static final DimensionsComponent INSTANCE = new DimensionsComponent();

    @Override
    public String componentType() {
        return TYPE_ID;
    }

    @Override
    public Class<?> componentClass() {
        return Dimensions.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, Dimensions value) {
        JsonArray array = new JsonArray();
        if (value == null) {
            return array;
        }

        for (ResourceKey<Level> key : value.dimensions()) {
            array.add(key.location().toString());
        }
        return array;
    }

    @Override
    public Dimensions read(RecipeJS recipe, Object from) {
        if (from == null || from instanceof Undefined || from == Scriptable.NOT_FOUND) {
            return Dimensions.empty();
        }

        if (from instanceof Dimensions dimensionsInstance) {
            return dimensionsInstance;
        }

        JsonElement element = JsonIO.of(from);
        if (element == null || element.isJsonNull()) {
            return Dimensions.empty();
        }

        if (element.isJsonArray()) {
            return parseArray(element.getAsJsonArray());
        }

        if (element.isJsonObject()) {
            return parseObject(element.getAsJsonObject());
        }

        throw new IllegalArgumentException("dimensions 必须是数组或对象");
    }

    private static Dimensions parseArray(JsonArray array) {
        if (array.isEmpty()) {
            return Dimensions.empty();
        }
        if (array.size() != 2) {
            throw new IllegalArgumentException("dimensions 数组必须为空或正好 2 个元素");
        }

        List<ResourceKey<Level>> dims = new ArrayList<>(2);
        dims.add(parseLevelKey(array.get(0)));
        dims.add(parseLevelKey(array.get(1)));
        return new Dimensions(dims);
    }

    private static Dimensions parseObject(JsonObject object) {
        if (!object.has("current") && !object.has("target")) {
            return Dimensions.empty();
        }

        if (!object.has("current") || !object.has("target")) {
            throw new IllegalArgumentException("dimensions 对象必须同时提供 current 和 target");
        }

        List<ResourceKey<Level>> dims = new ArrayList<>(2);
        dims.add(parseLevelKey(object.get("current")));
        dims.add(parseLevelKey(object.get("target")));
        return new Dimensions(dims);
    }

    private static ResourceKey<Level> parseLevelKey(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            throw new IllegalArgumentException("dimension 不能为空");
        }

        String raw = element.getAsString().trim();
        if (!raw.contains(":")) {
            raw = "minecraft:" + raw;
        }

        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            throw new IllegalArgumentException("无效维度 ID: " + raw);
        }
        return ResourceKey.create(Registries.DIMENSION, id);
    }
}
