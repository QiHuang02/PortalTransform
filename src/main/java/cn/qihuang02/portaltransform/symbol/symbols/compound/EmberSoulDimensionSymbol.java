package cn.qihuang02.portaltransform.symbol.symbols.compound;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.symbol.symbols.SymbolTier;

/**
 * 余烬之魂复合象征（三阶）：烈焰 + 灵魂 + 余烬。
 */
public final class EmberSoulDimensionSymbol extends CompoundDimensionSymbol {
    public EmberSoulDimensionSymbol() {
        super(
                PortalTransform.getRL("ember_soul"),
                0xC084FC,
                parents(weighted("flame", 2), weighted("soul", 3), weighted("cinder", 1)),
                SymbolTier.THIRD.tier(),
                null
        );
    }
}
