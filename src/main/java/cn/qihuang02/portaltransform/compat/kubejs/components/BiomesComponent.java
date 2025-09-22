package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Biomes;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.NativeArray;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.ScriptableObject;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class BiomesComponent implements RecipeComponent<Biomes> {
    public static final BiomesComponent BIOMES = new BiomesComponent();

    private BiomesComponent() {
    }

    @Override
    public Codec<Biomes> codec() {
        return Biomes.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(Biomes.class);
    }

    @Override
    public String toString() {
        return "portaltransform:biomes";
    }

    @Override
    public Biomes wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from == null || from instanceof Undefined) {
            return null;
        }

        if (from instanceof Biomes biomes) {
            return biomes;
        }

        if (from instanceof NativeArray array) {
            List<ResourceKey<Biome>> biomeList = new ArrayList<>();
            for (Object element : array) {
                if (element == null || element == ScriptableObject.NOT_FOUND || element instanceof Undefined) {
                    throw ScriptRuntime.typeError(cx, "Biome entries cannot be null or undefined.");
                }
                biomeList.add(BiomeComponent.BIOME.wrap(cx, recipe, element));
            }

            if (biomeList.isEmpty()) {
                throw ScriptRuntime.typeError(cx, "Biome list must contain at least one biome identifier.");
            }

            try {
                return new Biomes(biomeList);
            } catch (IllegalArgumentException e) {
                throw ScriptRuntime.typeError(cx, "Failed to create Biomes condition: " + e.getMessage());
            }
        }

        ResourceKey<Biome> biomeKey = BiomeComponent.BIOME.wrap(cx, recipe, from);
        return new Biomes(List.of(biomeKey));
    }
}
