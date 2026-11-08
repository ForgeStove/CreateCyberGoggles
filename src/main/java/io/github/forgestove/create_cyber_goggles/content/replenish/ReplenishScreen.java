package io.github.forgestove.create_cyber_goggles.content.replenish;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.*;
import com.simibubi.create.content.trains.schedule.DestinationSuggestions;
import com.simibubi.create.foundation.gui.*;
import io.github.forgestove.create_cyber_goggles.compat.jei.CCGJeiTitles;
import io.github.forgestove.create_cyber_goggles.content.replenish.ReplenishGroup.*;
import io.github.forgestove.create_cyber_goggles.foundation.gui.*;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.create_cyber_goggles.foundation.lang.*;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.*;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ScreenEvent.CharacterTyped.Pre;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.content.replenish.ReplenishLayout.*;
import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.mc;
public class ReplenishScreen extends AbstractSimiScreen implements ScissorArea {
	private static final Map<String, String> CACHE_ADDRS = new LinkedHashMap<>();
	private static final int PARKED = -10000;
	private static final int SUGGESTION_ANCHOR_BASE_Y = -72;
	private static final int SUGGESTION_MAX_ROWS = 7;
	private static final int SUGGESTION_ROW_H = 12;
	private static final int POPUP_ROW_H = 14;
	private static final int SWITCH_HINT_H = 11;
	private static boolean charHooked;
	private final Screen parent;
	private final ReplenishTree host;
	private final List<ReplenishGroup> groups;
	private final List<AddressEditBox> addrBoxes = new ArrayList<>();
	private final List<String> groupAddress = new ArrayList<>();
	/**
	 * 每组待发送的原料，按 {@link Item} 合并。
	 * <p>
	 * 数量 = 单次用量 × 可合成次数。
	 */
	private final List<List<ItemStack>> sendItems = new ArrayList<>();
	private final Set<Integer> hiddenGroups = new HashSet<>();
	private final ReplenishScrollPane scrollPane = new ReplenishScrollPane();
	private final UiContext ui = new UiContext();
	private final ReplenishHover hover = new ReplenishHover();
	private ReplenishLayout layout;
	/** 内容区裁剪范围，绝对屏幕坐标。 */
	private int scissorL, scissorT, scissorR, scissorB;
	private int panelH;
	private int viewH;
	private int restoreScroll = -1; // 重建界面后恢复的滚动位置，-1 表示顶部
	public ReplenishScreen(Screen parent, ReplenishTree host, List<ReplenishGroup> groups) {
		this.parent = parent;
		this.host = host;
		this.groups = groups;
		for (ReplenishGroup group : groups) groupAddress.add(CACHE_ADDRS.getOrDefault(group.name(), ""));
		scrollPane.content(this::renderContent);
		rebuildLayout();
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1));
		hookCharTyped();
	}
	/** 最高优先级拦截字符事件，只有消费成功才取消。 */
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
	private static int contentW() {return contentR() - CONTENT_L;}
	private static int contentR() {return CONTENT_R - SCROLL_W - 4;}
	/** 配方类型的本地化名：JEI 分类标题 → 命名空间.recipe.路径 → 路径。 */
	private static Component typeName(String typeId) {
		if (typeId == null || typeId.isBlank()) return CCGLang.translateDirect("gui.auto_replenish.uncraftable");
		// 原版配方类型没有命名空间，需要补 minecraft: 才能匹配 JEI 分类 uid。
		String lookupKey = typeId.indexOf(':') < 0 ? "minecraft:" + typeId : typeId;
		Component jeiTitle = CCGJeiTitles.get(lookupKey);
		if (jeiTitle != null) return jeiTitle;
		int idx = typeId.indexOf(':');
		if (idx <= 0) return Component.literal(typeId);
		String path = typeId.substring(idx + 1);
		String key = typeId.substring(0, idx) + ".recipe." + path;
		return Component.translatable(I18n.exists(key) ? key : path);
	}
	private void rebuildLayout() {
		buildSendItems();
		layout = new ReplenishLayout(groups, sendItems, hiddenGroups);
		scrollPane.setContentHeight(layout.contentH() - BODY_TOP);
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
	@Override
	protected void init() {
		// resize 重跑 init 时 Screen 已清空 widgets，残留的旧引用无法交互。
		addrBoxes.clear();
		// contentH 已含 BODY_TOP，不再重复相加
		int needed = Math.max(80, layout.contentH() + FOOTER_H);
		int screenH = mc.getWindow().getGuiScaledHeight();
		panelH = Math.min(screenH - 18, needed);
		viewH = Math.max(0, panelH - HEADER_H - FOOTER_H);
		setWindowSize(PANEL_W, panelH);
		Window window = mc.getWindow();
		setWindowOffset((window.getGuiScaledWidth() - PANEL_W) / 2, (window.getGuiScaledHeight() - panelH) / 2);
		super.init();
		// 宽度收窄，给右侧的剪贴板图标留位
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
		addRenderableWidget(new ReplenishButton(
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
		addRenderableWidget(new ReplenishButton(
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
		scrollPane.setBounds(new UiBounds(windowXOffset + CONTENT_L, windowYOffset + BODY_TOP, CONTENT_R - CONTENT_L, viewH));
		scrollPane.setPitch(NODE_H);
		scrollPane.setContentHeight(layout.contentH() - BODY_TOP);
		// 恢复滚动位置：此时 contentH/viewH 已算好，按新内容 clamp
		if (restoreScroll >= 0) {
			scrollPane.setScroll(restoreScroll);
			restoreScroll = -1;
		}
	}
	@Override
	public void tick() {
		// AddressEditBox 不是 TickableGuiEventListener，需显式 tick 才激活下拉建议。
		super.tick();
		addrBoxes.forEach(AddressEditBox::tick);
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (hover.popup() != null) hover.closePopup();
			else onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
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
		if (bodyEnd > HEADER_H) CCGGuiTextures.BODY.render(gui, 0, HEADER_H, PANEL_W, bodyEnd - HEADER_H);
		CCGGuiTextures.FOOTER.render(gui, 0, bodyEnd);
		pose.popPose();
	}
	@Override
	protected void renderWindow(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		hover.setMouse(mouseX, mouseY);
		hover.clearTargets();
		PoseStack pose = gui.pose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		Font font = mc.font;
		Component title = CCGLang.translateDirect("gui.auto_replenish.title");
		gui.drawString(font, title, (PANEL_W - font.width(title)) / 2, 4, 0xFF4A3728, false);
		pose.popPose();
		scissorL = windowXOffset + CONTENT_L;
		scissorT = windowYOffset + BODY_TOP;
		scissorR = windowXOffset + CONTENT_R;
		scissorB = windowYOffset + BODY_TOP + Math.max(1, viewH);
		ui.beginFrame(gui, partialTick, mouseX, mouseY);
		scrollPane.render(ui);
	}
	private void renderContent(GuiGraphics gui, double scroll) {
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		Font font = mc.font;
		if (groups.isEmpty()) {
			Component empty = CCGLang.translateDirect("gui.auto_replenish.empty");
			gui.drawString(font, empty, (PANEL_W - font.width(empty)) / 2, BODY_TOP + 4, 0xFFC8B688, false);
		}
		int viewBottom = BODY_TOP + viewH;
		int offset = (int) scroll;
		// 地址框不能用 setVisible(false) 裁剪：不可见时 EditBox.canConsumeInput() 为假，键盘输入会丢。
		// 滚出视口时改为挪到 PARKED。
		var onScreen = new boolean[addrBoxes.size()];
		for (Row r : layout.rows()) {
			int top = r.y() - offset;
			if (top + r.h() <= BODY_TOP) continue;
			if (top >= viewBottom) break;
			switch (r.kind()) {
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
		// 只处理刚滚出视口的框；每帧重建 SuggestionsList 会把滚动位置重置为 0。
		for (var i = 0; i < addrBoxes.size(); i++) {
			if (onScreen[i]) continue;
			AddressEditBox box = addrBoxes.get(i);
			if (box.getX() == PARKED) continue;
			box.setVisible(true);
			box.setX(PARKED);
			box.setY(PARKED);
			refreshSuggestionsAnchor(box);
		}
		pose.popPose();
	}
	@Override
	protected void renderWindowForeground(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		// 弹窗独占浮层，其余悬停提示会穿透，一概不画。
		if (hover.popup() != null) {
			renderRecipePopup(gui);
			renderPopupRecipeCard(gui);
			return;
		}
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
		if (hover.node() != null) renderNodeCard(gui, mouseX, mouseY, hover.node());
		if (hover.sendItem() != null) gui.renderTooltip(mc.font, hover.sendItem(), mouseX, mouseY);
	}
	@Override
	public int scissorLeft() {
		return scissorL;
	}
	@Override
	public int scissorTop() {
		return scissorT;
	}
	@Override
	public int scissorRight() {
		return scissorR;
	}
	@Override
	public int scissorBottom() {
		return scissorB;
	}
	@Override
	public void onClose() {
		removed();
		mc.screen = parent;
	}
	@Override
	protected void renderMenuBackground(@NotNull GuiGraphics gui, int x, int y, int width, int height) {}
	@Override
	public void resize(@NotNull Minecraft mc, int width, int height) {
		parent.resize(mc, width, height);
		super.resize(mc, width, height);
	}
	private void sendAll() {
		ReplenishSender.sendAll(host, groups, groupAddress);
		onClose();
	}
	private void drawGroup(GuiGraphics gui, Font font, Row r, int top) {
		int gx = CONTENT_L + GROUP_INDENT;
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
		boolean blocked = r.kind() == RowKind.BLOCKED;
		List<Node> nodes = blocked ? groups.get(r.gi()).blocked() : groups.get(r.gi()).nodes();
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
			// 无法合成时绘制目标产出
			int craftTimes = n.craftTimes();
			int totalOut = (craftTimes > 0 ? craftTimes : n.wantTimes()) * outPer(n.recipe());
			int cellX = CONTENT_L + BP_PAD + i % NODES_PER_ROW * CELL_W + 2;
			int iconY = gridTop + BP_PAD + i / NODES_PER_ROW * NODE_H + 2; // 16×16 图标在 20×20 格内居中
			boolean hov = hover.mouseX() >= windowXOffset + cellX
				&& hover.mouseX() < windowXOffset + cellX + 18
				&& hover.mouseY() >= windowYOffset + iconY
				&& hover.mouseY() < windowYOffset + iconY + 18;
			if (hov) {
				PoseStack pose = gui.pose();
				pose.pushPose();
				pose.translate(cellX + 9, iconY + 9, 0);
				pose.scale(1.075f, 1.075f, 1);
				pose.translate(-(cellX + 9), -(iconY + 9), 0);
				gui.renderItem(n.target(), cellX, iconY);
				pose.popPose();
				hover.node(n);
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
		drawItemBox(gui, groups.get(r.gi()).missing(), top + SEND_LABEL_H);
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
			boolean hov = hover.mouseX() >= windowXOffset + ix
				&& hover.mouseX() < windowXOffset + ix + SEND_CELL_W
				&& hover.mouseY() >= windowYOffset + iy
				&& hover.mouseY() < windowYOffset + iy + SEND_CELL_W;
			if (hov) {
				PoseStack pose = gui.pose();
				pose.pushPose();
				pose.translate(ix + 8, iy + 8, 0);
				pose.scale(1.075f, 1.075f, 1);
				pose.translate(-(ix + 8), -(iy + 8), 0);
				gui.renderItem(stack, ix, iy);
				pose.popPose();
				hover.sendItem(stack);
			} else gui.renderItem(stack, ix, iy);
			drawCount(gui, stack.getCount(), ix, iy);
		}
	}
	private void placeAddr(GuiGraphics gui, Row r, int top) {
		CCGGuiTextures.ADDRESS.render(gui, 0, top + 2);
		AddressEditBox box = addrBoxes.get(r.gi());
		box.setX(windowXOffset + ADDR_X + ADDR_TEXT_IN);
		// 补偿 EditBox 使用 8px 字高定位造成的偏差（实际 9px）。
		box.setY(windowYOffset + top + (ADDR_H - 10) / 2 + 2);
		// 地址框在滚动内容里，下拉锚点需跟随 boxY
		refreshSuggestionsAnchor(box);
		if (box.getValue().isBlank() && !box.isFocused()) {
			Font font = mc.font;
			Component hint = CCGCreateLang.translateDirect("gui.stock_keeper.package_address").withStyle(ChatFormatting.ITALIC);
			gui.drawString(font, hint, box.getX() - windowXOffset, box.getY() - windowYOffset, 0xFFCDBCA8, false);
		}
	}
	private void refreshSuggestionsAnchor(AddressEditBox box) {
		DestinationSuggestions suggestions = ((AddressEditBoxAccessor) box).getDestinationSuggestions();
		if (suggestions == null) return;
		// 列表自 yPos 向上排；顶出内容区时把 yPos 抬到框下方，列表改在框下显示。
		var accessor = (DestinationSuggestionsAccessor) suggestions;
		int rows = Math.min(SUGGESTION_MAX_ROWS, accessor.getCurrentSuggestions().size());
		int listH = rows * SUGGESTION_ROW_H;
		int anchor = box.getY() - listH < scissorT ? box.getY() + box.getHeight() + listH + 3 : box.getY();
		int yOffset = SUGGESTION_ANCHOR_BASE_Y + anchor;
		// 位置未变则不重建，否则 SuggestionsList 重建会把列表滚动位置重置为 0。
		if (yOffset == accessor.getYOffset()) return;
		accessor.setYOffset(yOffset);
		if (box.isFocused()) suggestions.showSuggestions(false);
	}
	private void renderRecipePopup(GuiGraphics gui) {
		Font font = mc.font;
		List<CandidateCard> candidates = hover.popup().candidates();
		hover.popupRow(-1);
		if (hover.mouseX() >= hover.popupX()
			&& hover.mouseX() < hover.popupX() + hover.popupW()
			&& hover.mouseY() >= hover.popupY() + 3
			&& hover.mouseY() < hover.popupY() + 3 + candidates.size() * POPUP_ROW_H) {
			int row = (hover.mouseY() - hover.popupY() - 3) / POPUP_ROW_H;
			if (row >= 0 && row < candidates.size()) hover.popupRow(row);
		}
		PoseStack pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, 400); // 盖住物品(z=150)、数字(z=200)、配方卡(z=300)
		TooltipRenderUtil.renderTooltipBackground(gui, hover.popupX(), hover.popupY(), hover.popupW(), hover.popupH(), 0);
		Recipe<?> current = hover.popup().recipe();
		for (var i = 0; i < candidates.size(); i++) {
			Recipe<?> recipe = candidates.get(i).holder().value();
			int y = hover.popupY() + 3 + i * POPUP_ROW_H;
			// 悬停行高亮，当前配方只用文字颜色区分。
			if (i == hover.popupRow())
				gui.fill(hover.popupX() + 3, y, hover.popupX() + hover.popupW() - 3, y + POPUP_ROW_H - 1, 0x40_FFFFFF);
			// 当前配方金色，其余白色
			gui.drawString(
				font,
				typeName(recipe.getType().toString()),
				hover.popupX() + 6,
				y + (POPUP_ROW_H - 9) / 2,
				recipe == current ? 0xFFFFD700 : 0xFFFFFFFF,
				true
			);
		}
		pose.popPose();
	}
	private void renderPopupRecipeCard(GuiGraphics gui) {
		if (hover.popupRow() < 0) return;
		CandidateCard card = hover.popup().candidates().get(hover.popupRow());
		PoseStack pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, 500); // 抬到弹窗(z=400)之上
		renderRecipeCard(
			gui,
			hover.mouseX(),
			hover.mouseY(),
			hover.popup().target(),
			card.holder().value(),
			card.items(),
			hover.popup().craftTimes() > 0 ? hover.popup().craftTimes() : hover.popup().wantTimes(),
			true
		);
		pose.popPose();
	}
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
		// 左列原料与右侧产物取高者，可切换时底部留一行提示。
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
			gui.drawString(font, "□", cardX + 3, y + 5, e.enough() ? 0x668D7F6B : 0xFF8D7F6B, false);
			if (e.enough()) gui.drawString(font, "✔", cardX + 3, y + 4, 0xFF31B25D, false);
			gui.renderItem(e.material(), cardX + 13, y + 1);
			// 原料足够为绿，不足但可合成为黄，不足且为原材料为红
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
		// 点面板内应用，面板外或非左键关闭。
		if (hover.popup() != null) {
			List<CandidateCard> candidates = hover.popup().candidates();
			if (button == 0
				&& mouseX >= hover.popupX()
				&& mouseX < hover.popupX() + hover.popupW()
				&& mouseY >= hover.popupY()
				&& mouseY < hover.popupY() + hover.popupH()) {
				var row = (int) ((mouseY - hover.popupY() - 3) / POPUP_ROW_H);
				if (row >= 0 && row < candidates.size()) {
					host.ccg$setRecipeChoice(hover.popup().target().getItem(), candidates.get(row).holder().id());
					hover.closePopup();
					rebuildAll();
					return true;
				}
			}
			hover.closePopup();
			return true;
		}
		if (button == 0 && hover.node() != null && hover.node().candidates().size() > 1) {
			hover.popup(hover.node());
			openRecipePopup(mouseX, mouseY);
			return true;
		}
		if (scrollPane.mouseClicked(ui, mouseX, mouseY, button)) return true;
		// 点组头三角切换展开/收起
		if (button == 0) {
			int clamped = (int) scrollPane.scroll();
			int iconX = windowXOffset + CONTENT_L + GROUP_INDENT;
			for (Row r : layout.rows()) {
				if (r.kind() != RowKind.GROUP) continue;
				int top = r.y() - clamped;
				if (top < BODY_TOP || top + GROUP_H > BODY_TOP + viewH) continue;
				int iconY = windowYOffset + top + (GROUP_H - 8) / 2;
				if (mouseX < iconX || mouseX >= iconX + 8 || mouseY < iconY || mouseY >= iconY + 8) continue;
				int gi = r.gi();
				if (!hiddenGroups.remove(gi)) hiddenGroups.add(gi);
				mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.5f));
				rebuildLayout();
				scrollPane.setScroll(scrollPane.scroll());
				return true;
			}
		}
		for (AddressEditBox box : addrBoxes) {
			if (!box.isFocused()) continue;
			boolean result = box.mouseClicked(mouseX, mouseY, button);
			if (box.isHovered() || result) return result;
			box.setFocused(false);
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (scrollPane.mouseReleased(ui, mouseX, mouseY, button)) return true;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (scrollPane.mouseDragged(ui, mouseX, mouseY, button, deltaX, deltaY)) return true;
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		// 下拉建议优先处理滚轮，需显式投递。
		if (getFocused() instanceof AddressEditBox box && box.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
		if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
		return scrollPane.mouseScrolled(ui, mouseX, mouseY, scrollY);
	}
	/** 根据新的配方选择重建整棵树并重开界面。 */
	private void rebuildAll() {
		var next = new ReplenishScreen(parent, host, host.ccg$buildGroups());
		next.restoreScroll = (int) scrollPane.scroll(); // 继承当前滚动位置
		mc.setScreen(next);
	}
	private void openRecipePopup(double mouseX, double mouseY) {
		List<CandidateCard> candidates = hover.popup().candidates();
		Font font = mc.font;
		var nameW = 0;
		for (CandidateCard card : candidates) nameW = Math.max(nameW, font.width(typeName(card.holder().value().getType().toString())));
		int popupW = 6 + nameW + 6;
		int popupH = 6 + candidates.size() * POPUP_ROW_H;
		Window window = mc.getWindow();
		hover.popupRect(
			Mth.clamp((int) mouseX + 10, 0, Math.max(0, window.getGuiScaledWidth() - popupW)),
			Mth.clamp((int) mouseY + 10, 0, Math.max(0, window.getGuiScaledHeight() - popupH)),
			popupW,
			popupH
		);
	}
}
