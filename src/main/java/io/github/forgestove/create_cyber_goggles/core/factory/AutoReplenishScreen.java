package io.github.forgestove.create_cyber_goggles.core.factory;
import com.simibubi.create.content.logistics.*;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts.CraftingEntry;
import com.simibubi.create.foundation.gui.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.compat.jei.CCGJeiTitles;
import io.github.forgestove.create_cyber_goggles.core.factory.ReplenishGroup.*;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.*;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
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
import net.minecraft.world.item.*;
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
 * 每节点显示其产物 + 全部原料（够的绿勾/不足红框）+ 可合成次数；发送区是<b>全局一块</b>（不分配方类型、
 * 不分轮次），列出所有待发物品。点发送只发<b>原料齐备</b>的配方——缺料的跳过，等它依赖的产物到货后
 * 重开界面再发，与发送区显示同源。下单与原版一致：装配类 convertRecipe（9 格 pattern），
 * 加工类通用 pattern，orderedStacks=每原料×craftTimes。
 *
 * <p>性能：内容预计算成<b>扁平行列表</b>（{@link #rows}），渲染时<b>只画视口内的行</b>，
 * 高度缓存 O(1)，不再逐帧全量重画全部原料。</p>
 */
public class AutoReplenishScreen extends AbstractSimiScreen {
	/** 地址缓存：仅进程内有效（关掉游戏即清空），键为组键（配方类型） */
	private static final Map<String, String> CACHE_ADDRS = new LinkedHashMap<>();
	// 面板几何（与 Create stock_keeper 同宽）
	private static final int PANEL_W = 256;
	private static final int HEADER_H = 16;
	private static final int FOOTER_H = 47;  // 用户 band3 底部背景高(棕色+灰边+烘好的方块/长条)
	// Create stock_keeper 块的棕色面板内容是居中内嵌的（两侧带灰色金属边栏）：x=33..223。
	// 内容(行/地址框/滚动条/底部按钮)必须对齐到这个棕色区域，否则会溢出到透明边。
	private static final int CONTENT_L = 36;
	private static final int CONTENT_R = 228;
	private static final int NODE_H = 20;          // 节点行高(图标18 + 上下各1 → 行间 2，与左右间距一致)
	private static final int BP_CORNER = 8;        // 蓝图角块边长（保持原 8px 像素密度）
	private static final int BP_TILE = 4;          // 蓝图边/中的平铺单位（整除物品格 20，任意行列数都不裁切）
	private static final int BP_PAD = 2;         // 蓝图左右内边距（受滚动条限制：9 列时宽 184 = contentW()）
	private static final int NODES_PER_ROW = 9;    // 节点网格每行最多 9 个
	private static final int CELL_W = 20;          // 网格单元宽(18 图标 + 2 间距)
	private static final int GROUP_H = 20;         // 组头行高
	private static final int GROUP_INDENT = 3;     // 组头（三角+文字）整体右移量
	private static final int ADDR_H = 20;          // 地址框行高（羊皮纸纹理 18 + 上边距 2，下边距 0）
	private static final int GROUP_GAP = 0;         // 组间距
	private static final int SEND_UP_H = 21;        // 发送物品框：上段高（含第一行物品）
	private static final int SEND_MID_H = 17;       // 发送物品框：中段高（每多一行加一段）
	private static final int SEND_DOWN_H = 11;      // 发送物品框：下段高
	private static final int SEND_CELL_W = 17;      // 发送物品框：格宽（= 物品宽，间距 0）
	private static final int SEND_ITEM_Y = SEND_UP_H - 16;   // 发送物品框：第一行物品的 y 偏移（UP 内靠底）
	private static final int SEND_LABEL_H = 12;     // 发送区域：各轮次子块标题行高
	private static final int SCROLL_W = 5;         // 滚动条宽
	private static final int BODY_TOP = HEADER_H;  // 内容起始(在头下方)
	// 地址行局部坐标
	private static final int ADDR_X = CONTENT_L;
	private static final int ADDR_TEXT_IN = 12;      // 文本左缩进(越过圆环)
	private static final int SUGGESTION_ANCHOR_BASE_Y = -72;
	private static final int SUGGESTION_MAX_ROWS = 7;    // 下拉最多行数（Create 的 suggestionLineLimit）
	private static final int SUGGESTION_ROW_H = 12;
	private final StockKeeperRequestScreen parent;
	private final StockTickerBlockEntity blockEntity;
	private final List<ReplenishGroup> groups;
	/** 缺失物品（全局一块，mixin 算好）：找不到配方的需求 + 不可合成原料的缺口，数量 = 还差多少个 */
	private final List<ItemStack> missing;
	private final List<AddressEditBox> addrBoxes = new ArrayList<>();
	private final List<String> groupAddress = new ArrayList<>();
	private final List<Row> rows = new ArrayList<>();
	/** 发送区显示物品：全部待发节点的原料按 {@link Item} 合并（数量 = 单次用量 × 可合成次数），全局一块、不分轮次 */
	private final List<ItemStack> sendItems = new ArrayList<>();
	/** 收起的组（按组索引），收起后只画组头（Create 同款 categoryEntry.hidden） */
	private final Set<Integer> hiddenGroups = new HashSet<>();
	/** 平滑滚动（Create 同款 LerpedFloat：滚轮按行推进后指数追赶目标值） */
	private final LerpedFloat scrollAnim = LerpedFloat.linear().startWithValue(0);
	/** 内容区裁剪范围（绝对屏幕坐标），供 {@link CCGAddressEditBox} 裁剪自身渲染 */
	int scissorL, scissorT, scissorR, scissorB;
	private int contentH;        // 内容总高（缓存，O(1)）
	private int panelH;          // 面板总高（init 时算）
	private int viewH;           // 可视内容高（= 滚动轨道高）
	private int scroll;
	private boolean scrollDragging;
	private double scrollDragOffset;
	private int mouseXPos, mouseYPos;   // 本帧鼠标位置（行内悬停判定用）
	private Node hoveredNode;           // 本帧悬停的配方节点（tooltip 用）
	private ItemStack hoveredSendItem;  // 本帧悬停的发送物品（tooltip 用）
	public AutoReplenishScreen(
		StockKeeperRequestScreen parent,
		StockTickerBlockEntity blockEntity,
		List<ReplenishGroup> groups,
		List<ItemStack> missing
	) {
		this.parent = parent;
		this.blockEntity = blockEntity;
		this.groups = groups;
		this.missing = missing;
		// 每类型地址（取进程内缓存，没有则空）
		for (ReplenishGroup group : groups) groupAddress.add(CACHE_ADDRS.getOrDefault(group.name(), ""));
		rebuildLayout();
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1));
	}
	/** 预计算扁平行列表 + 内容总高（数据不变，一次生成即可，渲染复用） */
	private void rebuildLayout() {
		buildSendItems();
		rows.clear();
		int y = BODY_TOP;
		for (var gi = 0; gi < groups.size(); gi++) {
			ReplenishGroup g = groups.get(gi);
			rows.add(new Row(y, GROUP_H, RowKind.GROUP, gi));
			y += GROUP_H;
			if (hiddenGroups.contains(gi)) continue;   // 收起：跳过组内所有行
			// 节点整合成网格：同类型从左往右，每行最多 NODES_PER_ROW 个，超出换行；
			// 整组只占一个行条目（高度 = 行数 × NODE_H），由 drawNode 画一条竖向扩展的蓝图横幅
			int nodeRows = (g.nodes().size() + NODES_PER_ROW - 1) / NODES_PER_ROW;
			if (nodeRows > 0) {
				int h = nodeRows * NODE_H + BP_PAD * 2;
				rows.add(new Row(y, h, RowKind.NODE, gi));
				y += h;
			}
			rows.add(new Row(y, ADDR_H, RowKind.ADDR, gi));
			y += ADDR_H + GROUP_GAP;
		}
		// 缺失物品：全局一块（找不到配方的需求 + 不可合成原料的缺口），排在发送区之前
		if (!missing.isEmpty()) {
			int missingLines = (missing.size() + NODES_PER_ROW - 1) / NODES_PER_ROW;
			int missingH = SEND_LABEL_H + SEND_UP_H + SEND_DOWN_H + (missingLines - 1) * SEND_MID_H;
			rows.add(new Row(y, missingH, RowKind.MISSING, -1));
			y += missingH;
		}
		// 发送区：全局一块（不分配方类型、不分轮次），排在所有组之后
		if (!sendItems.isEmpty()) {
			int lines = Math.max(1, (sendItems.size() + NODES_PER_ROW - 1) / NODES_PER_ROW);
			int sendH = SEND_LABEL_H + SEND_UP_H + SEND_DOWN_H + (lines - 1) * SEND_MID_H;
			rows.add(new Row(y, sendH, RowKind.SEND, -1));
			y += sendH;
		}
		contentH = y;
	}
	/**
	 * 汇总待发送的原料：全部节点（{@code craftTimes>0}）的原料按 {@link Item} 合并成<b>一个全局块</b>，
	 * 不分配方类型、不分轮次，数量 = 单次用量 × 可合成次数。{@link #sendAll()} 从同一批节点取用，
	 * 显示与实际发送同源。缺失物品由 mixin 算好后经构造函数传入（见 {@link #missing}）。
	 */
	private void buildSendItems() {
		sendItems.clear();
		Map<Item, Integer> merged = new LinkedHashMap<>();
		for (ReplenishGroup g : groups)
			for (Node n : g.nodes()) {
				if (n.craftTimes() <= 0 || n.items().isEmpty()) continue;
				for (ReplenishEntry e : n.items()) merged.merge(e.material().getItem(), e.per() * n.craftTimes(), Integer::sum);
			}
		merged.forEach((item, count) -> sendItems.add(new ItemStack(item, count)));
	}
	@Override
	protected void init() {
		// resize 会重跑 init：Screen 已清空 widgets，这里同步清掉旧引用，否则 placeAddr 会拿到不在 widgets 里的旧框（无法交互）
		addrBoxes.clear();
		// contentH 已含 BODY_TOP(=HEADER_H)，不能再加一次，否则视口比内容高 16px（滚到底时末尾留白）
		int needed = Math.max(80, contentH + FOOTER_H);
		int screenH = mc.getWindow().getGuiScaledHeight();
		panelH = Math.min(screenH - 18, needed);
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
			var box = new CCGAddressEditBox(this, mc.font, windowXOffset + ADDR_X + ADDR_TEXT_IN, windowYOffset + BODY_TOP, boxW, 10,
				true);
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
	/**
	 * 发送所有<b>原料齐备</b>的节点：任一原料按 craftTimes 的用量在仓库里不够 → 跳过该配方
	 * （等它依赖的产物到货后重开界面再发）。发送内容与发送区显示的物品同源。
	 * 装配类走 9 格 pattern（convertRecipe），加工类用通用 pattern，orderedStacks = 每原料 × craftTimes。
	 */
	private void sendAll() {
		// 共享原料按序扣减，避免两个配方都以为同一批库存够用
		Map<Item, Integer> remaining = new HashMap<>();
		for (var i = 0; i < groups.size(); i++) {
			String addr = groupAddress.get(i);
			for (Node n : groups.get(i).nodes()) {
				if (n.craftTimes() <= 0 || n.items().isEmpty()) continue;
				if (addr == null || addr.isBlank()) {
					CCG.LOGGER.info("ccg autoReplenish: 未填地址, 跳过 {}", n.target().getHoverName().getString());
					continue;
				}
				if (!hasMaterials(n, remaining)) {
					CCG.LOGGER.info("ccg autoReplenish: 原料不足, 跳过 {}", n.target().getHoverName().getString());
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
	/**
	 * 该配方此刻是否原料齐备：每个原料按 {@code per × craftTimes} 的用量都要有库存。
	 * 可合成但仓库还没有的中间产物算「不齐」——它得等自己那一轮发完到货后重开界面。
	 * 齐备才从 {@code remaining} 扣减，避免多个配方重复占用同一批库存。
	 */
	private boolean hasMaterials(Node n, Map<Item, Integer> remaining) {
		var summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
		if (summary == null) return false;
		for (ReplenishEntry e : n.items()) {
			Item item = e.material().getItem();
			int have = remaining.computeIfAbsent(item, k -> summary.getCountOf(k.getDefaultInstance()));
			if (have < e.per() * n.craftTimes()) return false;
		}
		for (ReplenishEntry e : n.items()) remaining.merge(e.material().getItem(), -e.per() * n.craftTimes(), Integer::sum);
		return true;
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
		scrollAnim.tickChaser();
		// 逼近目标时直接吸附，避免长期微小抖动（Create 同款 1/16 阈值）
		if (Math.abs(scrollAnim.getValue() - scrollAnim.getChaseTarget()) < 1 / 16f) scrollAnim.setValue(scrollAnim.getChaseTarget());
	}
	/** Create 蓝图横幅：左端(10) + 中端(4×n) + 右端(10)，高 25（待合成物品的蓝图框） */
	@Override
	protected void renderMenuBackground(@NotNull GuiGraphics gui, int x, int y, int width, int height) {}
	/** 蓝图横幅（竖向可扩展）：顶 3px + 中间 18px 平铺 + 底 4px；横向 左10 + 中4×n + 右10 */
	@Override
	protected void renderWindowBackground(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, -500);
		var current = mc.screen;
		mc.screen = parent;
		try {
			parent.renderBackground(gui, -1, -1, partialTick);
			parent.render(gui, -1, -1, partialTick);
		} finally {
			mc.screen = current;
		}
		// 压暗父屏，让内嵌面板的透明边看起来像 Create 的菜单遮罩
		gui.fill(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), 0x66_101010);
		pose.popPose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		CCGGuiTextures.AUTO_REPLENISH_HEADER.render(gui, 0, 0);
		var body = CCGGuiTextures.AUTO_REPLENISH_BODY;
		int bodyEnd = panelH - FOOTER_H;
		for (int y = HEADER_H; y < bodyEnd; y += 20)
			gui.blit(body.location, 0, y, body.getStartX(), body.getStartY(), PANEL_W, Math.min(20, bodyEnd - y));
		CCGGuiTextures.AUTO_REPLENISH_FOOTER.render(gui, 0, bodyEnd);
		pose.popPose();
	}
	@Override
	protected void renderWindow(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		mouseXPos = mouseX;
		mouseYPos = mouseY;
		hoveredNode = null;
		hoveredSendItem = null;
		int clamped = Mth.clamp((int) scrollAnim.getValue(partialTick), 0, maxScroll());
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		Font font = mc.font;
		Component title = Component.translatable("create_cyber_goggles.gui.auto_replenish.title");
		gui.drawString(font, title, (PANEL_W - font.width(title)) / 2, 4, 0xFF4A3728, false);
		// 可视窗口区（窗口局部）：[BODY_TOP, BODY_TOP + viewH)
		int scissorBottom = windowYOffset + BODY_TOP + Math.max(1, viewH);
		scissorL = windowXOffset + CONTENT_L;
		scissorT = windowYOffset + BODY_TOP;
		scissorR = windowXOffset + CONTENT_R;
		scissorB = scissorBottom;
		gui.enableScissor(scissorL, scissorT, scissorR, scissorB);
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
				case SEND -> drawSend(gui, r, top);
				case MISSING -> drawMissing(gui, r, top);
				case ADDR -> placeAddr(gui, r, top);
			}
		}
		gui.disableScissor();
		pose.popPose();
		if (maxScroll() > 0) renderScrollbar(gui, partialTick);
	}
	/** 可滚动距离：内容高 − 视口高。contentH 含 BODY_TOP 起始偏移，必须减掉，否则滚到底时末尾留白 */
	private int maxScroll() {return Math.max(0, contentH - BODY_TOP - viewH);}
	private void drawGroup(GuiGraphics gui, Font font, Row r, int top) {
		int gx = CONTENT_L + GROUP_INDENT;
		// 左侧展开/关闭三角（Create 同款纹理：收起用 HIDDEN、展开用 SHOWN），文字右移让位
		(
			hiddenGroups.contains(r.gi()) ? AllGuiTextures.STOCK_KEEPER_CATEGORY_HIDDEN : AllGuiTextures.STOCK_KEEPER_CATEGORY_SHOWN
		).render(gui, gx, top + (GROUP_H - 8) / 2);
		// 组头：只有类型名，无背景框；文本上下居中；阴影用 Create 双画法（深色右下 1px + 前景原位）
		var name = groupName(groups.get(r.gi()));
		int x = gx + 10, y = top + (GROUP_H - 9) / 2 + 1;
		gui.drawString(font, name, x + 1, y + 1, 0x4A2D31, false);
		gui.drawString(font, name, x, y, 0xF8F8EC, false);
	}
	/** 一行节点网格：蓝图横幅作整行底，节点图标从左往右排（最多 NODES_PER_ROW 个），悬停弹配方卡 */
	private void drawNode(GuiGraphics gui, Row r, int top) {
		ReplenishGroup g = groups.get(r.gi());
		List<Node> nodes = g.nodes();
		// 整组一条蓝图横幅，正好包住该组物品网格（列数 × 行数）
		int colCount = Math.min(nodes.size(), NODES_PER_ROW);
		int rowCount = (nodes.size() + NODES_PER_ROW - 1) / NODES_PER_ROW;
		drawBlueprint(gui, CONTENT_L, top, colCount, rowCount);
		for (var i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			int craftTimes = n.craftTimes();
			int totalOut = craftTimes * outPer(n);
			int cellX = CONTENT_L + BP_PAD + i % NODES_PER_ROW * CELL_W + 2;
			int iconY = top + BP_PAD + i / NODES_PER_ROW * NODE_H + 2;   // 16×16 图标在 20×20 格内居中
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
			drawCount(gui, totalOut, cellX, iconY);
		}
	}
	/** 将要发送的物品：全局一块（不分配方类型、不分轮次），横向固定 9 格 */
	private void drawSend(GuiGraphics gui, Row r, int top) {
		drawSendLabel(gui, Component.translatable("create_cyber_goggles.gui.auto_replenish.send_items"), top);
		drawItemBox(gui, sendItems, top + SEND_LABEL_H);
	}
	/** 缺失物品：全局一块，与发送区同款布局 */
	private void drawMissing(GuiGraphics gui, Row r, int top) {
		drawSendLabel(gui, Component.translatable("create_cyber_goggles.gui.auto_replenish.missing"), top);
		drawItemBox(gui, missing, top + SEND_LABEL_H);
	}
	/** 发送区域子块标题：组头同款双画阴影 */
	private static void drawSendLabel(GuiGraphics gui, Component text, int top) {
		Font font = mc.font;
		int x = CONTENT_L + 5, y = top + (SEND_LABEL_H - 9) / 2 + 1;
		gui.drawString(font, text, x + 1, y + 1, 0x4A2D31, false);
		gui.drawString(font, text, x, y, 0xF8F8EC, false);
	}
	/** 画一个发送物品框（UP + MIDDLE×(行数-1) + DOWN）及其中的物品 */
	private void drawItemBox(GuiGraphics gui, List<ItemStack> items, int blockTop) {
		int lines = Math.max(1, (items.size() + NODES_PER_ROW - 1) / NODES_PER_ROW);
		CCGGuiTextures.AUTO_REPLENISH_BOX_UP.render(gui, CONTENT_L, blockTop);
		int y = blockTop + SEND_UP_H;
		for (var i = 1; i < lines; i++) {
			CCGGuiTextures.AUTO_REPLENISH_BOX_MIDDLE.render(gui, CONTENT_L, y);
			y += SEND_MID_H;
		}
		CCGGuiTextures.AUTO_REPLENISH_BOX_DOWN.render(gui, CONTENT_L, y);
		int x = CONTENT_L + 5;
		for (var i = 0; i < items.size(); i++) {
			ItemStack stack = items.get(i);
			int ix = x + i % NODES_PER_ROW * SEND_CELL_W;
			int iy = blockTop + SEND_ITEM_Y + i / NODES_PER_ROW * SEND_MID_H;
			// 悬停：与节点图标同款放大（Create renderItemEntry: scaleFromHover += .075f）
			boolean hov = mouseXPos >= windowXOffset + ix
				&& mouseXPos < windowXOffset + ix + SEND_CELL_W
				&& mouseYPos >= windowYOffset + iy
				&& mouseYPos < windowYOffset + iy + SEND_CELL_W;
			if (hov) {
				var pose = gui.pose();
				pose.pushPose();
				pose.translate(ix + 8, iy + 8, 0);
				pose.scale(1.075f, 1.075f, 1);
				pose.translate(-(ix + 8), -(iy + 8), 0);
				gui.renderItem(stack, ix, iy);
				pose.popPose();
				hoveredSendItem = stack;
			} else gui.renderItem(stack, ix, iy);
			drawCount(gui, stack.getCount(), ix, iy);
		}
	}
	private void placeAddr(GuiGraphics gui, Row r, int top) {
		// 羊皮纸地址框(九宫格可拉伸) + 其上放置可输入 widget（左缩进越过圆环）
		//		gui.blitSprite(sprite("address_box"), ADDR_X, top, contentW(), ADDR_H);
		// 纹理 18 高，行高 20：上边距 2、下边距 0
		CCGGuiTextures.AUTO_REPLENISH_ADDRESS.render(gui, 0, top + 2);
		AddressEditBox box = addrBoxes.get(r.gi());
		box.setVisible(true);
		box.setX(windowXOffset + ADDR_X + ADDR_TEXT_IN);
		// 上边距 2（对齐纹理），其中 1px 用于补偿 EditBox 硬编码 8px 字高(实为9px)导致的偏高
		box.setY(windowYOffset + top + (ADDR_H - 10) / 2 + 2);
		// 地址框在滚动内容里，需让剪贴板下拉的 yOffset 跟随当前 boxY（Create 是固定框不滚，这里每次 reposition 刷新）
		refreshSuggestionsAnchor(box);
		// 暗色"包裹地址"占位：值空且未聚焦时（Create 同款：drawString 斜体 0xCDBCA8）
		if (box.getValue().isBlank() && !box.isFocused()) {
			Font font = mc.font;
			Component hint = Component.translatable("create.gui.stock_keeper.package_address").copy().withStyle(ChatFormatting.ITALIC);
			gui.drawString(font, hint, box.getX() - windowXOffset, box.getY() - windowYOffset, 0xFFCDBCA8, false);
		}
	}
	private void renderScrollbar(GuiGraphics gui, float partialTick) {
		int barX = scrollX();
		int barSize = thumbHeight();
		if (barSize >= scrollTrackHeight() - 2) return; // 内容不足，无需滚动条
		// 整根条随滚动平移（Create 做法：通道+上下端+中握）
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, thumbTop(partialTick) - scrollTrackTop(), 0);
		int baseY = scrollTrackTop();
		var pad = AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_PAD;
		gui.blit(pad.location, barX, baseY, SCROLL_W, barSize, pad.getStartX(), pad.getStartY(), pad.getWidth(), pad.getHeight(), 256,
			256);
		AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_TOP.render(gui, barX, baseY);
		if (barSize > 16) AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_MID.render(gui, barX, baseY + barSize / 2 - 4);
		AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_BOT.render(gui, barX, baseY + barSize - 5);
		pose.popPose();
	}
	/** 配方类型的本地化名：优先用 JEI 分类标题，其次 `<命名空间>.recipe.<路径>`，最后退回路径 */
	private static Component groupName(ReplenishGroup group) {
		if (group == null) return Component.empty();
		String groupName = group.name();
		if (groupName == null || groupName.isBlank()) return Component.empty();
		// 原版配方类型（RecipeType.register）的 toString() 是裸名（无命名空间），补 minecraft: 才能对上 JEI 分类 uid
		String lookupKey = groupName.indexOf(':') < 0 ? "minecraft:" + groupName : groupName;
		Component jeiTitle = CCGJeiTitles.get(lookupKey);
		if (jeiTitle != null) return jeiTitle;
		int idx = groupName.indexOf(':');
		if (idx <= 0) return Component.literal(groupName);
		String path = groupName.substring(idx + 1);
		String key = groupName.substring(0, idx) + ".recipe." + path;
		return Component.translatable(I18n.exists(key) ? key : path);
	}
	/**
	 * 蓝图横幅：3×3 图集（角 {@link #BP_CORNER}、边/中平铺单位 {@link #BP_TILE}），4 角固定、边与中心平铺。
	 * 尺寸 = 物品格数 × 格子尺寸 + 内边距，平铺单位整除物品格 20，任意行列数都不裁切。
	 */
	@SuppressWarnings("SameParameterValue")
	private static void drawBlueprint(GuiGraphics g, int x, int y, int cols, int rows) {
		int w = cols * CELL_W + BP_PAD * 2, h = rows * NODE_H + BP_PAD * 2;
		int iw = w - BP_CORNER * 2, ih = h - BP_CORNER * 2;
		blitTile(g, x, y, 0, 0);
		blitTile(g, x + w - BP_CORNER, y, 2, 0);
		blitTile(g, x, y + h - BP_CORNER, 0, 2);
		blitTile(g, x + w - BP_CORNER, y + h - BP_CORNER, 2, 2);
		for (var d = 0; d < iw; d += BP_TILE) {
			blitTile(g, x + BP_CORNER + d, y, 1, 0);
			blitTile(g, x + BP_CORNER + d, y + h - BP_CORNER, 1, 2);
		}
		for (var d = 0; d < ih; d += BP_TILE) {
			blitTile(g, x, y + BP_CORNER + d, 0, 1);
			blitTile(g, x + w - BP_CORNER, y + BP_CORNER + d, 2, 1);
		}
		for (var dy = 0; dy < ih; dy += BP_TILE)
			for (var dx = 0; dx < iw; dx += BP_TILE) blitTile(g, x + BP_CORNER + dx, y + BP_CORNER + dy, 1, 1);
	}
	/** 该节点单次配方的产出数量（至少 1） */
	private static int outPer(Node n) {
		return mc.level == null ? 1 : Math.max(1, n.recipe().getResultItem(mc.level.registryAccess()).getCount());
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
				slotX + 13 + x,
				slotY + 9,
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
	/**
	 * 让地址框的剪贴板下拉锚点(yOffset)跟随当前 boxY：Create 是固定框不滚，这里在滚动后刷新。
	 * yOffset 创建时按初始位置冻结，需用反射更新为 `-72 + box.getY()`(anchorToBottom=true)。
	 */
	private void refreshSuggestionsAnchor(AddressEditBox box) {
		var suggestions = ((AddressEditBoxAccessor) box).getDestinationSuggestions();
		if (suggestions == null) return;
		// 朝上会顶出内容区时改为朝下：anchorToBottom 恒为 true（列表自 yPos 向上排），
		// 把 yPos 抬到框下方足够远处，列表整体就落到框下面；行数取实际建议条数
		var accessor = (DestinationSuggestionsAccessor) suggestions;
		int rows = Math.min(SUGGESTION_MAX_ROWS, accessor.getCurrentSuggestions().size());
		int listH = rows * SUGGESTION_ROW_H;
		int anchor = box.getY() - listH < scissorT ? box.getY() + box.getHeight() + listH + 3 : box.getY();
		accessor.setYOffset(SUGGESTION_ANCHOR_BASE_Y + anchor);
		// 聚焦时让下拉贴住新位置（跟随滚动）
		if (box.isFocused()) suggestions.showSuggestions(false);
	}
	private int scrollX() {return windowXOffset + CONTENT_R - SCROLL_W - 4;}
	/** Create 比例滚动条拇指高：barSize = Max(5, floor(视口/总内容 × (视口-2)))，总内容 = maxScroll + 视口 */
	private int thumbHeight() {
		int viewport = scrollTrackHeight();
		int total = Math.max(viewport, maxScroll() + viewport);
		return Math.max(5, Mth.floor((float) viewport / total * (viewport - 2)));
	}
	private int scrollTrackHeight() {return Math.max(1, viewH);}
	private int thumbTop(float partialTick) {
		int thumb = thumbHeight();
		int maxThumbTop = scrollTrackHeight() - thumb;
		return scrollTrackTop() + (maxScroll() == 0 ? 0 : (int) (scrollAnim.getValue(partialTick) / maxScroll() * maxThumbTop));
	}
	private int scrollTrackTop() {return windowYOffset + BODY_TOP;}
	/** 画蓝图图集中的一块（col/row 为 0..2，各自占 8×8 的格）；中间行列的块是 {@link #BP_TILE}，其余是 {@link #BP_CORNER} */
	private static void blitTile(GuiGraphics gui, int x, int y, int col, int row) {
		var textures = CCGGuiTextures.AUTO_REPLENISH_BLUEPRINT;
		int w = col == 1 ? BP_TILE : BP_CORNER;
		int h = row == 1 ? BP_TILE : BP_CORNER;
		gui.blit(textures.location, x, y, textures.getStartX() + col * BP_CORNER, textures.getStartY() + row * BP_CORNER, w, h);
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
		if (hoveredSendItem != null) gui.renderTooltip(mc.font, hoveredSendItem, mouseX, mouseY);
	}
	/** 悬停配方卡：左侧逐行列出原料(图标+名称+总需求) → 箭头 → 产物；底色用原版 tooltip 样式，跟随鼠标并夹在屏幕内 */
	private void renderRecipeCard(GuiGraphics gui, int mouseX, int mouseY, Node n) {
		var font = mc.font;
		var items = n.items();
		int craftTimes = n.craftTimes();
		int totalOut = craftTimes * outPer(n);
		var rowH = 18;
		var nameW = 0;
		for (ReplenishEntry e : items) nameW = Math.max(nameW, font.width(e.material().getHoverName()));
		int listW = 10 + 18 + 2 + nameW;
		Component outName = n.target().getHoverName();
		int outNameW = font.width(outName);
		int cardW = 3 + listW + 6 + 42 + 6 + 16 + 6 + outNameW + 3;
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
			drawCount(gui, e.per() * craftTimes, cardX + 13, y + 1);
		}
		// 右侧：箭头 + 产物
		int midY = cardY + cardH / 2;
		int arrowX = cardX + 3 + listW + 6;
		AllGuiTextures.JEI_ARROW.render(gui, arrowX, midY - 5);
		int outX = arrowX + 42 + 6;
		// 产物（无背景框）
		gui.renderItem(n.target(), outX, midY - 8);
		// 产物名称；产出数量（合成次数 × 单次产出）用 NUMBERS 图集画在图标右下角
		gui.drawString(font, outName, outX + 20, midY - 5, 0xFFFFFFFF, true);
		drawCount(gui, totalOut, outX, midY - 8);
		pose.popPose();
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && maxScroll() > 0 && overScrollbar(mouseX, mouseY)) {
			scrollDragging = true;
			scrollDragOffset = mouseY - thumbTop(1);
			return true;
		}
		// 组头三角：切换展开/收起（Create 同款：命中分类头行即切换 + 播放音效）
		if (button == 0) {
			int clamped = Mth.clamp((int) scrollAnim.getValue(1), 0, maxScroll());
			int iconX = windowXOffset + CONTENT_L + GROUP_INDENT;
			for (Row r : rows) {
				if (r.kind != RowKind.GROUP) continue;
				int top = r.y - clamped;
				if (top < BODY_TOP || top + GROUP_H > BODY_TOP + viewH) continue;
				int iconY = windowYOffset + top + (GROUP_H - 8) / 2;
				if (mouseX < iconX || mouseX >= iconX + 8 || mouseY < iconY || mouseY >= iconY + 8) continue;
				int gi = r.gi();
				if (!hiddenGroups.remove(gi)) hiddenGroups.add(gi);
				mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.5f));
				rebuildLayout();
				// 收起后内容变短，滚动值可能越界
				scroll = Mth.clamp(scroll, 0, maxScroll());
				scrollAnim.setValue(scroll);
				scrollAnim.updateChaseTarget(scroll);
				return true;
			}
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
	private boolean overScrollbar(double mouseX, double mouseY) {
		return mouseX >= scrollX()
			&& mouseX < scrollX() + SCROLL_W
			&& mouseY >= scrollTrackTop()
			&& mouseY < scrollTrackTop() + scrollTrackHeight();
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (scrollDragging && button == 0) {
			int track = scrollTrackHeight();
			int thumb = thumbHeight();
			int maxThumbTop = track - thumb;
			int maxScroll = maxScroll();
			if (maxThumbTop <= 0 || maxScroll <= 0) {
				scroll = 0;
				scrollAnim.setValue(0);
				scrollAnim.updateChaseTarget(0);
				return true;
			}
			var top = (int) Mth.clamp(mouseY - scrollDragOffset, scrollTrackTop(), scrollTrackTop() + maxThumbTop);
			scroll = (int) ((float) (top - scrollTrackTop()) / maxThumbTop * maxScroll);
			// 拖动直接跟手：必须同步追赶目标，否则 tickChaser 会把值拉回滚轮留下的旧目标（抽搐）
			scrollAnim.setValue(scroll);
			scrollAnim.updateChaseTarget(scroll);
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
		// Create 同款：滚轮按行推进 + 指数追赶，实现平滑滚动
		var direction = (int) (Math.ceil(Math.abs(scrollY)) * -Math.signum(scrollY));
		scroll = Mth.clamp((int) scrollAnim.getChaseTarget() + direction * NODE_H, 0, maxScroll());
		scrollAnim.chase(scroll, 0.5, Chaser.EXP);
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
		SEND,
		MISSING,
		ADDR
	}
	/** 预计算的一行：y=内容相对顶部偏移（含 BODY_TOP 起始），kind=行类型，gi 索引到组数据（全局行 = -1） */
	private record Row(int y, int h, RowKind kind, int gi) {}
	/** 底部自绘按钮：正方形返回 / 长条发送（hover 用 Create 高亮） */
	private static final class CCGButton extends AbstractSimiWidget {
		private final ResourceLocation bg;      // 普通状态背景精灵
		private final AllGuiTextures hover;     // 可空：悬停高亮纹理(Create BUTTON_HOVER / SEND_HOVER)
		private final boolean hoverNative;      // 高亮是否按原生尺寸居中(发送条 SEND_HOVER 防箭头畸变)
		private final AllIcons innerIcon;       // 可空：内部图标(返回箭头)
		private final Component label;          // 可空：文字(发送)
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
			withCallback(action);
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
	}
}
