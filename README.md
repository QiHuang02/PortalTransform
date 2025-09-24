# PortalTransform

---

**PortalTransform** is a Minecraft **Forge** mod for 1.20.1 that allows items to transform when they travel through a nether portal.  The transformation can be gated behind multiple world conditions and optionally consume Forge Energy from nearby blocks.  This repository contains the 1.20.1 backport which keeps the gameplay from the NeoForge version while running entirely on Forge's legacy APIs.

**PortalTransform** 是一个面向 Minecraft 1.20.1 的 **Forge** 模组，当物品实体穿过地狱门时会按照配方发生转换。转换过程可以绑定多种世界条件，并且在需要时会消耗附近方块的 Forge 能量。此仓库为 1.20.1 回移植版本，在保留原有玩法的同时，完全基于 Forge 旧版 API 实现。

## Version Information
- **Mod Version**: 0.6.8+1.20.1
- **Author**: QiHuang02
- **License**: MIT
- **Minecraft Version**: 1.20.1
- **Forge Version**: 47.4.5
- **Loader**: JavaFML (Forge)

> ℹ️  Optional integrations such as KubeJS and EMI are not available in this Forge backport yet.  Recipes must currently be provided through data packs.

## Features / 模组特性

- **Portal Item Transformation / 传送门物品转换** – item entities can turn into other items when crossing a portal.
- **Conditional Recipes / 条件化配方** – control transformations with:
  - current & target dimension requirements
  - weather checks
  - biome allow-lists
  - Y level restrictions
  - day / night / tick range checks
  - catalyst blocks placed around the portal
  - Forge Energy (FE) consumption from nearby block entities
  - item data predicates (NBT checks)
- **Byproduct System / 副产物系统** – define multiple outputs with individual probabilities and stack ranges.

## Creating Recipes / 配方编写

Recipes are defined through JSON data packs using the `portaltransform:item_transform` recipe type.  Place your recipe files under `data/<namespace>/portal_transform/item_transform/<path>.json` inside a data pack or `data` folder in a development world.

A minimal example:

```json
{
  "type": "portaltransform:item_transform",
  "ingredient": { "item": "minecraft:cobblestone" },
  "result": { "item": "minecraft:prismarine" },
  "transform_chance": 0.75
}
```

More complex recipes can specify byproducts, energy requirements, dimensions, catalysts, and other optional sections.  Refer to the examples in `src/main/resources/data` for guidance.

## Development Setup / 开发环境

- Install JDK 17 and run `./gradlew build` to compile the project.
- `./gradlew runClient` now uses the working directory `./run-1.20/client`, keeping it separate from any NeoForge environment that relies on `./runs`.
- Data generation is available through `./gradlew runData`, and outputs are written to `src/generated/resources`.

欢迎提出 Issue 或者 Pull Request 来帮助改进本项目。
