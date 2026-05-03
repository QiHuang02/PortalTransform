package cn.qihuang02.project_dimension.symbol;

import cn.qihuang02.project_dimension.register.DimensionSymbolRegistry;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.StringJoiner;

public record DimensionSymbolVector(
        int life,
        int form,
        int flame,
        int voidAffinity,
        int soul,
        int phase
) {
    public static final int MIN = -10;
    public static final int MAX = 10;
    public static final DimensionSymbolVector ZERO = new DimensionSymbolVector(0, 0, 0, 0, 0, 0);

    public static final Codec<DimensionSymbolVector> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("life").forGetter(DimensionSymbolVector::life),
            Codec.INT.fieldOf("form").forGetter(DimensionSymbolVector::form),
            Codec.INT.fieldOf("flame").forGetter(DimensionSymbolVector::flame),
            Codec.INT.fieldOf("void").forGetter(DimensionSymbolVector::voidAffinity),
            Codec.INT.fieldOf("soul").forGetter(DimensionSymbolVector::soul),
            Codec.INT.fieldOf("phase").forGetter(DimensionSymbolVector::phase)
    ).apply(instance, DimensionSymbolVector::new));

    public static final Codec<DimensionSymbolVector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("dimension").forGetter(v -> ResourceLocation.withDefaultNamespace("unknown")),
            RAW_CODEC.fieldOf("symbols").forGetter(java.util.function.Function.identity())
    ).apply(instance, (dim, vec) -> vec));

    private static int clampValue(int value) {
        return Math.clamp(value, MIN, MAX);
    }

    private static String signed(int value) {
        return value > 0 ? "+" + value : Integer.toString(value);
    }

    public DimensionSymbolVector clamp() {
        return new DimensionSymbolVector(
                clampValue(life),
                clampValue(form),
                clampValue(flame),
                clampValue(voidAffinity),
                clampValue(soul),
                clampValue(phase)
        );
    }

    public DimensionSymbolVector plus(DimensionSymbolVector other) {
        return new DimensionSymbolVector(
                life + other.life,
                form + other.form,
                flame + other.flame,
                voidAffinity + other.voidAffinity,
                soul + other.soul,
                phase + other.phase
        );
    }

    public DimensionSymbolVector minus(DimensionSymbolVector other) {
        return new DimensionSymbolVector(
                life - other.life,
                form - other.form,
                flame - other.flame,
                voidAffinity - other.voidAffinity,
                soul - other.soul,
                phase - other.phase
        );
    }

    public String toShortString() {
        StringJoiner joiner = new StringJoiner(", ");
        for (IDimensionSymbol symbol : DimensionSymbolRegistry.builtins()) {
            if (!symbol.compound()) {
                joiner.add(symbol.displayName() + " " + signed(symbol.resolve(this)));
            }
        }
        return joiner.toString();
    }
}
