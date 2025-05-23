package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.PortalTransform;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class LevelComponent implements RecipeComponent<ResourceKey<Level>> {
    public static final LevelComponent DIMENSION = new LevelComponent();

    private static final Codec<ResourceKey<Level>> CODEC = ResourceKey.codec(Registries.DIMENSION);
    private static final String COMPONENT_NAME = PortalTransform.MODID + ":dimension";

    public LevelComponent() {
    }

    @Override
    public Codec<ResourceKey<Level>> codec() {
        return CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ResourceKey.class).withParams(TypeInfo.of(Level.class));
    }

    @Override
    public String toString() {
        return COMPONENT_NAME;
    }

    @Override
    public ResourceKey<Level> wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from instanceof ResourceKey<?> key) {
            if (key.isFor(Registries.DIMENSION)) {
                return (ResourceKey<Level>) key;
            } else {
                throw ScriptRuntime.typeError(cx, "Expected a ResourceKey for Dimension/Level, but got one for registry: " + key.registry());
            }
        }

        try {
            ResourceLocation rl = from instanceof ResourceLocation loc ? loc :
                    from instanceof String s ? ResourceLocation.tryParse(s) : null;

            if (rl != null) {
                return ResourceKey.create(Registries.DIMENSION, rl);
            }
        } catch (Exception ignored) {

        }
        throw ScriptRuntime.typeError(cx, "Expected a ResourceKey, but got " + from);
    }
}
