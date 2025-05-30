package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Weather;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.type.TypeInfo;

public class WeatherComponent implements RecipeComponent<Weather> {
    public static final WeatherComponent WEATHER = new WeatherComponent();
    public static final String COMPONENT_NAME = "portaltransform:weather_condition";

    @Override
    public Codec<Weather> codec() {
        return Weather.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(Weather.class);
    }

    @Override
    public String toString() {
        return COMPONENT_NAME;
    }

    @Override
    public Weather wrap(Context cx, KubeRecipe recipe, Object from) {
        switch (from) {
            case null -> {
                return null;
            }
            case Undefined ignored -> {
                return null;
            }
            case Weather weather -> {
                return weather;
            }
            case String s -> {
                Weather weather = Weather.fromName(s);
                if (weather != null) {
                    return weather;
                } else {
                    throw ScriptRuntime.typeError(cx, "Invalid weather condition string: '" + s + "'. Must be one of: any, clear, rain, thunder.");
                }
            }
            default -> {
            }
        }
        throw ScriptRuntime.typeError(cx, "Expected a conditions object, null, or undefined for 'conditions' field, but got " + from);
    }
}
