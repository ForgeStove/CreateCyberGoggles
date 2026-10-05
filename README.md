![Logo](https://cdn.modrinth.com/data/cached_images/9f1d22babad387de4381b095c41a0a1713be25da.png)

[![CurseForge](https://img.shields.io/curseforge/dt/1233804?logo=curseforge&label=&suffix=%20&style=flat&color=242629&labelColor=e04e14&logoColor=1c1c1c)](https://www.curseforge.com/minecraft/mc-mods/create-cyber-goggles)
[![Modrinth](https://img.shields.io/modrinth/dt/create-cyber-goggles?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c)](https://modrinth.com/mod/create-cyber-goggles)
[![License](https://img.shields.io/github/license/ForgeStove/CreateCyberGoggles?style=flat&color=900c3f)](https://github.com/ForgeStove/CreateCyberGoggles?tab=readme-ov-file#MIT-1-ov-file)
[![Crowdin](https://badges.crowdin.net/create-cyber-goggles/localized.svg)](https://crowdin.com/project/create-cyber-goggles)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ForgeStove/CreateCyberGoggles)

[English](README.md) | [简体中文](README.zh-CN.md)

## Overview

**Create: Cyber Goggles** is a client-side mod for [**Create**](https://modrinth.com/mod/create), providing modular assistance features.

## Features

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

**Schematic**:

- 3D preview: a rotatable, zoomable preview of the selected schematic next to the Schematic Table
- Tooltip preview: hold Alt while hovering a schematic item
- Export render: one-click render of a schematic to a PNG (1024 by default, hold Shift for 2048), with configurable orientation and
  antialiasing, fluids included
- Sable support: with [sable](https://modrinth.com/mod/sable) installed, sub-levels (ships) in the schematic are previewed and
  rendered as well
- Export command: `/ccg schematic export <file> [width] [orientation] [antialiasing]`
- Truncate long schematic names so they no longer overflow the scroll box

**Aeronautics**:

- Always show mass and friction
- Force overlay: gravity, lift, drag, levitation, balloon lift, propulsion, magnetic force and center of mass for the followed contraption,
  plus an HUD with mass and force values

**Misc**:

- Wrench and chain conveyor enhancements
- Create-style stack count, blueprint name fix, recursive blueprint scan, infinite edit-box length
- Quick request actions, JEI recipe transfer for stock keeper and redstone requester, preview filter, show stress network
- A few stability fixes (e.g. NBT crash)

## Versions

|   Minecraft   | Forge | Fabric/Quilt | NeoForge | Create: Cyber Goggles |           Create            |
|:-------------:|:-----:|:------------:|:--------:|:---------------------:|:---------------------------:|
| 1.21.8-26.1.2 |       |      ✅      |          |         3.0+          |      6.0+ (Create-Fly)      |
|    1.21.1     |       |              |    ✅    |         1.0+          |            6.0+             |
|    1.20.1     |  ✅   |      ✅      |    ✅    |         1.0+          | 1.x: 0.5+, 6.0+; 2.0+: 6.0+ |
| 1.18.2-1.19.2 |  ✅   |      ✅      |          |          1.x          |            0.5+             |

## Localization

Welcome to help translate this mod into more languages on [Crowdin](https://crowdin.com/project/create-cyber-goggles)!

## Credits

This project uses some code derived from the following mods:

- [ShulkerBoxTooltip](https://github.com/MisterPeModder/ShulkerBoxTooltip)
- [Schematician](https://github.com/Alex-Guha/schematician)
- [O123456789](https://github.com/catboybinary/O123456789)
- [Create: Schematic Preview](https://github.com/titlo10/Create-Schematic-Preview)
- [Create: Blueprinted](https://github.com/salem-5/Create-Blueprinted)
