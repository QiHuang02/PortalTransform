package cn.qihuang02.portaltransform.compat.emi;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Biomes;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Catalyst;
import cn.qihuang02.portaltransform.recipe.ItemTransform.EnergyRequirement;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Height;
import cn.qihuang02.portaltransform.recipe.ItemTransform.TimeCondition;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Weather;
import cn.qihuang02.portaltransform.recipe.ItemTransformRecipe;
import com.mojang.serialization.JsonOps;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.GsonHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.joml.Matrix4f;

import java.util.*;

public class EmiRecipe implements dev.emi.emi.api.recipe.EmiRecipe {
    private static final ResourceLocation TEXTURE_GUI = PortalTransform.getRL("textures/gui/emi/gui.png");
    private static final ItemStack CLOCK_STACK = new ItemStack(Items.CLOCK);

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

        recipe.getTimeRequirement().ifPresent(timeCondition -> {
            widgets.addDrawable(15, 25, 16, 16, (graphics, mouseX, mouseY, delta) ->
                    graphics.renderItem(CLOCK_STACK, 0, 0)
            );
            widgets.addTooltipText(getClockTooltipLines(timeCondition), 15, 25, 16, 16);
        });

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

        widgets.addTooltipText(getConditionTooltipLines(), 57, 46, 28, 28);

        widgets.addSlot(input, 15, 54).drawBack(false).recipeContext(this);
        widgets.addSlot(output, 107, 54).drawBack(false).recipeContext(this).appendTooltip(() -> {
            Component chance = getTransformChanceComponent();
            return chance != null ? createMultiLineTooltip(List.of(chance)) : createMultiLineTooltip(Collections.emptyList());
        });

