package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.List;

@HideFromJS
public class ByproductsComponent implements RecipeComponent<Byproducts> {
    public static final RecipeComponentType<Byproducts> BYPRODUCT = RecipeComponentType.unit(PortalTransform.getRL("byproduct"), ByproductsComponent::new);
    public static final RecipeComponent<List<Byproducts>> LIST = BYPRODUCT.instance().asList();

    private final RecipeComponentType<?> type;

    private ByproductsComponent(RecipeComponentType<?> type) {
        this.type = type;
    }

    @Override
    public RecipeComponentType<?> type() {
        return type;
    }

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
        return type.toString();
    }
}
