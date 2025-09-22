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
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record Biomes(
        List<ResourceKey<Biome>> biomes
) {
    public static final String ERROR_EMPTY_BIOME_LIST = "Biomes list cannot be empty.";
    public static final String ERROR_NULL_BIOME = "Biomes list cannot contain null entries.";

    public static final StreamCodec<RegistryFriendlyByteBuf, Biomes> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ResourceKey.streamCodec(Registries.BIOME)),
            Biomes::biomes,
            Biomes::new
    );

    private static final MapCodec<Biomes> BASE_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.BIOME).listOf().fieldOf("biomes").forGetter(Biomes::biomes)
    ).apply(instance, Biomes::new));

    public static final Codec<Biomes> CODEC = BASE_MAP_CODEC.codec().flatXmap(Biomes::validate, Biomes::validate);

    public Biomes(@NotNull List<ResourceKey<Biome>> biomes) {
        if (biomes.isEmpty()) {
            throw new IllegalArgumentException(ERROR_EMPTY_BIOME_LIST);
        }
        if (biomes.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(ERROR_NULL_BIOME);
        }
        this.biomes = List.copyOf(biomes);
    }

    private static DataResult<Biomes> validate(@NotNull Biomes biomesInstance) {
        List<ResourceKey<Biome>> biomes = biomesInstance.biomes();
        if (biomes.isEmpty()) {
            return DataResult.error(() -> ERROR_EMPTY_BIOME_LIST);
        }
        if (biomes.stream().anyMatch(Objects::isNull)) {
            return DataResult.error(() -> ERROR_NULL_BIOME);
        }
        return DataResult.success(biomesInstance);
    }

    public boolean contains(@NotNull ResourceKey<Biome> biomeKey) {
        return biomes.contains(biomeKey);
    }
}
