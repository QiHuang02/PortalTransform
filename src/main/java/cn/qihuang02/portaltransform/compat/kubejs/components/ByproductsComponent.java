package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.List;

public class ByproductsComponent implements RecipeComponent<Byproducts> {
    public static final ByproductsComponent BYPRODUCT = new ByproductsComponent();
    public static final RecipeComponent<List<Byproducts>> LIST = BYPRODUCT.asList();

    @Override
    public Codec<Byproducts> codec() {
        return Byproducts.CODEC.codec();
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(Byproducts.class);
    }

    @Override
    public String toString() {
        return "byproduct";
    }
}