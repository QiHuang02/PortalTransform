package cn.qihuang02.portaltransform.compat.kubejs.components;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

@HideFromJS
public final class BlockComponent {
    public static ResourceKey<Block> parse(Object from) {
        return RegistryKeyParsers.parse(from, Registries.BLOCK, "BLOCK", "无法解析方块: ");
    }
}
