package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Height;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.JsonIO;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.Optional;

@HideFromJS
public class HeightComponent implements RecipeComponent<Height> {
    public static final String TYPE_ID = "portaltransform:height";
    public static final HeightComponent INSTANCE = new HeightComponent();

    private HeightComponent() {
    }

    @Override
    public String componentType() {
        return TYPE_ID;
    }

    @Override
    public Class<?> componentClass() {
        return Height.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, Height value) {
        JsonObject object = new JsonObject();
        if (value == null) {
            return object;
        }

        value.minY().ifPresent(min -> object.addProperty("min", min));
        value.maxY().ifPresent(max -> object.addProperty("max", max));
        return object;
    }

    @Override
    public Height read(RecipeJS recipe, Object from) {
        if (from == null || from instanceof Undefined || from == Scriptable.NOT_FOUND) {
            return null;
        }

        if (from instanceof Height height) {
            return height;
        }

        JsonElement element = JsonIO.of(from);
        if (element == null || element.isJsonNull()) {
            return null;
        }

        if (element.isJsonArray()) {
            return parseFromArray(element.getAsJsonArray());
        }
        if (element.isJsonObject()) {
            return parseFromObject(element.getAsJsonObject());
        }

        throw new IllegalArgumentException("height 必须是 [min, max] 数组或 {min,max} 对象");
    }

    private static Height parseFromArray(JsonArray array) {
        if (array.size() != 2) {
            throw new IllegalArgumentException("height 数组必须为 [min, max]");
        }

        Optional<Integer> min = parseBound(array.get(0));
        Optional<Integer> max = parseBound(array.get(1));
        return new Height(min, max);
    }

    private static Height parseFromObject(JsonObject object) {
        Optional<Integer> min = object.has("min") ? parseBound(object.get("min")) : Optional.empty();
        Optional<Integer> max = object.has("max") ? parseBound(object.get("max")) : Optional.empty();
        return new Height(min, max);
    }

    private static Optional<Integer> parseBound(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }

        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("height 的边界值必须是数字或 null");
        }

        return Optional.of(element.getAsInt());
    }
}
