package cn.qihuang02.portaltransform.symbol;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 单个维度中声明的原始象征取值，可同时包含基础象征与复合象征。
 */
public record DimensionSymbolValues(
        ResourceKey<Level> dimension,
        Map<ResourceLocation, Integer> symbols
) {
    public static final Codec<DimensionSymbolValues> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DimensionSymbolValues::dimension),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
                    .fieldOf("symbols")
                    .forGetter(DimensionSymbolValues::symbols)
    ).apply(instance, DimensionSymbolValues::new));

    public DimensionSymbolValues {
        Objects.requireNonNull(dimension, "维度键不能为空");
        Objects.requireNonNull(symbols, "象征值映射不能为空");
        symbols = Map.copyOf(new LinkedHashMap<>(symbols));
    }
}
