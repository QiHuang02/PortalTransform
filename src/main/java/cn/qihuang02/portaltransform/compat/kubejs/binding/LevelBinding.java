package cn.qihuang02.portaltransform.compat.kubejs.binding;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class LevelBinding {
    private LevelBinding() {
    }

    public static final ResourceKey<Level> OVERWORLD =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"));

    public static final ResourceKey<Level> THE_NETHER =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("minecraft", "the_nether"));

    public static final ResourceKey<Level> THE_END =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("minecraft", "the_end"));
}
