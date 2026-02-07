package cn.qihuang02.portaltransform.compat.jei;

import cn.qihuang02.portaltransform.PortalTransform;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Biomes;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Byproducts;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Height;
import cn.qihuang02.portaltransform.recipe.ItemTransform.TimeCondition;
import cn.qihuang02.portaltransform.recipe.ItemTransform.Weather;
import cn.qihuang02.portaltransform.recipe.ItemTransformRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ItemTransformRecipeCategory implements IRecipeCategory<ItemTransformRecipe> {
    private static final ResourceLocation TEXTURE_GUI = PortalTransform.getRL("textures/gui/emi/gui.png");
    private static final ItemStack CLOCK_STACK = new ItemStack(Items.CLOCK);

    private static final int WIDTH = 185;
    private static final int HEIGHT = 125;
    private static final int SLOT_SIZE = 18;

    private static final int INPUT_X = 16;
    private static final int INPUT_Y = 55;
    private static final int OUTPUT_X = 108;
    private static final int OUTPUT_Y = 55;

    private static final int TIME_ICON_X = 15;
    private static final int TIME_ICON_Y = 25;
    private static final int WEATHER_ICON_X = 63;
    private static final int WEATHER_ICON_Y = 25;
    private static final int ICON_SIZE = 16;

    private static final int CONDITION_X = 57;
    private static final int CONDITION_Y = 46;
    private static final int CONDITION_WIDTH = 28;
    private static final int CONDITION_HEIGHT = 28;

    private static final int[][] BYPRODUCT_POSITIONS = {
            {108, 25}, {132, 31}, {152, 11},
            {138, 55}, {164, 39}, {164, 71},
            {108, 85}, {132, 79}, {152, 99}
    };

    private final IDrawable background;
    private final IDrawable icon;

    public ItemTransformRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE_GUI, 0, 0, WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.OBSIDIAN));
    }

    @Override
    public @NotNull mezz.jei.api.recipe.RecipeType<ItemTransformRecipe> getRecipeType() {
        return PortalTransformJeiPlugin.ITEM_TRANSFORM_RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.category.portaltransform.item_transform");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemTransformRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, INPUT_Y)
                .addIngredients(recipe.inputIngredient());

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
                .addItemStack(recipe.result());

        List<Byproducts> byproducts = recipe.getByproducts().orElse(List.of());
        int slotCount = Math.min(byproducts.size(), BYPRODUCT_POSITIONS.length);
        for (int i = 0; i < slotCount; i++) {
            int[] pos = BYPRODUCT_POSITIONS[i];
            builder.addSlot(RecipeIngredientRole.OUTPUT, pos[0], pos[1])
                    .addItemStack(byproducts.get(i).byproduct());
        }
    }

    @Override
    public void draw(ItemTransformRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        recipe.getTimeRequirement().ifPresent(time -> guiGraphics.renderItem(CLOCK_STACK, TIME_ICON_X, TIME_ICON_Y));

        recipe.getWeather()
                .filter(weather -> weather != Weather.ANY)
                .ifPresent(weather -> {
                    int u = switch (weather) {
                        case CLEAR -> 192;
                        case RAIN -> 208;
                        case THUNDER -> 224;
                        default -> -1;
                    };
                    if (u >= 0) {
                        guiGraphics.blit(TEXTURE_GUI, WEATHER_ICON_X, WEATHER_ICON_Y, u, 0, ICON_SIZE, ICON_SIZE, 256, 256);
                    }
                });
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(ItemTransformRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (isMouseOver(mouseX, mouseY, OUTPUT_X, OUTPUT_Y, SLOT_SIZE, SLOT_SIZE)) {
            Component chanceLine = getTransformChanceComponent(recipe);
            if (chanceLine != null) {
                return List.of(chanceLine);
            }
        }

        List<Byproducts> byproducts = recipe.getByproducts().orElse(List.of());
        int byproductCount = Math.min(byproducts.size(), BYPRODUCT_POSITIONS.length);
        for (int i = 0; i < byproductCount; i++) {
            int[] pos = BYPRODUCT_POSITIONS[i];
            if (isMouseOver(mouseX, mouseY, pos[0], pos[1], SLOT_SIZE, SLOT_SIZE)) {
                return getByproductChanceTooltip(byproducts.get(i));
            }
        }

        if (recipe.getTimeRequirement().isPresent() && isMouseOver(mouseX, mouseY, TIME_ICON_X, TIME_ICON_Y, ICON_SIZE, ICON_SIZE)) {
            return getClockTooltipLines(recipe.getTimeRequirement().get());
        }

        if (recipe.getWeather().isPresent() && isMouseOver(mouseX, mouseY, WEATHER_ICON_X, WEATHER_ICON_Y, ICON_SIZE, ICON_SIZE)) {
            return List.of(getWeatherComponent(recipe.getWeather()));
        }

        if (isMouseOver(mouseX, mouseY, CONDITION_X, CONDITION_Y, CONDITION_WIDTH, CONDITION_HEIGHT)) {
            return getConditionTooltipLines(recipe);
        }

        return Collections.emptyList();
    }

    private static boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static List<Component> getByproductChanceTooltip(Byproducts definition) {
        float chance = definition.chance();
        int minCount = definition.counts().min();
        int maxCount = definition.counts().max();

        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.portaltransform.item_transform.byproduct").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.portaltransform.item_transform.byproduct.chance", String.format("%.1f%%", chance * 100.0F)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.portaltransform.item_transform.byproduct.min_count", minCount).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.portaltransform.item_transform.byproduct.max_count", maxCount).withStyle(ChatFormatting.DARK_GRAY));
        return List.copyOf(tooltip);
    }

    private static List<Component> getConditionTooltipLines(ItemTransformRecipe recipe) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.portaltransform.item_transform.dimensions").withStyle(ChatFormatting.GOLD));
        lines.addAll(getDimensionTooltipLines(recipe));
        lines.add(Component.translatable("tooltip.portaltransform.item_transform.biomes").withStyle(ChatFormatting.DARK_GREEN));
        lines.addAll(getBiomeTooltipLines(recipe));
        lines.add(Component.translatable("tooltip.portaltransform.item_transform.height").withStyle(ChatFormatting.GRAY));
        lines.addAll(getHeightTooltipLines(recipe));
        lines.add(Component.translatable("tooltip.portaltransform.item_transform.item_predicate").withStyle(ChatFormatting.DARK_PURPLE));
        lines.addAll(getItemPredicateTooltipLines(recipe));
        return lines;
    }

    private static List<Component> getDimensionTooltipLines(ItemTransformRecipe recipe) {
        Component from = getDimensionComponent(recipe.getCurrent());
        Component to = getDimensionComponent(recipe.getTarget());
        Component line = from.copy()
                .append(Component.literal(" -> ").withStyle(ChatFormatting.GRAY))
                .append(to);
        return List.of(createRequirementLine(line));
    }

    private static Component getDimensionComponent(Optional<ResourceKey<Level>> dimensionKey) {
        return dimensionKey.<Component>map(key -> {
                    ResourceLocation loc = key.location();
                    String dimensionLangKey = "dimension." + loc.getNamespace() + "." + loc.getPath();
                    return I18n.exists(dimensionLangKey)
                            ? Component.translatable(dimensionLangKey).withStyle(ChatFormatting.GOLD)
                            : Component.literal(loc.toString()).withStyle(ChatFormatting.YELLOW);
                })
                .orElseGet(ItemTransformRecipeCategory::noRequirement);
    }

    private static List<Component> getBiomeTooltipLines(ItemTransformRecipe recipe) {
        Optional<Biomes> biomes = recipe.getBiomes();
        if (biomes.isEmpty()) {
            return List.of(createRequirementLine(noRequirement()));
        }
        List<Component> lines = biomes.get().biomes().stream()
                .map(ItemTransformRecipeCategory::getBiomeComponent)
                .map(ItemTransformRecipeCategory::createRequirementLine)
                .toList();
        return List.copyOf(lines);
    }

    private static Component getBiomeComponent(ResourceKey<Biome> biomeKey) {
        ResourceLocation loc = biomeKey.location();
        String biomeLangKey = "biome." + loc.getNamespace() + "." + loc.getPath();
        if (I18n.exists(biomeLangKey)) {
            return Component.translatable(biomeLangKey).withStyle(ChatFormatting.GREEN);
        }
        return Component.literal(loc.toString()).withStyle(ChatFormatting.YELLOW);
    }

    private static List<Component> getHeightTooltipLines(ItemTransformRecipe recipe) {
        Optional<Height> height = recipe.getHeightRequirement();
        if (height.isEmpty()) {
            return List.of(createRequirementLine(noRequirement()));
        }
        return List.of(createRequirementLine(getHeightComponent(height.get())));
    }

    private static Component getHeightComponent(Height requirement) {
        Optional<Integer> min = requirement.minY();
        Optional<Integer> max = requirement.maxY();
        if (min.isPresent() && max.isPresent()) {
            if (Objects.equals(min.get(), max.get())) {
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

    private static List<Component> getItemPredicateTooltipLines(ItemTransformRecipe recipe) {
        Optional<ItemPredicate> predicate = recipe.getItemDataPredicate();
        if (predicate.isEmpty()) {
            return List.of(createRequirementLine(noRequirement()));
        }
        return List.of(createRequirementLine(getItemPredicateComponent(predicate.get())));
    }

    private static Component getItemPredicateComponent(ItemPredicate predicate) {
        String display = GsonHelper.toStableString(predicate.serializeToJson());
        return Component.translatable("tooltip.portaltransform.item_transform.item_predicate.value", display)
                .withStyle(ChatFormatting.AQUA);
    }

    private static List<Component> getClockTooltipLines(TimeCondition condition) {
        return List.of(
                Component.translatable(
                                "tooltip.portaltransform.item_transform.time",
                                condition.startTick(),
                                condition.endTick()
                        )
                        .withStyle(ChatFormatting.AQUA)
        );
    }

    private static Component getWeatherComponent(Optional<Weather> weather) {
        Weather actualWeather = weather.orElse(Weather.ANY);
        String weatherName = actualWeather.getSerializedName();
        return Component.translatable("tooltip.portaltransform.item_transform.weather")
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("tooltip.portaltransform.item_transform.weather." + weatherName).withStyle(ChatFormatting.AQUA));
    }

    private static Component getTransformChanceComponent(ItemTransformRecipe recipe) {
        float chance = recipe.transformChance();
        if (chance >= 1.0F) {
            return null;
        }
        return Component.translatable(
                "tooltip.portaltransform.item_transform.transform_chance",
                String.format("%.1f%%", chance * 100.0F)
        ).withStyle(ChatFormatting.YELLOW);
    }

    private static Component createRequirementLine(Component value) {
        return Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)
                .append(value.copy());
    }

    private static Component noRequirement() {
        return Component.translatable("tooltip.portaltransform.item_transform.no_requirement")
                .withStyle(ChatFormatting.GREEN);
    }
}
