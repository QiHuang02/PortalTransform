package cn.qihuang02.portaltransform.compat.kubejs.binding;

import cn.qihuang02.portaltransform.recipe.custom.Byproducts;
import cn.qihuang02.portaltransform.recipe.custom.CountRange;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ByproductsBinding {
    private ByproductsBinding() {
    }

    @Info(params = {
            @Param(name = "ItemStack", value = "byproduct")
    })
    public static Byproducts of(ItemStack byproduct) {
        return create(byproduct, 1f, 1, 1);
    }

    @Info(params = {
            @Param(name = "ItemStack", value = "byproduct"),
            @Param(name = "chance", value = "chance")
    })
    public static Byproducts of(ItemStack byproduct, float chance) {
        return create(byproduct, chance, 1, 1);
    }

    @Info(params = {
            @Param(name = "ItemStack", value = "byproduct"),
            @Param(name = "chance", value = "chance"),
            @Param(name = "min", value = "min"),
            @Param(name = "max", value = "max")
    })
    public static Byproducts of(ItemStack byproduct, float chance, int min, int max) {
        return create(byproduct, chance, min, max);
    }

    private static Byproducts create(ItemStack item, float chance, int min, int max) {
        if (item == null || item.isEmpty()) {
            throw new IllegalArgumentException("Byproduct ItemStack cannot be null or empty.");
        }
        if (chance < 0.0f || chance > 1.0f) {
            throw new IllegalArgumentException("Byproduct chance must be between 0.0 and 1.0 (inclusive), got: " + chance);
        }
        if (min <= 0) {
            throw new IllegalArgumentException("Byproduct minimum count must be greater than 0, got: " + min);
        }
        if (max < min) {
            throw new IllegalArgumentException("Byproduct maximum count (" + max + ") cannot be less than minimum count (" + min + ")");
        }

        return new Byproducts(item, chance, new CountRange(min, max));
    }

    private static List<Byproducts> listOf(Byproducts... byproducts) {
        return List.of(byproducts);
    }
}
