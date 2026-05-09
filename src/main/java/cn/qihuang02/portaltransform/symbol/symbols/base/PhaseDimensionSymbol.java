package cn.qihuang02.portaltransform.symbol.symbols.base;

import cn.qihuang02.portaltransform.PortalTransform;

/**
 * 相位基础象征。
 */
public final class PhaseDimensionSymbol extends BaseDimensionSymbol {
    public PhaseDimensionSymbol() {
        super(PortalTransform.getRL("phase"), 0x60A5FA, PortalTransform.getRL("flame"));
    }
}
