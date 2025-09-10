package cn.qihuang02.portaltransform.recipe.itemtransform;

import net.minecraft.util.RandomSource;

public class CountRange {
    private final int min;
    private final int max;

    public CountRange(int min, int max) {
        if (min <= 0) {
            throw new IllegalArgumentException("Minimum count must be greater than 0, got: " + min);
        }
        if (max < min) {
            throw new IllegalArgumentException("Maximum count (" + max + ") cannot be less than minimum count (" + min + ")");
        }
        this.min = min;
        this.max = max;
    }

    public int getRandomCount(RandomSource random) {
        if (min == max) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
