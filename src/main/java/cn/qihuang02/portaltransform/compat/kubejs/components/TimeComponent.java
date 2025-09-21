package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.TimeCondition;
import cn.qihuang02.portaltransform.recipe.ItemTransform.TimeCondition.Mode;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.ScriptableObject;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.Locale;
import java.util.Optional;

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
            return parseModeString(cx, sequence.toString());
        }

        if (from instanceof ScriptableObject object) {
            String modeRaw = extractModeString(cx, object);
            Mode mode = parseMode(modeRaw, cx);

            Optional<Integer> start = parseOptionalInt(cx, object, "start");
            Optional<Integer> end = parseOptionalInt(cx, object, "end");

            if (mode == Mode.RANGE) {
                if (start.isEmpty() || end.isEmpty()) {
                    throw ScriptRuntime.typeError(cx, "Time condition with mode 'range' requires both 'start' and 'end' ticks.");
                }

                try {
                    return new TimeCondition(mode, start, end);
                } catch (IllegalArgumentException e) {
                    throw ScriptRuntime.typeError(cx, "Invalid time range: " + e.getMessage());
                }
            }

            if (start.isPresent() || end.isPresent()) {
                throw ScriptRuntime.typeError(cx, "Only 'range' time conditions may define 'start' or 'end' values.");
            }

            return switch (mode) {
                case ANY -> TimeCondition.any();
                case DAY -> TimeCondition.day();
                case NIGHT -> TimeCondition.night();
                case RANGE -> throw new IllegalStateException("Range handled earlier");
            };
        }

        throw ScriptRuntime.typeError(cx, "Invalid value for time condition. Expected string, object, or null but got " + from.getClass().getSimpleName());
    }

    private TimeCondition parseModeString(Context cx, String raw) {
        Mode mode = parseMode(raw, cx);
        return switch (mode) {
            case ANY -> TimeCondition.any();
            case DAY -> TimeCondition.day();
            case NIGHT -> TimeCondition.night();
            case RANGE ->
                    throw ScriptRuntime.typeError(cx, "String 'range' requires an object with 'start' and 'end' values.");
        };
    }

    private String extractModeString(Context cx, ScriptableObject object) {
        Object rawMode = ScriptableObject.getProperty(object, "mode", cx);
        if (rawMode == null || rawMode == ScriptableObject.NOT_FOUND || rawMode instanceof Undefined) {
            return "any";
        }
        return rawMode.toString();
    }

    private Mode parseMode(String value, Context cx) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "any" -> Mode.ANY;
            case "day" -> Mode.DAY;
            case "night" -> Mode.NIGHT;
            case "range" -> Mode.RANGE;
            default ->
                    throw ScriptRuntime.typeError(cx, "Unknown time mode '" + value + "'. Expected any, day, night, or range.");
        };
    }

    private Optional<Integer> parseOptionalInt(Context cx, ScriptableObject object, String key) {
        Object value = ScriptableObject.getProperty(object, key, cx);
        if (value == null || value == ScriptableObject.NOT_FOUND || value instanceof Undefined) {
            return Optional.empty();
        }

        if (value instanceof Number number) {
            return Optional.of((int) Math.floor(number.doubleValue()));
        }

        throw ScriptRuntime.typeError(cx, "Expected number for time property '" + key + "' but got " + value.getClass().getSimpleName());
    }
}
