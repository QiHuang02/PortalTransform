package cn.qihuang02.portaltransform.compat.kubejs.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.JsonIO;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.Undefined;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.advancements.critereon.ItemPredicate;

@HideFromJS
public class ItemPredicateComponent implements RecipeComponent<ItemPredicate> {
    public static final String TYPE_ID = "portaltransform:item_predicate";
    public static final ItemPredicateComponent INSTANCE = new ItemPredicateComponent();

    private ItemPredicateComponent() {
    }

    @Override
    public String componentType() {
        return TYPE_ID;
    }

    @Override
    public Class<?> componentClass() {
        return ItemPredicate.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, ItemPredicate value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return value.serializeToJson();
    }

    @Override
    public ItemPredicate read(RecipeJS recipe, Object from) {
        if (from == null || from instanceof Undefined || from == Scriptable.NOT_FOUND) {
            return null;
        }

        if (from instanceof ItemPredicate predicate) {
            return predicate;
        }

        JsonElement element;
        if (from instanceof CharSequence sequence) {
            element = JsonParser.parseString(sequence.toString());
        } else {
            element = JsonIO.of(from);
        }

        return ItemPredicate.fromJson(element);
    }
}
