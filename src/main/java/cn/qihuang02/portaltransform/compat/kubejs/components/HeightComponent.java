package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Height;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.*;
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

        if (from instanceof NativeArray array) {
            return parseNativeArray(cx, array);
        }

        if (from instanceof ScriptableObject) {
            throw ScriptRuntime.typeError(cx, "Height condition must be provided as [min, max] when using KubeJS. Use .height([min, max]).");
        }

        throw ScriptRuntime.typeError(cx, "Invalid value for height condition. Expected array or null but got " + from.getClass().getSimpleName());
    }

    private Height parseNativeArray(Context cx, NativeArray array) {
        long length = array.getLength();
        if (length != 2) {
            throw ScriptRuntime.typeError(cx, "Height range array must contain exactly two elements [min, max].");
        }

        Optional<Integer> min = Optional.empty();
        Optional<Integer> max = Optional.empty();

        Object first = array.get(0);
        min = parseArrayElement(cx, first, "min");

        Object second = array.get(1);
        max = parseArrayElement(cx, second, "max");

        if (min.isEmpty() && max.isEmpty()) {
            throw ScriptRuntime.typeError(cx, "Height range array must specify at least one bound. Use null for the unbounded side, e.g. .height([null, max]).");
        }

        try {
            return new Height(min, max);
        } catch (IllegalArgumentException e) {
            throw ScriptRuntime.typeError(cx, "Invalid height range: " + e.getMessage());
        }
    }

    private Optional<Integer> parseArrayElement(Context cx, Object element, String label) {
        if (element == null || element == ScriptableObject.NOT_FOUND || element instanceof Undefined) {
            return Optional.empty();
        }

        if (element instanceof Number number) {
            return Optional.of((int) Math.floor(number.doubleValue()));
        }

        throw ScriptRuntime.typeError(cx, "Height " + label + " must be a number or null, but got " + element.getClass().getSimpleName() + ".");
    }
}
