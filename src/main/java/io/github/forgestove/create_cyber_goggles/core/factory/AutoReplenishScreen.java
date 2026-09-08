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
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;

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
	private static final Map<String, String> SAVED_ADDRS = new LinkedHashMap<>();
	private static final Path ADDR_FILE = Path.of("config").resolve("create_cyber_goggles_auto_replenish_addrs.toml");
	// 面板几何（与 Create stock_keeper 同宽）
	private static final int PANEL_W = 256;
	private static final int HEADER_H = 36;
	private static final int FOOTER_H = 47;  // 用户 band3 底部背景高(棕色+灰边+烘好的方块/长条)
	// Create stock_keeper 块的棕色面板内容是居中内嵌的（两侧带灰色金属边栏）：x=33..223。
	// 内容(行/地址框/滚动条/底部按钮)必须对齐到这个棕色区域，否则会溢出到透明边。
	private static final int CONTENT_L = 38;
	private static final int CONTENT_R = 224;
	private static final int ROW_H = 18;           // 原料行高
	private static final int NODE_H = 25;          // 节点头行高(蓝图横幅 native 25)
	private static final int GROUP_H = 16;         // 组头行高
	private static final int ADDR_H = 18;          // 地址框(羊皮纸)行高
	private static final int GROUP_GAP = 16;        // 组间距
	private static final int NODE_GAP = 2;         // 节点间距
	private static final int SCROLL_W = 5;         // 滚动条宽
	private static final int BODY_TOP = HEADER_H;  // 内容起始(在头下方)
	private static final int ITEM = 16;            // 物品图标边长
	// 原料行局部坐标
	private static final int CHK_X = CONTENT_L + 2;           // 勾选框 x
	private static final int CHK_W = 12;
	private static final int MAT_ICON_X = CHK_X + CHK_W + 2;   // 原料槽 x
	private static final int MAT_NAME_X = MAT_ICON_X + 18 + 6; // 原料名 x(Create 槽 18 宽)
	// 节点头局部坐标
	private static final int NODE_ICON_X = CONTENT_L + 2;
	private static final int NODE_NAME_X = NODE_ICON_X + ITEM + 6;
	// 地址行局部坐标
	private static final int ADDR_X = CONTENT_L;
	private static final int ADDR_TEXT_IN = 12;      // 文本左缩进(越过圆环)
	static {loadAddrs();}
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
	public AutoReplenishScreen(StockKeeperRequestScreen parent, StockTickerBlockEntity blockEntity, List<ReplenishGroup> groups) {
		this.parent = parent;
		this.blockEntity = blockEntity;
		this.groups = groups;
		// 每类型地址（优先取磁盘上次记录，否则空）
		for (ReplenishGroup group : groups) groupAddress.add(SAVED_ADDRS.getOrDefault(group.name(), ""));
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
			List<Node> nodes = g.nodes();
			for (var ni = 0; ni < nodes.size(); ni++) {
				List<ReplenishEntry> items = nodes.get(ni).items();
				rows.add(new Row(y, NODE_H, RowKind.NODE, gi, ni, -1));
				y += NODE_H;
				for (var mi = 0; mi < items.size(); mi++) {
					rows.add(new Row(y, ROW_H, RowKind.MAT, gi, ni, mi));
					y += ROW_H;
				}
				y += NODE_GAP;
			}
			rows.add(new Row(y, ADDR_H, RowKind.ADDR, gi, -1, -1));
			y += ADDR_H + GROUP_GAP;
		}
		contentH = y;
	}
	private static void loadAddrs() {
		try {
			if (Files.exists(ADDR_FILE)) for (String line : Files.readAllLines(ADDR_FILE)) {
				int i = line.indexOf('=');
				if (i > 0) SAVED_ADDRS.put(line.substring(0, i), line.substring(i + 1));
			}
		} catch (Exception e) {CCG.LOGGER.warn("autoReplenish addr load failed", e);}
	}
	private static String groupName(String group) {
		if (group == null || group.isBlank()) return "";
		int idx = group.indexOf(':');
		return idx >= 0 ? group.substring(idx + 1) : group;
	}
	/** 文本超出最大宽度时按字符裁剪（避免挤到右侧数量） */
	private static String fitting(Font font, String s, int maxW) {
		if (maxW <= 0 || s == null) return "";
		while (font.width(s) > maxW && s.length() > 1) s = s.substring(0, s.length() - 1);
		return s;
	}
	@Override
	protected void init() {
		int needed = HEADER_H + contentH + FOOTER_H + 8;
		int screenH = mc.getWindow().getGuiScaledHeight();
		panelH = Math.clamp(screenH - 40, 80, needed);
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
			String type = groups.get(i).name();
			// anchorToBottom=true：剪贴板地址下拉建议显示在地址框上方（同 Create 仓管）
			var box = new AddressEditBox(this, mc.font, windowXOffset + ADDR_X + ADDR_TEXT_IN, windowYOffset + BODY_TOP, boxW, 10, true);
			box.setTextColor(0xFF714A40);
			box.setTextShadow(false); // 地址输入文字不渲染阴影(NeoForge textShadow)
			box.setValue(groupAddress.get(i));
			int idx = i;
			box.setResponder(text -> {
				groupAddress.set(idx, text);
				SAVED_ADDRS.put(type, text);
				saveAddrs();
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
	private static void saveAddrs() {
		try {
			var sb = new StringBuilder();
			for (Entry<String, String> e : SAVED_ADDRS.entrySet())
				sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');
			Files.writeString(ADDR_FILE, sb.toString());
		} catch (Exception e) {CCG.LOGGER.warn("autoReplenish addr save failed", e);}
	}
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
		AllGuiTextures.STOCK_KEEPER_REQUEST_HEADER.render(gui, 0, 0);
		var body = AllGuiTextures.STOCK_KEEPER_REQUEST_BODY;
		int bodyEnd = panelH - FOOTER_H;
		for (int y = HEADER_H; y < bodyEnd; y += 20)
			gui.blit(body.location, 0, y, body.getStartX(), body.getStartY(), PANEL_W, Math.min(20, bodyEnd - y));
		// 底部：用户提供的 band3 背景(棕色面板33..222 + 灰色底边 + 烘好的返回方块/发送长条)
		gui.blitSprite(sprite("bg_footer"), (PANEL_W - 224) / 2, bodyEnd, 224, FOOTER_H);
		pose.popPose();
	}
	private static ResourceLocation sprite(String name) {
		return ResourceLocation.fromNamespaceAndPath("create_cyber_goggles", "auto_replenish/" + name);
	}
	@Override
	protected void renderWindow(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
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
				case NODE -> drawNode(gui, font, r, top);
				case MAT -> drawMat(gui, font, r, top);
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
	/** Create 蓝图横幅：左端(10) + 中端(4×n) + 右端(10)，高 25（待合成物品的蓝图框） */
	private void drawBlueprint(GuiGraphics g, int x, int y, int w, int h) {
		AllGuiTextures.STOCK_KEEPER_REQUEST_BLUEPRINT_LEFT.render(g, x, y);
		int mx = x + 10;
		while (mx < x + w - 10) {
			AllGuiTextures.STOCK_KEEPER_REQUEST_BLUEPRINT_MIDDLE.render(g, mx, y);
			mx += 4;
		}
		AllGuiTextures.STOCK_KEEPER_REQUEST_BLUEPRINT_RIGHT.render(g, x + w - 10, y);
	}
	private void drawNode(GuiGraphics gui, Font font, Row r, int top) {
		Node n = groups.get(r.gi()).nodes().get(r.ni());
		// 蓝白蓝图框
		drawBlueprint(gui, CONTENT_L, top, contentW(), NODE_H);
		gui.renderItem(n.target(), NODE_ICON_X + 1, top + (NODE_H - ITEM) / 2);
		String count = "x" + n.craftTimes();
		int cw = font.width(count);
		int ty = top + (NODE_H - 9) / 2 + 1;
		gui.drawString(font, count, contentR() - cw - 12, ty, n.craftTimes() > 0 ? 0xFF1E2A6E : 0xFFB02A2A, false);
		String name = fitting(font, n.target().getHoverName().getString(), contentR() - cw - 12 - NODE_NAME_X - 6);
		gui.drawString(font, name, NODE_NAME_X, ty, 0xFF1B2F7A, false);
	}
	private void drawMat(GuiGraphics gui, Font font, Row r, int top) {
		Node n = groups.get(r.gi()).nodes().get(r.ni());
		ReplenishEntry e = n.items().get(r.mi());
		boolean enough = e.enough();
		// 行底 + 勾选 + 原料槽 + 名称 + 数量（右对齐）
		gui.blitSprite(sprite("row_bg"), CONTENT_L, top, contentW(), ROW_H - 2);
		// Create 原生勾选框(字体字符，同 ClipboardScreen)：□ 空心方块，够料时叠 ✔ 绿勾
		gui.drawString(font, "□", CHK_X, top + 4, enough ? 0x668D7F6B : 0xFF8D7F6B, false);
		if (enough) gui.drawString(font, "✔", CHK_X, top + 3, 0xFF31B25D, false);
		// Create 仓管页现成的原料槽(18x18)
		AllGuiTextures.STOCK_KEEPER_REQUEST_SLOT.render(gui, MAT_ICON_X, top);
		gui.renderItem(e.material(), MAT_ICON_X + 1, top + 1);   // 16x16 居中
		String count = e.per() + "×" + n.craftTimes();
		int cw = font.width(count);
		gui.drawString(font, count, contentR() - cw, top + 4, enough ? 0xFFC8B688 : 0xFFFF8A8A, false);
		String name = fitting(font, e.material().getHoverName().getString(), contentR() - cw - MAT_NAME_X - 6);
		gui.drawString(font, name, MAT_NAME_X, top + 4, enough ? 0xFF9FBF8F : 0xFFFF8A8A, false);
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
	private int scrollX() {return windowXOffset + CONTENT_R - SCROLL_W;}
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
		MAT,
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
				gui.drawString(mc.font, label, getX() + (getWidth() - tw) / 2, getY() + (getHeight() - 9) / 2 + 1, 0xFF000000, false);
			}
		}
		@Override
		public void onClick(double mx, double my) {action.run();}
	}
}
