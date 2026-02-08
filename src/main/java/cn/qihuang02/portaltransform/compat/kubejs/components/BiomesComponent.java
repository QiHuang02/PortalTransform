package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Biomes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.JsonIO;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

@HideFromJS
public class BiomesComponent implements RecipeComponent<Biomes> {
    public static final String TYPE_ID = "portaltransform:biomes";
    public static final BiomesComponent INSTANCE = new BiomesComponent();

    @Override
    public String componentType() {
        return TYPE_ID;
    }

    @Override
    public Class<?> componentClass() {
        return Biomes.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, Biomes value) {
        JsonArray array = new JsonArray();
        if (value == null) {
            return array;
        }

        for (ResourceKey<Biome> key : value.biomes()) {
            array.add(key.location().toString());
        }
        return array;
    }

    @Override
    public Biomes read(RecipeJS recipe, Object from) {
        if (from == null || from instanceof Undefined || from == Scriptable.NOT_FOUND) {
            return null;
        }

        if (from instanceof Biomes biomes) {
            return biomes;
        }

        JsonElement element = JsonIO.of(from);
        if (element == null || element.isJsonNull()) {
            return null;
        }

        if (element.isJsonArray()) {
            List<ResourceKey<Biome>> biomeList = new ArrayList<>();
            for (JsonElement entry : element.getAsJsonArray()) {
                biomeList.add(parseBiome(entry));
            }

            if (biomeList.isEmpty()) {
                throw new IllegalArgumentException("biomes 至少需要一个生物群系");
            }

            return new Biomes(biomeList);
        }

        return new Biomes(List.of(parseBiome(element)));
    }

    private static ResourceKey<Biome> parseBiome(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            throw new IllegalArgumentException("biome 不能为空");
        }

        String raw = element.getAsString().trim();
        if (!raw.contains(":")) {
            raw = "minecraft:" + raw;
        }

        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            throw new IllegalArgumentException("无效生物群系 ID: " + raw);
        }
        return ResourceKey.create(Registries.BIOME, id);
    }
}
