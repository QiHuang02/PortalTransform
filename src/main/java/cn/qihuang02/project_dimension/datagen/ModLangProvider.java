package cn.qihuang02.project_dimension.datagen;

import cn.qihuang02.project_dimension.ProjectDimension;
import cn.qihuang02.project_dimension.api.IDimensionSymbol;
import cn.qihuang02.project_dimension.register.DimensionSymbolRegistry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.List;
import java.util.Map;

public class ModLangProvider extends LanguageProvider {
    private static final Map<String, String> ZH_NAMES = Map.of(
            "life", "生", "form", "形", "flame", "焰",
            "void", "虚", "soul", "魂", "phase", "相"
    );

    private static final List<String> TIERS = List.of("low", "moderate", "high", "extreme");
    private static final List<String> NEG_TIERS = List.of("neg.low", "neg.moderate", "neg.high", "neg.extreme");

    private static final String[] EN_POS = {"Faint", "Present", "Strong", "Dominant"};
    private static final String[] EN_NEG = {"Dimmed", "Oppressed", "Overwhelmed", "Inverted"};
    private static final String[] ZH_POS = {"微弱", "显现", "强烈", "主导"};
    private static final String[] ZH_NEG = {"黯淡", "压制", "倾覆", "逆转"};

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

        for (IDimensionSymbol symbol : DimensionSymbolRegistry.builtins()) {
            if (symbol.compound()) continue;
            String k = symbol.key();
            add(k + ".name", zh ? ZH_NAMES.getOrDefault(k, symbol.displayName()) : symbol.displayName());

            String[] pos = zh ? ZH_POS : EN_POS;
            String[] neg = zh ? ZH_NEG : EN_NEG;

            add(k + ".desc.dormant", zh ? "沉寂" : "Dormant");

            for (int i = 0; i < TIERS.size(); i++) {
                add(k + ".desc." + TIERS.get(i), pos[i]);
                add(k + ".desc." + NEG_TIERS.get(i), neg[i]);
            }
        }
    }
}
