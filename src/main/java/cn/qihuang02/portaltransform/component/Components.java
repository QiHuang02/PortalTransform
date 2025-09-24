package cn.qihuang02.portaltransform.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Helper methods for legacy component-like flags on item stacks.
 */
public final class Components {
    private static final String NO_PORTAL_TRANSFORM_TAG = "PortalTransformNoTransform";

    private Components() {
    }

    public static void register(IEventBus eventBus) {
        // No-op on Forge 1.20.1: data components are not available, so nothing to register.
    }

    public static boolean hasNoPortalTransform(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(NO_PORTAL_TRANSFORM_TAG);
    }

    public static void markNoPortalTransform(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NO_PORTAL_TRANSFORM_TAG, true);
    }

    public static void clearNoPortalTransform(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(NO_PORTAL_TRANSFORM_TAG);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }
}
