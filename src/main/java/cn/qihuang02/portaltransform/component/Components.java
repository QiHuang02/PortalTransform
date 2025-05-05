package cn.qihuang02.portaltransform.component;

import cn.qihuang02.portaltransform.PortalTransform;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class Components {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, PortalTransform.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> NO_PORTAL_TRANSFORM =
            register(builder -> builder.persistent(Codec.BOOL));

    private static <T> @NotNull DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
            UnaryOperator<DataComponentType.Builder<T>> builderOperator
    ) {
        return DATA_COMPONENT_TYPES.register("no_portal_transform", () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
