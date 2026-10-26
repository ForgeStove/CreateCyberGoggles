package io.github.forgestove.create_cyber_goggles.factory;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.logistics.*;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts.CraftingEntry;
import com.simibubi.create.content.trains.schedule.DestinationSuggestions;
import com.simibubi.create.foundation.gui.*;
import io.github.forgestove.create_cyber_goggles.compat.jei.CCGJeiTitles;
import io.github.forgestove.create_cyber_goggles.factory.ReplenishGroup.*;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.*;
import io.github.forgestove.create_cyber_goggles.util.*;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ScreenEvent.CharacterTyped.Pre;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.util.CCGUtil.mc;
/**
 * 「自动补齐缺货」独立覆盖层 GUI，宿主屏幕提供 {@link CCGReplenishTree} 能力。
 * 内容预计算成 {@link #rows} 扁平行列表，渲染时只画视口内的行。
 */
public class ReplenishScreen extends AbstractSimiScreen {
	private static final Logger LOGGER = LogUtils.getLogger();
	/** 组键 → 地址，仅进程内有效。 */
	private static final Map<String, String> CACHE_ADDRS = new LinkedHashMap<>();
	private static final int PANEL_W = 256;
	private static final int HEADER_H = 16;
	private static final int FOOTER_H = 47; // 底部纹理高度
	// 内容位于面板中央的棕色内嵌区（两侧为灰色金属边栏），必须对齐，否则溢出到透明边。
	private static final int CONTENT_L = 36;
	private static final int CONTENT_R = 228;
	private static final int NODE_H = 20;
	private static final int BP_PAD = 2; // 9 列时宽度正好等于内容宽
	private static final int NODES_PER_ROW = 9;
	private static final int CELL_W = 20;
	private static final int GROUP_H = 20;
	private static final int GROUP_INDENT = 3;
	private static final int ADDR_H = 20; // 纹理高 18 + 上边距 2
	private static final int GROUP_GAP = 0;
	private static final int SEND_UP_H = 21; // 九宫格上段
	private static final int SEND_MID_H = 17; // 九宫格中段，每多一行加一段
	private static final int SEND_DOWN_H = 11; // 九宫格下段
	private static final int SEND_CELL_W = 17; // 格宽 = 物品宽，间距 0
	private static final int SEND_ITEM_Y = SEND_UP_H - 16; // 第一行物品在 UP 段内靠底
	private static final int SEND_LABEL_H = 12;
	private static final int SCROLL_W = 5;
	private static final int BODY_TOP = HEADER_H;
	private static final int ADDR_X = CONTENT_L;
	private static final int ADDR_TEXT_IN = 12; // 文本左移量，避开左端圆环
	/** 视口外地址框的停放坐标。 */
	private static final int PARKED = -10000;
	private static final int SUGGESTION_ANCHOR_BASE_Y = -72;
	private static final int SUGGESTION_MAX_ROWS = 7;
	private static final int SUGGESTION_ROW_H = 12;
	private static final int POPUP_ROW_H = 14;
	private static final int SWITCH_HINT_H = 11;
	/** 字符投递钩子是否已注册。 */
	private static boolean charHooked;
	private final Screen parent;
	private final CCGReplenishTree host;
	private final List<ReplenishGroup> groups;
	private final List<AddressEditBox> addrBoxes = new ArrayList<>();
	private final List<String> groupAddress = new ArrayList<>();
	private final List<Row> rows = new ArrayList<>();
	/** 每组待发送的原料，按 {@link Item} 合并，数量 = 单次用量 × 可合成次数。 */
	private final List<List<ItemStack>> sendItems = new ArrayList<>();
	/** 已收起的组索引。 */
	private final Set<Integer> hiddenGroups = new HashSet<>();
	private final LerpedFloat scrollAnim = LerpedFloat.linear().startWithValue(0);
	/** 内容区裁剪范围，绝对屏幕坐标。 */
	int scissorL, scissorT, scissorR, scissorB;
	private int contentH;
	private int panelH;
	private int viewH;
	private int scroll;
	private int restoreScroll = -1; // 重建界面后恢复的滚动位置，-1 表示顶部
	private boolean scrollDragging;
	private double scrollDragOffset;
	private int mouseXPos, mouseYPos;
	private Node hoveredNode;
	private ItemStack hoveredSendItem;
	private Node recipePopup; // 正在切换配方的节点，null 表示弹窗未打开
	private int popupX, popupY, popupW, popupH; // 弹窗矩形，渲染时计算
	private int hoveredPopupRow = -1; // 弹窗内悬停行，-1 表示不在弹窗上
	public ReplenishScreen(Screen parent, CCGReplenishTree host, List<ReplenishGroup> groups) {
		this.parent = parent;
		this.host = host;
		this.groups = groups;
		for (ReplenishGroup group : groups) groupAddress.add(CACHE_ADDRS.getOrDefault(group.name(), ""));
		rebuildLayout();
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1));
		hookCharTyped();
	}
	/** 预计算扁平行列表与内容总高。 */
	private void rebuildLayout() {
		buildSendItems();
		rows.clear();
		int y = BODY_TOP;
		for (var gi = 0; gi < groups.size(); gi++) {
			ReplenishGroup g = groups.get(gi);
			rows.add(new Row(y, GROUP_H, RowKind.GROUP, gi));
			y += GROUP_H;
			if (hiddenGroups.contains(gi)) continue;
			// 节点排成网格；整组只占一个行条目，高度随行数扩展。
			int nodeRows = (g.nodes().size() + NODES_PER_ROW - 1) / NODES_PER_ROW;
			if (nodeRows > 0) {
				int h = nodeRows * NODE_H + BP_PAD * 2;
				rows.add(new Row(y, h, RowKind.NODE, gi));
				y += h;
			}
			// 无法合成的配方排在可合成节点之后
			int blockedRows = (g.blocked().size() + NODES_PER_ROW - 1) / NODES_PER_ROW;
			if (blockedRows > 0) {
				int h = SEND_LABEL_H + blockedRows * NODE_H + BP_PAD * 2;
				rows.add(new Row(y, h, RowKind.BLOCKED, gi));
				y += h;
			}
			if (!g.missing().isEmpty()) {
				int missingLines = (g.missing().size() + NODES_PER_ROW - 1) / NODES_PER_ROW;
				int missingH = SEND_LABEL_H + SEND_UP_H + SEND_DOWN_H + (missingLines - 1) * SEND_MID_H;
				rows.add(new Row(y, missingH, RowKind.MISSING, gi));
				y += missingH;
			}
			List<ItemStack> send = sendItems.get(gi);
			if (!send.isEmpty()) {
				int sendLines = Math.max(1, (send.size() + NODES_PER_ROW - 1) / NODES_PER_ROW);
				int sendH = SEND_LABEL_H + SEND_UP_H + SEND_DOWN_H + (sendLines - 1) * SEND_MID_H;
				rows.add(new Row(y, sendH, RowKind.SEND, gi));
				y += sendH;
			}
			rows.add(new Row(y, ADDR_H, RowKind.ADDR, gi));
			y += ADDR_H + GROUP_GAP;
		}
		contentH = y;
	}
	/** 以最高优先级把字符投递给聚焦的地址框，消费成功才取消事件。 */
	private static void hookCharTyped() {
		if (charHooked) return;
		charHooked = true;
		NeoForge.EVENT_BUS.addListener(
			EventPriority.HIGHEST, (Pre event) -> {
				if (!(event.getScreen() instanceof ReplenishScreen screen)) return;
				if (!(screen.getFocused() instanceof CCGAddressEditBox box)) return;
				if (box.charTyped(event.getCodePoint(), event.getModifiers())) event.setCanceled(true);
			}
		);
	}
	/** 按配方类型汇总待发送的原料，与 {@link #sendAll()} 同源。 */
	private void buildSendItems() {
		sendItems.clear();
		for (ReplenishGroup g : groups) {
			Map<Item, Integer> merged = new LinkedHashMap<>();
			for (Node n : g.nodes()) {
				if (n.craftTimes() <= 0 || n.items().isEmpty()) continue;
				for (ReplenishEntry e : n.items()) merged.merge(e.material().getItem(), e.per() * n.craftTimes(), Integer::sum);
			}
			List<ItemStack> items = new ArrayList<>();
			merged.forEach((item, count) -> items.add(new ItemStack(item, count)));
			sendItems.add(items);
		}
	}
	private static void drawSendLabel(GuiGraphics gui, Component text, int top) {
		Font font = mc.font;
		int x = CONTENT_L + 5, y = top + (SEND_LABEL_H - 9) / 2 + 1;
		gui.drawString(font, text, x + 1, y + 1, 0x4A2D31, false);
		gui.drawString(font, text, x, y, 0xF8F8EC, false);
	}
	private static Component groupName(ReplenishGroup group) {
		return group == null ? Component.empty() : typeName(group.name());
	}
	@SuppressWarnings("SameParameterValue")
	private static void drawBlueprint(GuiGraphics g, int x, int y, int cols, int rows) {
		CCGGuiTextures.BLUEPRINT.render(g, x, y, cols * CELL_W + BP_PAD * 2, rows * NODE_H + BP_PAD * 2);
	}
	private static int outPer(Recipe<?> recipe) {
		return mc.level == null ? 1 : Math.max(1, recipe.getResultItem(mc.level.registryAccess()).getCount());
	}
	/** 用 NUMBERS 图集把数量画在槽位右下角。 */
	private static void drawCount(GuiGraphics gui, int count, int slotX, int slotY) {
		String text = count >= 1000000
			? count / 1000000 + "m"
			: count >= 10000
				? count / 1000 + "k"
				: count >= 1000 ? (count * 10 / 1000) / 10f + "k" : count >= 100 ? count + "" : " " + count;
		if (count >= BigItemStack.INF) text = "+";
		if (text.isBlank()) return;
		AllGuiTextures numbers = AllGuiTextures.NUMBERS;
		// 抬高 z 以盖住物品（物品画在 z=150）
		PoseStack pose = gui.pose();
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
	@Override
	protected void init() {
		// 清掉旧引用：resize 重跑 init 时 Screen 已清空 widgets，残留的旧框无法交互
		addrBoxes.clear();
		// contentH 已含 BODY_TOP，不再重复相加
		int needed = Math.max(80, contentH + FOOTER_H);
		int screenH = mc.getWindow().getGuiScaledHeight();
		panelH = Math.min(screenH - 18, needed);
		viewH = Math.max(0, panelH - HEADER_H - FOOTER_H);
		setWindowSize(PANEL_W, panelH);
		Window window = mc.getWindow();
		setWindowOffset((window.getGuiScaledWidth() - PANEL_W) / 2, (window.getGuiScaledHeight() - panelH) / 2);
		super.init();
		// 宽度收窄，给右挂的剪贴板图标留位
		int boxW = contentW() - ADDR_TEXT_IN - 24;
		for (var i = 0; i < groups.size(); i++) {
			// anchorToBottom：下拉建议显示在框上方
			var box = new CCGAddressEditBox(this, mc.font, windowXOffset + ADDR_X + ADDR_TEXT_IN, windowYOffset + BODY_TOP, boxW, 10,
				true);
			box.setTextColor(0xFF714A40);
			box.setTextShadow(false);
			box.setValue(groupAddress.get(i));
			int idx = i;
			String key = groups.get(i).name();
			box.setResponder(text -> {
				groupAddress.set(idx, text);
				CACHE_ADDRS.put(key, text);
			});
			addrBoxes.add(box);
			addRenderableWidget(box);
		}
		// 按钮画在底部纹理烘好的按钮之上，只补交互与前景
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
			CCGCreateLang.translateDirect("gui.stock_keeper.send"),
			this::sendAll
		).tooltip(CCGCreateLang.translateDirect("gui.stock_keeper.send")));
		// 恢复滚动位置：此时 contentH/viewH 已算好，按新内容 clamp
		if (restoreScroll >= 0) {
			scroll = Mth.clamp(restoreScroll, 0, maxScroll());
			scrollAnim.setValue(scroll);
			scrollAnim.updateChaseTarget(scroll);
			restoreScroll = -1;
		}
	}
	private static int contentW() {return contentR() - CONTENT_L;}
	@Override
	public void onClose() {
		removed();
		mc.screen = parent;
	}
	@Override
	protected void renderWindowBackground(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		PoseStack pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, -500);
		Screen current = mc.screen;
		mc.screen = parent;
		try {
			parent.renderBackground(gui, -1, -1, partialTick);
			parent.render(gui, -1, -1, partialTick);
		} finally {
			mc.screen = current;
		}
		gui.fill(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), 0x66_101010);
		pose.popPose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		CCGGuiTextures.HEADER.render(gui, 0, 0);
		int bodyEnd = panelH - FOOTER_H;
		// 纵向平铺
		if (bodyEnd > HEADER_H) CCGGuiTextures.BODY.render(gui, 0, HEADER_H, PANEL_W, bodyEnd - HEADER_H);
		CCGGuiTextures.FOOTER.render(gui, 0, bodyEnd);
		pose.popPose();
	}
	/** 可滚动距离：内容高 − 视口高；contentH 含 BODY_TOP 偏移，须减去。 */
	private int maxScroll() {return Math.max(0, contentH - BODY_TOP - viewH);}
	// ---- 几何（窗口局部） ----
	private static int contentR() {return CONTENT_R - SCROLL_W - 4;}
	/** 判断原料是否齐备；齐备才从 remaining 扣减。 */
	private boolean hasMaterials(Node n, Map<Item, Integer> remaining, InventorySummary summary) {
		if (summary == null) return false;
		for (ReplenishEntry e : n.items()) {
			Item item = e.material().getItem();
			int have = remaining.computeIfAbsent(item, k -> summary.getCountOf(k.getDefaultInstance()));
			if (have < e.per() * n.craftTimes()) return false;
		}
		for (ReplenishEntry e : n.items()) remaining.merge(e.material().getItem(), -e.per() * n.craftTimes(), Integer::sum);
		return true;
	}
	@Override
	public void resize(@NotNull Minecraft mc, int width, int height) {
		parent.resize(mc, width, height);
		super.resize(mc, width, height);
	}
	@Override
	public void tick() {
		// AddressEditBox 非 TickableGuiEventListener，需显式 tick 才能激活下拉建议
		super.tick();
		addrBoxes.forEach(AddressEditBox::tick);
		scrollAnim.tickChaser();
		// 逼近目标时直接吸附，避免微小抖动
		if (Math.abs(scrollAnim.getValue() - scrollAnim.getChaseTarget()) < 1 / 16f) scrollAnim.setValue(scrollAnim.getChaseTarget());
	}
	@Override
	protected void renderMenuBackground(@NotNull GuiGraphics gui, int x, int y, int width, int height) {}
	@Override
	protected void renderWindow(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		mouseXPos = mouseX;
		mouseYPos = mouseY;
		hoveredNode = null;
		hoveredSendItem = null;
		int clamped = Mth.clamp((int) scrollAnim.getValue(partialTick), 0, maxScroll());
		PoseStack pose = gui.pose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		Font font = mc.font;
		Component title = CCGLang.translateDirect("gui.auto_replenish.title");
		gui.drawString(font, title, (PANEL_W - font.width(title)) / 2, 4, 0xFF4A3728, false);
		int scissorBottom = windowYOffset + BODY_TOP + Math.max(1, viewH);
		scissorL = windowXOffset + CONTENT_L;
		scissorT = windowYOffset + BODY_TOP;
		scissorR = windowXOffset + CONTENT_R;
		scissorB = scissorBottom;
		gui.enableScissor(scissorL, scissorT, scissorR, scissorB);
		if (groups.isEmpty()) {
			Component empty = CCGLang.translateDirect("gui.auto_replenish.empty");
			gui.drawString(font, empty, (PANEL_W - font.width(empty)) / 2, BODY_TOP + 4, 0xFFC8B688, false);
		}
		int viewBottom = BODY_TOP + viewH;
		// 地址框不能用 setVisible(false) 裁剪：不可见时 EditBox.canConsumeInput() 为假，键盘输入会被丢；滚出视口时改为挪到 PARKED。
		var onScreen = new boolean[addrBoxes.size()];
		for (Row r : rows) {
			int top = r.y - clamped;
			if (top + r.h <= BODY_TOP) continue;
			if (top >= viewBottom) break;
			switch (r.kind) {
				case GROUP -> drawGroup(gui, font, r, top);
				case NODE, BLOCKED -> drawNode(gui, r, top);
				case SEND -> drawSend(gui, r, top);
				case MISSING -> drawMissing(gui, r, top);
				case ADDR -> {
					onScreen[r.gi()] = true;
					placeAddr(gui, r, top);
				}
			}
		}
		// 只处理刚滚出视口的框：每帧重建 SuggestionsList 会把列表滚动位置重置为 0
		for (var i = 0; i < addrBoxes.size(); i++) {
			if (onScreen[i]) continue;
			AddressEditBox box = addrBoxes.get(i);
			if (box.getX() == PARKED) continue;
			box.setVisible(true);
			box.setX(PARKED);
			box.setY(PARKED);
			refreshSuggestionsAnchor(box);
		}
		gui.disableScissor();
		pose.popPose();
		if (maxScroll() > 0) renderScrollbar(gui, partialTick);
	}
	/** 发送所有原料齐备的节点，内容与发送区同源。 */
	private void sendAll() {
		// 按序扣减共享原料，避免两个配方同时认为库存够用
		Map<Item, Integer> remaining = new HashMap<>();
		InventorySummary summary = host.ccg$summary(); // 快照可能是现算的，整轮只取一次
		for (var i = 0; i < groups.size(); i++) {
			String addr = groupAddress.get(i);
			for (Node n : groups.get(i).nodes()) {
				if (n.craftTimes() <= 0 || n.items().isEmpty()) continue;
				if (addr == null || addr.isBlank()) {
					LOGGER.debug("未填地址, 跳过 {}", n.target().getHoverName().getString());
					continue;
				}
				if (!hasMaterials(n, remaining, summary)) {
					LOGGER.debug("原料不足, 跳过 {}", n.target().getHoverName().getString());
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
				LOGGER.debug(
					"Send: {} addr={} craft={}/{} order={}",
					n.target().getHoverName().getString(),
					addr,
					n.craftTimes(),
					n.wantTimes(),
					order
				);
				host.ccg$sendOrder(packet, addr);
			}
		}
		onClose();
	}
	private void drawGroup(GuiGraphics gui, Font font, Row r, int top) {
		int gx = CONTENT_L + GROUP_INDENT;
		// 收起/展开三角
		(
			hiddenGroups.contains(r.gi()) ? AllGuiTextures.STOCK_KEEPER_CATEGORY_HIDDEN : AllGuiTextures.STOCK_KEEPER_CATEGORY_SHOWN
		).render(gui, gx, top + (GROUP_H - 8) / 2);
		// 阴影用深色右下偏移 1px 的双画法
		Component name = groupName(groups.get(r.gi()));
		int x = gx + 10, y = top + (GROUP_H - 9) / 2 + 1;
		gui.drawString(font, name, x + 1, y + 1, 0x4A2D31, false);
		gui.drawString(font, name, x, y, 0xF8F8EC, false);
	}
	private void drawNode(GuiGraphics gui, Row r, int top) {
		boolean blocked = r.kind == RowKind.BLOCKED;
		List<Node> nodes = blocked ? groups.get(r.gi).blocked() : groups.get(r.gi).nodes();
		int gridTop = top;
		if (blocked) {
			drawSendLabel(gui, CCGLang.translateDirect("gui.auto_replenish.blocked"), top);
			gridTop += SEND_LABEL_H;
		}
		int colCount = Math.min(nodes.size(), NODES_PER_ROW);
		int rowCount = (nodes.size() + NODES_PER_ROW - 1) / NODES_PER_ROW;
		drawBlueprint(gui, CONTENT_L, gridTop, colCount, rowCount);
		for (var i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			// 合不出来时画目标产出
			int craftTimes = n.craftTimes();
			int totalOut = (craftTimes > 0 ? craftTimes : n.wantTimes()) * outPer(n.recipe());
			int cellX = CONTENT_L + BP_PAD + i % NODES_PER_ROW * CELL_W + 2;
			int iconY = gridTop + BP_PAD + i / NODES_PER_ROW * NODE_H + 2; // 16×16 图标在 20×20 格内居中
			// 悬停时略微放大
			boolean hov = mouseXPos >= windowXOffset + cellX
				&& mouseXPos < windowXOffset + cellX + 18
				&& mouseYPos >= windowYOffset + iconY
				&& mouseYPos < windowYOffset + iconY + 18;
			if (hov) {
				PoseStack pose = gui.pose();
				pose.pushPose();
				pose.translate(cellX + 9, iconY + 9, 0);
				pose.scale(1.075f, 1.075f, 1);
				pose.translate(-(cellX + 9), -(iconY + 9), 0);
				gui.renderItem(n.target(), cellX, iconY);
				pose.popPose();
				hoveredNode = n;
			} else gui.renderItem(n.target(), cellX, iconY);
			drawCount(gui, totalOut, cellX, iconY);
		}
	}
	private void drawSend(GuiGraphics gui, Row r, int top) {
		drawSendLabel(gui, CCGLang.translateDirect("gui.auto_replenish.send_items"), top);
		drawItemBox(gui, sendItems.get(r.gi()), top + SEND_LABEL_H);
	}
	private void drawMissing(GuiGraphics gui, Row r, int top) {
		drawSendLabel(gui, CCGLang.translateDirect("gui.auto_replenish.missing"), top);
		drawItemBox(gui, groups.get(r.gi).missing(), top + SEND_LABEL_H);
	}
	private void drawItemBox(GuiGraphics gui, List<ItemStack> items, int blockTop) {
		int lines = Math.max(1, (items.size() + NODES_PER_ROW - 1) / NODES_PER_ROW);
		CCGGuiTextures.BOX.render(gui, CONTENT_L, blockTop, CCGGuiTextures.BOX.width(),
			SEND_UP_H + (lines - 1) * SEND_MID_H + SEND_DOWN_H);
		int x = CONTENT_L + 5;
		for (var i = 0; i < items.size(); i++) {
			ItemStack stack = items.get(i);
			int ix = x + i % NODES_PER_ROW * SEND_CELL_W;
			int iy = blockTop + SEND_ITEM_Y + i / NODES_PER_ROW * SEND_MID_H;
			boolean hov = mouseXPos >= windowXOffset + ix
				&& mouseXPos < windowXOffset + ix + SEND_CELL_W
				&& mouseYPos >= windowYOffset + iy
				&& mouseYPos < windowYOffset + iy + SEND_CELL_W;
			if (hov) {
				PoseStack pose = gui.pose();
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
		CCGGuiTextures.ADDRESS.render(gui, 0, top + 2);
		AddressEditBox box = addrBoxes.get(r.gi());
		box.setX(windowXOffset + ADDR_X + ADDR_TEXT_IN);
		// 1px 补偿 EditBox 按 8px 字高（实为 9px）定位导致的偏高
		box.setY(windowYOffset + top + (ADDR_H - 10) / 2 + 2);
		// 地址框在滚动内容里，下拉锚点需跟随 boxY
		refreshSuggestionsAnchor(box);
		// 空值且未聚焦时画占位提示
		if (box.getValue().isBlank() && !box.isFocused()) {
			Font font = mc.font;
			Component hint = CCGCreateLang.translateDirect("gui.stock_keeper.package_address").withStyle(ChatFormatting.ITALIC);
			gui.drawString(font, hint, box.getX() - windowXOffset, box.getY() - windowYOffset, 0xFFCDBCA8, false);
		}
	}
	private void renderScrollbar(GuiGraphics gui, float partialTick) {
		int barX = scrollX();
		int barSize = thumbHeight();
		if (barSize >= scrollTrackHeight() - 2) return;
		// 整条随滚动平移
		PoseStack pose = gui.pose();
		pose.pushPose();
		pose.translate(0, thumbTop(partialTick) - scrollTrackTop(), 0);
		int baseY = scrollTrackTop();
		AllGuiTextures pad = AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_PAD;
		gui.blit(pad.location, barX, baseY, SCROLL_W, barSize, pad.getStartX(), pad.getStartY(), pad.getWidth(), pad.getHeight(), 256,
			256);
		AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_TOP.render(gui, barX, baseY);
		if (barSize > 16) AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_MID.render(gui, barX, baseY + barSize / 2 - 4);
		AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_BOT.render(gui, barX, baseY + barSize - 5);
		pose.popPose();
	}
	/** 让下拉锚点跟随地址框当前的 y 位置。 */
	private void refreshSuggestionsAnchor(AddressEditBox box) {
		DestinationSuggestions suggestions = ((AddressEditBoxAccessor) box).getDestinationSuggestions();
		if (suggestions == null) return;
		// anchorToBottom 恒为 true（列表自 yPos 向上排），顶出内容区时把 yPos 抬到框下方，列表即落到框下面。
		var accessor = (DestinationSuggestionsAccessor) suggestions;
		int rows = Math.min(SUGGESTION_MAX_ROWS, accessor.getCurrentSuggestions().size());
		int listH = rows * SUGGESTION_ROW_H;
		int anchor = box.getY() - listH < scissorT ? box.getY() + box.getHeight() + listH + 3 : box.getY();
		int yOffset = SUGGESTION_ANCHOR_BASE_Y + anchor;
		// 位置未变则不重建：showSuggestions 会新建 SuggestionsList，把列表滚动位置重置为 0。
		if (yOffset == accessor.getYOffset()) return;
		accessor.setYOffset(yOffset);
		if (box.isFocused()) suggestions.showSuggestions(false);
	}
	@Override
	protected void renderWindowForeground(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		// 弹窗打开时是唯一浮层：其余悬停提示会穿透显示，故一概不画。
		if (recipePopup != null) {
			renderRecipePopup(gui);
			renderPopupRecipeCard(gui);
			return;
		}
		// 悬停空地址框时提示
		for (AddressEditBox box : addrBoxes)
			if (box.visible && box.getValue().isBlank() && !box.isFocused() && box.isMouseOver(mouseX, mouseY)) {
				gui.renderComponentTooltip(
					mc.font, List.of(
						CCGCreateLang.translateDirect("gui.factory_panel.restocker_address").withStyle(s -> s.withColor(0x5391E1)),
						CCGCreateLang.translateDirect("gui.schedule.lmb_edit")
							.withStyle(ChatFormatting.DARK_GRAY)
							.withStyle(ChatFormatting.ITALIC)
					), mouseX, mouseY
				);
				break;
			}
		if (hoveredNode != null) renderNodeCard(gui, mouseX, mouseY, hoveredNode);
		if (hoveredSendItem != null) gui.renderTooltip(mc.font, hoveredSendItem, mouseX, mouseY);
	}
	/** 配方切换弹窗：列出候选配方，点一行即切换。 */
	private void renderRecipePopup(GuiGraphics gui) {
		Font font = mc.font;
		List<CandidateCard> candidates = recipePopup.candidates();
		hoveredPopupRow = -1;
		if (mouseXPos >= popupX
			&& mouseXPos < popupX + popupW
			&& mouseYPos >= popupY + 3
			&& mouseYPos < popupY + 3 + candidates.size() * POPUP_ROW_H) {
			int row = (mouseYPos - popupY - 3) / POPUP_ROW_H;
			if (row >= 0 && row < candidates.size()) hoveredPopupRow = row;
		}
		PoseStack pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, 400); // 盖住物品(z=150)、数字(z=200)、配方卡(z=300)
		TooltipRenderUtil.renderTooltipBackground(gui, popupX, popupY, popupW, popupH, 0);
		Recipe<?> current = recipePopup.recipe();
		for (var i = 0; i < candidates.size(); i++) {
			Recipe<?> recipe = candidates.get(i).holder().value();
			int y = popupY + 3 + i * POPUP_ROW_H;
			// 悬停行高亮；当前配方只用文字颜色区分
			if (i == hoveredPopupRow) gui.fill(popupX + 3, y, popupX + popupW - 3, y + POPUP_ROW_H - 1, 0x40_FFFFFF);
			// 当前配方金色，其余白色
			gui.drawString(
				font,
				typeName(recipe.getType().toString()),
				popupX + 6,
				y + (POPUP_ROW_H - 9) / 2,
				recipe == current ? 0xFFFFD700 : 0xFFFFFFFF,
				true
			);
		}
		pose.popPose();
	}
	/** 弹窗内悬停行显示候选配方卡。 */
	private void renderPopupRecipeCard(GuiGraphics gui) {
		if (hoveredPopupRow < 0) return;
		CandidateCard card = recipePopup.candidates().get(hoveredPopupRow);
		PoseStack pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, 500); // 抬到弹窗(z=400)之上
		renderRecipeCard(
			gui,
			mouseXPos,
			mouseYPos,
			recipePopup.target(),
			card.holder().value(),
			card.items(),
			recipePopup.craftTimes() > 0 ? recipePopup.craftTimes() : recipePopup.wantTimes(),
			true
		);
		pose.popPose();
	}
	/** 节点卡：可合成时取可合成次数，否则退回目标次数。 */
	private void renderNodeCard(GuiGraphics gui, int mouseX, int mouseY, Node n) {
		renderRecipeCard(
			gui,
			mouseX,
			mouseY,
			n.target(),
			n.recipe(),
			n.items(),
			n.craftTimes() > 0 ? n.craftTimes() : n.wantTimes(),
			n.candidates().size() > 1
		);
	}
	/** 配方卡：左侧原料清单，右侧箭头与产物，跟随鼠标并夹在屏幕内。 */
	private void renderRecipeCard(
		GuiGraphics gui,
		int mouseX,
		int mouseY,
		ItemStack target,
		Recipe<?> recipe,
		List<ReplenishEntry> items,
		int craftTimes,
		boolean switchable
	) {
		Font font = mc.font;
		int totalOut = craftTimes * outPer(recipe);
		var rowH = 18;
		var nameW = 0;
		for (ReplenishEntry e : items) nameW = Math.max(nameW, font.width(e.material().getHoverName()));
		int listW = 10 + 18 + 2 + nameW;
		Component outName = target.getHoverName();
		int outNameW = font.width(outName);
		int cardW = 3 + listW + 6 + 42 + 6 + 16 + 6 + outNameW + 3;
		// 左列原料与右侧产物取高者，可切换时底部再留一行提示
		int contentH = 3 + Math.max(items.size() * rowH, 18) + 3;
		int cardH = contentH + (switchable ? SWITCH_HINT_H : 0);
		Window window = mc.getWindow();
		int cardX = Mth.clamp(mouseX + 12, 0, Math.max(0, window.getGuiScaledWidth() - cardW));
		int cardY = Mth.clamp(mouseY + 12, 0, Math.max(0, window.getGuiScaledHeight() - cardH));
		// 抬到物品与数字之上
		PoseStack pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, 300);
		TooltipRenderUtil.renderTooltipBackground(gui, cardX, cardY, cardW, cardH, 0);
		for (var i = 0; i < items.size(); i++) {
			ReplenishEntry e = items.get(i);
			int y = cardY + 3 + i * rowH;
			// 空心方块，够料时叠绿勾
			gui.drawString(font, "□", cardX + 3, y + 5, e.enough() ? 0x668D7F6B : 0xFF8D7F6B, false);
			if (e.enough()) gui.drawString(font, "✔", cardX + 3, y + 4, 0xFF31B25D, false);
			gui.renderItem(e.material(), cardX + 13, y + 1);
			// 够=绿，不够但可合成=黄，不够且为原材料=红
			int color = e.enough() ? 0xFF55FF55 : e.craftable() ? 0xFFFFD700 : 0xFFFF5555;
			gui.drawString(font, e.material().getHoverName(), cardX + 33, y + 5, color, true);
			drawCount(gui, e.per() * craftTimes, cardX + 13, y + 1);
		}
		int midY = cardY + contentH / 2;
		int arrowX = cardX + 3 + listW + 6;
		AllGuiTextures.JEI_ARROW.render(gui, arrowX, midY - 5);
		int outX = arrowX + 42 + 6;
		gui.renderItem(target, outX, midY - 8);
		gui.drawString(font, outName, outX + 20, midY - 5, 0xFFFFFFFF, true);
		drawCount(gui, totalOut, outX, midY - 8);
		if (switchable) gui.drawString(
			font,
			CCGLang.translateDirect("gui.auto_replenish.switch_recipe"),
			cardX + 4,
			cardY + contentH,
			0xFFAAAAAA,
			false
		);
		pose.popPose();
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// 弹窗优先：点面板内应用，点面板外或非左键关闭
		if (recipePopup != null) {
			List<CandidateCard> candidates = recipePopup.candidates();
			if (button == 0 && mouseX >= popupX && mouseX < popupX + popupW && mouseY >= popupY && mouseY < popupY + popupH) {
				var row = (int) ((mouseY - popupY - 3) / POPUP_ROW_H);
				if (row >= 0 && row < candidates.size()) {
					host.ccg$setRecipeChoice(recipePopup.target().getItem(), candidates.get(row).holder().id());
					recipePopup = null;
					rebuildAll();
					return true;
				}
			}
			recipePopup = null;
			return true;
		}
		// 点节点图标弹出候选配方列表
		if (button == 0 && hoveredNode != null && hoveredNode.candidates().size() > 1) {
			recipePopup = hoveredNode;
			openRecipePopup(mouseX, mouseY);
			return true;
		}
		if (button == 0 && maxScroll() > 0 && overScrollbar(mouseX, mouseY)) {
			scrollDragging = true;
			scrollDragOffset = mouseY - thumbTop(1);
			return true;
		}
		// 点组头三角切换展开/收起
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
				scroll = Mth.clamp(scroll, 0, maxScroll());
				scrollAnim.setValue(scroll);
				scrollAnim.updateChaseTarget(scroll);
				return true;
			}
		}
		// 点到地址框外则取消聚焦
		for (AddressEditBox box : addrBoxes) {
			if (!box.isFocused()) continue;
			boolean result = box.mouseClicked(mouseX, mouseY, button);
			if (box.isHovered() || result) return result;
			box.setFocused(false);
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	/** 按新的配方选择重建整棵树，换成同款新界面。 */
	private void rebuildAll() {
		var next = new ReplenishScreen(parent, host, host.ccg$buildGroups());
		next.restoreScroll = scroll; // 继承当前滚动位置
		mc.setScreen(next);
	}
	/** 按点击位置计算并固定弹窗矩形。 */
	private void openRecipePopup(double mouseX, double mouseY) {
		List<CandidateCard> candidates = recipePopup.candidates();
		Font font = mc.font;
		var nameW = 0;
		for (CandidateCard card : candidates) nameW = Math.max(nameW, font.width(typeName(card.holder().value().getType().toString())));
		popupW = 6 + nameW + 6;
		popupH = 6 + candidates.size() * POPUP_ROW_H;
		Window window = mc.getWindow();
		popupX = Mth.clamp((int) mouseX + 10, 0, Math.max(0, window.getGuiScaledWidth() - popupW));
		popupY = Mth.clamp((int) mouseY + 10, 0, Math.max(0, window.getGuiScaledHeight() - popupH));
	}
	private boolean overScrollbar(double mouseX, double mouseY) {
		return mouseX >= scrollX()
			&& mouseX < scrollX() + SCROLL_W
			&& mouseY >= scrollTrackTop()
			&& mouseY < scrollTrackTop() + scrollTrackHeight();
	}
	private int thumbTop(float partialTick) {
		int thumb = thumbHeight();
		int maxThumbTop = scrollTrackHeight() - thumb;
		return scrollTrackTop() + (maxScroll() == 0 ? 0 : (int) (scrollAnim.getValue(partialTick) / maxScroll() * maxThumbTop));
	}
	/** 配方类型的本地化名：JEI 分类标题 → 命名空间.recipe.路径 → 路径。 */
	private static Component typeName(String typeId) {
		if (typeId == null || typeId.isBlank()) return CCGLang.translateDirect("gui.auto_replenish.uncraftable");
		// 原版配方类型无命名空间，补 minecraft: 才对得上 JEI 分类 uid
		String lookupKey = typeId.indexOf(':') < 0 ? "minecraft:" + typeId : typeId;
		Component jeiTitle = CCGJeiTitles.get(lookupKey);
		if (jeiTitle != null) return jeiTitle;
		int idx = typeId.indexOf(':');
		if (idx <= 0) return Component.literal(typeId);
		String path = typeId.substring(idx + 1);
		String key = typeId.substring(0, idx) + ".recipe." + path;
		return Component.translatable(I18n.exists(key) ? key : path);
	}
	private int scrollX() {return windowXOffset + CONTENT_R - SCROLL_W - 4;}
	private int scrollTrackTop() {return windowYOffset + BODY_TOP;}
	private int scrollTrackHeight() {return Math.max(1, viewH);}
	/** 按视口与内容比例计算拇指高。 */
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
			int maxScroll = maxScroll();
			if (maxThumbTop <= 0 || maxScroll <= 0) {
				scroll = 0;
				scrollAnim.setValue(0);
				scrollAnim.updateChaseTarget(0);
				return true;
			}
			var top = (int) Mth.clamp(mouseY - scrollDragOffset, scrollTrackTop(), scrollTrackTop() + maxThumbTop);
			scroll = (int) ((float) (top - scrollTrackTop()) / maxThumbTop * maxScroll);
			// 必须同步追赶目标，否则 tickChaser 会把值拉回旧目标
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
		// 下拉建议优先吃滚轮，需显式投递
		if (getFocused() instanceof AddressEditBox box && box.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
		if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
		if (overScrollbar(mouseX, mouseY)) return true;
		// 滚轮按行推进 + 指数追赶
		var direction = (int) (Math.ceil(Math.abs(scrollY)) * -Math.signum(scrollY));
		scroll = Mth.clamp((int) scrollAnim.getChaseTarget() + direction * NODE_H, 0, maxScroll());
		scrollAnim.chase(scroll, 0.5, Chaser.EXP);
		return true;
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (recipePopup != null) recipePopup = null; // 弹窗开着时先关弹窗
			else onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	private enum RowKind {
		GROUP,
		NODE,
		BLOCKED,
		SEND,
		MISSING,
		ADDR
	}
	/** 预计算的一行；y 含 BODY_TOP 起始偏移，gi 索引到组数据。 */
	private record Row(int y, int h, RowKind kind, int gi) {}
	/** 底部按钮：返回方块与发送长条。 */
	private static final class CCGButton extends AbstractSimiWidget {
		private final ResourceLocation bg;
		private final AllGuiTextures hover;
		private final boolean hoverNative; // 按原生尺寸居中，避免拉伸畸变
		private final AllIcons innerIcon;
		private final Component label;
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
		CCGButton tooltip(Component text) {
			toolTip.clear();
			toolTip.add(text);
			return this;
		}
		@Override
		public void doRender(@NotNull GuiGraphics gui, int mx, int my, float pt) {
			if (isHovered() && hover != null) if (hoverNative) {
				// 按原生尺寸居中叠加，避免拉伸畸变
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
			if (label != null) {
				int tw = mc.font.width(label);
				gui.drawString(mc.font, label, getX() + (getWidth() - tw) / 2, getY() + (getHeight() - 9) / 2 + 1, 0x252525, false);
			}
		}
	}
}
