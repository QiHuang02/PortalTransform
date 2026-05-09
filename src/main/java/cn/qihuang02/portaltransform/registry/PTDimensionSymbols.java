package cn.qihuang02.portaltransform.registry;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.symbol.symbols.IDimensionSymbol;
import cn.qihuang02.portaltransform.symbol.symbols.base.*;
import cn.qihuang02.portaltransform.symbol.symbols.compound.BloomDimensionSymbol;
import cn.qihuang02.portaltransform.symbol.symbols.compound.CinderDimensionSymbol;
import cn.qihuang02.portaltransform.symbol.symbols.compound.EmberSoulDimensionSymbol;
import cn.qihuang02.portaltransform.symbol.symbols.compound.EntropyDimensionSymbol;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 维度象征内容注册入口。
 */
public final class PTDimensionSymbols {
    public static final DeferredRegister<IDimensionSymbol> DIMENSION_SYMBOLS =
            DeferredRegister.create(PTRegistries.DIMENSION_SYMBOL, PortalTransform.MODID);

    public static final Registry<IDimensionSymbol> REGISTRY =
            DIMENSION_SYMBOLS.makeRegistry(PTRegistries::configureDimensionSymbolRegistry);

    public static final Supplier<Registry<IDimensionSymbol>> REGISTRY_SUPPLIER =
            DIMENSION_SYMBOLS.getRegistry();

    // ===== 基础象征 =====
    public static final DeferredHolder<IDimensionSymbol, IDimensionSymbol> LIFE =
            DIMENSION_SYMBOLS.register("life", LifeDimensionSymbol::new);
    public static final DeferredHolder<IDimensionSymbol, IDimensionSymbol> VOID =
            DIMENSION_SYMBOLS.register("void", VoidDimensionSymbol::new);
    public static final DeferredHolder<IDimensionSymbol, IDimensionSymbol> FLAME =
            DIMENSION_SYMBOLS.register("flame", FlameDimensionSymbol::new);
    public static final DeferredHolder<IDimensionSymbol, IDimensionSymbol> PHASE =
            DIMENSION_SYMBOLS.register("phase", PhaseDimensionSymbol::new);
    public static final DeferredHolder<IDimensionSymbol, IDimensionSymbol> FORM =
            DIMENSION_SYMBOLS.register("form", FormDimensionSymbol::new);
    public static final DeferredHolder<IDimensionSymbol, IDimensionSymbol> SOUL =
            DIMENSION_SYMBOLS.register("soul", SoulDimensionSymbol::new);

    // ===== 复合象征 =====
    public static final DeferredHolder<IDimensionSymbol, IDimensionSymbol> BLOOM =
            DIMENSION_SYMBOLS.register("bloom", BloomDimensionSymbol::new);
    public static final DeferredHolder<IDimensionSymbol, IDimensionSymbol> CINDER =
            DIMENSION_SYMBOLS.register("cinder", CinderDimensionSymbol::new);
    public static final DeferredHolder<IDimensionSymbol, IDimensionSymbol> ENTROPY =
            DIMENSION_SYMBOLS.register("entropy", EntropyDimensionSymbol::new);
    public static final DeferredHolder<IDimensionSymbol, IDimensionSymbol> EMBER_SOUL =
            DIMENSION_SYMBOLS.register("ember_soul", EmberSoulDimensionSymbol::new);

    public static void register(IEventBus eventBus) {
        DIMENSION_SYMBOLS.register(eventBus);
    }
}
