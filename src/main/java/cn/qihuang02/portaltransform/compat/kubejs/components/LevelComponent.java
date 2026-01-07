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
import net.minecraft.world.level.Level;

@HideFromJS
public class LevelComponent implements RecipeComponent<ResourceKey<Level>> {
    public static final RecipeComponentType<ResourceKey<Level>> DIMENSION = RecipeComponentType.unit(PortalTransform.getRL("dimension"), LevelComponent::new);

    private static final Codec<ResourceKey<Level>> CODEC = ResourceKey.codec(Registries.DIMENSION);
    private final RecipeComponentType<?> type;

    private LevelComponent(RecipeComponentType<?> type) {
        this.type = type;
    }

    @Override
    public RecipeComponentType<?> type() {
        return type;
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
        return type.toString();
    }

    @Override
    public ResourceKey<Level> wrap(RecipeScriptContext cx, Object from) {
        var context = cx.cx();

        if (from instanceof ResourceKey<?> key) {
            if (key.isFor(Registries.DIMENSION)) {
                return (ResourceKey<Level>) key;
            } else {
                throw ScriptRuntime.typeError(context, "Expected a ResourceKey for Dimension/Level, but got one for registry: " + key.registry());
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
        throw ScriptRuntime.typeError(context, "Expected a ResourceKey, but got " + from);
    }
}
