package cn.qihuang02.project_dimension.symbol;

import cn.qihuang02.project_dimension.register.DimensionSymbolRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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

    public static DimensionSymbolVector fromJson(JsonObject json) {
        return new DimensionSymbolVector(
                readSymbol(json, "life"),
                readSymbol(json, "form"),
                readSymbol(json, "flame"),
                readSymbol(json, "void"),
                readSymbol(json, "soul"),
                readSymbol(json, "phase")
        ).clamp();
    }

    private static int readSymbol(JsonObject json, String key) {
        if (!json.has(key)) {
            return 0;
        }
        if (!json.get(key).isJsonPrimitive() || !json.get(key).getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("象征值必须是数字：" + key);
        }
        return json.get(key).getAsInt();
    }

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

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("life", life);
        json.addProperty("form", form);
        json.addProperty("flame", flame);
        json.addProperty("void", voidAffinity);
        json.addProperty("soul", soul);
        json.addProperty("phase", phase);
        return json;
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
