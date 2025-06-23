package cn.qihuang02.portaltransform.compat.emi;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Weather;
import cn.qihuang02.portaltransform.recipe.ItemTransformRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.joml.Matrix4f;

import java.util.*;

public class EmiRecipe implements dev.emi.emi.api.recipe.EmiRecipe {
    private static final ResourceLocation TEXTURE_GUI = PortalTransform.getRL("textures/gui/emi/gui.png");

    private final RecipeHolder<ItemTransformRecipe> recipeHolder;
    private final ItemTransformRecipe recipe;
    private final EmiIngredient input;
    private final List<EmiStack> byproducts;
    private final EmiStack output;

    public EmiRecipe(@NotNull RecipeHolder<ItemTransformRecipe> holder) {
        this.recipeHolder = holder;
        this.recipe = holder.value();
        this.input = EmiIngredient.of(recipe.inputIngredient());

        HolderLookup.Provider registries = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : null;
        this.output = registries != null ? EmiStack.of(recipe.getResultItem(registries)) : EmiStack.EMPTY;

        this.byproducts = recipe.byproducts()
                .map(byproducts -> byproducts.stream()
                        .map(def -> EmiStack.of(def.byproduct()))
                        .filter(stack -> !stack.isEmpty())
                        .toList())
                .orElse(List.of());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EmiClientPlugin.ITEM_TRANSFORMATION_CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipeHolder.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> outputs = new ArrayList<>();
        outputs.add(output);

        outputs.addAll(byproducts.stream()
                .limit(9)
                .toList());

        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 185;
    }

    @Override
    public int getDisplayHeight() {
        return 125;
    }

    @Override
    public void addWidgets(@NotNull WidgetHolder widgets) {
        widgets.addTexture(
                TEXTURE_GUI,
                0, 0,
                185, 125,
                0, 0,
                185, 125,
                256, 256
        );

        recipe.getWeather().ifPresent(weather -> {
            int u = 0;
            boolean shouldDrow = true;
            switch (weather) {
                case CLEAR -> u = 192;
                case RAIN -> u = 208;
                case THUNDER -> u = 224;
                default -> shouldDrow = false;
            }

            if (shouldDrow) {
                Component weatherLine = getWeatherComponent(recipe.getWeather());
                widgets.addTexture(TEXTURE_GUI, 63, 25, 16, 16, u, 0, 16, 16, 256, 256)
                        .tooltipText(weatherLine != null ? List.of(weatherLine) : Collections.emptyList());
            }
        });

        widgets.addTooltipText(getDimensionTooltipLines(), 57, 46, 28, 28);

        widgets.addSlot(input, 15, 54).drawBack(false).recipeContext(this);
        widgets.addSlot(output, 107, 54).drawBack(false).recipeContext(this).appendTooltip(() -> {
            Component chance = getTransformChanceComponent();
            return chance != null ? createMultiLineTooltip(List.of(chance)) : createMultiLineTooltip(Collections.emptyList());
        });

        addByproductsSlots(widgets);
    }

    private @NotNull Component getDimensionComponent(@NotNull Optional<ResourceKey<Level>> dimensionKey) {
        return dimensionKey.map(key -> {
                    ResourceLocation loc = key.location();
                    String dimensionLangKey = "dimension." + loc.getNamespace() + "." + loc.getPath();
                    return I18n.exists(dimensionLangKey)
                            ? Component.translatable(dimensionLangKey).withStyle(ChatFormatting.GOLD)
                            : Component.literal(loc.toString()).withStyle(ChatFormatting.YELLOW);
                })
                .orElse(Component.translatable("tooltip.portaltransform.item_transform.no_requirement") // "无要求"
                        .withStyle(ChatFormatting.GREEN));
    }

    private @NotNull @Unmodifiable List<Component> getDimensionTooltipLines() {
        Component from = getDimensionComponent(recipe.getCurrent());
        Component to = getDimensionComponent(recipe.getTarget());

        Component line = from.copy()
                .append(Component.literal(" -> ").withStyle(ChatFormatting.GRAY))
                .append(to);

        return List.of(line);
    }

