package io.github.forgestove.create_cyber_goggles.core.factory;
import com.simibubi.create.content.logistics.*;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts.CraftingEntry;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.factory.ReplenishGroup.*;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.platform.CatnipServices;
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
 * 继承 catnip {@link AbstractSimiScreen}。<b>每组一个配方类型</b>（组内该类型所有待合成节点），
 * 组尾一个<b>共享地址框</b>（按类型，曾用地址持久化到磁盘，下次自动填充）。
 * 每节点显示其产物 + 全部原料（够的绿勾/不足红框）+ 可合成次数；
 * 下单与原版一致：装配类 convertRecipe（9 格 pattern），加工类通用 pattern，orderedStacks=每原料×craftTimes
 * （不足一次则整节点阻止发送）。界面带可拖动滚动条。
 */
public class AutoReplenishScreen extends AbstractSimiScreen {
	private static final Map<String, String> SAVED_ADDRS = new LinkedHashMap<>();
	private static final Path ADDR_FILE = Path.of("config").resolve("create_cyber_goggles_auto_replenish_addrs.txt");
	// 面板几何（GUI 缩放像素）
	private static final int PANEL_W = 250;
	private static final int HEADER_H = 18;
	private static final int FOOTER_H = 24;
	private static final int ROW_H = 18;
	private static final int GROUP_HEADER = 14;
	private static final int ADDR_H = 14;
	private static final int GROUP_GAP = 6;
	private static final int ROW_X = 4;
	private static final int BODY_TOP = HEADER_H + 3;
	private static final int ITEM_SLOT = 12;
	private static final int SCROLL_W = 5;
	static {loadAddrs();}
	private final StockKeeperRequestScreen parent;
	private final StockTickerBlockEntity blockEntity;
	private final List<ReplenishGroup> groups;
	private final List<AddressEditBox> addrBoxes = new ArrayList<>();
	private final List<String> groupAddress = new ArrayList<>();
	private int scroll;
	private boolean scrollDragging;
	private double scrollDragOffset;
	public AutoReplenishScreen(StockKeeperRequestScreen parent, StockTickerBlockEntity blockEntity, List<ReplenishGroup> groups) {
		this.parent = parent;
		this.blockEntity = blockEntity;
		this.groups = groups;
		// 每类型地址（优先取磁盘上次记录，否则空）
		for (ReplenishGroup group : groups) groupAddress.add(SAVED_ADDRS.getOrDefault(group.name(), ""));
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1));
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
	private int bodyArea() {return height() - HEADER_H - FOOTER_H - 8;}
	private int maxScroll() {return Math.max(0, contentHeight() - bodyArea());}
	private int scrollX() {return windowXOffset + PANEL_W - SCROLL_W - 3;}
	private int scrollTrackTop() {return windowYOffset + BODY_TOP;}
	private int scrollTrackHeight() {return Math.max(1, bodyArea());}
	private int thumbHeight() {return Math.max(10, (int) ((float) bodyArea() / Math.max(1, contentHeight()) * scrollTrackHeight()));}
	private int thumbTop() {
		int track = scrollTrackHeight();
		int thumb = thumbHeight();
		int maxThumbTop = track - thumb;
		return scrollTrackTop() + (maxScroll() == 0 ? 0 : (int) ((float) scroll / maxScroll() * maxThumbTop));
	}
	@Override
	protected void init() {
		setWindowSize(PANEL_W, height());
		var window = mc.getWindow();
		setWindowOffset((window.getGuiScaledWidth() - PANEL_W) / 2, (window.getGuiScaledHeight() - height()) / 2);
		super.init();
		// 每组（类型）一个共享地址框（按类型持久化）
		for (var i = 0; i < groups.size(); i++) {
			String type = groups.get(i).name();
			var box = new AddressEditBox(
				this,
				mc.font,
				windowXOffset + addrX(),
				windowYOffset + BODY_TOP,
				addrWidth(),
				10,
				true,
				groupAddress.get(i)
			);
			box.setTextColor(0xFF714A40);
			int idx = i;
			box.setResponder(text -> {
				groupAddress.set(idx, text);
				SAVED_ADDRS.put(type, text);
				saveAddrs();
			});
			addrBoxes.add(box);
			addRenderableWidget(box);
		}
		var confirm = new IconButton(windowXOffset + PANEL_W - 46, windowYOffset + height() - FOOTER_H + 4, AllIcons.I_CONFIRM);
		confirm.withCallback(this::sendAll);
		confirm.setToolTip(Component.translatable("config.ui.quit.confirm"));
		addRenderableWidget(confirm);
		var cancel = new IconButton(windowXOffset + PANEL_W - 26, windowYOffset + height() - FOOTER_H + 4, AllIcons.I_CONFIG_BACK);
		cancel.withCallback(this::onClose);
		cancel.setToolTip(Component.translatable("gui.cancel"));
		addRenderableWidget(cancel);
	}
	private int height() {
		int needed = HEADER_H + contentHeight() + FOOTER_H + 8;
		int screenH = mc.getWindow().getGuiScaledHeight();
		return Math.clamp(screenH - 40, 80, needed);
	}
	private int addrX() {return ROW_X + ITEM_SLOT + 26;}
	private int addrWidth() {return PANEL_W - addrX() - 8;}
	private static void saveAddrs() {
		try {
			var sb = new StringBuilder();
			for (Entry<String, String> e : SAVED_ADDRS.entrySet())
				sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');
			Files.writeString(ADDR_FILE, sb.toString());
		} catch (Exception e) {CCG.LOGGER.warn("autoReplenish addr save failed", e);}
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
	@Override
	public void onClose() {
		removed();
		mc.screen = parent;
	}
	private int contentHeight() {
		var h = 0;
		for (ReplenishGroup g : groups) {
			var px = 0;
			for (Node n : g.nodes())
				px += 12 + n.items().size() * ROW_H;   // 节点头 12 + 原料行
			h += GROUP_HEADER + Math.max(1, px) + ADDR_H + GROUP_GAP;
		}
		return h;
	}
	@Override
	public void resize(@NotNull Minecraft mc, int width, int height) {
		parent.resize(mc, width, height);
		super.resize(mc, width, height);
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
		pose.popPose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		gui.blitSprite(sprite("frame"), 0, 0, PANEL_W, height());
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
		Component title = Component.translatable("create_cyber_goggles.gui.auto_replenish.title");
		gui.drawString(mc.font, title, (PANEL_W - mc.font.width(title)) / 2, 4, 0xFFFCF6E6, false);
		int scissorBottom = Math.max(windowYOffset + BODY_TOP + 1, windowYOffset + BODY_TOP + Math.max(1, bodyArea()) - 2);
		gui.enableScissor(windowXOffset + 4, windowYOffset + BODY_TOP, windowXOffset + PANEL_W - 4, scissorBottom);
		Font font = mc.font;
		if (groups.isEmpty()) {
			Component empty = Component.translatable("create_cyber_goggles.gui.auto_replenish.empty");
			gui.drawString(font, empty, (PANEL_W - font.width(empty)) / 2, BODY_TOP + 4, 0xFFC8B688, false);
		}
		int y = BODY_TOP;
		for (var gi = 0; gi < groups.size(); gi++) {
			ReplenishGroup g = groups.get(gi);
			int groupTop = y - clamped;
			gui.drawString(font, groupName(g.name()), ROW_X, groupTop + 1, 0xFFD9C792, false);
			gui.blitSprite(sprite("divider"), ROW_X, groupTop + GROUP_HEADER - 2, PANEL_W - ROW_X * 2, 1);
			y += GROUP_HEADER;
			for (Node n : g.nodes()) {
				// 节点头：产物名 x可合成次数
				Component head = Component.literal(n.target().getHoverName().getString() + "  x" + n.craftTimes());
				gui.drawString(font, head, ROW_X, y - clamped + 1, n.craftTimes() > 0 ? 0xFFE8DEC8 : 0xFFFF6B6B, false);
				y += 12;
				// 全部原料，每原料勾选框
				for (ReplenishEntry e : n.items()) {
					int rowTop = y - clamped;
					gui.blitSprite(sprite("row_bg"), ROW_X, rowTop, PANEL_W - ROW_X * 2, ROW_H - 2);
					gui.blitSprite(sprite(e.enough() ? "box_checked" : "box_unchecked"), ROW_X + 2, rowTop + 2, 12, 12);
					gui.renderItem(e.material(), ROW_X + 16, rowTop + 2);
					String name = e.material().getHoverName().getString();
					gui.drawString(
						font,
						name.length() > 10 ? name.substring(0, 10) : name,
						ROW_X + 32,
						rowTop + 3,
						e.enough() ? 0xFF9FBF8F : 0xFFFF8A8A,
						false
					);
					gui.drawString(font, "x" + e.per() + " * " + n.craftTimes(), ROW_X + 32, rowTop + 10, 0xFFC8B688, false);
					y += ROW_H;
				}
			}
			// 组尾共享地址框（按类型）
			int addrLocalY = y - clamped;
			gui.blitSprite(sprite("address_box"), addrX(), addrLocalY, addrWidth(), ADDR_H);
			if (gi < addrBoxes.size()) {
				AddressEditBox box = addrBoxes.get(gi);
				box.setX(windowXOffset + addrX());
				box.setY(windowYOffset + addrLocalY + 1);
			}
			y += ADDR_H + GROUP_GAP;
		}
		gui.disableScissor();
		pose.popPose();
		if (maxScroll() > 0) renderScrollbar(gui);
	}
	private void renderScrollbar(GuiGraphics gui) {
		int x = scrollX();
		int top = scrollTrackTop();
		int trackH = scrollTrackHeight();
		gui.fill(x, top, x + SCROLL_W, top + trackH, 0xFF20242C);
		int thumb = thumbHeight();
		gui.fill(x, thumbTop(), x + SCROLL_W, thumbTop() + thumb, 0xFFC8B688);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && maxScroll() > 0 && overScrollbar(mouseX, mouseY)) {
			scrollDragging = true;
			scrollDragOffset = mouseY - thumbTop();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
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
	private boolean overScrollbar(double mouseX, double mouseY) {
		return mouseX >= scrollX()
			&& mouseX < scrollX() + SCROLL_W
			&& mouseY >= scrollTrackTop()
			&& mouseY < scrollTrackTop() + scrollTrackHeight();
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (overScrollbar(mouseX, mouseY)) return true;
		scroll = Mth.clamp((int) (scroll - scrollY * 12), 0, maxScroll());
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
}
