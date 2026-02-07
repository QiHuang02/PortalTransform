package cn.qihuang02.portaltransform.compat.kubejs.components;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

@HideFromJS
public final class BiomeComponent {
    private BiomeComponent() {
    }

    @SuppressWarnings("unchecked")
    public static ResourceKey<Biome> parse(Object from) {
        if (from instanceof ResourceKey<?> key) {
            if (key.isFor(Registries.BIOME)) {
                return (ResourceKey<Biome>) key;
            }
            throw new IllegalArgumentException("需要 BIOME 注册表键，实际为: " + key.registry());
        }

        ResourceLocation rl = from instanceof ResourceLocation loc ? loc :
                from instanceof String s ? ResourceLocation.tryParse(s) : null;

        if (rl == null && from instanceof String s && !s.contains(":")) {
            rl = ResourceLocation.tryParse("minecraft:" + s);
        }

        if (rl != null) {
            return ResourceKey.create(Registries.BIOME, rl);
        }

        throw new IllegalArgumentException("无法解析 biome: " + from);
    }
}
