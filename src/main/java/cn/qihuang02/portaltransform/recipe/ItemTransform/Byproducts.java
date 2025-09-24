package cn.qihuang02.portaltransform.recipe.ItemTransform;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record Byproducts(
        ItemStack byproduct,
        float chance,
        CountRange counts
) {
    public static final MapCodec<Byproducts> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.fieldOf("byproduct").forGetter(Byproducts::byproduct),
            Codec.FLOAT.fieldOf("chance").forGetter(Byproducts::chance),
            CountRange.CODEC.fieldOf("counts").forGetter(Byproducts::counts)
    ).apply(instance, Byproducts::new));

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeItem(byproduct);
        buf.writeFloat(chance);
        counts.toNetwork(buf);
    }

    public static Byproducts fromNetwork(FriendlyByteBuf buf) {
        ItemStack stack = buf.readItem();
        float chance = buf.readFloat();
        CountRange range = CountRange.fromNetwork(buf);
        return new Byproducts(stack, chance, range);
    }

    public static Byproducts fromJson(JsonObject json) {
        ItemStack stack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "byproduct"));
        float chance = GsonHelper.getAsFloat(json, "chance");
        CountRange range = CountRange.fromJson(GsonHelper.getAsJsonObject(json, "counts"));
        return new Byproducts(stack, chance, range);
    }

    public Byproducts copy() {
        return new Byproducts(byproduct.copy(), chance, counts.copy());
    }

    public Byproducts {
        if (byproduct == null || byproduct.isEmpty()) {
            throw new IllegalArgumentException("Byproduct ItemStack cannot be null or empty.");
        }
        if (chance <= 0.0f || chance > 1.0f) {
            // 注意：允许1.0，原代码不允许，这里修正为 <= 0 或 > 1 才报错
            throw new IllegalArgumentException("Byproduct chance must be between 0.0 (exclusive) and 1.0 (inclusive), got: " + chance);
        }
    }

    public Optional<ItemStack> getResult(@NotNull RandomSource random) {
        if (random.nextFloat() < this.chance) {
            int count = this.counts.getRandomCount(random);
            return Optional.of(this.byproduct.copyWithCount(count));
        }
        return Optional.empty();
    }
}
