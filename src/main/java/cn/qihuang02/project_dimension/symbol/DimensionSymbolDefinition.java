package cn.qihuang02.project_dimension.symbol;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record DimensionSymbolDefinition(ResourceKey<Level> dimension, DimensionSymbolVector symbols) {
    public static DimensionSymbolDefinition fromJson(JsonObject json) {
        if (!json.has("dimension") || !json.get("dimension").isJsonPrimitive()) {
            throw new JsonParseException("维度象征定义缺少 dimension 字段");
        }
        if (!json.has("symbols") || !json.get("symbols").isJsonObject()) {
            throw new JsonParseException("维度象征定义缺少 symbols 对象");
        }

        ResourceLocation dimensionId = ResourceLocation.parse(json.get("dimension").getAsString());
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
        return new DimensionSymbolDefinition(dimensionKey, DimensionSymbolVector.fromJson(json.getAsJsonObject("symbols")));
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("dimension", dimension.location().toString());
        root.add("symbols", symbols.toJson());
        return root;
    }
}
