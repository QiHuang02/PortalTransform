package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Weather;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;

@HideFromJS
public class WeatherComponent implements RecipeComponent<Weather> {
    public static final RecipeComponentType<Weather> WEATHER = RecipeComponentType.unit(PortalTransform.getRL("weather_condition"), WeatherComponent::new);
    public static final String COMPONENT_NAME = "portaltransform:weather_condition";

    private final RecipeComponentType<?> type;

    private WeatherComponent(RecipeComponentType<?> type) {
        this.type = type;
    }

    @Override
    public RecipeComponentType<?> type() {
        return type;
    }

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
        return type.toString();
    }

    @Override
    public Weather wrap(RecipeScriptContext cx, Object from) {
        var context = cx.cx();

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
                    throw ScriptRuntime.typeError(context, "Invalid weather condition string: '" + s + "'. Must be one of: any, clear, rain, thunder.");
                }
            }
            default -> {
            }
        }
        throw ScriptRuntime.typeError(context, "Expected a conditions object, null, or undefined for 'conditions' field, but got " + from);
    }
}
