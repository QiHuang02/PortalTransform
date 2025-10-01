package cn.qihuang02.portaltransform.compat.kubejs.recipe;

import cn.qihuang02.portaltransform.PortalTransform;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.rhino.util.HideFromJS;

@HideFromJS
public class ItemTransformKubeRecipe extends KubeRecipe {
    public static final KubeRecipeFactory FACTORY = new KubeRecipeFactory(
            PortalTransform.getRL("item_transform"),
            ItemTransformKubeRecipe.class,
            ItemTransformKubeRecipe::new
    );
}
