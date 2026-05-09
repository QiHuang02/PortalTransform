package cn.qihuang02.portaltransform.symbol.symbols.compound;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.symbol.symbols.SymbolTier;

/**
 * 熵复合象征（二阶）：虚空 + 相位。
 */
public final class EntropyDimensionSymbol extends CompoundDimensionSymbol {
    public EntropyDimensionSymbol() {
        super(
                PortalTransform.getRL("entropy"),
                0x64748B,
                parents(weighted("void", 5), weighted("phase", 3)),
                SymbolTier.SECOND.tier(),
                PortalTransform.getRL("bloom")
        );
    }
}
