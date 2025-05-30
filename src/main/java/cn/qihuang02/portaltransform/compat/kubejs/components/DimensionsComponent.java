package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Dimensions;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.*;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class DimensionsComponent implements RecipeComponent<Dimensions> {
    public static final DimensionsComponent DIMENSIONS = new DimensionsComponent();

    private static final Codec<Dimensions> CODEC = Dimensions.CODEC;

    public DimensionsComponent() {
    }

    @Override
    public Codec<Dimensions> codec() {
        return CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(Dimensions.class);
    }

    @Override
    public String toString() {
        return "portaltransform:dimensions";
    }

    @Override
    public Dimensions wrap(Context cx, KubeRecipe recipe, Object from) {
        switch (from) {
            case null -> {
                return Dimensions.empty();
            }
            case Undefined ignored -> {
                return Dimensions.empty();
            }
            case Dimensions dimensions -> {
                return dimensions;
            }
            case NativeArray jsArray -> {
                long length = jsArray.getLength();
                Optional<ResourceKey<Level>> currentDimKey = Optional.empty();
                Optional<ResourceKey<Level>> targetDimKey = Optional.empty();

                if (length == 0) {
                    return Dimensions.empty();
                }

                if (length == 2) {
                    Object currentRaw = jsArray.getFirst();
                    if (currentRaw != null && currentRaw != ScriptableObject.NOT_FOUND && !(currentRaw instanceof Undefined)) {
                        try {
                            currentDimKey = Optional.ofNullable(LevelComponent.DIMENSION.wrap(cx, recipe, currentRaw));
                        } catch (Exception e) {
                            throw ScriptRuntime.typeError(cx, "Invalid 'current' dimension at index 0 in dimensions array: " + e.getMessage());
                        }
                    }

                    Object targetRaw = jsArray.get(1);
                    if (targetRaw != null && targetRaw != ScriptableObject.NOT_FOUND && !(targetRaw instanceof Undefined)) {
                        try {
                            targetDimKey = Optional.ofNullable(LevelComponent.DIMENSION.wrap(cx, recipe, targetRaw));
                        } catch (Exception e) {
                            throw ScriptRuntime.typeError(cx, "Invalid 'target' dimension at index 1 in dimensions array: " + e.getMessage());
                        }
                    }

                    return new Dimensions(currentDimKey, targetDimKey);
                } else {
                    throw ScriptRuntime.typeError(cx, "Expected a Dimensions array with 0 or 2 elements (e.g., ['minecraft:overworld', 'minecraft:the_nether'] or []), but got " + length + " elements.");
                }
            }
            default -> {
            }
        }

        throw ScriptRuntime.typeError(cx, "Expected a Dimensions array (e.g., ['minecraft:overworld', 'minecraft:the_nether'] or []) or null/undefined for 'dimensions' field, but got " + from.getClass().getSimpleName());
    }
}
