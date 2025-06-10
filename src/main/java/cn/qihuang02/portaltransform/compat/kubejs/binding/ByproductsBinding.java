package cn.qihuang02.portaltransform.compat.kubejs.binding;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.ItemTransform.CountRange;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public final class ByproductsBinding {
    private ByproductsBinding() {
    }

    @Contract("null -> fail")
    @Info(params = {
            @Param(name = "ItemStack", value = "byproduct")
    })
    public static @NotNull Byproducts of(ItemStack byproduct) {
        return create(byproduct, 1f, 1, 1);
    }

    @Contract("null, _ -> fail")
    @Info(params = {
            @Param(name = "ItemStack", value = "byproduct"),
            @Param(name = "chance", value = "chance")
    })
    public static @NotNull Byproducts of(ItemStack byproduct, float chance) {
        return create(byproduct, chance, 1, 1);
    }

    @Contract("null, _, _, _ -> fail")
    @Info(params = {
            @Param(name = "ItemStack", value = "byproduct"),
            @Param(name = "chance", value = "chance"),
            @Param(name = "min", value = "min"),
            @Param(name = "max", value = "max")
    })
    public static @NotNull Byproducts of(ItemStack byproduct, float chance, int min, int max) {
        return create(byproduct, chance, min, max);
    }

    @Contract("null, _, _, _ -> fail")
    private static @NotNull Byproducts create(ItemStack item, float chance, int min, int max) {
        return new Byproducts(item, chance, new CountRange(min, max));
    }

    @Contract(pure = true)
    private static @NotNull @Unmodifiable List<Byproducts> listOf(Byproducts... byproducts) {
        return List.of(byproducts);
    }
}
