package io.github.forgestove.create_cyber_goggles.core.factory;
import com.simibubi.create.content.logistics.*;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts.CraftingEntry;
import com.simibubi.create.foundation.gui.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.factory.ReplenishGroup.*;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/**
 * 「自动补齐缺货」独立覆盖层 GUI（宿主 = Create {@link StockKeeperRequestScreen}，右下角触发进入）。
 * 继承 catnip {@link AbstractSimiScreen}。外观与 Create {@code stock_keeper} 面板一致（同宽 256）：
 * <b>头</b>(Create header) + <b>身体层</b>(Create body 平铺) + <b>底部</b>(返回方块 + 发送长条，发送 hover 用
 * Create 高亮长条)。每组一个配方类型，组尾一个<b>共享地址框</b>（羊皮纸背景，按类型持久化到磁盘）。
 * 每节点显示其产物 + 全部原料（够的绿勾/不足红框）+ 可合成次数；下单与原版一致：
 * 装配类 convertRecipe（9 格 pattern），加工类通用 pattern，orderedStacks=每原料×craftTimes。
 *
 * <p>性能：内容预计算成<b>扁平行列表</b>（{@link #rows}），渲染时<b>只画视口内的行</b>，
 * 高度缓存 O(1)，不再逐帧全量重画全部原料。</p>
 */
