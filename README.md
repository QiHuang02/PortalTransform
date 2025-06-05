# PortalTransform

---

**PortalTransform** 是一个 Minecraft Neoforge 模组，它允许物品在通过传送门时根据特定条件发生转换，并可能产生副产物。
这个模组旨在为整合包制作者和服务器管理员提供更灵活的物品转换机制。

### 核心特性

* **传送门物品转换**: 当物品实体（掉落物形式）通过传送门时，它可以转变为另一种物品。
* **条件化转换**:
    * **维度限制**: 可以指定转换发生的特定起始维度和目标维度。 例如，只有当物品从主世界进入下界时才发生转换。如果未指定维度，则表示无维度要求。
    * **天气条件**: 可以将转换限制在特定的天气条件下，如晴天、下雨或雷暴。 默认为任意天气。
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
    // 基础示例：将圆石在从主世界进入下界传送门时，有75%概率转换为海晶石，
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
        // 6. transform_chance (Float) - 可选, 默认为 1.0F
        // 因此，如果你想指定转换概率而不指定维度和天气，你需要为维度和天气提供其“空”或“任意”的表示。
        // 例如，使用空数组 `[]` 代表任意维度，使用 `"any"` 代表任意天气。
        [],       // 任意维度
        "any",    // 任意天气
        0.5       // 50% 转换概率
    );
});
```

