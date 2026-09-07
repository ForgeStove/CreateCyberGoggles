![Logo](https://cdn.modrinth.com/data/cached_images/9f1d22babad387de4381b095c41a0a1713be25da.png)

[![CurseForge](https://img.shields.io/curseforge/dt/1233804?logo=curseforge&label=&suffix=%20&style=flat&color=242629&labelColor=e04e14&logoColor=1c1c1c)](https://www.curseforge.com/minecraft/mc-mods/create-cyber-goggles)
[![Modrinth](https://img.shields.io/modrinth/dt/create-cyber-goggles?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c)](https://modrinth.com/mod/create-cyber-goggles)
[![License](https://img.shields.io/github/license/ForgeStove/CreateCyberGoggles?style=flat&color=900c3f)](https://github.com/ForgeStove/CreateCyberGoggles?tab=readme-ov-file#MIT-1-ov-file)
[![Crowdin](https://badges.crowdin.net/create-cyber-goggles/localized.svg)](https://crowdin.com/project/create-cyber-goggles)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ForgeStove/CreateCyberGoggles)

---

## 概述 / Overview

**Create: Cyber Goggles** 是[**机械动力**](https://modrinth.com/mod/create)的客户端附属模组，为其提供了模块化的辅助功能。

**Create: Cyber Goggles** is a client-side mod for [**Create**](https://modrinth.com/mod/create), providing modular assistance features.

---

## 功能 / Features

本模组的功能都能在配置里单独开关，下面按配置的分类列出主要内容。

**护目镜**：

- 高级信息显示：显示更完整的转速、应力、流速信息
- 精确数值：显示更多小数位，最大分数位数可配置
- 隐藏静态旋转信息：只保留仍在变化的内容
- 更好的工厂仪表：允许以更多方式连接、允许任意移动工厂仪表
- 更好的商店信息：优化桌布商店的信息显示与操作
- 渲染目标方块的旋转粒子

**工具提示**：

- 对以下会显示额外的内容：
- 容器、流体容器、末影箱、工具箱、剪贴板、地图、背罐、潜水靴、扳手、控制器、列表过滤器、属性过滤器
- 包裹、置物板、桌面（桌布）、置物台、机械手、石磨、粉碎控制器、红石请求器、工厂仪表

**叠加层**：

- 物品信息叠加层：在方块上渲染物品信息，可以切换主题与颜色
- 图解视图：对整个画面做像素化、描边处理，可以配置像素缩放与线条颜色

**边框渲染**：

- 渲染模拟盒与连接线，可以配置颜色与延时渲染时长

**航空学**：

- 始终显示方块质量、摩擦力
- 受力覆盖层：为跟随的物理结构显示重力、升力、阻力、悬浮力、气球升力、推进力、磁力与质心标记，附带显示质量与力大小的 HUD 面板

**杂项**：

- 扳手、锁链传动轮增强
- 机械动力风格堆叠数字、蓝图名称修复、递归扫描蓝图、无限编辑框长度
- 仓储请求与红石请求器的快捷操作、JEI 配方转移、预览过滤器、显示应力网络信息
- 一些稳定性修复（如 NBT 过长导致的崩溃）

---

**English**

All features can be toggled individually in the config.

**Goggles**:

- Advanced info: fuller rotation speed, stress and flow info
- Precise numbers: more decimal places, configurable
- Hide static rotation info: only show what still changes
- Better factory gauge: connect / move factory gauges in more ways
- Better store info: improved table-cloth store display and interaction
- Render rotation particles of the targeted block

**Tooltips**:

- Additional content will be displayed below:
- Container, fluid container, ender chest, toolbox, clipboard, map, backtank, diving boots, wrench, linked controller, list filter,
  attribute filter
- Package, placard, table cloth, depot, deployer, millstone, crushing controller, redstone requester, factory gauge

**Overlays**:

- Item info overlay on blocks, with switchable theme and colors
- Drafting view: pixelated, outlined post-processing, with adjustable pixel scale and line color

**Outliner**:

- Analog box and connection lines, configurable colors and delayed render

**Aeronautics**:

- Always show mass and friction
- Force overlay: gravity, lift, drag, levitation, balloon lift, propulsion, magnetic force and center of mass for the followed contraption,
  plus an HUD with mass and force values

**Misc**:

- Wrench and chain conveyor enhancements
- Create-style stack count, blueprint name fix, recursive blueprint scan, infinite edit-box length
- Quick request actions, JEI recipe transfer for stock keeper and redstone requester, preview filter, show stress network
- A few stability fixes (e.g. NBT crash)

---

## 版本 / Versions

|   Minecraft   | Forge | Fabric/Quilt | NeoForge | Create: Cyber Goggles |           Create            |
|:-------------:|:-----:|:------------:|:--------:|:---------------------:|:---------------------------:|
| 1.21.8-26.1.2 |       |      ✅      |          |         3.0+          |      6.0+ (Create-Fly)      |
|    1.21.1     |       |              |    ✅    |         1.0+          |            6.0+             |
|    1.20.1     |  ✅   |      ✅      |    ✅    |         1.0+          | 1.x: 0.5+, 6.0+; 2.0+: 6.0+ |
| 1.18.2-1.19.2 |  ✅   |      ✅      |          |          1.x          |            0.5+             |

## 本地化 / Localization

欢迎通过 [Crowdin](https://crowdin.com/project/create-cyber-goggles) 帮助将此模组翻译成更多语言！

Welcome to help translate this mod into more languages on [Crowdin](https://crowdin.com/project/create-cyber-goggles)!

## 鸣谢 / Credits

本项目使用了部分源自以下模组的代码：

This project uses some code derived from the following mods:

- [ShulkerBoxTooltip](https://github.com/MisterPeModder/ShulkerBoxTooltip)
- [Schematician](https://github.com/Alex-Guha/schematician)
- [O123456789](https://github.com/catboybinary/O123456789)