        addByproductsSlots(widgets);
    }

    private @NotNull Component getDimensionComponent(@NotNull Optional<ResourceKey<Level>> dimensionKey) {
        return dimensionKey.<Component>map(key -> {
                    ResourceLocation loc = key.location();
                    String dimensionLangKey = "dimension." + loc.getNamespace() + "." + loc.getPath();
                    return I18n.exists(dimensionLangKey)
                            ? Component.translatable(dimensionLangKey).withStyle(ChatFormatting.GOLD)
                            : Component.literal(loc.toString()).withStyle(ChatFormatting.YELLOW);
                })
                .orElse(getNoRequirementComponent());
    }

    private @NotNull @Unmodifiable List<Component> getDimensionTooltipLines() {
        Component from = getDimensionComponent(recipe.getCurrent());
        Component to = getDimensionComponent(recipe.getTarget());

        Component line = from.copy()
                .append(Component.literal(" -> ").withStyle(ChatFormatting.GRAY))
                .append(to);

        return List.of(createRequirementLine(line));
    }

    private @NotNull @Unmodifiable List<Component> getBiomeTooltipLines() {
        Optional<Biomes> biomes = recipe.getBiomes();
        List<Component> lines = new ArrayList<>();

        if (biomes.isEmpty()) {
            lines.add(createRequirementLine(getNoRequirementComponent()));
        } else {
            for (ResourceKey<Biome> biomeKey : biomes.get().biomes()) {
                lines.add(createRequirementLine(getBiomeComponent(biomeKey)));
            }
        }

        return List.copyOf(lines);
    }

    private @NotNull Component getBiomeComponent(@NotNull ResourceKey<Biome> biomeKey) {
        ResourceLocation loc = biomeKey.location();
        String biomeLangKey = "biome." + loc.getNamespace() + "." + loc.getPath();
        if (I18n.exists(biomeLangKey)) {
            return Component.translatable(biomeLangKey).withStyle(ChatFormatting.GREEN);
        }
        return Component.literal(loc.toString()).withStyle(ChatFormatting.YELLOW);
    }

    private @NotNull @Unmodifiable List<Component> getHeightTooltipLines() {
        return recipe.getHeightRequirement()
                .map(requirement -> List.of(createRequirementLine(getHeightComponent(requirement))))
                .orElse(List.of(createRequirementLine(getNoRequirementComponent())));
    }

    private @NotNull Component getHeightComponent(@NotNull Height requirement) {
        Optional<Integer> min = requirement.minY();
        Optional<Integer> max = requirement.maxY();

        if (min.isPresent() && max.isPresent()) {
            if (min.get().equals(max.get())) {
                return Component.translatable("tooltip.portaltransform.item_transform.height.equal", min.get())
                        .withStyle(ChatFormatting.AQUA);
            }
            return Component.translatable("tooltip.portaltransform.item_transform.height.range", min.get(), max.get())
                    .withStyle(ChatFormatting.AQUA);
        }

        if (min.isPresent()) {
            return Component.translatable("tooltip.portaltransform.item_transform.height.min", min.get())
                    .withStyle(ChatFormatting.AQUA);
        }

        return Component.translatable("tooltip.portaltransform.item_transform.height.max", max.orElse(0))
                .withStyle(ChatFormatting.AQUA);
    }

    private @NotNull @Unmodifiable List<Component> getClockTooltipLines(@NotNull TimeCondition condition) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.portaltransform.item_transform.time").withStyle(ChatFormatting.AQUA));
        lines.add(createRequirementLine(getTimeComponent(condition)));
        return List.copyOf(lines);
    }

    private @NotNull Component getTimeComponent(@NotNull TimeCondition condition) {
        if (condition.coversWholeDay()) {
            return Component.translatable("tooltip.portaltransform.item_transform.time.any")
                    .withStyle(ChatFormatting.GREEN);
        }

        return condition.keyword()
                .map(keyword -> switch (keyword) {
                    case DAY -> Component.translatable("tooltip.portaltransform.item_transform.time.day")
                            .withStyle(ChatFormatting.GOLD);
                    case NIGHT -> Component.translatable("tooltip.portaltransform.item_transform.time.night")
                            .withStyle(ChatFormatting.BLUE);
                    case NOON -> Component.translatable("tooltip.portaltransform.item_transform.time.noon")
                            .withStyle(ChatFormatting.YELLOW);
                    case MIDNIGHT -> Component.translatable("tooltip.portaltransform.item_transform.time.midnight")
                            .withStyle(ChatFormatting.DARK_BLUE);
                })
                .orElse(Component.translatable("tooltip.portaltransform.item_transform.time.range",
                                formatTick(condition.startTick()),
                                formatTick(condition.endTick()))
                        .withStyle(ChatFormatting.AQUA));
    }

    private String formatTick(int tick) {
        return Integer.toString(tick);
    }

    private @NotNull @Unmodifiable List<Component> getItemPredicateTooltipLines() {
        return recipe.getItemDataPredicate()
                .map(predicate -> List.of(createRequirementLine(getItemPredicateComponent(predicate))))
                .orElse(List.of(createRequirementLine(getNoRequirementComponent())));
    }

    private @NotNull Component getItemPredicateComponent(@NotNull ItemPredicate predicate) {
        String display = getItemPredicateDisplay(predicate);
        return Component.translatable("tooltip.portaltransform.item_transform.item_predicate.value", display)
                .withStyle(ChatFormatting.AQUA);
    }

    private @NotNull String getItemPredicateDisplay(@NotNull ItemPredicate predicate) {
        return ItemPredicate.CODEC.encodeStart(JsonOps.INSTANCE, predicate)
                .map(GsonHelper::toStableString)
                .result()
                .orElseGet(predicate::toString);
    }

    private @NotNull Component getNoRequirementComponent() {
        return Component.translatable("tooltip.portaltransform.item_transform.no_requirement")
                .withStyle(ChatFormatting.GREEN);
    }

    private @NotNull Component createRequirementLine(@NotNull Component value) {
        return Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)
                .append(value.copy());
    }

    private @NotNull @Unmodifiable List<Component> getConditionTooltipLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.portaltransform.item_transform.dimensions").withStyle(ChatFormatting.GOLD));
        lines.addAll(getDimensionTooltipLines());
        lines.add(Component.translatable("tooltip.portaltransform.item_transform.biomes").withStyle(ChatFormatting.DARK_GREEN));
        lines.addAll(getBiomeTooltipLines());
        lines.add(Component.translatable("tooltip.portaltransform.item_transform.height").withStyle(ChatFormatting.GRAY));
        lines.addAll(getHeightTooltipLines());
        lines.add(Component.translatable("tooltip.portaltransform.item_transform.catalyst").withStyle(ChatFormatting.LIGHT_PURPLE));
        lines.addAll(getCatalystTooltipLines());
        lines.add(Component.translatable("tooltip.portaltransform.item_transform.energy").withStyle(ChatFormatting.RED));
        lines.addAll(getEnergyTooltipLines());
        lines.add(Component.translatable("tooltip.portaltransform.item_transform.item_predicate").withStyle(ChatFormatting.DARK_PURPLE));
        lines.addAll(getItemPredicateTooltipLines());
        return List.copyOf(lines);
    }

    private @NotNull @Unmodifiable List<Component> getCatalystTooltipLines() {
        return recipe.getCatalystRequirement()
                .map(this::createCatalystLines)
                .orElse(List.of(createRequirementLine(getNoRequirementComponent())));
    }

    private @NotNull @Unmodifiable List<Component> createCatalystLines(@NotNull Catalyst catalyst) {
        List<Component> lines = new ArrayList<>();
        for (ResourceKey<Block> blockKey : catalyst.blocks()) {
            lines.add(createRequirementLine(getBlockComponent(blockKey)));
        }
        lines.add(createRequirementLine(getCatalystRangeComponent(catalyst)));
        return List.copyOf(lines);
    }

    private @NotNull Component getBlockComponent(@NotNull ResourceKey<Block> blockKey) {
        ResourceLocation loc = blockKey.location();
        String blockLangKey = "block." + loc.getNamespace() + "." + loc.getPath();
        if (I18n.exists(blockLangKey)) {
            return Component.translatable(blockLangKey).withStyle(ChatFormatting.YELLOW);
        }
        return Component.literal(loc.toString()).withStyle(ChatFormatting.YELLOW);
    }

    private @NotNull Component getCatalystRangeComponent(@NotNull Catalyst catalyst) {
        return Component.translatable("tooltip.portaltransform.item_transform.catalyst.range",
                        catalyst.horizontalRange(), catalyst.verticalRange())
                .withStyle(ChatFormatting.GRAY);
    }

    private @NotNull @Unmodifiable List<Component> getEnergyTooltipLines() {
        return recipe.getEnergyRequirement()
                .map(requirement -> List.of(
                        createRequirementLine(getEnergyAmountComponent(requirement)),
                        createRequirementLine(getEnergyRangeComponent(requirement))
                ))
                .orElse(List.of(createRequirementLine(getNoRequirementComponent())));
    }

    private @NotNull Component getEnergyAmountComponent(@NotNull EnergyRequirement requirement) {
        return Component.translatable("tooltip.portaltransform.item_transform.energy.amount", requirement.amount())
                .withStyle(ChatFormatting.GOLD);
    }

    private @NotNull Component getEnergyRangeComponent(@NotNull EnergyRequirement requirement) {
        return Component.translatable("tooltip.portaltransform.item_transform.energy.range",
                        requirement.horizontalRange(), requirement.verticalRange())
                .withStyle(ChatFormatting.GRAY);
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
