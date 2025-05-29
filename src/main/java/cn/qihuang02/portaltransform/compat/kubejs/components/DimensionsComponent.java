package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Dimensions;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.ScriptableObject;
import dev.latvian.mods.rhino.Undefined;
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
            case Dimensions dimensions -> {
                return dimensions;
            }
            case ScriptableObject so -> {
                Optional<ResourceKey<Level>> currentDimKey = Optional.empty();
                Optional<ResourceKey<Level>> targetDimKey = Optional.empty();

                Object currentRaw = so.get(cx, "current", so);
                if (currentRaw != null && currentRaw != ScriptableObject.NOT_FOUND && !(currentRaw instanceof Undefined)) {
                    currentDimKey = Optional.ofNullable(LevelComponent.DIMENSION.wrap(cx, recipe, currentRaw));
                }

                Object targetRaw = so.get(cx, "target", so);
                if (targetRaw != null && targetRaw != ScriptableObject.NOT_FOUND && !(targetRaw instanceof Undefined)) {
                    targetDimKey = Optional.ofNullable(LevelComponent.DIMENSION.wrap(cx, recipe, targetRaw));
                }
                return new Dimensions(currentDimKey, targetDimKey);
            }
            case null -> {
                return Dimensions.empty();
            }
            case Undefined ignored -> {
                return Dimensions.empty();
            }
            default -> {
            }
        }

        throw ScriptRuntime.typeError(cx, "Expected a Dimensions object, got " + from.getClass().getSimpleName());
    }
}
