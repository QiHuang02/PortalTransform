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
import net.minecraft.world.level.block.Block;

@HideFromJS
public class BlockComponent implements RecipeComponent<ResourceKey<Block>> {
    public static final BlockComponent BLOCK = new BlockComponent();

    private static final Codec<ResourceKey<Block>> CODEC = ResourceKey.codec(Registries.BLOCK);
    private static final String COMPONENT_NAME = PortalTransform.MODID + ":block";

    private BlockComponent() {
    }

    @Override
    public Codec<ResourceKey<Block>> codec() {
        return CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ResourceKey.class).withParams(TypeInfo.of(Block.class));
    }

    @Override
    public String toString() {
        return COMPONENT_NAME;
    }

    @Override
    public ResourceKey<Block> wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from instanceof ResourceKey<?> key) {
            if (key.isFor(Registries.BLOCK)) {
                return (ResourceKey<Block>) key;
            }
            throw ScriptRuntime.typeError(cx, "Expected a block resource key but got registry: " + key.registry());
        }

        ResourceLocation rl = from instanceof ResourceLocation loc ? loc :
                from instanceof String s ? ResourceLocation.tryParse(s) : null;

        if (rl != null) {
            return ResourceKey.create(Registries.BLOCK, rl);
        }

        throw ScriptRuntime.typeError(cx, "Expected a block identifier, but got " + from);
    }
}
