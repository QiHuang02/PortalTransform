package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record Catalyst(
        List<ResourceKey<Block>> blocks,
        int horizontalRange,
        int verticalRange
) {
    public static final String ERROR_EMPTY_BLOCK_LIST = "Catalyst block list cannot be empty.";
    public static final String ERROR_NEGATIVE_RANGE = "Catalyst search range values must be non-negative.";
    public static final int DEFAULT_HORIZONTAL_RANGE = 3;
    public static final int DEFAULT_VERTICAL_RANGE = 1;
    private static final int MAX_RANGE = 16;

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(blocks.size());
        for (ResourceKey<Block> key : blocks) {
            buf.writeResourceLocation(key.location());
        }
        buf.writeVarInt(horizontalRange);
        buf.writeVarInt(verticalRange);
    }

    public static Catalyst fromNetwork(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ResourceKey<Block>> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(ResourceKey.create(Registries.BLOCK, buf.readResourceLocation()));
        }
        int horizontalRange = buf.readVarInt();
        int verticalRange = buf.readVarInt();
        return new Catalyst(entries, horizontalRange, verticalRange);
    }

    private static final MapCodec<Catalyst> BASE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.BLOCK).listOf().fieldOf("blocks").forGetter(Catalyst::blocks),
            Codec.intRange(0, MAX_RANGE).optionalFieldOf("horizontal_range", DEFAULT_HORIZONTAL_RANGE).forGetter(Catalyst::horizontalRange),
            Codec.intRange(0, MAX_RANGE).optionalFieldOf("vertical_range", DEFAULT_VERTICAL_RANGE).forGetter(Catalyst::verticalRange)
    ).apply(instance, Catalyst::new));

    public static final Codec<Catalyst> CODEC = BASE_CODEC.codec().flatXmap(Catalyst::validate, Catalyst::validate);

    public Catalyst(@NotNull List<ResourceKey<Block>> blocks, int horizontalRange, int verticalRange) {
        if (blocks.isEmpty()) {
            throw new IllegalArgumentException(ERROR_EMPTY_BLOCK_LIST);
        }
        if (blocks.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Catalyst block list cannot contain null entries.");
        }
        if (horizontalRange < 0 || verticalRange < 0 || horizontalRange > MAX_RANGE || verticalRange > MAX_RANGE) {
            throw new IllegalArgumentException(ERROR_NEGATIVE_RANGE);
        }
        this.blocks = List.copyOf(blocks);
        this.horizontalRange = horizontalRange;
        this.verticalRange = verticalRange;
    }

    private static DataResult<Catalyst> validate(@NotNull Catalyst catalyst) {
        if (catalyst.blocks.isEmpty()) {
            return DataResult.error(() -> ERROR_EMPTY_BLOCK_LIST);
        }
        if (catalyst.blocks.stream().anyMatch(Objects::isNull)) {
            return DataResult.error(() -> "Catalyst block list cannot contain null entries.");
        }
        if (catalyst.horizontalRange < 0 || catalyst.verticalRange < 0 || catalyst.horizontalRange > MAX_RANGE || catalyst.verticalRange > MAX_RANGE) {
            return DataResult.error(() -> ERROR_NEGATIVE_RANGE);
        }
        return DataResult.success(catalyst);
    }

    public boolean matches(@NotNull ServerLevel level, @NotNull BlockPos center) {
        Registry<Block> blockRegistry = level.registryAccess().registryOrThrow(Registries.BLOCK);
        Set<ResourceKey<Block>> allowed = Set.copyOf(blocks);

        int horizontal = Math.max(0, horizontalRange);
        int vertical = Math.max(0, verticalRange);
        BlockPos min = center.offset(-horizontal, -vertical, -horizontal);
        BlockPos max = center.offset(horizontal, vertical, horizontal);

        return BlockPos.betweenClosedStream(min, max)
                .anyMatch(pos -> matchesBlock(level, blockRegistry, allowed, pos));
    }

    private boolean matchesBlock(ServerLevel level, Registry<Block> blockRegistry, Set<ResourceKey<Block>> allowed, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        Optional<ResourceKey<Block>> key = blockRegistry.getResourceKey(state.getBlock());
        return key.filter(allowed::contains).isPresent();
    }
}
