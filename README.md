# PortalTransform

---

**PortalTransform** is a Minecraft NeoForge mod that allows items to transform when passing through portals based on specific conditions and can produce byproducts. This mod aims to provide more flexible item transformation mechanisms for modpack creators and server administrators.

**PortalTransform** 是一个 Minecraft NeoForge 模组，它允许物品在通过传送门时根据特定条件发生转换，并可能产生副产物。这个模组旨在为整合包制作者和服务器管理员提供更灵活的物品转换机制。

## Version Information
- **Mod Version**: 0.6.7+1.21.1
- **Author**: QiHuang02
- **License**: MIT
- **Minecraft Version**: 1.21.1
- **NeoForge Version**: 21.1.176

### 核心特性

* **传送门物品转换**: 当物品实体（掉落物形式）通过传送门时，它可以转变为另一种物品。
* **条件化转换**:
    * **维度限制**: 可以指定转换发生的特定起始维度和目标维度。 例如，只有当物品从主世界进入下界时才发生转换。如果未指定维度，则表示无维度要求。
    * **天气条件**: 可以将转换限制在特定的天气条件下，如晴天、下雨或雷暴。 默认为任意天气。
    * **生物群系条件**: 可以要求传送门所在位置处于特定生物群系列表，例如只允许繁茂洞穴中的传送门触发转换。
    * **高度条件**: 可要求传送门入口位于某个 Y 坐标以上、以下或特定高度范围内。
    * **时间条件**: 支持限定在白天、夜晚或指定的时间刻范围内触发配方。
    * **催化剂条件**: 需要在传送门附近放置指定方块作为“催化剂”，默认在水平 3 格、垂直 1 格范围内搜索，可在配方中自定义范围与多个方块。
    * **能量需求**: 可要求在传送门附近的方块/方块实体中拥有足够的 FE（或兼容能量）并在转换成功时消耗指定数值，范围同样支持自定义。
    * **物品数据条件**: 通过 `ItemPredicate` 描述输入物品所需的 NBT/数据组件，例如附魔、命名或耐久。
    * **转换概率**: 可以为每次转换设置一个成功概率（0.0 到 1.0 之间）。 如果转换失败（且概率小于1.0），物品将会消失。 默认为 100% 成功率。
* **副产物系统**: 物品转换成功时，可以配置产生多种副产物。
    * 每种副产物都有其独立的出现概率。
    * 每种副产物都可以设置一个产出数量范围（最小值和最大值）。
* **KubeJS 高度集成**: 你可以使用 JavaScript 轻松添加、删除或修改物品的转换配方。
* **EMI 配方查看器支持**: 所有通过 KubeJS 或者数据包添加的转换配方都可以在 EMI 物品管理器中清晰地查看到。

### 安装需求

1.  **Minecraft Neoforge**: 确保你已经安装了与模组版本兼容的 Neoforge。 (模组基于 Minecraft 1.21.1 和 Neoforge 21.1.169+ 构建)
2.  **KubeJS (可选但推荐)**: 为了定义和管理转换配方，强烈建议安装 KubeJS。 (模组兼容 KubeJS 版本 2101.7.1-build.181+)
3.  **EMI (可选但推荐)**: 为了在游戏中方便地查看配方，建议安装 EMI。 (模组兼容 EMI 版本 1.1.21+)

### 如何通过 KubeJS 添加配方

你可以通过 KubeJS 的服务端脚本来添加自定义的物品转换配方。脚本文件通常位于你的整合包或存档的 `kubejs/server_scripts/` 目录下 (例如 `kubejs/server_scripts/portal_transform_recipes.js`)。

下面是一个 KubeJS 脚本的示例，展示了如何定义物品转换:

