package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.PortalTransform;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.NativeArray;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.ScriptableObject;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.advancements.critereon.ItemPredicate;

import java.util.Map;
import java.util.Optional;

@HideFromJS
public class ItemPredicateComponent implements RecipeComponent<ItemPredicate> {
    public static final RecipeComponentType<ItemPredicate> ITEM_PREDICATE = RecipeComponentType.unit(PortalTransform.getRL("item_predicate"), ItemPredicateComponent::new);

    private final RecipeComponentType<?> type;

    private ItemPredicateComponent(RecipeComponentType<?> type) {
        this.type = type;
    }

    @Override
    public RecipeComponentType<?> type() {
        return type;
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
        return type.toString();
    }

    @Override
    public ItemPredicate wrap(RecipeScriptContext cx, Object from) {
        var context = cx.cx();

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

        throw ScriptRuntime.typeError(context, "Invalid value for item predicate condition. Expected string, object, or null but got " + from.getClass().getSimpleName());
    }

    private ItemPredicate parsePredicate(RecipeScriptContext cx, JsonElement element) {
        DataResult<ItemPredicate> result = ItemPredicate.CODEC.parse(JsonOps.INSTANCE, element);
        Optional<ItemPredicate> parsed = result.result();
        if (parsed.isPresent()) {
            return parsed.get();
        }

        String error = result.error().map(DataResult.Error::message).orElse("unknown error");
        throw ScriptRuntime.typeError(cx.cx(), "Failed to parse item predicate: " + error);
    }

    private JsonElement toJsonElement(RecipeScriptContext cx, Object value) {
        if (value == null || value == ScriptableObject.NOT_FOUND || value instanceof Undefined) {
            return JsonNull.INSTANCE;
        }

        if (value instanceof ItemPredicate predicate) {
            DataResult<JsonElement> encoded = ItemPredicate.CODEC.encodeStart(JsonOps.INSTANCE, predicate);
            return encoded.result().orElseThrow(() -> ScriptRuntime.typeError(cx.cx(), "Failed to serialize nested item predicate: " + encoded.error().map(DataResult.Error::message).orElse("unknown error")));
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
            for (Object id : object.getIds(cx.cx())) {
                String key = id.toString();
                Object property = ScriptableObject.getProperty(object, key, cx.cx());
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

        throw ScriptRuntime.typeError(cx.cx(), "Unsupported value type in item predicate JSON conversion: " + value.getClass().getSimpleName());
    }
}
