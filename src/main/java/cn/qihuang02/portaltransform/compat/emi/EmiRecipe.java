package cn.qihuang02.portaltransform.compat.emi;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.itemTransformation.Byproducts;
import cn.qihuang02.portaltransform.recipe.itemTransformation.ItemTransformationRecipe;
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
import org.joml.Matrix4f;

import java.util.*;

public class EmiRecipe implements dev.emi.emi.api.recipe.EmiRecipe {
    public static final ResourceLocation TEXTURE_TRANSFORM = PortalTransform.getRL("textures/gui/emi/transform.png");
    public static final ResourceLocation TEXTURE_BIGSLOT = PortalTransform.getRL("textures/gui/emi/big_slot.png");
    private static final int BYPRODUCT_GRID_X = 34;
    private static final int BYPRODUCT_GRID_Y = 60;
    private static final int GRID_CELL_SIZE = 18;

    private final RecipeHolder<ItemTransformationRecipe> recipeHolder;
    private final ItemTransformationRecipe recipe;
    private final EmiIngredient input;
    private final List<EmiStack> byproductsForDisplay;
    private final EmiStack output;

    public EmiRecipe(@NotNull RecipeHolder<ItemTransformationRecipe> holder) {
        this.recipeHolder = holder;
        this.recipe = holder.value();
        this.input = EmiIngredient.of(recipe.inputIngredient());

        HolderLookup.Provider registries = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : null;
        this.output = registries != null ? EmiStack.of(recipe.getResultItem(registries)) : EmiStack.EMPTY;

        this.byproductsForDisplay = recipe.byproducts()
                .map(byproducts -> byproducts.stream()
                        .map(def -> EmiStack.of(def.byproduct()))
                        .filter(stack -> !stack.isEmpty())
                        .toList())
                .orElse(List.of());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return FTEmiClientPlugin.ITEM_TRANSFORMATION_CATEGORY;
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

        outputs.addAll(byproductsForDisplay.stream()
                .limit(9)
                .toList());

        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 120;
    }

    @Override
    public int getDisplayHeight() {
        return 120;
    }

    @Override
    public void addWidgets(@NotNull WidgetHolder widgets) {
        int centerX = (getDisplayWidth() / 2) - 8;

        widgets.addSlot(input, centerX, 2).appendTooltip(() -> createCombinedTooltip(recipe.currentDimension(), true));

        widgets.addTexture(
                TEXTURE_TRANSFORM,
                centerX + 2, 22,
                13, 16,
                0, 0,
                13, 16,
                13, 16
        );
        widgets.addSlot(output, centerX, 40).recipeContext(this).appendTooltip(() -> createCombinedTooltip(recipe.targetDimension(), false));
        widgets.addTexture(
                TEXTURE_BIGSLOT,
                34, 60,
                54, 54,
                0, 0,
                54, 54,
                54, 54
        );

        addByproductsSlots(widgets);
    }

    private ClientTooltipComponent createCombinedTooltip(Optional<ResourceKey<Level>> dimensionKey, boolean isInput) {
        List<Component> lines = new ArrayList<>();

        // Dimension Info
        lines.add(getDimensionComponent(dimensionKey));

        // Transform Chance Info (only for input)
        if (isInput) {
            Component chanceComponent = getTransformChanceComponent();
            if (chanceComponent != null) {
                lines.add(chanceComponent);
            }
        }

        return createMultiLineTooltip(lines);
    }

    private Component getDimensionComponent(Optional<ResourceKey<Level>> dimensionKey) {
        // (Implementation unchanged)
        return Component.translatable("tooltip.portaltransform.item_transformation.dimension")
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(
                        dimensionKey.map(key -> {
                                    ResourceLocation loc = key.location();
                                    String dimensionLangKey = "dimension." + loc.getNamespace() + "." + loc.getPath();
                                    return I18n.exists(dimensionLangKey)
                                            ? Component.translatable(dimensionLangKey).withStyle(ChatFormatting.GOLD)
                                            : Component.literal(loc.toString()).withStyle(ChatFormatting.YELLOW);
                                })
                                .orElse(Component.translatable("tooltip.portaltransform.item_transformation.no_requirement")
                                        .withStyle(ChatFormatting.GREEN))
                );
    }

    @Nullable
    private Component getTransformChanceComponent() {
        float chance = recipe.transformChance();
        if (chance >= 1.0f) {
            return null;
        }
        return Component.translatable(
                "tooltip.portaltransform.item_transformation.transform_chance",
                String.format("%.1f%%", chance * 100)
        ).withStyle(ChatFormatting.YELLOW);
    }

    private void addByproductsSlots(WidgetHolder widgets) {
        int index = 0;
        for (EmiStack byproduct : byproductsForDisplay) {
            if (index >= 9) break;
            int[] pos = getGridPosition(index);

            int finalIndex = index;
            widgets.addSlot(byproduct, pos[0], pos[1])
                    .drawBack(false)
                    .appendTooltip(() ->
                            // Use the renamed method here
                            createMultiLineTooltip(getByproductChanceTooltip(finalIndex))
                    );
            index++;
        }
    }

    private List<Component> getByproductChanceTooltip(int index) {
        // (Implementation unchanged)
        return recipe.byproducts()
                .filter(byproducts -> index < byproducts.size())
                .map(byproducts -> {
                    Byproducts definition = byproducts.get(index);
                    float chance = definition.chance();
                    int minCount = definition.counts().min();
                    int maxCount = definition.counts().max();
                    List<Component> tooltipLines = new ArrayList<>();
                    tooltipLines.add(Component.translatable("tooltip.portaltransform.item_transformation.byproduct").withStyle(ChatFormatting.DARK_PURPLE));
                    tooltipLines.add(Component.translatable("tooltip.portaltransform.item_transformation.byproduct.chance", String.format("%.1f%%", chance * 100)).withStyle(ChatFormatting.GRAY));
                    tooltipLines.add(Component.translatable("tooltip.portaltransform.item_transformation.byproduct.min_count", minCount).withStyle(ChatFormatting.DARK_GRAY));
                    tooltipLines.add(Component.translatable("tooltip.portaltransform.item_transformation.byproduct.max_count", maxCount).withStyle(ChatFormatting.DARK_GRAY));

                    return tooltipLines;
                })
                .orElse(Collections.singletonList(Component.literal("Error: Invalid byproduct index").withStyle(ChatFormatting.RED)));
    }

    private ClientTooltipComponent createMultiLineTooltip(List<Component> components) {
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

    private int[] getGridPosition(int index) {
        int row = index / 3;
        int col = index % 3;

        return new int[]{
                BYPRODUCT_GRID_X + col * (GRID_CELL_SIZE),
                BYPRODUCT_GRID_Y + row * (GRID_CELL_SIZE),
        };
    }

    private List<Component> getChanceTooltip(int index) {
        return recipe.byproducts()
                .filter(byproducts -> index < byproducts.size())
                .map(byproducts -> {
                    float chance = byproducts.get(index).chance();
                    int minCount = byproducts.get(index).counts().min();
                    int maxCount = byproducts.get(index).counts().max();
                    return List.<Component>of(
                            Component.translatable("tooltip.portaltransform.item_transformation.byproduct").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.portaltransform.item_transformation.byproduct.chance", chance * 100).withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.portaltransform.item_transformation.byproduct.min_count", minCount).withStyle(ChatFormatting.DARK_GRAY),
                            Component.translatable("tooltip.portaltransform.item_transformation.byproduct.max_count", maxCount).withStyle(ChatFormatting.DARK_GRAY)
                    );
                })
                .orElse(List.of());
    }
}
