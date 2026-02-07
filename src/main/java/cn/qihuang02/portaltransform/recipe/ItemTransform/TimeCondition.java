package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record TimeCondition(int startTick, int endTick, Optional<Keyword> keyword) {
    public static final int DAY_LENGTH = 24000;
    public static final String ERROR_RANGE_OUT_OF_BOUNDS = "Time range must be between 0 and 23999 inclusive.";
    public static final String ERROR_INVALID_RANGE_ARRAY = "Time range must be an array containing exactly two integers.";
    public static final String ERROR_KEYWORD_RANGE_MISMATCH = "Provided range does not match keyword-defined range.";

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeBoolean(keyword.isPresent());
        if (keyword.isPresent()) {
            buf.writeUtf(keyword.get().getSerializedName());
        } else {
            buf.writeVarInt(startTick);
            buf.writeVarInt(endTick);
        }
    }

    public static TimeCondition fromNetwork(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            String name = buf.readUtf(16);
            Keyword value = Keyword.byName(name)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown time keyword '" + name + "'."));
            return TimeCondition.keyword(value);
        }

        int start = buf.readVarInt();
        int end = buf.readVarInt();
        return TimeCondition.range(start, end);
    }

    private static final Codec<List<Integer>> RANGE_ARRAY_CODEC = Codec.INT.listOf().comapFlatMap(
            list -> list.size() == 2
                    ? DataResult.success(list)
                    : DataResult.error(() -> ERROR_INVALID_RANGE_ARRAY),
            list -> list
    );

    public static final Codec<TimeCondition> CODEC = Codec.either(Keyword.CODEC, RANGE_ARRAY_CODEC).flatXmap(
            either -> either.map(
                    keyword -> DataResult.success(TimeCondition.keyword(keyword)),
                    list -> createRange(list.get(0), list.get(1))
            ),
            condition -> condition.keyword()
                    .<DataResult<Either<Keyword, List<Integer>>>>map(keyword -> DataResult.success(Either.left(keyword)))
                    .orElseGet(() -> DataResult.success(Either.right(List.of(condition.startTick(), condition.endTick()))))
    );

    public TimeCondition {
        Objects.requireNonNull(keyword, "keyword optional cannot be null");
        if (!isWithinBounds(startTick) || !isWithinBounds(endTick)) {
            throw new IllegalArgumentException(ERROR_RANGE_OUT_OF_BOUNDS);
        }
        if (keyword.isPresent()) {
            Keyword value = keyword.get();
            if (value.startTick != startTick || value.endTick != endTick) {
                throw new IllegalArgumentException(ERROR_KEYWORD_RANGE_MISMATCH);
            }
        } else {
            keyword = Keyword.fromRange(startTick, endTick);
        }
    }

    public static TimeCondition any() {
        return new TimeCondition(0, DAY_LENGTH - 1, Optional.empty());
    }

    public static TimeCondition day() {
        return keyword(Keyword.DAY);
    }

    public static TimeCondition night() {
        return keyword(Keyword.NIGHT);
    }

    public static TimeCondition noon() {
        return keyword(Keyword.NOON);
    }

    public static TimeCondition midnight() {
        return keyword(Keyword.MIDNIGHT);
    }

    public static TimeCondition keyword(@NotNull Keyword keyword) {
        Objects.requireNonNull(keyword, "keyword");
        return new TimeCondition(keyword.startTick, keyword.endTick, Optional.of(keyword));
    }

    public static TimeCondition range(int start, int end) {
        return new TimeCondition(start, end, Optional.empty());
    }

    private static DataResult<TimeCondition> createRange(int start, int end) {
        if (!isWithinBounds(start) || !isWithinBounds(end)) {
            return DataResult.error(() -> ERROR_RANGE_OUT_OF_BOUNDS);
        }
        return DataResult.success(TimeCondition.range(start, end));
    }

    private static boolean isWithinBounds(int value) {
        return value >= 0 && value < DAY_LENGTH;
    }

    public boolean matches(@NotNull ServerLevel level) {
        return coversWholeDay() || matchesRange(level);
    }

    private boolean matchesRange(@NotNull Level level) {
        int timeOfDay = (int) (level.getDayTime() % DAY_LENGTH);
        if (timeOfDay < 0) {
            timeOfDay += DAY_LENGTH;
        }

        if (startTick <= endTick) {
            return timeOfDay >= startTick && timeOfDay <= endTick;
        }

        return timeOfDay >= startTick || timeOfDay <= endTick;
    }

    public boolean coversWholeDay() {
        return startTick == 0 && endTick == DAY_LENGTH - 1;
    }

    public enum Keyword implements StringRepresentable {
        DAY("day", 1000, 12999),
        NIGHT("night", 13000, 22999),
        NOON("noon", 6000, 6000),
        MIDNIGHT("midnight", 18000, 18000);

        public static final Codec<Keyword> CODEC = StringRepresentable.fromEnum(Keyword::values);

        private final String name;
        private final int startTick;
        private final int endTick;

        Keyword(String name, int startTick, int endTick) {
            this.name = name;
            this.startTick = startTick;
            this.endTick = endTick;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }

        public static Optional<Keyword> byName(String name) {
            for (Keyword keyword : values()) {
                if (keyword.name.equalsIgnoreCase(name)) {
                    return Optional.of(keyword);
                }
            }
            return Optional.empty();
        }

        public static Optional<Keyword> fromRange(int start, int end) {
            for (Keyword keyword : values()) {
                if (keyword.startTick == start && keyword.endTick == end) {
                    return Optional.of(keyword);
                }
            }
            return Optional.empty();
        }
    }
}
