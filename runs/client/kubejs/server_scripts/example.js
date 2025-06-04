ServerEvents.recipes((event) => {
    event.recipes.portaltransform.item_transform(
        "minecraft:cobblestone",
        "minecraft:prismarine",
        [
                 Byproduct.of("minecraft:redstone", 0.8, 1, 4)
        ],
        ["minecraft:overworld", "minecraft:the_nether"],
        "rain",
        1
    );
})