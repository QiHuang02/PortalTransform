package cn.qihuang02.portaltransform.util;

import cn.qihuang02.portaltransform.PortalTransform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Utility helpers for storing portal-transform metadata on item stacks using NBT.
 */
public final class NbtUtil {
    private static final String TAG_NO_TRANSFORM = "no_portal_transform";

    private NbtUtil() {}

    /**
     * Returns true if the given stack is marked to skip further portal transformations.
     */
    public static boolean hasNoPortalTransform(@NotNull ItemStack stack) {
        CompoundTag tag = stack.getTagElement(PortalTransform.MODID);
        return tag != null && tag.getBoolean(TAG_NO_TRANSFORM);
    }

    /**
     * Marks the given stack so that it will not be transformed again when travelling through portals.
     */
    public static void setNoPortalTransform(@NotNull ItemStack stack) {
        stack.getOrCreateTagElement(PortalTransform.MODID).putBoolean(TAG_NO_TRANSFORM, true);
    }

    /**
     * Clears the no-transform flag from the given stack so it may be processed again.
     */
    public static void clearNoPortalTransform(@NotNull ItemStack stack) {
        CompoundTag tag = stack.getTagElement(PortalTransform.MODID);
        if (tag != null) {
            tag.remove(TAG_NO_TRANSFORM);
            if (tag.isEmpty()) {
                stack.removeTagKey(PortalTransform.MODID);
            }
        }
    }
}
