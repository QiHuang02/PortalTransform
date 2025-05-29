package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record Dimensions(
        Optional<ResourceKey<Level>> current,
        Optional<ResourceKey<Level>> target
) {
    @Contract(" -> new")
    public static @NotNull Dimensions empty() {
        return new Dimensions(Optional.empty(), Optional.empty());
    }

    public static final Codec<Dimensions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("current").forGetter(dr -> dr.current),
            ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("target").forGetter(dr -> dr.target)
    ).apply(instance, Dimensions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Dimensions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)), Dimensions::current,
            ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)), Dimensions::target,
            Dimensions::new
    );
}
