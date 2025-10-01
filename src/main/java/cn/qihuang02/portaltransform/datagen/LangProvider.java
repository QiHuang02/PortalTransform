package cn.qihuang02.portaltransform.datagen;

import cn.qihuang02.portaltransform.PortalTransform;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class LangProvider extends LanguageProvider {
    public LangProvider(PackOutput output) {
        super(output, PortalTransform.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {

        this.add("emi.category.portaltransform.item_transform", "Item Transform");

        this.add("tooltip.portaltransform.item_transform.no_requirement", "No requirement");
        this.add("tooltip.portaltransform.item_transform.dimensions", "Dimensions");
        this.add("tooltip.portaltransform.item_transform.biomes", "Biomes");

        this.add("tooltip.portaltransform.item_transform.height", "Height");
        this.add("tooltip.portaltransform.item_transform.height.equal", "Y = %s");
        this.add("tooltip.portaltransform.item_transform.height.min", "Y ≥ %s");
        this.add("tooltip.portaltransform.item_transform.height.max", "Y ≤ %s");
        this.add("tooltip.portaltransform.item_transform.height.range", "%s ≤ Y ≤ %s");
        this.add("tooltip.portaltransform.item_transform.time", "Time");
        this.add("tooltip.portaltransform.item_transform.time.any", "Any time");
        this.add("tooltip.portaltransform.item_transform.time.day", "Daytime");
        this.add("tooltip.portaltransform.item_transform.time.night", "Nighttime");
        this.add("tooltip.portaltransform.item_transform.time.noon", "High noon");
        this.add("tooltip.portaltransform.item_transform.time.midnight", "Midnight");
        this.add("tooltip.portaltransform.item_transform.time.range", "Between %s and %s ticks");
        this.add("tooltip.portaltransform.item_transform.catalyst", "Catalyst");
        this.add("tooltip.portaltransform.item_transform.catalyst.range", "Within %s horizontal and %s vertical blocks");
        this.add("tooltip.portaltransform.item_transform.energy", "Energy");
        this.add("tooltip.portaltransform.item_transform.energy.amount", "Consumes %s FE");
        this.add("tooltip.portaltransform.item_transform.energy.range", "Checks %s horizontal and %s vertical blocks");
        this.add("tooltip.portaltransform.item_transform.item_predicate", "Item Data");
        this.add("tooltip.portaltransform.item_transform.item_predicate.value", "Predicate: %s");

        this.add("tooltip.portaltransform.item_transform.byproduct", "Byproduct");
        this.add("tooltip.portaltransform.item_transform.byproduct.chance", "Chance: %s");
        this.add("tooltip.portaltransform.item_transform.byproduct.min_count", "Min Count: %s");
        this.add("tooltip.portaltransform.item_transform.byproduct.max_count", "Max Count: %s");

        this.add("tooltip.portaltransform.item_transform.transform_chance", "Transform Chance: %s");

        this.add("tooltip.portaltransform.item_transform.weather", "Weather");
        this.add("tooltip.portaltransform.item_transform.weather.clear", "Clear");
        this.add("tooltip.portaltransform.item_transform.weather.rain", "Rain");
        this.add("tooltip.portaltransform.item_transform.weather.thunder", "Thunder");
        this.add("tooltip.portaltransform.item_transform.weather.any", "Any");
    }
}
