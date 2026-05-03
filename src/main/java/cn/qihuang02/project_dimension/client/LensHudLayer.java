package cn.qihuang02.project_dimension.client;

import cn.qihuang02.project_dimension.ProjectDimension;
import cn.qihuang02.project_dimension.register.item.ModItems;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolManager;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;
import cn.qihuang02.project_dimension.symbol.descriptor.SymbolDescriptor;
import cn.qihuang02.project_dimension.symbol.resolver.SymbolContextFactory;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class LensHudLayer {

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.is(ModItems.LENS.get())) return;

        var conn = mc.getConnection();
        if (conn == null) return;

        Registry<DimensionSymbolVector> registry = conn.registryAccess()
                .registryOrThrow(DimensionSymbolManager.DIMENSION_SYMBOL_REGISTRY_KEY);

        DimensionSymbolVector effective = SymbolContextFactory.computeLocal(
                registry, player.level(), player.blockPosition());

        List<Component> lines = SymbolDescriptor.describe(effective);
        if (lines.isEmpty()) {
            guiGraphics.drawString(mc.font, Component.translatable(ProjectDimension.MODID + ".lens.no_symbols"),
                    5, 5, 0xFFAAAAAA, true);
            return;
        }

        int y = 5;
        for (Component line : lines) {
            int color = line.getStyle().getColor() != null
                    ? line.getStyle().getColor().getValue()
                    : 0xFFFFFFFF;
            guiGraphics.drawString(mc.font, line, 5, y, color, true);
            y += 10;
        }
    }
}
