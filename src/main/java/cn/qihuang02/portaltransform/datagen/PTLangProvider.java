package cn.qihuang02.portaltransform.datagen;

import cn.qihuang02.portaltransform.PortalTransform;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * Generates localization files for PortalTransform.
 */
public class PTLangProvider extends LanguageProvider {
    private final String locale;

    public PTLangProvider(PackOutput output, String locale) {
        super(output, PortalTransform.MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        switch (locale) {
            case "zh_cn" -> {
                add("config.portaltransform.autoInsertIntoChests", "自动插入附近箱子");
                add("config.portaltransform.chestSearchRadius", "箱子搜索半径");
            }
            case "en_us" -> {
                add("config.portaltransform.autoInsertIntoChests", "Automatically insert items into nearby chests");
                add("config.portaltransform.chestSearchRadius", "Chest search radius");
            }
        }
    }
}
