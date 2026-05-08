package cn.qihuang02.project_dimension;

import cn.qihuang02.project_dimension.client.LensHudLayer;
import cn.qihuang02.project_dimension.register.item.ModItems;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolManager;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;
import cn.qihuang02.project_dimension.symbol.descriptor.SymbolDescriptor;
import cn.qihuang02.project_dimension.symbol.resolver.SymbolContextFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.jetbrains.annotations.NotNull;

@Mod(value = ProjectDimension.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ProjectDimension.MODID, value = Dist.CLIENT)
public class ProjectDimensionClient {
    private static final ResourceLocation LENS_SHADER =
            ResourceLocation.fromNamespaceAndPath(ProjectDimension.MODID, "shaders/post/lens.json");
    private static boolean shaderActive = false;

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        ProjectDimension.LOGGER.info("HELLO FROM CLIENT SETUP");
        ProjectDimension.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void onRegisterGuiLayers(@NotNull RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(ProjectDimension.MODID, "lens_hud"),
                LensHudLayer::render
        );
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) {
            shaderActive = false;
            return;
        }

        boolean wearing = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.LENS.get());
        PostChain currentEffect = mc.gameRenderer.currentEffect();
        boolean effectLoaded = currentEffect != null;

        // 退出世界、重进存档、切换视角等场景下，Minecraft 可能会把后处理链清掉。
        // 这里以真实 effect 状态为准，确保佩戴 lens 时能自动重新加载。
        if (wearing && !effectLoaded) {
            mc.gameRenderer.loadEffect(LENS_SHADER);
            currentEffect = mc.gameRenderer.currentEffect();
            effectLoaded = currentEffect != null;
        } else if (!wearing && effectLoaded) {
            mc.gameRenderer.shutdownEffect();
            currentEffect = null;
            effectLoaded = false;
        }

        shaderActive = wearing && effectLoaded;
        if (shaderActive) {
            updateShaderUniforms(mc);
        }
    }

    private static void updateShaderUniforms(@NotNull Minecraft mc) {
        PostChain effect = mc.gameRenderer.currentEffect();
        if (effect == null || effect.passes.isEmpty()) return;

        var conn = mc.getConnection();
        if (conn == null) return;
        Registry<DimensionSymbolVector> registry = conn.registryAccess()
                .registryOrThrow(DimensionSymbolManager.DIMENSION_SYMBOL_REGISTRY_KEY);
        DimensionSymbolVector effective = SymbolContextFactory.computeLocal(
                registry, mc.player.level(), mc.player.blockPosition());

        SymbolDescriptor.ShaderUniforms u = SymbolDescriptor.computeShaderUniforms(effective);
        float time = (float) (System.currentTimeMillis() % 1000000) / 1000.0f;

        var pass = effect.passes.getFirst();
        for (int i = 0; i < 6; i++) {
            int c = u.colors()[i];
            pass.getEffect().safeGetUniform("Color" + i).set(
                    ((c >> 16) & 0xFF) / 255.0f,
                    ((c >> 8) & 0xFF) / 255.0f,
                    (c & 0xFF) / 255.0f);
            pass.getEffect().safeGetUniform("Weight" + i).set(u.weights()[i]);
        }
        pass.getEffect().safeGetUniform("Time").set(time);
    }
}
