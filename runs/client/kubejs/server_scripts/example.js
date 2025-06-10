ServerEvents.recipes((event) => {
    const { item_transform } = event.recipes.portaltransform;

    item_transform("minecraft:cobblestone", "minecraft:prismarine")
    .chance(0.8)
    .dimensions(["overworld", "the_nether"])
    .byproducts([Byproduct.of("minecraft:redstone", 0.8, 1, 4), Byproduct.of('minecraft:lapis_lazuli', 0.9, 1, 4)])
    .weather("rain");

    item_transform(
        "minecraft:nether_star", 
        "minecraft:netherite_ingot",
        [
            Byproduct.of("minecraft:gold_ingot", 0.22, 1, 1)
        ]
    ).chance(1.0);

    item_transform("minecraft:dirt", "minecraft:stone")
        .byproducts([Byproduct.of("minecraft:sand", 0.25, 1, 1)])
        .chance(0.5);
})