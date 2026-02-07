package cn.qihuang02.portaltransform.compat.jei;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.ItemTransformRecipe;
import cn.qihuang02.portaltransform.recipe.Recipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class PortalTransformJeiPlugin implements IModPlugin {
    public static final RecipeType<ItemTransformRecipe> ITEM_TRANSFORM_RECIPE_TYPE =
            RecipeType.create(PortalTransform.MODID, "item_transform", ItemTransformRecipe.class);

    private static final ResourceLocation PLUGIN_UID = PortalTransform.getRL("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ItemTransformRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = resolveRecipeManager();
        if (recipeManager == null) {
            PortalTransform.LOGGER.warn("JEI 注册 PortalTransform 配方时未找到可用 RecipeManager，跳过本次注册。");
            return;
        }

        List<ItemTransformRecipe> recipes = recipeManager.getAllRecipesFor(Recipes.PORTAL_ITEM_TRANSFORM_TYPE.get());
        registration.addRecipes(ITEM_TRANSFORM_RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Items.OBSIDIAN), ITEM_TRANSFORM_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.FLINT_AND_STEEL), ITEM_TRANSFORM_RECIPE_TYPE);
    }

    private static RecipeManager resolveRecipeManager() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            return minecraft.level.getRecipeManager();
        }
        if (minecraft.getConnection() != null) {
            return minecraft.getConnection().getRecipeManager();
        }
        return null;
    }
}

