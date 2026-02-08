package cn.qihuang02.portaltransform.compat.kubejs.event;

import cn.qihuang02.portaltransform.event.PortalItemTransformedEvent;
import cn.qihuang02.portaltransform.recipe.ItemTransformRecipe;
import dev.latvian.mods.kubejs.level.LevelEventJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Server-side KubeJS event fired after an item successfully transforms inside a portal.
 */
public class PortalItemTransformedKubeEvent extends LevelEventJS {
    private final PortalItemTransformedEvent event;

    @HideFromJS
    public PortalItemTransformedKubeEvent(@NotNull PortalItemTransformedEvent event) {
        this.event = event;
    }

    @Override
    public ServerLevel getLevel() {
        return event.getLevel();
    }

    public ResourceKey<Level> getCurrentDimension() {
        return event.getCurrentDimension();
    }

    public ResourceKey<Level> getTargetDimension() {
        return event.getTargetDimension();
    }

    public Vec3 getPosition() {
        return event.getPosition();
    }

    public ItemStack getInput() {
        return event.getOriginalStack();
    }

    public ItemStack getResult() {
        return event.getResultStack();
    }

    public ItemStack getRemaining() {
        return event.getRemainingStack();
    }

    public boolean wasInsertedIntoContainer() {
        return event.wasInsertedIntoContainer();
    }

    public List<ItemStack> getByproducts() {
        return event.getByproducts();
    }

    public ItemTransformRecipe getRecipe() {
        return event.getRecipe();
    }

    public ResourceLocation getRecipeId() {
        return event.getRecipeId();
    }

    @HideFromJS
    public PortalItemTransformedEvent asForgeEvent() {
        return event;
    }
}
