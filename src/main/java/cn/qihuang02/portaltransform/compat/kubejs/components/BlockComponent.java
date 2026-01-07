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
import net.minecraft.world.level.block.Block;

@HideFromJS
public class BlockComponent implements RecipeComponent<ResourceKey<Block>> {
    public static final RecipeComponentType<ResourceKey<Block>> BLOCK = RecipeComponentType.unit(PortalTransform.getRL("block"), BlockComponent::new);

    private static final Codec<ResourceKey<Block>> CODEC = ResourceKey.codec(Registries.BLOCK);
    private final RecipeComponentType<?> type;

    private BlockComponent(RecipeComponentType<?> type) {
        this.type = type;
    }

    @Override
    public RecipeComponentType<?> type() {
        return type;
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
        return type.toString();
    }

    @Override
    public ResourceKey<Block> wrap(RecipeScriptContext cx, Object from) {
        var context = cx.cx();

        if (from instanceof ResourceKey<?> key) {
            if (key.isFor(Registries.BLOCK)) {
                return (ResourceKey<Block>) key;
            }
            throw ScriptRuntime.typeError(context, "Expected a block resource key but got registry: " + key.registry());
        }

        ResourceLocation rl = from instanceof ResourceLocation loc ? loc :
                from instanceof String s ? ResourceLocation.tryParse(s) : null;

        if (rl != null) {
            return ResourceKey.create(Registries.BLOCK, rl);
        }

        throw ScriptRuntime.typeError(context, "Expected a block identifier, but got " + from);
    }
}
