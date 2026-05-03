package cn.qihuang02.project_dimension.datagen;

import cn.qihuang02.project_dimension.ProjectDimension;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.List;
import java.util.Map;

public class ModLangProvider extends LanguageProvider {
    private static final Map<String, String> ZH_NAMES = Map.of(
            "life", "生", "form", "形", "flame", "焰",
            "void", "虚", "soul", "魂", "phase", "相"
    );

    private static final List<String> TIER_KEYS = List.of(
            "neg.extreme", "neg.high", "neg.moderate", "neg.low",
            "dormant",
            "low", "moderate", "high", "extreme"
    );

    private static final String[] ZH_TIERS = {"逆转", "倾覆", "压制", "黯淡", "沉寂", "微弱", "显现", "强烈", "主导"};
    private static final String[] EN_TIERS = {"Inverted", "Overwhelmed", "Oppressed", "Dimmed", "Dormant", "Faint", "Present", "Strong", "Dominant"};

    private final String locale;

    public ModLangProvider(PackOutput output, String locale) {
        super(output, ProjectDimension.MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        boolean zh = "zh_cn".equals(locale);

        add("item.project_dimension.lens", zh ? "位面透镜" : "Planar Lens");
        add("project_dimension.lens.no_symbols", zh ? "未检测到显著的象征" : "No significant symbols detected");

        for (var entry : ZH_NAMES.entrySet()) {
            String k = entry.getKey();
            String enName = k.substring(0, 1).toUpperCase() + k.substring(1);
            add("symbol." + k, zh ? entry.getValue() : enName);
        }

        String[] tiers = zh ? ZH_TIERS : EN_TIERS;
        for (int i = 0; i < TIER_KEYS.size(); i++) {
            add("symbol.tier." + TIER_KEYS.get(i), tiers[i]);
        }
    }
}
