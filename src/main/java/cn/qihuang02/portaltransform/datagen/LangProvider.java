package cn.qihuang02.portaltransform.datagen;

import cn.qihuang02.portaltransform.PortalTransform;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.List;

public class LangProvider extends LanguageProvider {
    private static final List<Entry> ENTRIES = List.of(
            entry("jei.category.portaltransform.item_transform", "Portal Item Transform", "传送门转化"),
            entry("emi.category.portaltransform.item_transform", "Item Transform", "物品转化"),

            entry("tooltip.portaltransform.item_transform.no_requirement", "No requirement", "无"),
            entry("tooltip.portaltransform.item_transform.dimensions", "Dimensions", "维度"),
            entry("tooltip.portaltransform.item_transform.biomes", "Biomes", "生物群系"),

            entry("tooltip.portaltransform.item_transform.height", "Height", "高度"),
            entry("tooltip.portaltransform.item_transform.height.equal", "Y = %s", "Y = %s"),
            entry("tooltip.portaltransform.item_transform.height.min", "Y ≥ %s", "Y ≥ %s"),
            entry("tooltip.portaltransform.item_transform.height.max", "Y ≤ %s", "Y ≤ %s"),
            entry("tooltip.portaltransform.item_transform.height.range", "%s ≤ Y ≤ %s", "%s ≤ Y ≤ %s"),
            entry("tooltip.portaltransform.item_transform.time", "Time", "时间"),
            entry("tooltip.portaltransform.item_transform.time.any", "Any time", "任意时间"),
            entry("tooltip.portaltransform.item_transform.time.day", "Daytime", "白天"),
            entry("tooltip.portaltransform.item_transform.time.night", "Nighttime", "夜晚"),
            entry("tooltip.portaltransform.item_transform.time.range", "Between %s and %s ticks", "介于 %s 到 %s 刻"),
            entry("tooltip.portaltransform.item_transform.catalyst", "Catalyst", "催化剂"),
            entry("tooltip.portaltransform.item_transform.catalyst.range", "Within %s horizontal and %s vertical blocks", "范围: 水平 %s 格, 垂直 %s 格"),
            entry("tooltip.portaltransform.item_transform.energy", "Energy", "能量"),
            entry("tooltip.portaltransform.item_transform.energy.amount", "Consumes %s FE", "消耗 %s FE"),
            entry("tooltip.portaltransform.item_transform.energy.range", "Checks %s horizontal and %s vertical blocks", "检测范围: 水平 %s 格, 垂直 %s 格"),
            entry("tooltip.portaltransform.item_transform.item_predicate", "Item Data", "物品数据"),
            entry("tooltip.portaltransform.item_transform.item_predicate.value", "Predicate: %s", "条件: %s"),

            entry("tooltip.portaltransform.item_transform.byproduct", "Byproduct", "副产物"),
            entry("tooltip.portaltransform.item_transform.byproduct.chance", "Chance: %s", "概率: %s"),
            entry("tooltip.portaltransform.item_transform.byproduct.min_count", "Min Count: %s", "最小数量: %s"),
            entry("tooltip.portaltransform.item_transform.byproduct.max_count", "Max Count: %s", "最大数量: %s"),

            entry("tooltip.portaltransform.item_transform.transform_chance", "Transform Chance: %s", "转换概率: %s"),

            entry("tooltip.portaltransform.item_transform.weather", "Weather", "天气"),
            entry("tooltip.portaltransform.item_transform.weather.clear", "Clear", "晴天"),
            entry("tooltip.portaltransform.item_transform.weather.rain", "Rain", "雨天"),
            entry("tooltip.portaltransform.item_transform.weather.thunder", "Thunder", "雷暴雨"),
            entry("tooltip.portaltransform.item_transform.weather.any", "Any", "任意")
    );

    private final String locale;

    public LangProvider(PackOutput output, String locale) {
        super(output, PortalTransform.MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        boolean zhCn = "zh_cn".equals(locale);
        for (Entry entry : ENTRIES) {
            add(entry.key(), zhCn ? entry.zhCn() : entry.enUs());
        }
    }

    private static Entry entry(String key, String enUs, String zhCn) {
        return new Entry(key, enUs, zhCn);
    }

    private record Entry(String key, String enUs, String zhCn) {}
}
