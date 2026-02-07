package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.ItemTransform.CountRange;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.JsonIO;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.item.ItemStack;

@HideFromJS
public class ByproductsComponent implements RecipeComponent<Byproducts> {
    public static final String TYPE_ID = "portaltransform:byproduct";
    public static final ByproductsComponent INSTANCE = new ByproductsComponent();
    public static final RecipeComponent<Byproducts[]> ARRAY = INSTANCE.asArray();

    @Override
    public String componentType() {
        return TYPE_ID;
    }

    @Override
    public Class<?> componentClass() {
        return Byproducts.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, Byproducts value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }

        JsonObject object = new JsonObject();
        object.add("byproduct", recipe.writeOutputItem(OutputItem.of(value.byproduct(), 1D)));
        object.addProperty("chance", value.chance());

        JsonObject counts = new JsonObject();
        counts.addProperty("min", value.counts().min());
        counts.addProperty("max", value.counts().max());
        object.add("counts", counts);
        return object;
    }

    @Override
    public Byproducts read(RecipeJS recipe, Object from) {
        if (from == null || from instanceof Undefined || from == Scriptable.NOT_FOUND) {
            return null;
        }

        if (from instanceof Byproducts byproducts) {
            return byproducts;
        }

        try {
            if (from instanceof ItemStack stack) {
                return new Byproducts(stack.copy(), 1F, new CountRange(1, stack.getCount()));
            }

            OutputItem outputDirect = OutputItem.of(from);
            if (outputDirect != null && !outputDirect.isEmpty()) {
                return new Byproducts(outputDirect.item.copy(), 1F, new CountRange(1, outputDirect.getCount()));
            }

            JsonElement element = JsonIO.of(from);
            if (element == null || element.isJsonNull()) {
                return null;
            }

            if (!element.isJsonObject()) {
                OutputItem output = OutputItem.of(JsonIO.toObject(element));
                if (output == null || output.isEmpty()) {
                    throw new IllegalArgumentException("无法解析副产物物品");
                }
                return new Byproducts(output.item.copy(), 1F, new CountRange(1, output.getCount()));
            }

            JsonObject object = element.getAsJsonObject();
            if (!object.has("byproduct")) {
                throw new IllegalArgumentException("byproducts 项缺少 byproduct 字段");
            }

            OutputItem output = OutputItem.of(JsonIO.toObject(object.get("byproduct")));
            if (output == null || output.isEmpty()) {
                throw new IllegalArgumentException("byproduct 不能为空");
            }

            float chance = object.has("chance") ? object.get("chance").getAsFloat() : 1F;
            CountRange counts = parseCounts(object.get("counts"), output.getCount());
            return new Byproducts(output.item.copy(), chance, counts);
        } catch (Exception e) {
            throw new IllegalArgumentException("解析 byproducts 失败: " + e.getMessage(), e);
        }
    }

    private static CountRange parseCounts(JsonElement element, int fallbackCount) {
        if (element == null || element.isJsonNull()) {
            int count = Math.max(1, fallbackCount);
            return new CountRange(count, count);
        }

        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            int value = element.getAsInt();
            return new CountRange(value, value);
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() != 2) {
                throw new IllegalArgumentException("counts 数组必须为 [min, max]");
            }
            return new CountRange(array.get(0).getAsInt(), array.get(1).getAsInt());
        }

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            int min = object.has("min") ? object.get("min").getAsInt() : 1;
            int max = object.has("max") ? object.get("max").getAsInt() : min;
            return new CountRange(min, max);
        }

        throw new IllegalArgumentException("counts 必须是数字、数组或对象");
    }
}
