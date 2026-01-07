package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Biomes;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.*;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

@HideFromJS
public class BiomesComponent implements RecipeComponent<Biomes> {
    public static final RecipeComponentType<Biomes> BIOMES = RecipeComponentType.unit(PortalTransform.getRL("biomes"), BiomesComponent::new);
    private final RecipeComponentType<?> type;

    private BiomesComponent(RecipeComponentType<?> type) {
        this.type = type;
    }

    @Override
    public RecipeComponentType<?> type() {
        return type;
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
        return type.toString();
    }

    @Override
    public Biomes wrap(RecipeScriptContext cx, Object from) {
        var context = cx.cx();

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
                    throw ScriptRuntime.typeError(context, "Biome entries cannot be null or undefined.");
                }
                biomeList.add(BiomeComponent.BIOME.instance().wrap(cx, element));
            }

            if (biomeList.isEmpty()) {
                throw ScriptRuntime.typeError(context, "Biome list must contain at least one biome identifier.");
            }

            try {
                return new Biomes(biomeList);
            } catch (IllegalArgumentException e) {
                throw ScriptRuntime.typeError(context, "Failed to create Biomes condition: " + e.getMessage());
            }
        }

        ResourceKey<Biome> biomeKey = BiomeComponent.BIOME.instance().wrap(cx, from);
        return new Biomes(List.of(biomeKey));
    }
}
