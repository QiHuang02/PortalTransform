package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.ItemTransform.TimeCondition;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.NativeArray;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.ScriptableObject;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.Locale;

@HideFromJS
public class TimeComponent implements RecipeComponent<TimeCondition> {
    public static final RecipeComponentType<TimeCondition> TIME = RecipeComponentType.unit(PortalTransform.getRL("time"), TimeComponent::new);

    private final RecipeComponentType<?> type;

    private TimeComponent(RecipeComponentType<?> type) {
        this.type = type;
    }

    @Override
    public RecipeComponentType<?> type() {
        return type;
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
        return type.toString();
    }


    @Override
    public TimeCondition wrap(RecipeScriptContext cx, Object from) {
        var context = cx.cx();

        return switch (from) {
            case null -> null;
            case Undefined ignored -> null;
            case TimeCondition condition -> condition;
            case CharSequence sequence -> parseKeywordString(cx, sequence.toString());
            case NativeArray array -> parseRangeArray(cx, array);
            default ->
                    throw ScriptRuntime.typeError(context, "Invalid value for time condition. Expected string keyword or [start, end] array (use .time(\"keyword\") or .time([start, end])). Got " + from.getClass().getSimpleName() + ".");
        };
    }

    private TimeCondition parseKeywordString(RecipeScriptContext cx, String raw) {
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return TimeCondition.Keyword.byName(normalized)
                .map(TimeCondition::keyword)
                .orElseThrow(() -> ScriptRuntime.typeError(cx.cx(), "Unknown time keyword '" + raw + "'. Use .time(\"day\"), .time(\"night\"), .time(\"noon\"), or .time(\"midnight\"), or define a custom range with .time([start, end])."));
    }

    private TimeCondition parseRangeArray(RecipeScriptContext cx, NativeArray array) {
        var context = cx.cx();

        long length = array.getLength();
        if (length != 2) {
            throw ScriptRuntime.typeError(context, "Time range must be defined as an array with exactly two elements: .time([start, end]).");
        }

        int[] bounds = new int[2];
        for (int i = 0; i < 2; i++) {
            Object element = array.get(i);
            if (element == null || element == Undefined.INSTANCE || element == ScriptableObject.NOT_FOUND) {
                throw ScriptRuntime.typeError(context, "Time range array cannot contain null or undefined values. Use .time([start, end]).");
            }

            if (!(element instanceof Number number)) {
                throw ScriptRuntime.typeError(context, "Time range array must contain numbers. Use .time([start, end]).");
            }

            bounds[i] = (int) Math.floor(number.doubleValue());
        }

        try {
            return TimeCondition.range(bounds[0], bounds[1]);
        } catch (IllegalArgumentException e) {
            throw ScriptRuntime.typeError(context, "Invalid time range: " + e.getMessage() + " (use .time([start, end])).");
        }
    }
}