public class AutoReplenishScreen extends AbstractSimiScreen {
	/** 地址缓存：仅进程内有效（关掉游戏即清空），键为组键（配方类型） */
	private static final Map<String, String> CACHE_ADDRS = new LinkedHashMap<>();
	/** Create 的配方类型路径与其 lang 键路径不一致时的映射（取自 CreateJEI 的分类命名） */
	private static final Map<String, String> RECIPE_TYPE_ALIAS = new HashMap<>();
	// 面板几何（与 Create stock_keeper 同宽）
	private static final int PANEL_W = 256;
	private static final int HEADER_H = 16;
	private static final int FOOTER_H = 47;  // 用户 band3 底部背景高(棕色+灰边+烘好的方块/长条)
	// Create stock_keeper 块的棕色面板内容是居中内嵌的（两侧带灰色金属边栏）：x=33..223。
	// 内容(行/地址框/滚动条/底部按钮)必须对齐到这个棕色区域，否则会溢出到透明边。
	private static final int CONTENT_L = 35;
	private static final int CONTENT_R = 228;
	private static final int NODE_H = 20;          // 节点行高(图标18 + 上下各1 → 行间 2，与左右间距一致)
	private static final int BP_DECOR = 5;        // 蓝图横幅上下固定装饰总高(9-slice 顶/底各 8)
	private static final int NODES_PER_ROW = 9;    // 节点网格每行最多 9 个
	private static final int CELL_W = 20;          // 网格单元宽(18 图标 + 2 间距)
	private static final int GROUP_H = 16;         // 组头行高
	private static final int ADDR_H = 18;          // 地址框(羊皮纸)行高
	private static final int GROUP_GAP = 16;        // 组间距
	private static final int SCROLL_W = 5;         // 滚动条宽
	private static final int BODY_TOP = HEADER_H;  // 内容起始(在头下方)
	// 地址行局部坐标
	private static final int ADDR_X = CONTENT_L;
	private static final int ADDR_TEXT_IN = 12;      // 文本左缩进(越过圆环)
	static {
		RECIPE_TYPE_ALIAS.put("create:cutting", "sawing");
		RECIPE_TYPE_ALIAS.put("create:splashing", "fan_washing");
		RECIPE_TYPE_ALIAS.put("create:haunting", "fan_haunting");
		RECIPE_TYPE_ALIAS.put("create:compacting", "packing");
		RECIPE_TYPE_ALIAS.put("create:filling", "spout_filling");
		RECIPE_TYPE_ALIAS.put("create:emptying", "draining");
		RECIPE_TYPE_ALIAS.put("create:conversion", "mystery_conversion");
	}
	private final StockKeeperRequestScreen parent;
	private final StockTickerBlockEntity blockEntity;
	private final List<ReplenishGroup> groups;
	private final List<AddressEditBox> addrBoxes = new ArrayList<>();
	private final List<String> groupAddress = new ArrayList<>();
	private final List<Row> rows = new ArrayList<>();
	private int contentH;        // 内容总高（缓存，O(1)）
	private int panelH;          // 面板总高（init 时算）
	private int viewH;           // 可视内容高（= 滚动轨道高）
	private int scroll;
	private boolean scrollDragging;
	private double scrollDragOffset;
	private int mouseXPos, mouseYPos;   // 本帧鼠标位置（行内悬停判定用）
	private Node hoveredNode;           // 本帧悬停的配方节点（tooltip 用）
	public AutoReplenishScreen(StockKeeperRequestScreen parent, StockTickerBlockEntity blockEntity, List<ReplenishGroup> groups) {
		this.parent = parent;
		this.blockEntity = blockEntity;
		this.groups = groups;
		// 每类型地址（取进程内缓存，没有则空）
		for (ReplenishGroup group : groups) groupAddress.add(CACHE_ADDRS.getOrDefault(group.name(), ""));
		rebuildLayout();
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1));
	}
	/** 预计算扁平行列表 + 内容总高（数据不变，一次生成即可，渲染复用） */
	private void rebuildLayout() {
		rows.clear();
		int y = BODY_TOP;
		for (var gi = 0; gi < groups.size(); gi++) {
			ReplenishGroup g = groups.get(gi);
			rows.add(new Row(y, GROUP_H, RowKind.GROUP, gi, -1, -1));
			y += GROUP_H;
			// 节点整合成网格：同类型从左往右，每行最多 NODES_PER_ROW 个，超出换行；
			// 整组只占一个行条目（高度 = 行数 × NODE_H），由 drawNode 画一条竖向扩展的蓝图横幅
			int nodeRows = (g.nodes().size() + NODES_PER_ROW - 1) / NODES_PER_ROW;
			if (nodeRows > 0) {
				int h = nodeRows * NODE_H + BP_DECOR;
				rows.add(new Row(y, h, RowKind.NODE, gi, 0, -1));
				y += h;
			}
			rows.add(new Row(y, ADDR_H, RowKind.ADDR, gi, -1, -1));
			y += ADDR_H + GROUP_GAP;
		}
		contentH = y;
	}
	/** 配方类型的本地化名：优先 `<命名空间>.recipe.<路径>`（Create 等模组惯例，如 create.recipe.crushing），无则退回路径 */
	private static Component groupName(String group) {
		if (group == null || group.isBlank()) return Component.empty();
		int idx = group.indexOf(':');
		if (idx <= 0) return Component.literal(group);
		String path = RECIPE_TYPE_ALIAS.getOrDefault(group, group.substring(idx + 1));
		String key = group.substring(0, idx) + ".recipe." + path;
		return Component.translatable(I18n.exists(key) ? key : path);
	}
	/** Create 样式数量：用 NUMBERS 数字图集(5×8)画在 18×18 槽位右下角（同 StockKeeperRequestScreen#drawItemCount） */
	private static void drawCount(GuiGraphics gui, int count, int slotX, int slotY) {
		String text = count >= 1000000
			? count / 1000000 + "m"
			: count >= 10000
				? count / 1000 + "k"
				: count >= 1000 ? (count * 10 / 1000) / 10f + "k" : count >= 100 ? count + "" : " " + count;
		if (count >= BigItemStack.INF) text = "+";
		if (text.isBlank()) return;
		var numbers = AllGuiTextures.NUMBERS;
		// 抬高 z 盖在物品之上（renderItem 画在 z=150；Create 同款 190→200）
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, 200);
		var x = (int) Math.floor(-text.length() * 2.5);
		for (char c : text.toCharArray()) {
			if (c == ' ') {
				x += 4;
				continue;
			}
			int xOffset = (c - '0') * 6;
			int spriteWidth = numbers.getWidth();
			switch (c) {
				case '.' -> {
					spriteWidth = 3;
					xOffset = 60;
				}
				case 'k' -> xOffset = 64;
				case 'm' -> {
					spriteWidth = 7;
					xOffset = 70;
				}
				case '+' -> {
					spriteWidth = 9;
					xOffset = 84;
				}
				default -> {}
			}
			gui.blit(
				numbers.location,
				slotX + 14 + x,
				slotY + 10,
				0,
				numbers.getStartX() + xOffset,
				numbers.getStartY(),
				spriteWidth,
				numbers.getHeight(),
				256,
				256
			);
			x += spriteWidth - 1;
		}
		pose.popPose();
	}
	/** 蓝图横幅：8×8 九宫格，4 角固定、边/中<b>平铺</b>（不拉伸，逐块重复保持像素风），支持横向/纵向任意尺寸 */
	private static void drawBlueprint(GuiGraphics g, int x, int y, int w, int h) {
		blit9(g, CCGGuiTextures.AUTO_REPLENISH_BLUEPRINT_00, x, y, 8, 8);
		blit9(g, CCGGuiTextures.AUTO_REPLENISH_BLUEPRINT_20, x + w - 8, y, 8, 8);
		blit9(g, CCGGuiTextures.AUTO_REPLENISH_BLUEPRINT_02, x, y + h - 8, 8, 8);
		blit9(g, CCGGuiTextures.AUTO_REPLENISH_BLUEPRINT_22, x + w - 8, y + h - 8, 8, 8);
		tileH(g, CCGGuiTextures.AUTO_REPLENISH_BLUEPRINT_10, x + 8, y, w - 16, 8);
		tileH(g, CCGGuiTextures.AUTO_REPLENISH_BLUEPRINT_12, x + 8, y + h - 8, w - 16, 8);
		tileV(g, CCGGuiTextures.AUTO_REPLENISH_BLUEPRINT_01, x, y + 8, 8, h - 16);
		tileV(g, CCGGuiTextures.AUTO_REPLENISH_BLUEPRINT_21, x + w - 8, y + 8, 8, h - 16);
		tileHV(g, CCGGuiTextures.AUTO_REPLENISH_BLUEPRINT_11, x + 8, y + 8, w - 16, h - 16);
	}
	/** 水平平铺：横向逐 8 重复，仅末段(<8)轻微裁切 */
	private static void tileH(GuiGraphics g, CCGGuiTextures t, int x, int y, int w, int h) {
		int d = 0;
		while (d < w) {
			var seg = Math.min(8, w - d);
			blit9(g, t, x + d, y, seg, h);
			d += seg;
		}
	}
	/** 垂直平铺：纵向逐 8 重复，仅末段(<8)轻微裁切 */
	private static void tileV(GuiGraphics g, CCGGuiTextures t, int x, int y, int w, int h) {
		int d = 0;
		while (d < h) {
			var seg = Math.min(8, h - d);
			blit9(g, t, x, y + d, w, seg);
			d += seg;
		}
	}
	/** 双向平铺（中心） */
	private static void tileHV(GuiGraphics g, CCGGuiTextures t, int x, int y, int w, int h) {
		int dy = 0;
		while (dy < h) {
			var segH = Math.min(8, h - dy);
			int dx = 0;
			while (dx < w) {
				var segW = Math.min(8, w - dx);
				blit9(g, t, x + dx, y + dy, segW, segH);
				dx += segW;
			}
			dy += segH;
		}
	}
	/** 画一个 9-slice 子块（从图集 (startX,startY) 取 segW×segH） */
	private static void blit9(GuiGraphics g, CCGGuiTextures t, int x, int y, int w, int h) {
		if (w <= 0 || h <= 0) return;
		g.blit(t.location, x, y, t.getStartX(), t.getStartY(), w, h);
	}
	@Override
	protected void init() {
		int needed = HEADER_H + contentH + FOOTER_H;
		int screenH = mc.getWindow().getGuiScaledHeight();
		panelH = Math.clamp(screenH - 18, 80, needed);
		viewH = Math.max(0, panelH - HEADER_H - FOOTER_H);
		setWindowSize(PANEL_W, panelH);
		var window = mc.getWindow();
		setWindowOffset((window.getGuiScaledWidth() - PANEL_W) / 2, (window.getGuiScaledHeight() - panelH) / 2);
		super.init();
		// 每组（类型）一个共享地址框（按类型持久化；羊皮纸背景，左缩进越过圆环）
		// 对齐 Create 写法：localAddress 不传(建议用剪贴板#条目)，用 setValue 种入上次地址
		// 框宽收窄一截，让 AddressEditBox 右挂的剪贴板提示图标左移(图标在 getX()+width+4)
		int boxW = contentW() - ADDR_TEXT_IN - 24;
		for (var i = 0; i < groups.size(); i++) {
			// anchorToBottom=true：剪贴板地址下拉建议显示在地址框上方（同 Create 仓管）
			var box = new AddressEditBox(this, mc.font, windowXOffset + ADDR_X + ADDR_TEXT_IN, windowYOffset + BODY_TOP, boxW, 10, true);
			box.setTextColor(0xFF714A40);
			box.setTextShadow(false); // 地址输入文字不渲染阴影(NeoForge textShadow)
			box.setValue(groupAddress.get(i));
			int idx = i;
			String key = groups.get(i).name();
			box.setResponder(text -> {
				groupAddress.set(idx, text);
				CACHE_ADDRS.put(key, text);   // 仅进程内缓存
			});
			addrBoxes.add(box);
			addRenderableWidget(box);
		}
		// 底部按钮：叠在 band3 烘好的按钮上（透明底不重复画，仅补给交互：hover 高亮+tooltip / 图标 / 文字）
		// band3 内方块(image x135..151,y87..104) 与长条(x161..235,y87..104)，bg_footer 画在 x16
		addRenderableWidget(new CCGButton(
			windowXOffset + 135,
			windowYOffset + panelH - FOOTER_H + 7,
			17,
			18,
			null,
			AllGuiTextures.BUTTON_HOVER,
			false,
			AllIcons.I_CONFIG_BACK,
			null,
			this::onClose
		).tooltip(Component.translatable("gui.cancel")));
		addRenderableWidget(new CCGButton(
			windowXOffset + 161,
			windowYOffset + panelH - FOOTER_H + 7,
			75,
			18,
			null,
			AllGuiTextures.STOCK_KEEPER_REQUEST_SEND_HOVER,
			true,
			null,
			Component.translatable("create.gui.stock_keeper.send"),
			this::sendAll
		).tooltip(Component.translatable("create.gui.stock_keeper.send")));
	}
	private static int contentW() {return contentR() - CONTENT_L;}
	@Override
	public void onClose() {
		removed();
		mc.screen = parent;
	}
	private void sendAll() {
		for (var i = 0; i < groups.size(); i++) {
			String addr = groupAddress.get(i);
			for (Node n : groups.get(i).nodes()) {
				if (n.craftTimes() <= 0 || n.items().isEmpty()) continue;
				if (addr == null || addr.isBlank()) {
					CCG.LOGGER.info("ccg autoReplenish: 未填地址, 跳过 {}", n.target().getHoverName().getString());
					continue;
				}
				List<BigItemStack> order = new ArrayList<>();
				for (ReplenishEntry e : n.items())
					order.add(new BigItemStack(e.material().copy(), e.per() * n.craftTimes()));
				List<BigItemStack> pattern = new ArrayList<>();
				if (n.recipe() instanceof CraftingRecipe cr)
					pattern = FactoryPanelScreen.convertRecipeToPackageOrderContext(cr, order, true);
				else for (ReplenishEntry e : n.items()) pattern.add(new BigItemStack(e.material().copy(), e.per()));
				var packet = new PackageOrderWithCrafts(
					new PackageOrder(order),
					List.of(new CraftingEntry(new PackageOrder(pattern), n.craftTimes()))
				);
				CCG.LOGGER.info(
					"ccg autoReplenish send: {} addr={} craft={}/{} order={}",
					n.target().getHoverName().getString(),
					addr,
					n.craftTimes(),
					n.wantTimes(),
					order
				);
				CatnipServices.NETWORK.sendToServer(new PackageOrderRequestPacket(blockEntity.getBlockPos(), packet, addr, false));
			}
		}
		onClose();
	}
	// ---- 几何（窗口局部） ----
	private static int contentR() {return CONTENT_R - SCROLL_W - 4;}
	@Override
	public void resize(@NotNull Minecraft mc, int width, int height) {
		parent.resize(mc, width, height);
		super.resize(mc, width, height);
	}
	@Override
	public void tick() {
		// AddressEditBox 非 TickableGuiEventListener，catnip 不自动 tick；显式 tick 以激活剪贴板地址下拉建议(同 Create 仓管)
		super.tick();
		for (AddressEditBox box : addrBoxes) box.tick();
	}
	@Override
	protected void renderMenuBackground(@NotNull GuiGraphics gui, int x, int y, int width, int height) {}
	/** Create 蓝图横幅：左端(10) + 中端(4×n) + 右端(10)，高 25（待合成物品的蓝图框） */
	/** 蓝图横幅（竖向可扩展）：顶 3px + 中间 18px 平铺 + 底 4px；横向 左10 + 中4×n + 右10 */
	@Override
	protected void renderWindowBackground(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, -500);
		var current = mc.screen;
		mc.screen = parent;
		parent.renderBackground(gui, -1, -1, partialTick);
		parent.render(gui, -1, -1, partialTick);
		mc.screen = current;
		// 压暗父屏，让内嵌面板的透明边看起来像 Create 的菜单遮罩
		gui.fill(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), 0x66_101010);
		pose.popPose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		// 头(Create) + 身体层(平铺到 footer 前)
		CCGGuiTextures.AUTO_REPLENISH_HEADER.render(gui, 0, 0);
		var body = CCGGuiTextures.AUTO_REPLENISH_BODY;
		int bodyEnd = panelH - FOOTER_H;
		for (int y = HEADER_H; y < bodyEnd; y += 20)
			gui.blit(body.location, 0, y, body.getStartX(), body.getStartY(), PANEL_W, Math.min(20, bodyEnd - y));
		// 底部：用户提供的 band3 背景(棕色面板33..222 + 灰色底边 + 烘好的返回方块/发送长条)
