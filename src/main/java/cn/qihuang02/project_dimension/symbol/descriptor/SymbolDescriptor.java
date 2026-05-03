package cn.qihuang02.project_dimension.symbol.descriptor;

import cn.qihuang02.project_dimension.api.IDimensionSymbol;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public final class SymbolDescriptor {

    public static List<Component> describe(DimensionSymbolVector vector) {
        List<Component> lines = new ArrayList<>();
        for (IDimensionSymbol symbol : cn.qihuang02.project_dimension.register.DimensionSymbolRegistry.builtins()) {
            if (symbol.compound()) continue;

            int value = symbol.resolve(vector);
            String tier = tierKey(value);
            MutableComponent component = Component.translatable("symbol.tier." + tier)
                    .append(" ")
                    .append(Component.translatable("symbol." + symbol.key()))
                    .withColor(value == 0 ? 0xFF888888 : symbol.color());
            lines.add(component);
        }
        return lines;
    }

    public static ShaderUniforms computeShaderUniforms(DimensionSymbolVector vector) {
        int[] colors = new int[6];
        float[] weights = new float[6];
        int i = 0;
        for (IDimensionSymbol symbol : cn.qihuang02.project_dimension.register.DimensionSymbolRegistry.builtins()) {
            if (symbol.compound()) continue;
            int value = symbol.resolve(vector);
            colors[i] = symbol.color();
            weights[i] = (float) Math.abs(value) / 10.0f;
            i++;
        }
        return new ShaderUniforms(colors, weights);
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

    public record ShaderUniforms(int[] colors, float[] weights) {
    }
}
