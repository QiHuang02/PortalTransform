package cn.qihuang02.portaltransform.compat.kubejs.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeArray;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.ScriptableObject;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.advancements.critereon.ItemPredicate;

import java.util.Map;
import java.util.Optional;

public class ItemPredicateComponent implements RecipeComponent<ItemPredicate> {
    public static final ItemPredicateComponent ITEM_PREDICATE = new ItemPredicateComponent();

    private ItemPredicateComponent() {
    }

    @Override
    public Codec<ItemPredicate> codec() {
        return ItemPredicate.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ItemPredicate.class);
    }

    @Override
    public String toString() {
        return "portaltransform:item_predicate";
    }

    @Override
    public ItemPredicate wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from == null || from instanceof Undefined) {
            return null;
        }

        if (from instanceof ItemPredicate predicate) {
            return predicate;
        }

        if (from instanceof CharSequence sequence) {
            JsonElement element = JsonParser.parseString(sequence.toString());
            return parsePredicate(cx, element);
        }

        if (from instanceof Scriptable scriptable) {
            JsonElement element = toJsonElement(cx, scriptable);
            return parsePredicate(cx, element);
        }

        throw ScriptRuntime.typeError(cx, "Invalid value for item predicate condition. Expected string, object, or null but got " + from.getClass().getSimpleName());
    }

    private ItemPredicate parsePredicate(Context cx, JsonElement element) {
        DataResult<ItemPredicate> result = ItemPredicate.CODEC.parse(JsonOps.INSTANCE, element);
        Optional<ItemPredicate> parsed = result.result();
        if (parsed.isPresent()) {
            return parsed.get();
        }

        String error = result.error().map(DataResult.Error::message).orElse("unknown error");
        throw ScriptRuntime.typeError(cx, "Failed to parse item predicate: " + error);
    }

    private JsonElement toJsonElement(Context cx, Object value) {
        if (value == null || value == ScriptableObject.NOT_FOUND || value instanceof Undefined) {
            return JsonNull.INSTANCE;
        }

        if (value instanceof ItemPredicate predicate) {
            DataResult<JsonElement> encoded = ItemPredicate.CODEC.encodeStart(JsonOps.INSTANCE, predicate);
            return encoded.result().orElseThrow(() -> ScriptRuntime.typeError(cx, "Failed to serialize nested item predicate: " + encoded.error().map(DataResult.Error::message).orElse("unknown error")));
        }

        if (value instanceof CharSequence sequence) {
            return JsonParser.parseString(sequence.toString());
        }

        if (value instanceof Number number) {
            return new JsonPrimitive(number);
        }

        if (value instanceof Boolean bool) {
            return new JsonPrimitive(bool);
        }

        if (value instanceof NativeArray array) {
            JsonArray jsonArray = new JsonArray();
            for (Object element : array) {
                jsonArray.add(toJsonElement(cx, element));
            }
            return jsonArray;
        }

        if (value instanceof ScriptableObject object) {
            JsonObject jsonObject = new JsonObject();
            for (Object id : object.getIds(cx)) {
                String key = id.toString();
                Object property = ScriptableObject.getProperty(object, key, cx);
                jsonObject.add(key, toJsonElement(cx, property));
            }
            return jsonObject;
        }

        if (value instanceof Map<?, ?> map) {
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                if (key == null) {
                    continue;
                }
                jsonObject.add(key.toString(), toJsonElement(cx, entry.getValue()));
            }
            return jsonObject;
        }

        throw ScriptRuntime.typeError(cx, "Unsupported value type in item predicate JSON conversion: " + value.getClass().getSimpleName());
    }
}
