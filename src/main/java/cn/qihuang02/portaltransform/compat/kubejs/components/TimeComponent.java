package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.TimeCondition;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeArray;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.ScriptableObject;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.Locale;

public class TimeComponent implements RecipeComponent<TimeCondition> {
    public static final TimeComponent TIME = new TimeComponent();

    private TimeComponent() {
    }

    @Override
    public Codec<TimeCondition> codec() {
        return TimeCondition.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(TimeCondition.class);
    }

    @Override
    public String toString() {
        return "portaltransform:time";
    }

    @Override
    public TimeCondition wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from == null || from instanceof Undefined) {
            return null;
        }

        if (from instanceof TimeCondition condition) {
            return condition;
        }

        if (from instanceof CharSequence sequence) {
            return parseKeywordString(cx, sequence.toString());
        }

        if (from instanceof NativeArray array) {
            return parseRangeArray(cx, array);
        }

        throw ScriptRuntime.typeError(cx, "Invalid value for time condition. Expected string keyword or [start, end] array (use .time(\"keyword\") or .time([start, end])). Got " + from.getClass().getSimpleName() + ".");
    }

    private TimeCondition parseKeywordString(Context cx, String raw) {
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return TimeCondition.Keyword.byName(normalized)
                .map(TimeCondition::keyword)
                .orElseThrow(() -> ScriptRuntime.typeError(cx, "Unknown time keyword '" + raw + "'. Use .time(\"day\"), .time(\"night\"), .time(\"noon\"), or .time(\"midnight\"), or define a custom range with .time([start, end])."));
    }

    private TimeCondition parseRangeArray(Context cx, NativeArray array) {
        long length = array.getLength();
        if (length != 2) {
            throw ScriptRuntime.typeError(cx, "Time range must be defined as an array with exactly two elements: .time([start, end]).");
        }

        int[] bounds = new int[2];
        for (int i = 0; i < 2; i++) {
            Object element = array.get(i);
            if (element == null || element == Undefined.INSTANCE || element == ScriptableObject.NOT_FOUND) {
                throw ScriptRuntime.typeError(cx, "Time range array cannot contain null or undefined values. Use .time([start, end]).");
            }

            if (!(element instanceof Number number)) {
                throw ScriptRuntime.typeError(cx, "Time range array must contain numbers. Use .time([start, end]).");
            }

            bounds[i] = (int) Math.floor(number.doubleValue());
        }

        try {
            return TimeCondition.range(bounds[0], bounds[1]);
        } catch (IllegalArgumentException e) {
            throw ScriptRuntime.typeError(cx, "Invalid time range: " + e.getMessage() + " (use .time([start, end])).");
        }
    }
}