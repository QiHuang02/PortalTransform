package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.EnergyRequirement;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.ScriptableObject;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.type.TypeInfo;

public class EnergyComponent implements RecipeComponent<EnergyRequirement> {
    public static final EnergyComponent ENERGY = new EnergyComponent();

    private EnergyComponent() {
    }

    @Override
    public Codec<EnergyRequirement> codec() {
        return EnergyRequirement.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(EnergyRequirement.class);
    }

    @Override
    public String toString() {
        return "portaltransform:energy";
    }

    @Override
    public EnergyRequirement wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from == null || from instanceof Undefined) {
            return null;
        }

        if (from instanceof EnergyRequirement requirement) {
            return requirement;
        }

        if (from instanceof Number number) {
            int amount = number.intValue();
            if (amount <= 0) {
                throw ScriptRuntime.typeError(cx, "Energy amount must be greater than zero.");
            }
            return new EnergyRequirement(amount, EnergyRequirement.DEFAULT_HORIZONTAL_RANGE, EnergyRequirement.DEFAULT_VERTICAL_RANGE);
        }

        if (from instanceof ScriptableObject object) {
            Object amountValue = findProperty(cx, object, "amount", "energy", "cost");
            if (isMissing(amountValue)) {
                throw ScriptRuntime.typeError(cx, "Energy requirement must include an 'amount'.");
            }

            int amount = parsePositiveInt(cx, amountValue, "amount");
            Object horizontalValue = findProperty(cx, object, "horizontal_range", "horizontalRange", "horizontal", "radius", "range");
            Object verticalValue = findProperty(cx, object, "vertical_range", "verticalRange", "vertical", "radius", "range");

            int horizontal = parseRange(cx, horizontalValue, EnergyRequirement.DEFAULT_HORIZONTAL_RANGE);
            int vertical = parseRange(cx, verticalValue, EnergyRequirement.DEFAULT_VERTICAL_RANGE);

            try {
                return new EnergyRequirement(amount, horizontal, vertical);
            } catch (IllegalArgumentException ex) {
                throw ScriptRuntime.typeError(cx, "Invalid energy requirement: " + ex.getMessage());
            }
        }

        throw ScriptRuntime.typeError(cx, "Unsupported energy requirement specification: " + from);
    }

    private int parsePositiveInt(Context cx, Object value, String name) {
        if (!(value instanceof Number number)) {
            throw ScriptRuntime.typeError(cx, "Expected a number for energy " + name + " but got " + value);
        }
        int parsed = number.intValue();
        if (parsed <= 0) {
            throw ScriptRuntime.typeError(cx, "Energy " + name + " must be greater than zero.");
        }
        return parsed;
    }

    private int parseRange(Context cx, Object value, int defaultValue) {
        if (isMissing(value)) {
            return defaultValue;
        }

        int parsed = parseNonNegativeInt(cx, value, "range");
        return parsed;
    }

    private Object findProperty(Context cx, ScriptableObject object, String... names) {
        for (String name : names) {
            Object value = ScriptableObject.getProperty(object, name, cx);
            if (!isMissing(value)) {
                return value;
            }
        }
        return ScriptableObject.NOT_FOUND;
    }

    private boolean isMissing(Object value) {
        return value == null || value instanceof Undefined || value == ScriptableObject.NOT_FOUND;
    }

    private int parseNonNegativeInt(Context cx, Object value, String name) {
        if (!(value instanceof Number number)) {
            throw ScriptRuntime.typeError(cx, "Expected a number for energy " + name + " but got " + value);
        }
        int parsed = number.intValue();
        if (parsed < 0) {
            throw ScriptRuntime.typeError(cx, "Energy " + name + " must be non-negative.");
        }
        return parsed;
    }
}
