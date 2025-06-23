package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Dimensions(
        List<ResourceKey<Level>> dimensions
) {
    public static final String ERROR_INVALID_DIMENSIONS_SIZE = "Dimensions list must be empty or contain exactly two elements (current and target).";
    public static final StreamCodec<RegistryFriendlyByteBuf, Dimensions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ResourceKey.streamCodec(Registries.DIMENSION)),
            Dimensions::dimensions,
            Dimensions::new
    );
    private static final MapCodec<Dimensions> BASE_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("dimensions").forGetter(Dimensions::dimensions)
    ).apply(instance, Dimensions::new));
    public static final Codec<Dimensions> CODEC = BASE_MAP_CODEC.codec().flatXmap(Dimensions::validate, Dimensions::validate);

    public Dimensions(@NotNull List<ResourceKey<Level>> dimensions) {
        if (!dimensions.isEmpty() && dimensions.size() != 2) {
            throw new IllegalArgumentException(ERROR_INVALID_DIMENSIONS_SIZE);
        }
        this.dimensions = List.copyOf(dimensions);
    }

    @Contract(" -> new")
    public static @NotNull Dimensions empty() {
        return new Dimensions(List.of());
    }

    private static DataResult<Dimensions> validate(@NotNull Dimensions dimensionsInstance) {
        List<ResourceKey<Level>> dims = dimensionsInstance.dimensions();
        if (!dims.isEmpty() && dims.size() != 2) {
            return DataResult.error(() -> ERROR_INVALID_DIMENSIONS_SIZE);
        }
        if (dims.size() == 2 && (dims.get(0) == null || dims.get(1) == null)) {
            return DataResult.error(() -> "Dimension keys in the list cannot be null.");
        }
        return DataResult.success(dimensionsInstance);
    }

    public Optional<ResourceKey<Level>> current() {
        return dimensions.size() == 2 ? Optional.of(dimensions.getFirst()) : Optional.empty();
    }

    public Optional<ResourceKey<Level>> target() {
        return dimensions.size() == 2 ? Optional.of(dimensions.getLast()) : Optional.empty();
    }
}
