![Logo](https://cdn.modrinth.com/data/cached_images/45b7c12e5bfe61cf6cf3a47ecc761b041825de67.png)

[![Supported Versions](https://cf.way2muchnoise.eu/versions/1233804(c70039).svg)](https://www.curseforge.com/minecraft/mc-mods/create-cyber-goggles/files)
[![CurseForge](http://cf.way2muchnoise.eu/1233804.svg)](https://www.curseforge.com/minecraft/mc-mods/create-cyber-goggles)
[![Modrinth](https://img.shields.io/modrinth/dt/create-cyber-goggles?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c)](https://modrinth.com/mod/create-cyber-goggles)
[![License](https://img.shields.io/github/license/ForgeStove/CreateCyberGoggles?style=flat&color=900c3f)](https://github.com/ForgeStove/CreateCyberGoggles?tab=readme-ov-file#MIT-1-ov-file)
[![Crowdin](https://badges.crowdin.net/create-cyber-goggles/localized.svg)](https://crowdin.com/project/create-cyber-goggles)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ForgeStove/CreateCyberGoggles)

## 概述 / Overview

> **Create: Cyber Goggles** 是[**机械动力**](https://modrinth.com/mod/create)的客户端附属模组，为其提供了模块化的辅助功能。

> **Create: Cyber Goggles** is a client-side mod for [**Create**](https://modrinth.com/mod/create), providing modular assistance features.

## 功能 / Features

<details>
<summary><strong>功能列表</strong></summary>

> 此列表适用于版本 1.21.1 NeoForge，其余版本功能会有所不同。

### 配置

#### 护目镜

| 功能         | 默认值 | 描述                    |
|:-----------|:---:|:----------------------|
| 高级信息显示     |  ✅  | 让护目镜显示更多信息            |
| 隐藏静态旋转信息   |  ❌  | 在护目镜中隐藏静态方块的旋转信息      |
| 仅在护目镜启用时启用 |  ❌  | 使大多数附加显示功能都会受到护目镜限制   |
| 更好的商店信息    |  ✅  | 优化桌布商店信息显示以及操作逻辑      |
| 更好的工厂仪表    |  ✅  | 始终允许任何方式的连接和移动工厂仪表    |
| 渲染旋转粒子     |  ✅  | 渲染目标方块的旋转粒子效果         |
| 禁用界面内护目镜   |  ✅  | 任意界面出现时禁用护目镜显示        |
| 允许值框出现时渲染  |  ❌  | 出现值框时允许渲染护目镜工具提示      |
| 去重工具提示行    |  ✅  | 移除护目镜工具提示中相邻的重复或近似重复行 |
| 精确数值       |  ✅  | 显示更加精确的数值信息           |
| 最大分数位数     |  2  | 精确数值所使用的最大分数位数        |

##### 游戏模式

| 功能     | 默认值 | 描述            |
|:-------|:---:|:--------------|
| 启用护目镜  |  ✅  | 护目镜主开关        |
| 生存模式启用 |  ✅  | 在生存模式下始终启用护目镜 |
| 创造模式启用 |  ✅  | 在创造模式下始终启用护目镜 |
| 旁观模式启用 |  ✅  | 在旁观模式下始终启用护目镜 |
| 冒险模式启用 |  ✅  | 在冒险模式下始终启用护目镜 |

#### 工具提示

| 功能       | 默认值 | 描述                       |
|:---------|:---:|:-------------------------|
| 额外物品工具提示 |  ✅  | 在物品工具提示中显示额外信息           |
| 属性过滤器    |  ✅  | 显示属性过滤器详情                |
| 背罐       |  ✅  | 显示背罐的额外工具提示信息            |
| 剪贴板      |  ✅  | 启用第一人称与工具提示叠加层中的剪贴板自定义渲染 |
| 容器       |  ✅  | 显示容器内容                   |
| 粉碎控制器    |  ✅  | 显示粉碎控制器产出与进度             |
| 机械手      |  ✅  | 显示机械手溢出物品                |
| 置物台      |  ✅  | 显示置物台内容                  |
| 潜水靴      |  ✅  | 显示潜水靴的额外工具提示信息           |
| 末影箱      |  ✅  | 显示末影箱内容                  |
| 流体容器     |  ✅  | 显示流体容器内容                 |
| 护目镜      |  ✅  | 显示护目镜的额外工具提示信息           |
| 物品实体     |  ✅  | 显示携带物品信息                 |
| 控制器      |  ✅  | 显示控制器的频率槽位               |
| 列表过滤器    |  ✅  | 显示列表过滤器内容                |
| 地图       |  ✅  | 显示地图的额外工具提示信息            |
| 石磨       |  ✅  | 显示石磨的额外工具提示信息            |
| 包裹实体     |  ✅  | 显示包裹实体内容                 |
| 包裹物品     |  ✅  | 显示包裹物品内容                 |
| 红石请求器    |  ✅  | 显示红石请求器内容                |
| 桌面/桌布    |  ✅  | 显示桌面/桌布内容                |
| 工具箱      |  ✅  | 显示工具箱内容                  |
| 扳手       |  ✅  | 显示扳手的额外工具提示信息            |

#### 叠加层

| 功能        |    默认值     | 描述               |
|:----------|:----------:|:-----------------|
| 渲染物品信息叠加层 |     ✅      | 允许渲染方块上的物品信息叠加层  |
| 叠加层偏移     |    0, 0    | 物品信息叠加层的水平与垂直偏移量 |
| 提示框类型     |     默认     | 选择提示框的显示类型       |
| 提示框主题     |     默认     | 选择提示框的主题样式       |
| 使用自定义颜色   |     ❌      | 启用物品信息叠加层的自定义颜色  |
| 背景颜色      | 0x00000000 | 自定义的背景颜色         |
| 边框顶部颜色    | 0x00000000 | 自定义的边框顶部颜色       |
| 边框底部颜色    | 0x00000000 | 自定义的边框底部颜色       |

##### 图解视图

| 功能     |   默认值    | 描述                    |
|:-------|:--------:|:----------------------|
| 启用图解视图 |    ❌     | 对整个画面应用蓝图/图解风格的后处理效果  |
| 调色板偏移  |   0.25   | 调色板纹理的水平查找偏移 (0..1)   |
| 像素化    |    ✅     | 将画面粗化以获得蓝图效果          |
| 像素缩放   |    4     | 每个虚拟像素覆盖的屏幕像素数 (1=关闭) |
| 线条颜色   | 0x2E3032 | 边缘轮廓颜色                |
| 阴影线条颜色 | 0x696965 | 深度提示轮廓的阴影面颜色          |

#### 边框渲染

| 功能     |   默认值   | 描述                 |
|:-------|:-------:|:-------------------|
| 渲染模拟盒  |    ✅    | 渲染各种方块显示的模拟盒       |
| 更好的连接线 |    ✅    | 优化部分渲染连接线          |
| 延时渲染时长 |   60    | 模拟盒继续渲染的时长 [Ticks] |
| 向外颜色   | #DDC166 | 模拟盒向外时显示的颜色        |
| 向内颜色   | #7FCDE0 | 模拟盒向内时显示的颜色        |
| 彩虹调试   |    ❌    | 启用彩虹调试模式           |

#### 航空学

| 功能           | 默认值 | 描述             |
|:-------------|:---:|:---------------|
| 始终显示质量       |  ✅  | 显示任何方块的质量      |
| 始终显示摩擦力      |  ❌  | 显示任何方块的摩擦力     |
| 解限把手范围       |  ❌  | 解除铁把手的范围限制     |
| 自定义把手移动物理结构键 |  ❌  | 启用自定义把手移动物理结构键 |
| 始终允许悬挂绳索     |  ✅  | 无需扳手即可悬挂在绳索上   |

##### 力覆盖层

| 功能        | 默认值  | 描述                         |
|:----------|:----:|:---------------------------|
| 启用受力覆盖层   |  ✅   | 在目标物理结构上渲染力的 3D 箭头和质心标记    |
| 启用力的提示框   |  ✅   | 显示包含质量和力大小的 HUD 面板         |
| 渲染质心      |  ✅   | 渲染质心的方块标记                  |
| 覆盖层位置     | 0, 0 | HUD 提示框位置偏移                |
| 聚类角度阈值    | 0.1  | 受力方向聚类的角度方差阈值 (弧度)         |
| 平滑因子      | 0.5  | 指数移动平均平滑因子 (0=无变化, 1=即时)   |
| 重力箭头比例    | 0.5  | 重力箭头长度占包围盒高度的比例            |
| 箭头饱和度     |  3   | 控制大力映射到箭头长度的方式             |
| 最小箭头长度    | 0.1  | 以方块为单位的最小箭头长度              |
| 目标搜索范围    |  4   | 用于目标物理结构的最大射线追踪距离 (以区块为单位) |
| 最小覆盖层像素大小 |  0   | 覆盖层元素的最小显示大小 (像素)          |
| 显示重力      |  ✅   | 渲染重力箭头                     |
| 显示阻力      |  ✅   | 渲染阻力箭头                     |
| 显示悬浮力     |  ✅   | 渲染悬浮力箭头                    |
| 显示气球升力    |  ✅   | 渲染气球升力箭头                   |
| 显示推进力     |  ✅   | 渲染推进力箭头                    |
| 显示升力      |  ✅   | 渲染升力箭头                     |
| 显示磁力      |  ✅   | 渲染磁力箭头                     |

#### 杂项

| 功能         | 默认值 | 描述                         |
|:-----------|:---:|:---------------------------|
| 移除动力臂限制    |  ❌  | 移除动力臂的距离限制                 |
| 移除请求限制     |  ✅  | 移除工厂仪表和红石请求器的请求数量限制并优化快速滚动 |
| 仓储请求快捷操作   |  ✅  | 启用仓储请求界面的快捷操作              |
| 递归扫描蓝图     |  ✅  | 刷新蓝图列表时包含子文件夹中的蓝图          |
| 防止选区丢弃     |  ✅  | 防止在切换物品时丢弃选区               |
| 阻止自动关闭过滤器  |  ❌  | 阻止过滤器界面自动关闭                |
| 无限编辑框长度    |  ❌  | 无限编辑框长度 (实际上是 2^31 -1)     |
| 移除纸板套视线遮挡  |  ✅  | 移除纸板套在潜行时的视线遮挡             |
| 移除第一人称背罐渲染 |  ❌  | 移除下界合金背罐的第一人称渲染效果          |
| 允许潜水靴      |  ✅  | 潜水靴的功能，禁用以使它们像普通的靴子一样      |
| 修复蓝图名称     |  ✅  | 保存蓝图时可以使用非 ASCII 字符        |
| 移除列车伤害     |  ❌  | 阻止列车碰撞对玩家造成伤害              |
| 启用负无穷油门    |  ❌  | 允许列车油门滚动突破正常最低限制           |
| 强制飞轮优化     |  ❌  | 强制启用飞轮渲染后端 (与着色器冲突)        |
| NBT 修复     |  ❌  | 修复 NBT 过长而导致的崩溃，可能导致卡顿     |
| 显示废料内容     |  ✅  | 显示序列组装中随机废料的具体内容           |

##### 锁链传动轮

| 功能       | 默认值 | 描述                  |
|:---------|:---:|:--------------------|
| 始终允许悬挂锁链 |  ❌  | 无需扳手即可悬挂在锁链传动轮上     |
| 阻止自动分离   |  ❌  | 阻止在锁链传动轮上因为障碍物而自动分离 |
| 增强型连接    |  ✅  | 移除锁链传动轮的最小距离和最大倾角限制 |
| 自动打包自己   |  ❌  | 挂上锁链时自动变成纸箱         |

##### 扳手

| 功能       | 默认值 | 描述                |
|:---------|:---:|:------------------|
| 更好的齿轮箱   |  ✅  | 空手时允许右键切换齿轮箱的开关状态 |
| 更好的管道箱   |  ✅  | 空手时允许右键切换管道箱的开关状态 |
| 更好的底盘    |  ✅  | 空手时允许右键切换底盘的胶粘状态  |
| 始终显示滚动值  |  ✅  | 始终在方块上显示滚动值       |
| 始终允许旋转方块 |  ✅  | 无需扳手即可通过快捷键旋转方块   |
| 左键快速拆除   |  ✅  | 使用左键单击立即拆除组件      |
| 移除冷却     |  ✅  | 移除方块旋转菜单的冷却时间     |
| 增强的旋转菜单  |  ❌  | 允许方块旋转菜单修改更多方块属性  |

### 快捷键

|      功能       |  默认值  |
|:-------------:|:-----:|
|    打开模组配置     |  未指定  |
| 通过仓储发报机访问网络存储 |  未指定  |
|     预览过滤器     |  未指定  |
|   显示应力网络信息    |  Tab  |
|     显示强力胶     |  未指定  |
|     显示蜂蜜胶     |  未指定  |
|    切换潜水靴开关    |  未指定  |
|    切换护目镜开关    |  未指定  |
|   切换要显示的物品    | 左Ctrl |
|   穿透移动结构交互    | 左Ctrl |
|   仓储请求快捷全选    | 左Alt  |
| 打开仓储请求数量设置界面  |  中键   |
|  与选中方块面的反面交互  |  Tab  |
|     使用蓝图      |  未指定  |
|    剪贴板滚轮翻页    |  未指定  |
|    回正物理结构     |  未指定  |
|   把手移动物理结构    |  未指定  |
|    使用物理法杖     |  未指定  |

</details>

<details>
<summary><strong>Feature List</strong></summary>

> This list is for version 1.21.1 NeoForge, and the rest of the version features will vary.

### Config

#### Goggles

| Feature                     | Default | Description                                                          |
|:----------------------------|:-------:|:---------------------------------------------------------------------|
| Enhanced Info Display       |    ✅    | Display more information with goggles                                |
| Hide Static Rotational Info |    ❌    | Hide kinetic information for static blocks in goggles                |
| Only On With Goggles        |    ❌    | Most additional display functions are restricted by goggles          |
| Better Store Info           |    ✅    | Optimize table cloth store info display and interaction logic        |
| Better Factory Gauge        |    ✅    | Allows any way to connect and move factory gauges                    |
| Render Kinetic Particles    |    ✅    | Render rotation particle effects for target blocks                   |
| Disable In-Screen Goggles   |    ✅    | Disable goggles display when any screen is open                      |
| Render with Value Box       |    ❌    | Allow goggle tooltip rendering when value box is visible             |
| Deduplicate Tooltip Lines   |    ✅    | Remove adjacent duplicate or near-duplicate lines in goggles tooltip |
| Precise Numbers             |    ✅    | Display more precise numerical values                                |
| Max Fraction Digits         |    2    | The maximum fraction digits used for precise number                  |

##### Game Mode

| Feature             | Default | Description                             |
|:--------------------|:-------:|:----------------------------------------|
| Enable Goggles      |    ✅    | Master switch for goggles               |
| Enable in Survival  |    ✅    | Always enable goggles in Survival mode  |
| Enable in Creative  |    ✅    | Always enable goggles in Creative mode  |
| Enable in Spectator |    ✅    | Always enable goggles in Spectator mode |
| Enable in Adventure |    ✅    | Always enable goggles in Adventure mode |

#### Tooltip

| Feature             | Default | Description                                                           |
|:--------------------|:-------:|:----------------------------------------------------------------------|
| Extra Item Tooltip  |    ✅    | Show extra information in item tooltip                                |
| Attribute Filter    |    ✅    | Show attribute filter details                                         |
| Backtank            |    ✅    | Show extra tooltip info for backtank                                  |
| Clipboard           |    ✅    | Enable clipboard custom rendering in first person and tooltip overlay |
| Container           |    ✅    | Show container content                                                |
| Crushing Controller |    ✅    | Show crushing controller outputs and progress                         |
| Deployer            |    ✅    | Show deployer overflow items                                          |
| Depot               |    ✅    | Show depot content                                                    |
| Diving Boots        |    ✅    | Show extra tooltip info for diving boots                              |
| Ender Chest         |    ✅    | Show ender chest content                                              |
| Fluid Container     |    ✅    | Show fluid container content                                          |
| Goggles             |    ✅    | Show extra tooltip info for goggles                                   |
| Item Entity         |    ✅    | Show carried item info                                                |
| Linked Controller   |    ✅    | Show frequency slots for linked controller                            |
| List Filter         |    ✅    | Show list filter content                                              |
| Map                 |    ✅    | Show additional tooltip information for the map                       |
| Millstone           |    ✅    | Show extra tooltip info for millstone                                 |
| Package Entity      |    ✅    | Show package entity content                                           |
| Package Item        |    ✅    | Show package item content                                             |
| Redstone Requester  |    ✅    | Show redstone requester content                                       |
| Table Cloth         |    ✅    | Show table cloth content                                              |
| Toolbox             |    ✅    | Show toolbox content                                                  |
| Wrench              |    ✅    | Show extra tooltip info for wrench                                    |

#### Overlay

| Feature             |  Default   | Description                                         |
|:--------------------|:----------:|:----------------------------------------------------|
| Render Item Overlay |     ✅      | Allow rendering item info overlay on blocks         |
| Overlay Offset      |    0, 0    | Horizontal and vertical offset of item info overlay |
| Tooltip Flag Type   |  Default   | Select the tooltip display type                     |
| Tooltip Theme       |  Default   | Select the tooltip theme style                      |
| Use Custom Color    |     ❌      | Enable custom colors for item info overlay          |
| Background Color    | 0x00000000 | Custom background color                             |
| Border Top Color    | 0x00000000 | Custom border top color                             |
| Border Bottom Color | 0x00000000 | Custom border bottom color                          |

##### Drafting View

| Feature              | Default  | Description                                                             |
|:---------------------|:--------:|:------------------------------------------------------------------------|
| Enable Drafting View |    ❌     | Apply a blueprint/schematic post-processing effect to the entire screen |
| Palette Offset       |   0.25   | Horizontal lookup offset (0..1) into the palette texture                |
| Pixelate             |    ✅     | Snap the screen to a coarser virtual grid for a blueprint look          |
| Pixel Scale          |    4     | Each virtual pixel covers this many screen pixels (1=off)               |
| Line Color           | 0x2E3032 | Edge outline colour                                                     |
| Line Shadow Color    | 0x696965 | Shadow side colour for depth-cued outlines                              |

#### Outliner

| Feature               | Default | Description                                           |
|:----------------------|:-------:|:------------------------------------------------------|
| Render Analog Box     |    ✅    | Render analog boxes displayed by various blocks       |
| Better Lines          |    ✅    | Optimize some rendered connection lines               |
| Delay Render Duration |   60    | Duration for analog box to continue rendering [Ticks] |
| Outward Color         | #DDC166 | Color displayed when analog box faces outward         |
| Inward Color          | #7FCDE0 | Color displayed when analog box faces inward          |
| Rainbow Debug         |    ❌    | Enable rainbow debug mode                             |

#### Aeronautics

| Feature                         | Default | Description                                |
|:--------------------------------|:-------:|:-------------------------------------------|
| Always Show Mass                |    ✅    | Show the mass of any blocks                |
| Always Show Friction            |    ❌    | Show the friction of any blocks            |
| Lift Limit Of Handle Range      |    ❌    | Lift the limit of the iron handle range    |
| Custom Handle Move Sublevel Key |    ❌    | Enable custom handle movement Sublevel key |
| Always Allow Riding Rope        |    ✅    | Hang on rope without needing a wrench      |

##### Force Overlay

| Feature                 | Default | Description                                                        |
|:------------------------|:-------:|:-------------------------------------------------------------------|
| Enable Force Overlay    |    ✅    | Render in-world force arrows and COM marker on targeted sub-levels |
| Enable Force Tooltip    |    ✅    | Show a HUD tooltip with mass and force magnitudes                  |
| Render Center of Mass   |    ✅    | Render the center-of-mass cube marker                              |
| Overlay Position        |  0, 0   | HUD tooltip position offset                                        |
| Cluster Angle Threshold |   0.1   | Angular variance threshold for force clustering (radians)          |
| Smoothing Factor        |   0.5   | EMA smoothing factor (0=no change, 1=instant)                      |
| Gravity Arrow Fraction  |   0.5   | Gravity arrow length as a fraction of bounding box height          |
| Arrow Saturation        |    3    | Controls how large forces map to arrow length                      |
| Min Arrow Length        |   0.1   | Minimum arrow length in block units                                |
| Targeting Range         |    4    | Maximum raycast distance for targeting sub-levels (in chunks)      |
| Min Overlay Pixel Size  |    0    | Minimum visual size of overlay elements in pixels                  |
| Show Gravity            |    ✅    | Render gravity force arrows                                        |
| Show Drag               |    ✅    | Render drag force arrows                                           |
| Show Levitation         |    ✅    | Render levitation force arrows                                     |
| Show Balloon Lift       |    ✅    | Render balloon lift force arrows                                   |
| Show Propulsion         |    ✅    | Render propulsion force arrows                                     |
| Show Lift               |    ✅    | Render lift force arrows                                           |
| Show Magnetic Force     |    ✅    | Render magnetic force arrows                                       |

#### Misc

| Feature                                | Default | Description                                                                                       |
|:---------------------------------------|:-------:|:--------------------------------------------------------------------------------------------------|
| Remove Mechanical Arm Limit            |    ❌    | Remove distance limit for mechanical arms                                                         |
| Remove Request Limit                   |    ✅    | Remove request quantity limit for factory panels and redstone requesters, optimize fast scrolling |
| Stock Request Quick Actions            |    ✅    | Enable quick actions in stock request screen                                                      |
| Recursive Schematic Scan               |    ✅    | Include schematics in subfolders when refreshing the list                                         |
| Prevent Selection Discard              |    ✅    | Prevent discarding selection when switching items                                                 |
| Prevent Auto Close Filter              |    ❌    | Prevent the filter screen from closing automatically                                              |
| Inf Edit Box Length                    |    ❌    | Infinite edit box length (Actually 2^31 - 1)                                                      |
| Remove Cardboard Overlay               |    ✅    | Remove cardboard box vision obstruction when sneaking                                             |
| Remove Netherite Backtank First Person |    ❌    | Remove netherite backtank first person rendering effect                                           |
| Allow Diving Boots                     |    ✅    | Diving boots functionality, disable to make them act like normal boots                            |
| Fix Schematic Name                     |    ✅    | Allow non-ASCII characters when saving schematics                                                 |
| Remove Train Damage                    |    ❌    | Prevent train collisions from dealing damage to players                                           |
| Enable Negative Infinity Throttle      |    ❌    | Allow train throttle to scroll past the normal minimum limit                                      |
| Force Flywheel Backend                 |    ❌    | Force enable Flywheel rendering backend (conflicts with shaders)                                  |
| NBT Fix                                |    ❌    | Fix crashes caused by NBT being too long, may cause lag                                           |
| Show Scrap Content                     |    ✅    | Show specific contents of random scrap in sequenced assembly                                      |

##### Chain Conveyor

| Feature                   | Default | Description                                                          |
|:--------------------------|:-------:|:---------------------------------------------------------------------|
| Always Allow Riding Chain |    ❌    | Hang on chain conveyors without needing a wrench                     |
| Prevent Auto Detach       |    ❌    | Prevent automatic detachment from chain conveyors due to obstacles   |
| Enhanced Connection       |    ✅    | Remove minimum distance and maximum angle limits for chain conveyors |
| Auto Cardboard Yourself   |    ❌    | Automatically become a cardboard box when hanging on chains          |

##### Wrench

| Feature                   | Default | Description                                                        |
|:--------------------------|:-------:|:-------------------------------------------------------------------|
| Better Encased Cogwheel   |    ✅    | Allow right-click to toggle encased cogwheel state with empty hand |
| Better Encased Pipe       |    ✅    | Allow right-click to toggle encased pipe state with empty hand     |
| Better Chassis            |    ✅    | Allow right-click to toggle chassis sticky state with empty hand   |
| Always Show Scroll Value  |    ✅    | Always display scroll values on blocks                             |
| Always Allow Rotating     |    ✅    | Rotate blocks with hotkey without needing a wrench                 |
| Left Click Fast Dismantle |    ✅    | Instantly dismantle components with left click                     |
| Remove Cooldown           |    ✅    | Remove cooldown for block rotation menu                            |
| Enhanced Rotation Menu    |    ❌    | Allow rotation menu to modify more block properties                |

### Hotkeys

|                Function                 |   Default    |
|:---------------------------------------:|:------------:|
|               Open Config               |  Not Bound   |
| Access network storage via stock ticker |  Not Bound   |
|             Preview Filter              |  Not Bound   |
|        Show Stress Network Info         |     Tab      |
|             Show Super Glue             |  Not Bound   |
|             Show Honey Glue             |  Not Bound   |
|        Toggle Diving Boot On/Off        |  Not Bound   |
|          Toggle Goggle On/Off           |  Not Bound   |
|         Toggle Item To Display          | Left Control |
|      Interact Through Contraptions      | Left Control |
|     Stock Request Quick Select All      |   Left Alt   |
|  Open Stock Request Quantity Settings   | Middle Mouse |
|      Interact Opposite Block Face       |     Tab      |
|              Use Schematic              |  Not Bound   |
|          Scroll Clipboard Page          |  Not Bound   |
|           Correction Sublevel           |  Not Bound   |
|          Handle Move Sublevel           |  Not Bound   |
|            Use Physics Staff            |  Not Bound   |

</details>

## 版本 / Versions

|   Minecraft   | Forge | Fabric/Quilt | NeoForge | Create: Cyber Goggles |             Create             |
|:-------------:|:-----:|:------------:|:--------:|:---------------------:|:------------------------------:|
| 1.21.8-26.1.2 |       |      ✅       |          |         3.0+          |       6.0+ (Create-Fly)        |
|    1.21.1     |       |              |    ✅     |        1.x~7.x        |              6.0+              |
|    1.20.1     |   ✅   |      ✅       |    ✅     |        1.x~7.x        | 1.x: 0.5+, 6.0+; 2.x~7.x: 6.0+ |
| 1.18.2-1.19.2 |   ✅   |      ✅       |          |          1.x          |              0.5+              |

## 本地化 / Localization

> 欢迎通过 [Crowdin](https://crowdin.com/project/create-cyber-goggles) 帮助将此模组翻译成更多语言！

> You are welcome to help translate this mod into more languages on [Crowdin](https://crowdin.com/project/create-cyber-goggles)!

[![Crowdin](https://badges.crowdin.net/create-cyber-goggles/localized.svg)](https://crowdin.com/project/create-cyber-goggles)

## 致谢 / Credits

> 本项目包含了源自 [Schematician](https://github.com/Alex-Guha/schematician) 的代码

> This project includes code derived from [Schematician](https://github.com/Alex-Guha/schematician).
