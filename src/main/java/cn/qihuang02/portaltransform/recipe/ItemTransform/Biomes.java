package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(biomes.size());
        for (ResourceKey<Biome> biome : biomes) {
            buf.writeResourceLocation(biome.location());
        }
    }

    public static Biomes fromNetwork(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ResourceKey<Biome>> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            entries.add(ResourceKey.create(Registries.BIOME, id));
        }
        return new Biomes(entries);
    }

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