//		gui.blitSprite(sprite("bg_footer"), (PANEL_W - 224) / 2, bodyEnd, 224, FOOTER_H);
		CCGGuiTextures.AUTO_REPLENISH_FOOTER.render(gui, 0, bodyEnd);
		pose.popPose();
	}
	private static ResourceLocation sprite(String name) {
		return ResourceLocation.fromNamespaceAndPath("create_cyber_goggles", "auto_replenish/" + name);
	}
	@Override
	protected void renderWindow(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		mouseXPos = mouseX;
		mouseYPos = mouseY;
		hoveredNode = null;
		int clamped = Mth.clamp(scroll, 0, maxScroll());
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		Font font = mc.font;
		Component title = Component.translatable("create_cyber_goggles.gui.auto_replenish.title");
		gui.drawString(font, title, (PANEL_W - font.width(title)) / 2, 4, 0xFF4A3728, false);
		// 可视窗口区（窗口局部）：[BODY_TOP, BODY_TOP + viewH)
		int scissorBottom = windowYOffset + BODY_TOP + Math.max(1, viewH);
		gui.enableScissor(windowXOffset + CONTENT_L, windowYOffset + BODY_TOP, windowXOffset + CONTENT_R, scissorBottom);
		if (groups.isEmpty()) {
			Component empty = Component.translatable("create_cyber_goggles.gui.auto_replenish.empty");
			gui.drawString(font, empty, (PANEL_W - font.width(empty)) / 2, BODY_TOP + 4, 0xFFC8B688, false);
		}
		int viewBottom = BODY_TOP + viewH;
		// 每帧先隐藏全部地址框，只对可见行重新定位显示（避免离屏 widget 参与渲染）
		if (!addrBoxes.isEmpty()) addrBoxes.forEach(b -> b.setVisible(false));
		for (Row r : rows) {
			int top = r.y - clamped;           // 窗口局部 y
			if (top + r.h <= BODY_TOP) continue; // 整行在视口上方，跳过（不画仍推进 y）
			if (top >= viewBottom) break;       // 之后都在视口下方，剩余无需处理
			switch (r.kind) {
				case GROUP -> drawGroup(gui, font, r, top);
				case NODE -> drawNode(gui, r, top);
				case ADDR -> placeAddr(gui, r, top);
			}
		}
		gui.disableScissor();
		pose.popPose();
		if (maxScroll() > 0) renderScrollbar(gui);
	}
	private void drawGroup(GuiGraphics gui, Font font, Row r, int top) {
		ReplenishGroup g = groups.get(r.gi());
		// 组头：底色条 + 类型名
		gui.blitSprite(sprite("row_bg"), CONTENT_L, top, contentW(), GROUP_H);
		gui.drawString(font, groupName(g.name()), CONTENT_L + 2, top + 3, 0xFFE0E0E0, false);
	}
	/** 一行节点网格：蓝图横幅作整行底，节点图标从左往右排（最多 NODES_PER_ROW 个），悬停弹配方卡 */
	private void drawNode(GuiGraphics gui, Row r, int top) {
		ReplenishGroup g = groups.get(r.gi());
		List<Node> nodes = g.nodes();
		// 整组一条竖向扩展的蓝图横幅（高度 = 行数 × NODE_H）
		drawBlueprint(gui, CONTENT_L, top, contentW(), r.h);
		for (var i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			int cellX = CONTENT_L + i % NODES_PER_ROW * CELL_W + 4;
			int iconY = top + 4 + i / NODES_PER_ROW * NODE_H;   // 越过 9-slice 顶部边框(8)，行间 2px
			// 悬停：产物图标略微放大（Create renderItemEntry: scaleFromHover += .075f）
			boolean hov = mouseXPos >= windowXOffset + cellX
				&& mouseXPos < windowXOffset + cellX + 18
				&& mouseYPos >= windowYOffset + iconY
				&& mouseYPos < windowYOffset + iconY + 18;
			if (hov) {
				var pose = gui.pose();
				pose.pushPose();
				pose.translate(cellX + 9, iconY + 9, 0);
				pose.scale(1.075f, 1.075f, 1);
				pose.translate(-(cellX + 9), -(iconY + 9), 0);
				gui.renderItem(n.target(), cellX, iconY);
				pose.popPose();
				hoveredNode = n;
			} else gui.renderItem(n.target(), cellX, iconY);
			// 产出总数画在产物图标上（Create NUMBERS 样式；= 合成次数 × 单次产出）
			drawCount(gui, n.craftTimes() * outPer(n), cellX, iconY);
		}
	}
	private void placeAddr(GuiGraphics gui, Row r, int top) {
		// 羊皮纸地址框(九宫格可拉伸) + 其上放置可输入 widget（左缩进越过圆环）
		gui.blitSprite(sprite("address_box"), ADDR_X, top, contentW(), ADDR_H);
		AddressEditBox box = addrBoxes.get(r.gi());
		box.setVisible(true);
		box.setX(windowXOffset + ADDR_X + ADDR_TEXT_IN);
		// box 下移 1px 补偿 EditBox 硬编码 8px 字高(实为9px)导致的偏高
		box.setY(windowYOffset + top + (ADDR_H - 10) / 2 + 1);
		// 地址框在滚动内容里，需让剪贴板下拉的 yOffset 跟随当前 boxY（Create 是固定框不滚，这里每次 reposition 刷新）
		refreshSuggestionsAnchor(box);
		// 暗色"包裹地址"占位：值空且未聚焦时（Create 同款：drawString 斜体 0xCDBCA8）
		if (box.getValue().isBlank() && !box.isFocused()) {
			Font font = mc.font;
			Component hint = Component.translatable("create.gui.stock_keeper.package_address").copy().withStyle(ChatFormatting.ITALIC);
			gui.drawString(font, hint, box.getX() - windowXOffset, box.getY() - windowYOffset, 0xFFCDBCA8, false);
		}
	}
	/**
	 * 让地址框的剪贴板下拉锚点(yOffset)跟随当前 boxY：Create 是固定框不滚，这里在滚动后刷新。
	 * yOffset 创建时按初始位置冻结，需用反射更新为 `-72 + box.getY()`(anchorToBottom=true)。
	 */
	private void refreshSuggestionsAnchor(AddressEditBox box) {
		try {
			var dsF = AddressEditBox.class.getDeclaredField("destinationSuggestions");
			dsF.setAccessible(true);
			Object ds = dsF.get(box);
			var yF = ds.getClass().getDeclaredField("yOffset");
			yF.setAccessible(true);
			yF.setInt(ds, -72 + box.getY());
			// 聚焦时让下拉贴住新位置（跟随滚动）：showSuggestions 是 public，反射调用重定位
			if (box.isFocused()) ds.getClass().getMethod("showSuggestions", boolean.class).invoke(ds, false);
		} catch (Exception e) {
			CCG.LOGGER.debug("addr suggestions anchor refresh failed", e);
		}
	}
	@Override
	protected void renderWindowForeground(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		// 悬停空地址框时提示（Create restocker 同款 4 行：深蓝标题+灰则明+深灰斜体"左键点击编辑"）
		for (AddressEditBox box : addrBoxes)
			if (box.visible && box.getValue().isBlank() && !box.isFocused() && box.isMouseOver(mouseX, mouseY)) {
				gui.renderComponentTooltip(
					mc.font, List.of(
						Component.translatable("create.gui.factory_panel.restocker_address").withStyle(s -> s.withColor(0x5391E1)),
						Component.translatable("create.gui.schedule.lmb_edit")
							.withStyle(ChatFormatting.DARK_GRAY)
							.withStyle(ChatFormatting.ITALIC)
					), mouseX, mouseY
				);
				break;
			}
		// 悬停配方产物图标：弹出配方卡（原料格 → 箭头 → 产物）
		if (hoveredNode != null) renderRecipeCard(gui, mouseX, mouseY, hoveredNode);
	}
	/** 悬停配方卡：左侧逐行列出原料(图标+名称+总需求) → 箭头 → 产物；底色用原版 tooltip 样式，跟随鼠标并夹在屏幕内 */
	private void renderRecipeCard(GuiGraphics gui, int mouseX, int mouseY, Node n) {
		var font = mc.font;
		var items = n.items();
		int rowH = 18;
		int nameW = 0, countW = 0;
		for (ReplenishEntry e : items) {
			nameW = Math.max(nameW, font.width(e.material().getHoverName()));
			countW = Math.max(countW, font.width("x" + e.per() * n.craftTimes()));
		}
		int listW = 10 + 18 + 2 + nameW + 6 + countW;
		Component outName = n.target().getHoverName();
		int outNameW = font.width(outName);
		// 产出数量 = 合成次数 × 单次产出（如 4 次 × 9 = 36），避免与「次数」混淆
		String outCnt = "x" + n.craftTimes() * outPer(n);
		int outCntW = font.width(outCnt);
		int cardW = 3 + listW + 6 + 42 + 6 + 16 + 6 + outNameW + 6 + outCntW + 3;
		int cardH = 3 + Math.max(items.size() * rowH, 18) + 3;
		var window = mc.getWindow();
		int cardX = Mth.clamp(mouseX + 12, 0, Math.max(0, window.getGuiScaledWidth() - cardW));
		int cardY = Mth.clamp(mouseY + 12, 0, Math.max(0, window.getGuiScaledHeight() - cardH));
		// 抬到列表物品(renderItem z=150)/数字(z=200)之上，否则被盖住
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, 300);
		// 原版 tooltip 底（渐变 + 边框）
		TooltipRenderUtil.renderTooltipBackground(gui, cardX, cardY, cardW, cardH, 0);
		// 左侧：逐行原料（勾选框 + 图标 + 名称 + 总需求数量）
		for (var i = 0; i < items.size(); i++) {
			ReplenishEntry e = items.get(i);
			int y = cardY + 3 + i * rowH;
			// 勾选框（同列表：□ 空心方块 + 够料时叠 ✔ 绿勾）
			gui.drawString(font, "□", cardX + 3, y + 5, e.enough() ? 0x668D7F6B : 0xFF8D7F6B, false);
			if (e.enough()) gui.drawString(font, "✔", cardX + 3, y + 4, 0xFF31B25D, false);
			gui.renderItem(e.material(), cardX + 13, y + 1);
			// 充足=绿 / 不足=红
			// 够=绿；不够但自己可合成（会被继续拆解）=黄；不够且是原材料=红
			int color = e.enough() ? 0xFF55FF55 : e.craftable() ? 0xFFFFD700 : 0xFFFF5555;
			gui.drawString(font, e.material().getHoverName(), cardX + 33, y + 5, color, true);
			String cnt = "x" + e.per() * n.craftTimes();
			gui.drawString(font, cnt, cardX + 3 + listW - font.width(cnt), y + 5, color, true);
		}
		// 右侧：箭头 + 产物
		int midY = cardY + cardH / 2;
		int arrowX = cardX + 3 + listW + 6;
		AllGuiTextures.JEI_ARROW.render(gui, arrowX, midY - 5);
		int outX = arrowX + 42 + 6;
		// 产物（无背景框）
		gui.renderItem(n.target(), outX, midY - 8);
		// 产物名称 + 可合成次数
		gui.drawString(font, outName, outX + 20, midY - 5, 0xFFFFFFFF, true);
		gui.drawString(font, outCnt, cardX + cardW - 3 - outCntW, midY - 5, 0xFFAAAAAA, true);
		pose.popPose();
	}
	/** 该节点单次配方的产出数量（至少 1） */
	private static int outPer(Node n) {
		return mc.level == null ? 1 : Math.max(1, n.recipe().getResultItem(mc.level.registryAccess()).getCount());
	}
	private void renderScrollbar(GuiGraphics gui) {
		int barX = scrollX();
		int barSize = thumbHeight();
		if (barSize >= scrollTrackHeight() - 2) return; // 内容不足，无需滚动条
		// 整根条随滚动平移（Create 做法：通道+上下端+中握）
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, thumbTop() - scrollTrackTop(), 0);
		int baseY = scrollTrackTop();
		var pad = AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_PAD;
		gui.blit(pad.location, barX, baseY, SCROLL_W, barSize, pad.getStartX(), pad.getStartY(), pad.getWidth(), pad.getHeight(), 256,
			256);
		AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_TOP.render(gui, barX, baseY);
		if (barSize > 16) AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_MID.render(gui, barX, baseY + barSize / 2 - 4);
		AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_BOT.render(gui, barX, baseY + barSize - 5);
		pose.popPose();
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && maxScroll() > 0 && overScrollbar(mouseX, mouseY)) {
			scrollDragging = true;
			scrollDragOffset = mouseY - thumbTop();
			return true;
		}
		// 点击地址框外取消聚焦（Create 同款：框点到自己/被消费则保持，否则失焦）
		for (AddressEditBox box : addrBoxes) {
			if (!box.isFocused()) continue;
			boolean result = box.mouseClicked(mouseX, mouseY, button);
			if (box.isHovered() || result) return result;
			box.setFocused(false);
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	private int maxScroll() {return Math.max(0, contentH - viewH);}
	private boolean overScrollbar(double mouseX, double mouseY) {
		return mouseX >= scrollX()
			&& mouseX < scrollX() + SCROLL_W
			&& mouseY >= scrollTrackTop()
			&& mouseY < scrollTrackTop() + scrollTrackHeight();
	}
	private int thumbTop() {
		int thumb = thumbHeight();
		int maxThumbTop = scrollTrackHeight() - thumb;
		return scrollTrackTop() + (maxScroll() == 0 ? 0 : (int) ((float) scroll / maxScroll() * maxThumbTop));
	}
	private int scrollX() {return windowXOffset + CONTENT_R - SCROLL_W - 4;}
	private int scrollTrackTop() {return windowYOffset + BODY_TOP;}
	private int scrollTrackHeight() {return Math.max(1, viewH);}
	/** Create 比例滚动条拇指高：barSize = Max(5, floor(视口/总内容 × (视口-2)))，总内容 = maxScroll + 视口 */
	private int thumbHeight() {
		int viewport = scrollTrackHeight();
		int total = Math.max(viewport, maxScroll() + viewport);
		return Math.max(5, Mth.floor((float) viewport / total * (viewport - 2)));
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (scrollDragging && button == 0) {
			int track = scrollTrackHeight();
			int thumb = thumbHeight();
			int maxThumbTop = track - thumb;
			var top = (int) Mth.clamp(mouseY - scrollDragOffset, scrollTrackTop(), scrollTrackTop() + maxThumbTop);
			int maxScroll = maxScroll();
			scroll = maxScroll == 0 ? 0 : (int) ((float) (top - scrollTrackTop()) / maxThumbTop * maxScroll);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (scrollDragging && button == 0) {
			scrollDragging = false;
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		// 先交给子控件（地址框建议列表等）处理
		if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
		if (overScrollbar(mouseX, mouseY)) return true;
		scroll = Mth.clamp((int) (scroll - scrollY * 20), 0, maxScroll());
		return true;
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	private enum RowKind {
		GROUP,
		NODE,
		ADDR
	}
	/** 预计算的一行：y=内容相对顶部偏移（含 BODY_TOP 起始），kind=行类型，gi/ni/mi 索引到数据 */
	private record Row(int y, int h, RowKind kind, int gi, int ni, int mi) {}
	/** 底部自绘按钮：正方形返回 / 长条发送（hover 用 Create 高亮） */
	private static final class CCGButton extends AbstractSimiWidget {
		private final ResourceLocation bg;      // 普通状态背景精灵
		private final AllGuiTextures hover;     // 可空：悬停高亮纹理(Create BUTTON_HOVER / SEND_HOVER)
		private final boolean hoverNative;      // 高亮是否按原生尺寸居中(发送条 SEND_HOVER 防箭头畸变)
		private final AllIcons innerIcon;       // 可空：内部图标(返回箭头)
		private final Component label;          // 可空：文字(发送)
		private final Runnable action;
		CCGButton(
			int x,
			int y,
			int w,
			int h,
			ResourceLocation bg,
			AllGuiTextures hover,
			boolean hoverNative,
			AllIcons innerIcon,
			Component label,
			Runnable action
		) {
			super(x, y, w, h, label == null ? Component.empty() : label);
			this.bg = bg;
			this.hover = hover;
			this.hoverNative = hoverNative;
			this.innerIcon = innerIcon;
			this.label = label;
			this.action = action;
		}
		/** 悬停提示（同 Create IconButton 的 setToolTip） */
		CCGButton tooltip(Component text) {
			toolTip.clear();
			toolTip.add(text);
			return this;
		}
		@Override
		public void doRender(@NotNull GuiGraphics gui, int mx, int my, float pt) {
			if (isHovered() && hover != null) if (hoverNative) {
				// 高亮(SEND_HOVER 80x20)按原生尺寸居中叠加，避免拉伸导致箭头畸变
				int hw = hover.getWidth(), hh = hover.getHeight();
				gui.blit(
					hover.location,
					getX() + (getWidth() - hw) / 2,
					getY() + (getHeight() - hh) / 2,
					hover.getStartX(),
					hover.getStartY(),
					hw,
					hh
				);
			} else gui.blit(hover.location, getX(), getY(), hover.getStartX(), hover.getStartY(), getWidth(), getHeight());
			else if (bg != null) gui.blitSprite(bg, getX(), getY(), getWidth(), getHeight());
			if (innerIcon != null) innerIcon.render(gui, getX() + (getWidth() - 16) / 2, getY() + (getHeight() - 16) / 2);
			// 发送条右端有箭头，文字左对齐防止重叠
			if (label != null) {
				int tw = mc.font.width(label);
				// 文字居中 + 深灰 0x252525（同 Create 仓管发送按钮）
				gui.drawString(mc.font, label, getX() + (getWidth() - tw) / 2, getY() + (getHeight() - 9) / 2 + 1, 0x252525, false);
			}
		}
		@Override
		public void onClick(double mx, double my) {
			// 点击音效（同 Create：playUiSound(UI_BUTTON_CLICK)）
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
			action.run();
		}
	}
}
