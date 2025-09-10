package cn.qihuang02.portaltransform.recipe.itemtransform;

import org.jetbrains.annotations.Nullable;

/**
 * Simple enum representing weather conditions a recipe may require.
 */
public enum Weather {
    ANY("any"),
    CLEAR("clear"),
    RAIN("rain"),
    THUNDER("thunder");

    private final String name;

    Weather(String name) {
        this.name = name;
    }

    public String getSerializedName() {
        return name;
    }

    @Nullable
    public static Weather fromName(String name) {
        for (Weather w : values()) {
            if (w.name.equalsIgnoreCase(name)) {
                return w;
            }
        }
        return null;
    }
}
