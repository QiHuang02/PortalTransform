package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public record TimeCondition(Mode mode, Optional<Integer> start, Optional<Integer> end) {
    public static final int DAY_LENGTH = 24000;
    public static final String ERROR_MISSING_RANGE = "Time range must specify both start and end when mode is 'range'.";
    public static final String ERROR_RANGE_OUT_OF_BOUNDS = "Time range must be between 0 and 23999 inclusive.";

    public static final StreamCodec<RegistryFriendlyByteBuf, TimeCondition> STREAM_CODEC = StreamCodec.of(
            (buf, condition) -> {
                buf.writeVarInt(condition.mode().ordinal());
                encodeOptionalInt(buf, condition.start());
                encodeOptionalInt(buf, condition.end());
            },
            buf -> {
                int index = buf.readVarInt();
                Mode[] modes = Mode.values();
                if (index < 0 || index >= modes.length) {
                    throw new IllegalArgumentException("Invalid time condition mode index: " + index);
                }
                Mode mode = modes[index];
                Optional<Integer> start = decodeOptionalInt(buf);
                Optional<Integer> end = decodeOptionalInt(buf);
                return new TimeCondition(mode, start, end);
            }
    );

    private static void encodeOptionalInt(RegistryFriendlyByteBuf buf, Optional<Integer> value) {
        buf.writeBoolean(value.isPresent());
        value.ifPresent(buf::writeVarInt);
    }

    private static Optional<Integer> decodeOptionalInt(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? Optional.of(buf.readVarInt()) : Optional.empty();
    }

    private static final MapCodec<TimeCondition> BASE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Mode.CODEC.optionalFieldOf("mode", Mode.ANY).forGetter(TimeCondition::mode),
            Codec.INT.optionalFieldOf("start").forGetter(TimeCondition::start),
            Codec.INT.optionalFieldOf("end").forGetter(TimeCondition::end)
    ).apply(instance, TimeCondition::new));

    public static final Codec<TimeCondition> CODEC = BASE_CODEC.codec().flatXmap(TimeCondition::validate, TimeCondition::validate);

    public TimeCondition(@NotNull Mode mode, @NotNull Optional<Integer> start, @NotNull Optional<Integer> end) {
        this.mode = Objects.requireNonNull(mode, "mode");
        this.start = Objects.requireNonNull(start, "start optional cannot be null");
        this.end = Objects.requireNonNull(end, "end optional cannot be null");
    }

    public static TimeCondition any() {
        return new TimeCondition(Mode.ANY, Optional.empty(), Optional.empty());
    }

    public static TimeCondition day() {
        return new TimeCondition(Mode.DAY, Optional.empty(), Optional.empty());
    }

    public static TimeCondition night() {
        return new TimeCondition(Mode.NIGHT, Optional.empty(), Optional.empty());
    }

    public static TimeCondition range(int start, int end) {
        return new TimeCondition(Mode.RANGE, Optional.of(start), Optional.of(end));
    }

    private static DataResult<TimeCondition> validate(@NotNull TimeCondition condition) {
        Mode mode = condition.mode();
        Optional<Integer> startOpt = condition.start();
        Optional<Integer> endOpt = condition.end();

        if (mode == Mode.RANGE) {
            if (startOpt.isEmpty() || endOpt.isEmpty()) {
                return DataResult.error(() -> ERROR_MISSING_RANGE);
            }

            int start = startOpt.get();
            int end = endOpt.get();
            if (!isWithinBounds(start) || !isWithinBounds(end)) {
                return DataResult.error(() -> ERROR_RANGE_OUT_OF_BOUNDS);
            }
        } else if (startOpt.isPresent() || endOpt.isPresent()) {
            return DataResult.error(() -> "Time start/end can only be specified when mode is 'range'.");
        }

        return DataResult.success(condition);
    }

    private static boolean isWithinBounds(int value) {
        return value >= 0 && value < DAY_LENGTH;
    }

    public boolean matches(@NotNull ServerLevel level) {
        return switch (mode) {
            case ANY -> true;
            case DAY -> level.isDay();
            case NIGHT -> !level.isDay();
            case RANGE -> matchesRange(level);
        };
    }

    private boolean matchesRange(@NotNull Level level) {
        int timeOfDay = (int) (level.getDayTime() % DAY_LENGTH);
        if (timeOfDay < 0) {
            timeOfDay += DAY_LENGTH;
        }

        int startTick = start.orElseThrow();
        int endTick = end.orElseThrow();

        if (startTick <= endTick) {
            return timeOfDay >= startTick && timeOfDay <= endTick;
        }

        return timeOfDay >= startTick || timeOfDay <= endTick;
    }

    public enum Mode implements StringRepresentable {
        ANY("any"),
        DAY("day"),
        NIGHT("night"),
        RANGE("range");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}
