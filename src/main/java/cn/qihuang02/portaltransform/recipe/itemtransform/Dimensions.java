package cn.qihuang02.portaltransform.recipe.itemtransform;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Holds optional current and target dimension restrictions for a recipe.
 * Both values must either be present or absent together.
 */
public class Dimensions {
    private final ResourceKey<Level> current;
    private final ResourceKey<Level> target;

    public Dimensions(@Nullable ResourceKey<Level> current, @Nullable ResourceKey<Level> target) {
        if ((current == null) != (target == null)) {
            throw new IllegalArgumentException("Current and target dimensions must both be specified or both be null");
        }
        this.current = current;
        this.target = target;
    }

    public Optional<ResourceKey<Level>> current() {
        return Optional.ofNullable(current);
    }

    public Optional<ResourceKey<Level>> target() {
        return Optional.ofNullable(target);
    }
}
