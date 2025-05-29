// Visit the wiki for more info - https://kubejs.com/
console.info('Hello, World! (Loaded server example script)')

ServerEvents.recipes((event) => {
    event.recipes.portaltransform.item_transform(
        "minecraft:cobblestone",
        "minecraft:prismarine",
        0.1,
        "overworld",
        "the_nether",
        [
            Byproduct.of("minecraft:redstone", 0.8, 1, 4)
        ]
    )
    // event.recipes.portaltransform.item_transfor(
    //     'minecraft:cobblestone',
    //     'minecraft:prismarine',
    //     0.1,
    //     'overworld',
    //     'the_nether',
    //     [
    //         Byproduct.of('minecraft:redstone', 0.8, 1, 4)
    //     ]
    // );
    // event.custom({
    //     "type": "portaltransform:item_transform",
    //     "input": {
    //         "item": "minecraft:cobblestone"
    //     },
    //     "result": {
    //         "item": "minecraft:netherrack"
    //     },
    //     "chance": 0.8,
    //     /**
    //      * @typedef {byproducts[]} 副产物数组是一个可选的属性，用于指定副产物。
    //      */
    //     "byproducts": [
    //         {
    //             "byproduct": {
    //                 "item": "minecraft:glowstone_dust"
    //             },
    //             "chance": 0.5,
    //             "counts": {
    //                 "min": 1,
    //                 "max": 3
    //             }
    //         }
    //     ],
    //     /**
    //      * @typedef {Object} conditions 用于限定配方在哪些维度、时间、生物群系下可以发生转换。
    //      * @param {string[]} [dimension] 用于限定配方发生的维度，默认为`minecraft:overworld`到`minecraft:the_nether`。
    //      *                                  固定为两个字符串的数组（会预先转换为对应 ResourceKey 进行判断），
    //      *                                  其中第一项固定为物品出发的维度，第二项固定为物品将要去的维度。
    //      * @param {string[]} [time] 用于限定配方发生的时间，默认为不进行匹配，即任意时间都匹配。
    //      * @property {string[]} [biome] 用于限定配方发生的生物群系，默认为不进行匹配，即任意生物群系都匹配。
    //      * @property {string[]} [weather] 用于限定配方发生的天气，默认为不进行匹配，即任意天气都匹配。
    //      */
    //     "conditions": {
    //         "dimension": ["minecraft:overworld", "minecraft:the_nether"],
    //         "time": 90,
    //         "biome": ["minecraft:plains"],
    //         "weather": ["clear"]
    //     }
    // })
})