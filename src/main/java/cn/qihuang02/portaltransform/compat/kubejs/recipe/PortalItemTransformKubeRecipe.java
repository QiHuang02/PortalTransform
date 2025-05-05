package cn.qihuang02.portaltransform.compat.kubejs.recipe;

import cn.qihuang02.portaltransform.PortalTransform;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;

public class PortalItemTransformKubeRecipe extends KubeRecipe {
    public static final KubeRecipeFactory FACTORY = new KubeRecipeFactory(
            PortalTransform.getRL("item_transformation"),
            PortalItemTransformKubeRecipe.class,
            PortalItemTransformKubeRecipe::new
    );
}
