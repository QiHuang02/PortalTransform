package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Dimensions;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.*;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

@HideFromJS
public class DimensionsComponent implements RecipeComponent<Dimensions> {
    public static final RecipeComponentType<Dimensions> DIMENSIONS = RecipeComponentType.unit(PortalTransform.getRL("dimensions"), DimensionsComponent::new);

    private static final Codec<Dimensions> CODEC = Dimensions.CODEC;
    private final RecipeComponentType<?> type;

    private DimensionsComponent(RecipeComponentType<?> type) {
        this.type = type;
    }

    @Override
    public RecipeComponentType<?> type() {
        return type;
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
        return type.toString();
    }

    @Override
    public Dimensions wrap(RecipeScriptContext cx, Object from) {
        var context = cx.cx();

        if (from == null || from instanceof Undefined) {
            return Dimensions.empty();
        }

        if (from instanceof Dimensions dimensionsInstance) {
            return dimensionsInstance;
        }

        if (from instanceof NativeArray jsArray) {
            long length = jsArray.getLength();
            List<ResourceKey<Level>> dimsList = new ArrayList<>();

            if (length == 0) {
                return Dimensions.empty();
            }

            if (length == 2) {
                Object currentRaw = jsArray.getFirst();
                if (currentRaw == null || currentRaw == ScriptableObject.NOT_FOUND || currentRaw instanceof Undefined) {
                    throw ScriptRuntime.typeError(context, "Dimensions array element at index 0 (current dimension) cannot be null or undefined.");
                }
                try {
                    ResourceKey<Level> currentDimKey = LevelComponent.DIMENSION.instance().wrap(cx, currentRaw);
                    dimsList.add(currentDimKey);
                } catch (RhinoException e) {
                    throw ScriptRuntime.typeError(context, "Invalid 'current' dimension at index 0 in dimensions array: " + e.getMessage());
                } catch (Exception e) {
                    throw ScriptRuntime.typeError(context, "Error parsing 'current' dimension at index 0: " + e.getMessage());
                }

                Object targetRaw = jsArray.get(1);
                if (targetRaw == null || targetRaw == ScriptableObject.NOT_FOUND || targetRaw instanceof Undefined) {
                    throw ScriptRuntime.typeError(context, "Dimensions array element at index 1 (target dimension) cannot be null or undefined.");
                }
                try {
                    ResourceKey<Level> targetDimKey = LevelComponent.DIMENSION.instance().wrap(cx, targetRaw);
                    dimsList.add(targetDimKey);
                } catch (RhinoException e) {
                    throw ScriptRuntime.typeError(context, "Invalid 'target' dimension at index 1 in dimensions array: " + e.getMessage());
                } catch (Exception e) {
                    throw ScriptRuntime.typeError(context, "Error parsing 'target' dimension at index 1: " + e.getMessage());
                }
                try {
                    return new Dimensions(dimsList);
                } catch (IllegalArgumentException e) {
                    throw ScriptRuntime.typeError(context, "Failed to create Dimensions object: " + e.getMessage());
                }

            } else {
                throw ScriptRuntime.typeError(context, "Dimensions array must contain exactly 0 or 2 elements (e.g., ['minecraft:overworld', 'minecraft:the_nether'] or []), but found " + length + " elements.");
            }
        }

        throw ScriptRuntime.typeError(context, "Invalid type for 'dimensions' field. Expected an array of 0 or 2 dimension IDs (e.g., ['minecraft:overworld', 'minecraft:the_nether']), null, or undefined, but got " + from.getClass().getSimpleName() + ".");
    }
}
