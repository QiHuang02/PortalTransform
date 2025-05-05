package cn.qihuang02.portaltransform.compat.emi;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.Recipes;
import cn.qihuang02.portaltransform.recipe.itemTransformation.ItemTransformationRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

@EmiEntrypoint
public class FTEmiClientPlugin implements EmiPlugin {
    public static final ResourceLocation TEXTURE = PortalTransform.getRL("textures/gui/emi/icon.png");
    public static final EmiTexture ICON = new EmiTexture(TEXTURE, 0, 0, 16, 16, 16, 16, 16, 16);
    public static final ResourceLocation CATEGORY_ID = PortalTransform.getRL("item_transformation");
    public static final EmiRecipeCategory ITEM_TRANSFORMATION_CATEGORY = new EmiRecipeCategory(
            CATEGORY_ID,
            ICON
    );

    @Override
    public void register(@NotNull EmiRegistry registry) {
        registry.addCategory(ITEM_TRANSFORMATION_CATEGORY);

        RecipeManager recipeManager = null;
        if (Minecraft.getInstance().level != null) {
            recipeManager = Minecraft.getInstance().level.getRecipeManager();
        }
        if (recipeManager != null) {
            for (RecipeHolder<ItemTransformationRecipe> holder : recipeManager.getAllRecipesFor(Recipes.ITEM_TRANSFORMATION_TYPE.get())) {
                registry.addRecipe(new EmiRecipe(holder));
            }
        }
    }
}
