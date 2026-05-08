package cn.qihuang02.project_dimension.symbol;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class EnvironmentSymbolResolver {
    private EnvironmentSymbolResolver() {
    }

    public static DimensionSymbolVector resolve(ServerLevel level, BlockPos pos) {
        DimensionSymbolVector result = weather(level)
                .plus(dayTime(level))
                .plus(height(pos));
        return result.clamp();
    }

    private static DimensionSymbolVector weather(ServerLevel level) {
        if (level.isThundering()) {
            return new DimensionSymbolVector(0, 0, 2, 0, 1, 1);
        }
        if (level.isRaining()) {
            return new DimensionSymbolVector(1, 0, -1, 0, 0, 1);
        }
        return new DimensionSymbolVector(0, 1, 0, 0, 0, 0);
    }

    private static DimensionSymbolVector dayTime(ServerLevel level) {
        long dayTime = level.getDayTime() % 24000L;
        DimensionSymbolVector result = DimensionSymbolVector.ZERO;

        if (dayTime >= 0L && dayTime < 12000L) {
            result = result.plus(new DimensionSymbolVector(1, 0, 1, 0, 0, 0));
        } else {
            result = result.plus(new DimensionSymbolVector(0, 0, 0, 1, 1, 0));
        }

        if (dayTime >= 5500L && dayTime <= 6500L) {
            result = result.plus(new DimensionSymbolVector(0, 0, 2, 0, 0, 0));
        }
        if (dayTime >= 17500L && dayTime <= 18500L) {
            result = result.plus(new DimensionSymbolVector(0, 0, 0, 2, 2, 0));
        }

        return result;
    }

    private static DimensionSymbolVector height(BlockPos pos) {
        if (pos.getY() >= 160) {
            return new DimensionSymbolVector(0, 0, 0, 1, 0, 1);
        }
        if (pos.getY() <= -32) {
            return new DimensionSymbolVector(-1, 1, 0, 1, 0, 0);
        }
        return DimensionSymbolVector.ZERO;
    }
}
