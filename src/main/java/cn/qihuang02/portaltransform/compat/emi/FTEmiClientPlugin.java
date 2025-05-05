package cn.qihuang02.portaltransform.compat.emi;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.Recipes;
import cn.qihuang02.portaltransform.recipe.custom.PortalTransformRecipe;
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
    public static final ResourceLocation CATEGORY_ID = PortalTransform.getRL("portal_transform");
    public static final EmiRecipeCategory PORTAL_TRANSFORM_CATEGORY = new EmiRecipeCategory(
            CATEGORY_ID,
            ICON
    );

    @Override
    public void register(@NotNull EmiRegistry registry) {
        registry.addCategory(PORTAL_TRANSFORM_CATEGORY);

        RecipeManager recipeManager = null;
        if (Minecraft.getInstance().level != null) {
            recipeManager = Minecraft.getInstance().level.getRecipeManager();
        }
        if (recipeManager != null) {
            for (RecipeHolder<PortalTransformRecipe> holder : recipeManager.getAllRecipesFor(Recipes.PORTAL_TRANSFORM_TYPE.get())) {
                registry.addRecipe(new EmiRecipe(holder));
            }
        }
    }
}