```javascript
ServerEvents.recipes(event => {
    // 基础示例：将圆石在从主世界进入下界的传送门中（入口位于繁茂洞穴）时，有75%概率转换为海晶石，
    // 并有80%概率掉落1-4个红石，无特定天气要求。
    event.recipes.portaltransform.item_transform(
        "minecraft:cobblestone",                     // 输入物品的ID (必需)
        "minecraft:prismarine",                      // 输出物品的ID (必需)
        [                                            // 副产物列表 (可选, 可以是空数组 [])
            Byproduct.of("minecraft:redstone", 0.8, 1, 4) // 参数: 物品ID, 出现概率 (0.0-1.0), 最小数量, 最大数量
            // 你可以添加更多 Byproduct.of(...) 来定义多种副产物
        ],
        ["minecraft:overworld", "minecraft:the_nether"], // 维度条件 (可选): [当前维度ID, 目标维度ID]
                                                        // 如果是空数组 [] 或省略此参数，则表示无特定维度要求。
        "any",                                       // 天气条件 (可选): "any", "clear", "rain", "thunder"
                                                        // 如果省略此参数，默认为 "any"。
        ["minecraft:lush_caves"],                    // 生物群系条件 (可选): 入口所在的必需生物群系列表
                                                        // 如果省略此参数，表示无特定生物群系要求。
        { min: -16 },                                 // 高度条件 (可选): 允许的最低 Y 值, 也可以提供 { max: 80 } 或 { min: 0, max: 64 }
        "night",                                     // 时间条件 (可选): 传入关键字字符串 ("day", "night", "noon", "midnight")
                                                      // 或者使用数组指定范围，例如 [13000, 23000]
        {                                             // 催化剂条件 (可选): 需要在范围内存在的方块/方块列表
            blocks: ["minecraft:beacon"],            // 可以是单个方块 ID 或数组
            horizontal_range: 2,                      // 水平范围 (默认 3)
            vertical_range: 1                         // 垂直范围 (默认 1)
        },
        {                                             // 能量需求 (可选): 转换时消耗的能量数值及搜索范围
            amount: 100000,                           // 每次成功转换需要消耗的 FE 数量
            horizontal_range: 4,                      // 搜索水平范围 (默认 2)
            vertical_range: 2                         // 搜索垂直范围 (默认 1)
        },
        { components: { "minecraft:enchantments": { "minecraft:fire_aspect": 1 } } }, // 物品数据条件 (可选): ItemPredicate JSON 对象
        0.8                                          // 转换成功概率 (可选, 0.0-1.0)
                                                        // 如果省略此参数，默认为 1.0 (100%)。
    );

    // 沙子在任何维度之间转换时，若天气为晴朗，则100%转换为玻璃，无副产物。
    event.recipes.portaltransform.item_transform(
        "minecraft:sand",
        "minecraft:glass",
        [],                                          // 无副产物
        [],                                          // 无特定维度求 (注要意：KubeJS中定义时，如果JSON中是可选且有默认值，这里需要显式提供一个符合类型的值，如空数组代表无要求)
        "clear",                                     // 天气条件：晴天
        null,                                        // 生物群系条件：null 或 undefined 表示无要求
        1.0                                          // 转换概率 (1.0可以省略，因为是默认值)
    );

    // 示例3：泥土在任何条件下（任何维度、任何天气）通过传送门时，有50%概率变成石头，25%概率变成沙子（作为副产物）。
    event.recipes.portaltransform.item_transform(
        "minecraft:dirt",
        "minecraft:stone",
        [
            Byproduct.of("minecraft:sand", 0.25, 1, 1) // 25%概率掉落1个沙子
        ],
        /// 如果维度、天气、概率都使用默认值，可以省略这些参数，但KubeJS的API调用可能需要按顺序提供或使用更明确的构建器（如果支持）。
        // 根据当前API，如果想省略中间参数，可能需要提供null或者默认值。
        // 查阅 `PortalItemTransformRecipeSchema.java` 和 KubeJS 插件实现，参数定义如下：
        // 1. input (Ingredient) - 必需
        // 2. result (ItemStack) - 必需
        // 3. byproducts (List<Byproducts>) - 可选, 默认为空列表
        // 4. dimensions (Dimensions) - 可选, 默认为 Dimensions.empty() (即无要求)
        // 5. weather (Weather) - 可选, 默认为 Weather.ANY
        // 6. biomes (Biomes) - 可选, 默认为无要求
        // 7. height (Height) - 可选, 默认为无要求
        //    使用 `.height([min, max])` 来设置高度范围，可通过 `null` 省略某一端
        // 8. time (TimeCondition) - 可选, 默认为无要求
        //    使用 `.time("day")`、`.time("night")`、`.time("noon")`、`.time("midnight")` 或 `.time([start, end])`
        // 9. catalyst (Catalyst) - 可选, 默认为无要求 (在范围内检测指定方块)
        // 10. energy (EnergyRequirement) - 可选, 默认为无要求 (检测并消耗能量)
        // 11. item_predicate (ItemPredicate) - 可选, 默认为无要求
        // 12. transform_chance (Float) - 可选, 默认为 1.0F
        // 因此，如果你想指定转换概率而不指定维度和天气，你需要为维度和天气提供其“空”或“任意”的表示。
        // 例如，使用空数组 `[]` 代表任意维度，使用 `"any"` 代表任意天气，使用 null 代表任意生物群系。
        [],       // 任意维度
        "any",    // 任意天气
        null,     // 任意生物群系
        0.5       // 50% 转换概率
    );
});
```

### 数据包示例：通过 JSON 配方添加物品转换

如果你更喜欢使用数据包而不是 KubeJS，可以在 `data/<命名空间>/recipes/` 中放置一个 JSON 文件来定义传送门物品转换配方。基本操作步骤为：

1. 在你的数据包中创建 `data/<命名空间>/recipes/` 目录。
2. 在该目录下新建一个 `.json` 文件，并填写符合 `portaltransform:item_transform` 规范的内容。
3. 通过 `/reload` 或重启世界加载数据包更新。

下面提供一个完整的示例：

```
data/your_pack/recipes/lapis_to_diamond.json
```

```jsonc
{
  "type": "portaltransform:item_transform",
  "input": {
    "item": "minecraft:lapis_lazuli"
  },
  "result": {
    "item": "minecraft:diamond"
  },
  "byproducts": [
    {
      "item": "minecraft:experience_bottle",
      "chance": 0.35,
      "min_count": 1,
      "max_count": 2
    }
  ],
  "dimensions": {
    "current": "minecraft:overworld",
    "target": "minecraft:the_nether"
  },
  "weather": "clear",
  "biomes": ["minecraft:basalt_deltas", "minecraft:soul_sand_valley"],
  "height": {
    "min": 10,
    "max": 80
  },
  "time": "midnight", // 使用字符串关键字。若需自定义时间段，改为 [6000, 12000]
  "catalyst": {
    "blocks": ["minecraft:crying_obsidian"],
    "horizontal_range": 2,
    "vertical_range": 1
  },
  "energy": {
    "amount": 25000,
    "horizontal_range": 2,
    "vertical_range": 1
  },
  "item_predicate": {
    "components": {
      "minecraft:enchantments": {
        "minecraft:fortune": 1
      }
    }
  },
  "transform_chance": 0.6
}
```

> **提示**
>
> * 所有时间条件都需要写成字符串关键字 (`"day"`、`"night"`、`"noon"`、`"midnight"`) 或包含两个整数的数组 (`[start, end]`)。
> * 如果想表示任意时间，请完全省略 `time` 字段。
> * 数据包和 KubeJS 使用相同的底层校验逻辑，因此你在 JSON 中填写的值也会受到 0-23999 的时间范围约束。
