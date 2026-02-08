package cn.qihuang02.portaltransform.compat.kubejs.components;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

@HideFromJS
public final class BiomeComponent {

    public static ResourceKey<Biome> parse(Object from) {
        return RegistryKeyParsers.parse(from, Registries.BIOME, "BIOME", "无法解析 biome: ");
    }
}