    private @NotNull Component getWeatherComponent(@NotNull Optional<Weather> weather) {
        Weather actualWeather = weather.orElse(Weather.ANY);
        String weatherName = actualWeather.getSerializedName();
        String langKey = "tooltip.portaltransform.item_transform.weather." + weatherName;

        return Component.translatable("tooltip.portaltransform.item_transform.weather")
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable(langKey).withStyle(ChatFormatting.AQUA));
    }

    @Nullable
    private Component getTransformChanceComponent() {
        float chance = recipe.transformChance();
        if (chance >= 1.0f) {
            return null;
        }
        return Component.translatable(
                "tooltip.portaltransform.item_transform.transform_chance",
                String.format("%.1f%%", chance * 100)
        ).withStyle(ChatFormatting.YELLOW);
    }

    private void addByproductsSlots(WidgetHolder widgets) {
        int[][] slotPositions = {
                {107, 24}, {131, 30}, {151, 10},
                {137, 54}, {163, 38}, {163, 70},
                {107, 84}, {131, 78}, {151, 98}
        };

        int slotCount = Math.min(byproducts.size(), slotPositions.length);

        for (int i = 0; i < slotCount; i++) {
            final int currentIndex = i;
            int[] currentPos = slotPositions[currentIndex];

            widgets.addSlot(byproducts.get(currentIndex), currentPos[0], currentPos[1])
                    .drawBack(false)
                    .appendTooltip(() ->
                            createMultiLineTooltip(getByproductChanceTooltip(currentIndex))
                    );
        }
    }

    private List<Component> getByproductChanceTooltip(int index) {
        return recipe.byproducts()
                .filter(byproducts -> index < byproducts.size())
                .map(byproducts -> {
                    Byproducts definition = byproducts.get(index);
                    float chance = definition.chance();
                    int minCount = definition.counts().min();
                    int maxCount = definition.counts().max();
                    List<Component> tooltipLines = new ArrayList<>();
                    tooltipLines.add(Component.translatable("tooltip.portaltransform.item_transform.byproduct").withStyle(ChatFormatting.DARK_PURPLE));
                    tooltipLines.add(Component.translatable("tooltip.portaltransform.item_transform.byproduct.chance", String.format("%.1f%%", chance * 100)).withStyle(ChatFormatting.GRAY));
                    tooltipLines.add(Component.translatable("tooltip.portaltransform.item_transform.byproduct.min_count", minCount).withStyle(ChatFormatting.DARK_GRAY));
                    tooltipLines.add(Component.translatable("tooltip.portaltransform.item_transform.byproduct.max_count", maxCount).withStyle(ChatFormatting.DARK_GRAY));

                    return tooltipLines;
                })
                .orElse(Collections.singletonList(Component.literal("Error: Invalid byproduct index").withStyle(ChatFormatting.RED)));
    }

    private ClientTooltipComponent createMultiLineTooltip(@NotNull List<Component> components) {
        List<ClientTooltipComponent> lines = components.stream()
                .filter(Objects::nonNull)
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();

        if (lines.size() <= 1) {
            return lines.stream().findFirst().orElseGet(() -> ClientTooltipComponent.create(Component.empty().getVisualOrderText()));
        }

        return new ClientTooltipComponent() {
            private static final int LINE_SPACING = 2;

            @Override
            public int getHeight() {
                if (lines.isEmpty()) return 0;
                int totalTextHeight = lines.stream().mapToInt(ClientTooltipComponent::getHeight).sum();
                int spacing = Math.max(0, lines.size() - 1) * LINE_SPACING;
                return totalTextHeight + spacing;
            }

            @Override
            public int getWidth(@NotNull Font font) {
                return lines.stream().mapToInt(c -> c.getWidth(font)).max().orElse(0);
            }

            @Override
            public void renderText(@NotNull Font font, int x, int y, @NotNull Matrix4f matrix, MultiBufferSource.@NotNull BufferSource bufferSource) {
                int currentY = y;
                for (ClientTooltipComponent line : lines) {
                    line.renderText(font, x, currentY, matrix, bufferSource);
                    currentY += line.getHeight() + LINE_SPACING;
                }
            }
        };
    }
}
