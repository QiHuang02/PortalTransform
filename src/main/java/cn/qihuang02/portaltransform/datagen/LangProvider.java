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

        this.add("tooltip.portaltransform.item_transform.dimension", "Dimension Requirement");
        this.add("tooltip.portaltransform.item_transform.unknown_dimension", "Unknown dimensions");
        this.add("tooltip.portaltransform.item_transform.no_requirement", "No requirement");

        this.add("tooltip.portaltransform.item_transform.byproduct", "Byproduct");
        this.add("tooltip.portaltransform.item_transform.byproduct.chance", "Chance: %s");
        this.add("tooltip.portaltransform.item_transform.byproduct.min_count", "Min Count: %s");
        this.add("tooltip.portaltransform.item_transform.byproduct.max_count", "Max Count: %s");

        this.add("tooltip.portaltransform.item_transform.transform_chance", "Transform Chance: %s");
    }
}
