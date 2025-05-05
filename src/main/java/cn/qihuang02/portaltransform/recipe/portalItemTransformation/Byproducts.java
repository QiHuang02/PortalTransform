package cn.qihuang02.portaltransform.recipe.portalItemTransformation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Byproducts(
        ItemStack byproduct,
        float chance,
        CountRange counts
) {
    public static final String ERROR_EMPTY_BYPRODUCT = "Byproduct byproduct at index %d cannot be empty";
    public static final String ERROR_INVALID_COUNTS = "Byproduct at index %d has invalid min/max counts";
    public static final String ERROR_INVALID_CHANCE = "Byproduct at index %d has invalid chance (must be > 0 and <= 1)";

    public static final MapCodec<Byproducts> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.STRICT_CODEC.fieldOf("byproduct").forGetter(Byproducts::byproduct),
            Codec.FLOAT.fieldOf("chance").forGetter(Byproducts::chance),
            CountRange.CODEC.fieldOf("counts").forGetter(Byproducts::counts)
    ).apply(instance, Byproducts::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Byproducts> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, Byproducts::byproduct,
            ByteBufCodecs.FLOAT, Byproducts::chance,
            CountRange.STREAM_CODEC, Byproducts::counts,
            Byproducts::new
    );


    public static @Nullable String validate(@NotNull Byproducts byproduct, int index) {
        if (byproduct.byproduct().isEmpty()) {
            return String.format(ERROR_EMPTY_BYPRODUCT, index);
        }

        if (byproduct.counts().min() <= 0 || byproduct.counts().max() < byproduct.counts().min()) {
            return String.format(ERROR_INVALID_COUNTS, index);
        }

        if (byproduct.chance() <= 0 || byproduct.chance() > 1) {
            return String.format(ERROR_INVALID_CHANCE, index);
        }

        return null;
    }
}
