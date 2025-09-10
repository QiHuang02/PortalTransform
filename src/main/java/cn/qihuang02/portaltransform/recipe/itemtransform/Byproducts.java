package cn.qihuang02.portaltransform.recipe.itemtransform;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class Byproducts {
    private final ItemStack byproduct;
    private final float chance;
    private final CountRange counts;

    public Byproducts(ItemStack byproduct, float chance, CountRange counts) {
        if (byproduct == null || byproduct.isEmpty()) {
            throw new IllegalArgumentException("Byproduct ItemStack cannot be null or empty.");
        }
        if (chance <= 0.0f || chance > 1.0f) {
            throw new IllegalArgumentException("Byproduct chance must be between 0.0 (exclusive) and 1.0 (inclusive), got: " + chance);
        }
        this.byproduct = byproduct;
        this.chance = chance;
        this.counts = counts;
    }

    public Optional<ItemStack> getResult(RandomSource random) {
        if (random.nextFloat() < this.chance) {
            int count = this.counts.getRandomCount(random);
            return Optional.of(this.byproduct.copyWithCount(count));
        }
        return Optional.empty();
    }

    public ItemStack getByproduct() {
        return byproduct;
    }

    public float getChance() {
        return chance;
    }

    public CountRange getCounts() {
        return counts;
    }
}
