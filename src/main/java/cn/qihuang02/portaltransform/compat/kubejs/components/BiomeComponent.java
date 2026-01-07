package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.PortalTransform;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

@HideFromJS
public class BiomeComponent implements RecipeComponent<ResourceKey<Biome>> {
    public static final RecipeComponentType<ResourceKey<Biome>> BIOME = RecipeComponentType.unit(PortalTransform.getRL("biome"), BiomeComponent::new);
    private static final Codec<ResourceKey<Biome>> CODEC = ResourceKey.codec(Registries.BIOME);
    private final RecipeComponentType<?> type;

    private BiomeComponent(RecipeComponentType<?> type) {
        this.type = type;
    }

    @Override
    public RecipeComponentType<?> type() {
        return type;
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
        return type.toString();
    }

    @Override
    public ResourceKey<Biome> wrap(RecipeScriptContext cx, Object from) {
        var context = cx.cx();

        if (from instanceof ResourceKey<?> key) {
            if (key.isFor(Registries.BIOME)) {
                @SuppressWarnings("unchecked")
                ResourceKey<Biome> biomeKey = (ResourceKey<Biome>) key;
                return biomeKey;
            }
            throw ScriptRuntime.typeError(context, "Expected a Biome resource key, but got registry: " + key.registry());
        }

        ResourceLocation rl = from instanceof ResourceLocation loc ? loc :
                from instanceof String s ? ResourceLocation.tryParse(s) : null;

        if (rl != null) {
            return ResourceKey.create(Registries.BIOME, rl);
        }

        throw ScriptRuntime.typeError(context, "Expected a biome ResourceKey or resource location string, but got " + from);
    }
}
