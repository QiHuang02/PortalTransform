package cn.qihuang02.portaltransform.compat.kubejs.recipe;

import dev.latvian.mods.rhino.util.HideFromJS;

/**
 * 2001 版本使用 RecipeSchema 注册，不再需要独立的 KubeRecipe 工厂类。
 * 保留该类型仅用于兼容历史引用，避免外部反射/类加载报错。
 */
@HideFromJS
public final class ItemTransformKubeRecipe {
    private ItemTransformKubeRecipe() {
    }
}
