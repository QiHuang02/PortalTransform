package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.TimeCondition;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.JsonIO;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.Locale;

@HideFromJS
public class TimeComponent implements RecipeComponent<TimeCondition> {
    public static final String TYPE_ID = "portaltransform:time";
    public static final TimeComponent INSTANCE = new TimeComponent();

    @Override
    public String componentType() {
        return TYPE_ID;
    }

    @Override
    public Class<?> componentClass() {
        return TimeCondition.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, TimeCondition value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }

        if (value.keyword().isPresent()) {
            return new JsonPrimitive(value.keyword().get().getSerializedName());
        }

        JsonArray array = new JsonArray();
        array.add(value.startTick());
        array.add(value.endTick());
        return array;
    }

    @Override
    public TimeCondition read(RecipeJS recipe, Object from) {
        if (from == null || from instanceof Undefined || from == Scriptable.NOT_FOUND) {
            return null;
        }

        if (from instanceof TimeCondition condition) {
            return condition;
        }

        JsonElement element = JsonIO.of(from);
        if (element == null || element.isJsonNull()) {
            return null;
        }

        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return parseKeywordString(element.getAsString());
        }

        if (element.isJsonArray()) {
            return parseRangeArray(element.getAsJsonArray());
        }

        if (element.isJsonObject()) {
            return parseLegacyObject(element.getAsJsonObject());
        }

        throw new IllegalArgumentException("time 必须是关键字字符串、[start,end] 数组或 legacy 对象");
    }

    private TimeCondition parseKeywordString(String raw) {
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return TimeCondition.Keyword.byName(normalized)
                .map(TimeCondition::keyword)
                .orElseThrow(() -> new IllegalArgumentException("未知 time 关键字: " + raw));
    }

    private TimeCondition parseRangeArray(JsonArray array) {
        if (array.size() != 2) {
            throw new IllegalArgumentException("time 范围必须是 [start, end]");
        }

        int start = array.get(0).getAsInt();
        int end = array.get(1).getAsInt();
        try {
            return TimeCondition.range(start, end);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效 time 范围: " + e.getMessage(), e);
        }
    }

    private TimeCondition parseLegacyObject(JsonObject object) {
        if (!object.has("mode") && object.has("start") && object.has("end")) {
            return TimeCondition.range(object.get("start").getAsInt(), object.get("end").getAsInt());
        }

        String mode = object.has("mode") ? object.get("mode").getAsString().toLowerCase(Locale.ROOT) : "any";
        return switch (mode) {
            case "any" -> TimeCondition.any();
            case "day" -> TimeCondition.day();
            case "night" -> TimeCondition.night();
            case "noon" -> TimeCondition.noon();
            case "midnight" -> TimeCondition.midnight();
            case "range" -> {
                if (!object.has("start") || !object.has("end")) {
                    throw new IllegalArgumentException("time.mode=range 时必须提供 start 与 end");
                }
                yield TimeCondition.range(object.get("start").getAsInt(), object.get("end").getAsInt());
            }
            default -> throw new IllegalArgumentException("未知 time.mode: " + mode);
        };
    }
}
