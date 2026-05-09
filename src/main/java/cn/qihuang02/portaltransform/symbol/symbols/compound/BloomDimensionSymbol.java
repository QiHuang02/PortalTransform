package cn.qihuang02.portaltransform.symbol.symbols.compound;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.symbol.symbols.SymbolTier;

/**
 * 绽放复合象征（二阶）：生命 + 形体。
 */
public final class BloomDimensionSymbol extends CompoundDimensionSymbol {
    public BloomDimensionSymbol() {
        super(
                PortalTransform.getRL("bloom"),
                0x7ED957,
                parents(weighted("life", 3), weighted("form", 9)),
                SymbolTier.SECOND.tier(),
                PortalTransform.getRL("entropy")
        );
    }
}
