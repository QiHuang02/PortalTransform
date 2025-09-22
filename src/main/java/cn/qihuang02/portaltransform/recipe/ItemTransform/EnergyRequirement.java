package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record EnergyRequirement(
        int amount,
        int horizontalRange,
        int verticalRange
) {
    public static final String ERROR_INVALID_AMOUNT = "Energy requirement amount must be positive.";
    public static final String ERROR_INVALID_RANGE = "Energy requirement ranges must be between 0 and 16.";
    public static final int DEFAULT_HORIZONTAL_RANGE = 2;
    public static final int DEFAULT_VERTICAL_RANGE = 1;
    private static final int MAX_RANGE = 16;

    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyRequirement> STREAM_CODEC = StreamCodec.of(
            (buf, requirement) -> {
                buf.writeVarInt(requirement.amount);
                buf.writeVarInt(requirement.horizontalRange);
                buf.writeVarInt(requirement.verticalRange);
            },
            buf -> new EnergyRequirement(
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt()
            )
    );

    private static final MapCodec<EnergyRequirement> BASE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(EnergyRequirement::amount),
            Codec.intRange(0, MAX_RANGE).optionalFieldOf("horizontal_range", DEFAULT_HORIZONTAL_RANGE).forGetter(EnergyRequirement::horizontalRange),
            Codec.intRange(0, MAX_RANGE).optionalFieldOf("vertical_range", DEFAULT_VERTICAL_RANGE).forGetter(EnergyRequirement::verticalRange)
    ).apply(instance, EnergyRequirement::new));

    public static final Codec<EnergyRequirement> CODEC = BASE_CODEC.codec().flatXmap(EnergyRequirement::validate, EnergyRequirement::validate);

    public EnergyRequirement(int amount, int horizontalRange, int verticalRange) {
        if (amount <= 0) {
            throw new IllegalArgumentException(ERROR_INVALID_AMOUNT);
        }
        if (horizontalRange < 0 || verticalRange < 0 || horizontalRange > MAX_RANGE || verticalRange > MAX_RANGE) {
            throw new IllegalArgumentException(ERROR_INVALID_RANGE);
        }
        this.amount = amount;
        this.horizontalRange = horizontalRange;
        this.verticalRange = verticalRange;
    }

    private static DataResult<EnergyRequirement> validate(@NotNull EnergyRequirement requirement) {
        if (requirement.amount <= 0) {
            return DataResult.error(() -> ERROR_INVALID_AMOUNT);
        }
        if (requirement.horizontalRange < 0 || requirement.verticalRange < 0
                || requirement.horizontalRange > MAX_RANGE || requirement.verticalRange > MAX_RANGE) {
            return DataResult.error(() -> ERROR_INVALID_RANGE);
        }
        return DataResult.success(requirement);
    }

    public Optional<EnergyPlan> planConsumption(@NotNull ServerLevel level, @NotNull BlockPos center) {
        int horizontal = Math.max(0, horizontalRange);
        int vertical = Math.max(0, verticalRange);
        BlockPos min = center.offset(-horizontal, -vertical, -horizontal);
        BlockPos max = center.offset(horizontal, vertical, horizontal);

        int remaining = amount;
        List<EnergyTarget> targets = new ArrayList<>();
        Map<IEnergyStorage, Direction> seenStorages = new IdentityHashMap<>();

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            remaining = collectFromPosition(level, pos, remaining, targets, seenStorages);
            if (remaining <= 0) {
                break;
            }
        }

        if (remaining > 0) {
            return Optional.empty();
        }

        return Optional.of(new EnergyPlan(targets));
    }

    private int collectFromPosition(ServerLevel level, BlockPos pos, int remaining, List<EnergyTarget> targets, Map<IEnergyStorage, Direction> seen) {
        if (remaining <= 0) {
            return 0;
        }

        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        if (storage != null && !seen.containsKey(storage)) {
            seen.put(storage, null);
            remaining = attemptAddTarget(storage, pos, null, remaining, targets);
        }

        if (remaining <= 0) {
            return 0;
        }

        for (Direction direction : Direction.values()) {
            storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, direction);
            if (storage != null && !seen.containsKey(storage)) {
                seen.put(storage, direction);
                remaining = attemptAddTarget(storage, pos, direction, remaining, targets);
                if (remaining <= 0) {
                    return 0;
                }
            }
        }

        return remaining;
    }

    private int attemptAddTarget(IEnergyStorage storage, BlockPos pos, @Nullable Direction direction, int remaining, List<EnergyTarget> targets) {
        if (remaining <= 0) {
            return 0;
        }
        int simulated = storage.extractEnergy(remaining, true);
        if (simulated <= 0) {
            return remaining;
        }
        targets.add(new EnergyTarget(pos.immutable(), direction, simulated));
        return remaining - simulated;
    }

    public record EnergyPlan(List<EnergyTarget> targets) {
        public static final EnergyPlan EMPTY = new EnergyPlan(List.of());

        public boolean isEmpty() {
            return targets.isEmpty();
        }

        public boolean consume(@NotNull ServerLevel level) {
            if (targets.isEmpty()) {
                return true;
            }

            List<IEnergyStorage> storages = new ArrayList<>(targets.size());
            for (EnergyTarget target : targets) {
                IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, target.pos, target.direction);
                if (storage == null) {
                    return false;
                }
                int simulated = storage.extractEnergy(target.amount, true);
                if (simulated < target.amount) {
                    return false;
                }
                storages.add(storage);
            }

            for (int i = 0; i < targets.size(); i++) {
                EnergyTarget target = targets.get(i);
                IEnergyStorage storage = storages.get(i);
                storage.extractEnergy(target.amount, false);
            }

            return true;
        }
    }

    public record EnergyTarget(BlockPos pos, @Nullable Direction direction, int amount) {
        public EnergyTarget {
            if (amount <= 0) {
                throw new IllegalArgumentException(ERROR_INVALID_AMOUNT);
            }
        }
    }
}
