package cn.qihuang02.portaltransform.compat.kubejs.event;

import cn.qihuang02.portaltransform.event.PortalItemTransformedEvent;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * KubeJS event definitions for PortalTransform.
 */
public interface PortalTransformKubeEvents {
    EventGroup GROUP = EventGroup.of("PortalTransformEvents");
    EventHandler ITEM_TRANSFORMED = GROUP.server("itemTransformed", () -> PortalItemTransformedKubeEvent.class);

    static void postItemTransformed(@NotNull PortalItemTransformedEvent event) {
        ITEM_TRANSFORMED.post(new PortalItemTransformedKubeEvent(event));
    }
}
