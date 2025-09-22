package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Catalyst;
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
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class CatalystComponent implements RecipeComponent<Catalyst> {
    public static final CatalystComponent CATALYST = new CatalystComponent();

    private CatalystComponent() {
    }

    @Override
    public Codec<Catalyst> codec() {
        return Catalyst.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(Catalyst.class);
    }

    @Override
    public String toString() {
        return "portaltransform:catalyst";
    }

    @Override
    public Catalyst wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from == null || from instanceof Undefined) {
            return null;
        }

        if (from instanceof Catalyst catalyst) {
            return catalyst;
        }

        if (from instanceof ScriptableObject object) {
            List<ResourceKey<Block>> blocks = parseBlocks(cx, recipe, findProperty(cx, object, "blocks", "catalysts"));
            if (blocks.isEmpty()) {
                Object single = findProperty(cx, object, "block", "catalyst");
                blocks = parseBlocks(cx, recipe, single);
            }

            if (blocks.isEmpty()) {
                throw ScriptRuntime.typeError(cx, "Catalyst configuration requires a 'blocks' array or 'block' entry.");
            }

            Object radiusValue = findProperty(cx, object, "radius", "range");
            Object horizontalValue = findProperty(cx, object, "horizontal_range", "horizontalRange", "horizontal");
            Object verticalValue = findProperty(cx, object, "vertical_range", "verticalRange", "vertical");

            int horizontal = parseRange(cx, !isMissing(horizontalValue) ? horizontalValue : radiusValue, Catalyst.DEFAULT_HORIZONTAL_RANGE);
            int vertical = parseRange(cx, !isMissing(verticalValue) ? verticalValue : radiusValue, Catalyst.DEFAULT_VERTICAL_RANGE);

            try {
                return new Catalyst(blocks, horizontal, vertical);
            } catch (IllegalArgumentException ex) {
                throw ScriptRuntime.typeError(cx, "Invalid catalyst definition: " + ex.getMessage());
            }
        }

        List<ResourceKey<Block>> singleBlock = parseBlocks(cx, recipe, from);
        if (!singleBlock.isEmpty()) {
            return new Catalyst(singleBlock, Catalyst.DEFAULT_HORIZONTAL_RANGE, Catalyst.DEFAULT_VERTICAL_RANGE);
        }

        throw ScriptRuntime.typeError(cx, "Unsupported catalyst specification: " + from);
    }

    private List<ResourceKey<Block>> parseBlocks(Context cx, KubeRecipe recipe, Object value) {
        if (isMissing(value)) {
            return List.of();
        }

        if (value instanceof Catalyst catalyst) {
            return catalyst.blocks();
        }

        if (value instanceof NativeArray array) {
            List<ResourceKey<Block>> blocks = new ArrayList<>();
            for (Object entry : array) {
                if (entry == null || entry instanceof Undefined || entry == ScriptableObject.NOT_FOUND) {
                    throw ScriptRuntime.typeError(cx, "Catalyst block entries cannot be null or undefined.");
                }
                blocks.add(BlockComponent.BLOCK.wrap(cx, recipe, entry));
            }
            return blocks;
        }

        ResourceKey<Block> blockKey = BlockComponent.BLOCK.wrap(cx, recipe, value);
        return List.of(blockKey);
    }

    private int parseRange(Context cx, Object value, int defaultValue) {
        if (isMissing(value)) {
            return defaultValue;
        }

        if (value instanceof Number number) {
            int range = number.intValue();
            if (range < 0) {
                throw ScriptRuntime.typeError(cx, "Catalyst range must be non-negative.");
            }
            return range;
        }

        throw ScriptRuntime.typeError(cx, "Expected a number for catalyst range but got " + value);
    }

    private Object findProperty(Context cx, ScriptableObject object, String... names) {
        for (String name : names) {
            Object value = ScriptableObject.getProperty(object, name, cx);
            if (!isMissing(value)) {
                return value;
            }
        }
        return ScriptableObject.NOT_FOUND;
    }

    private boolean isMissing(Object value) {
        return value == null || value instanceof Undefined || value == ScriptableObject.NOT_FOUND;
    }
}
