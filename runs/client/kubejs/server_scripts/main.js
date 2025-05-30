ServerEvents.recipes((event) => {
    event.recipes.portaltransform.item_transform(
        "minecraft:cobblestone",
        "minecraft:prismarine",
        [
                 Byproduct.of("minecraft:redstone", 0.8, 1, 4)
        ],
        [Level.OVERWORLD, Level.THE_NETHER],
        "any",
        0.1
    );
})