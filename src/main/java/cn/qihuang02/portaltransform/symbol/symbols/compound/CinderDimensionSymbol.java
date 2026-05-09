package cn.qihuang02.portaltransform.symbol.symbols.compound;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.symbol.symbols.SymbolTier;

/**
 * 余烬复合象征（二阶）：烈焰 + 虚空。
 */
public final class CinderDimensionSymbol extends CompoundDimensionSymbol {
    public CinderDimensionSymbol() {
        super(
                PortalTransform.getRL("cinder"),
                0xFB923C,
                parents(weighted("flame", 4), weighted("void", 2)),
                SymbolTier.SECOND.tier(),
                PortalTransform.getRL("bloom")
        );
    }
}
