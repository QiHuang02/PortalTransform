package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.PortalTransform;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

@HideFromJS
public class BiomeComponent implements RecipeComponent<ResourceKey<Biome>> {
    public static final BiomeComponent BIOME = new BiomeComponent();
    private static final Codec<ResourceKey<Biome>> CODEC = ResourceKey.codec(Registries.BIOME);
    private static final String COMPONENT_NAME = PortalTransform.MODID + ":biome";

    private BiomeComponent() {
    }

    @Override
    public Codec<ResourceKey<Biome>> codec() {
        return CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ResourceKey.class).withParams(TypeInfo.of(Biome.class));
    }

    @Override
    public String toString() {
        return COMPONENT_NAME;
    }

    @Override
    public ResourceKey<Biome> wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from instanceof ResourceKey<?> key) {
            if (key.isFor(Registries.BIOME)) {
                @SuppressWarnings("unchecked") ResourceKey<Biome> biomeKey = (ResourceKey<Biome>) key;
                return biomeKey;
            }
            throw ScriptRuntime.typeError(cx, "Expected a Biome resource key, but got registry: " + key.registry());
        }

        ResourceLocation rl = from instanceof ResourceLocation loc ? loc :
                from instanceof String s ? ResourceLocation.tryParse(s) : null;

        if (rl != null) {
            return ResourceKey.create(Registries.BIOME, rl);
        }

        throw ScriptRuntime.typeError(cx, "Expected a biome ResourceKey or resource location string, but got " + from);
    }
}
