package cn.qihuang02.portaltransform.compat.kubejs.components;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

@HideFromJS
public final class LevelComponent {
    public static ResourceKey<Level> parse(Object from) {
        return RegistryKeyParsers.parse(from, Registries.DIMENSION, "DIMENSION", "无法解析维度: ");
    }
}
