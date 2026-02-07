package cn.qihuang02.portaltransform.compat.kubejs.components;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

final class RegistryKeyParsers {
    @SuppressWarnings("unchecked")
    static <T> ResourceKey<T> parse(
            Object from,
            ResourceKey<? extends Registry<T>> registryKey,
            String registryName,
            String parseErrorPrefix
    ) {
        if (from instanceof ResourceKey<?> key) {
            if (key.isFor(registryKey)) {
                return (ResourceKey<T>) key;
            }
            throw new IllegalArgumentException("需要 " + registryName + " 注册表键，实际为: " + key.registry());
        }

        ResourceLocation rl = null;
        if (from instanceof ResourceLocation loc) {
            rl = loc;
        } else if (from instanceof String s) {
            String raw = s.trim();
            if (!raw.contains(":")) {
                raw = "minecraft:" + raw;
            }
            rl = ResourceLocation.tryParse(raw);
        }

        if (rl != null) {
            return ResourceKey.create(registryKey, rl);
        }

        throw new IllegalArgumentException(parseErrorPrefix + from);
    }
}
