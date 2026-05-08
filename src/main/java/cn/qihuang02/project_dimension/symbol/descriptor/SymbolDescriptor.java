package cn.qihuang02.project_dimension.symbol.descriptor;

import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public final class SymbolDescriptor {
    private static final List<BasicSymbolChannel> BASIC_SYMBOL_CHANNELS = List.of(
            new BasicSymbolChannel("flame", 0xFFAA55, DimensionSymbolVector::flame),
            new BasicSymbolChannel("life", 0x55FF55, DimensionSymbolVector::life),
            new BasicSymbolChannel("void", 0xAA55FF, DimensionSymbolVector::voidAffinity),
            new BasicSymbolChannel("soul", 0x5555FF, DimensionSymbolVector::soul),
            new BasicSymbolChannel("form", 0x55FFFF, DimensionSymbolVector::form),
            new BasicSymbolChannel("phase", 0xFFFF55, DimensionSymbolVector::phase)
    );

    public static List<Component> describe(DimensionSymbolVector vector) {
        List<Component> lines = new ArrayList<>();
        for (BasicSymbolChannel channel : BASIC_SYMBOL_CHANNELS) {
            int value = channel.resolve(vector);
            String tier = tierKey(value);
            MutableComponent component = Component.translatable("symbol.tier." + tier)
                    .append(" ")
                    .append(Component.translatable("symbol." + channel.key()))
                    .withColor(value == 0 ? 0xFF888888 : channel.color());
            lines.add(component);
        }
        return lines;
    }

    public static ShaderUniforms computeShaderUniforms(DimensionSymbolVector vector) {
        int[] colors = new int[BASIC_SYMBOL_CHANNELS.size()];
        float[] weights = new float[BASIC_SYMBOL_CHANNELS.size()];
        for (int i = 0; i < BASIC_SYMBOL_CHANNELS.size(); i++) {
            BasicSymbolChannel channel = BASIC_SYMBOL_CHANNELS.get(i);
            int value = channel.resolve(vector);
            colors[i] = channel.color();
            weights[i] = shaderWeight(value);
        }
        return new ShaderUniforms(colors, weights);
    }

    private static float shaderWeight(int value) {
        int abs = Math.abs(value);
        if (abs == 0) {
            return 0.0f;
        }
        float normalized = Math.min(1.0f, abs / (float) DimensionSymbolVector.MAX);
        // 使用开方压缩高值差距，让弱象征也能在云层中留下少量可见颜色。
        return Math.min(1.0f, 0.18f + 0.82f * (float) Math.sqrt(normalized));
    }

    private static String tierKey(int value) {
        if (value == 0) return "dormant";
        int abs = Math.abs(value);
        String prefix = value > 0 ? "" : "neg.";
        if (abs <= 3) return prefix + "low";
        if (abs <= 6) return prefix + "moderate";
        if (abs <= 9) return prefix + "high";
        return prefix + "extreme";
    }

    private record BasicSymbolChannel(String key, int color, ToIntFunction<DimensionSymbolVector> resolver) {
        private int resolve(DimensionSymbolVector vector) {
            return resolver.applyAsInt(vector);
        }
    }

    public record ShaderUniforms(int[] colors, float[] weights) {
    }
}
