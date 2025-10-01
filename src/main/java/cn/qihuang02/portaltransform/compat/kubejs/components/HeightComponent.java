package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Height;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.ScriptableObject;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.Optional;

@HideFromJS
public class HeightComponent implements RecipeComponent<Height> {
    public static final HeightComponent HEIGHT = new HeightComponent();

    private HeightComponent() {
    }

    @Override
    public Codec<Height> codec() {
        return Height.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(Height.class);
    }

    @Override
    public String toString() {
        return "portaltransform:height";
    }

    @Override
    public Height wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from == null || from instanceof Undefined) {
            return null;
        }

        if (from instanceof Height height) {
            return height;
        }

        if (from instanceof Number number) {
            int value = (int) Math.floor(number.doubleValue());
            return new Height(Optional.of(value), Optional.empty());
        }

        if (from instanceof ScriptableObject object) {
            Optional<Integer> min = parseOptionalInt(cx, object, "min");
            Optional<Integer> max = parseOptionalInt(cx, object, "max");

            if (min.isEmpty() && max.isEmpty()) {
                throw ScriptRuntime.typeError(cx, "Height condition object must specify at least 'min' or 'max'.");
            }

            try {
                return new Height(min, max);
            } catch (IllegalArgumentException e) {
                throw ScriptRuntime.typeError(cx, "Invalid height condition: " + e.getMessage());
            }
        }

        throw ScriptRuntime.typeError(cx, "Invalid value for height condition. Expected number, object, or null but got " + from.getClass().getSimpleName());
    }

    private Optional<Integer> parseOptionalInt(Context cx, ScriptableObject object, String key) {
        Object value = ScriptableObject.getProperty(object, key, cx);
        if (value == null || value == ScriptableObject.NOT_FOUND || value instanceof Undefined) {
            return Optional.empty();
        }

        if (value instanceof Number number) {
            return Optional.of((int) Math.floor(number.doubleValue()));
        }

        throw ScriptRuntime.typeError(cx, "Expected number for height property '" + key + "' but got " + value.getClass().getSimpleName());
    }
}
