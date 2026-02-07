package cn.qihuang02.portaltransform.compat.kubejs.components;

import cn.qihuang02.portaltransform.recipe.ItemTransform.Weather;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.JsonIO;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.util.HideFromJS;

@HideFromJS
public class WeatherComponent implements RecipeComponent<Weather> {
    public static final String TYPE_ID = "portaltransform:weather_condition";
    public static final WeatherComponent INSTANCE = new WeatherComponent();

    private WeatherComponent() {
    }

    @Override
    public String componentType() {
        return TYPE_ID;
    }

    @Override
    public Class<?> componentClass() {
        return Weather.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, Weather value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(value.getSerializedName());
    }

    @Override
    public Weather read(RecipeJS recipe, Object from) {
        if (from == null || from instanceof Undefined || from == Scriptable.NOT_FOUND) {
            return null;
        }

        if (from instanceof Weather weather) {
            return weather;
        }

        JsonElement element = JsonIO.of(from);
        if (element == null || element.isJsonNull()) {
            return null;
        }

        String value = element.getAsString();
        Weather weather = Weather.fromName(value);
        if (weather != null) {
            return weather;
        }

        throw new IllegalArgumentException("无效天气条件: " + value + "，可用值为 any/clear/rain/thunder");
    }
}
