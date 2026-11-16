package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.pharmacology.DreamStudyMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** The existing owner-authored alchemy frame and narrated native buttons. Clues stay readable at any visual intensity. */
public final class DreamStudyScreen extends AbstractContainerScreen<DreamStudyMenu> {
	private final TransmutationScreen.ActionButton[] glyphs = new TransmutationScreen.ActionButton[4];
	private final TransmutationScreen.ActionButton[] lessons = new TransmutationScreen.ActionButton[3];
	public DreamStudyScreen(DreamStudyMenu menu, Inventory inventory, Component title) { super(menu, inventory, title, 302, 214); }
	private static Component ui(String key, Object... args) { return Component.translatable("screen.jugcraft.dream_study." + key, args); }
	@Override protected void init() {
		super.init();
		for (int i = 0; i < 3; i++) {
			int id = i;
			lessons[i] = addRenderableWidget(new TransmutationScreen.ActionButton(leftPos + 16 + i * 90, topPos + 34, 88, ui("lesson_" + i), b -> press(4 + id)));
			lessons[i].setTooltip(Tooltip.create(ui("requires_" + i)));
		}
		for (int i = 0; i < 4; i++) {
			int id = i;
			glyphs[i] = addRenderableWidget(new TransmutationScreen.ActionButton(leftPos + 16 + i * 68, topPos + 148, 66, ui("glyph_" + i), b -> press(id)));
		}
		refresh();
	}
	private void press(int id) { if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id); }
	private void refresh() {
		for (var glyph : glyphs) {
			glyph.active = menu.ready();
			glyph.setTooltip(Tooltip.create(menu.ready()?ui("clue_tooltip",ui("rule_"+menu.lesson()),clue(0),clue(1),clue(2),trace(0),trace(1),trace(2)):ui(menu.status()==2?"status_2":"requires_"+menu.lesson())));
		}
		for (int i = 0; i < lessons.length; i++) lessons[i].selected = menu.lesson() == i;
	}
	private Component clue(int index) {return !menu.ready() || menu.clue(index)<0?ui("hidden"):ui("glyph_"+menu.clue(index));}
	private Component trace(int index) {return menu.trace(index)<0?ui("hidden"):ui("glyph_"+menu.trace(index));}
	@Override protected void containerTick() { super.containerTick(); refresh(); }
	@Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) { extractBackground(g, mx, my, delta); super.extractRenderState(g, mx, my, delta); extractTooltip(g, mx, my); }
	@Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {
		g.fill(0, 0, width, height, 0xB0081018);
		OwnerAlchemyArt.frame(g, leftPos, topPos, imageWidth, imageHeight);
		OwnerAlchemyArt.panel(g, leftPos + 14, topPos + 61, 274, 80, OwnerAlchemyArt.VIOLET);
		OwnerAlchemyArt.meter(g, leftPos + 20, topPos + 134, 262, menu.progress(), 3, OwnerAlchemyArt.TEAL);
	}
	private void text(GuiGraphicsExtractor g, Component text, int x, int y, int color) { g.text(font, BookArt.fit(font, text.getString(), 262), x, y, color, false); }
	@Override protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
		text(g, title, 18, 15, 0xFFE1DAE9);
		text(g, ui("rule_" + menu.lesson()), 22, 68, 0xFFB8F0ED);
		int step=menu.lesson()==1?2-menu.progress():menu.progress();
		for(int i=0;i<3;i++) {
			int x=22+i*85;boolean current=menu.ready() && menu.progress()<3 && i==step;
			g.fill(x,81,x+80,118,current?0xFF92D5CF:0xFF6C6684);
			g.fill(x+1,82,x+79,117,current?0xFF223D43:0xFF171E2C);
			g.text(font,BookArt.fit(font,ui("card",i+1,clue(i)).getString(),72),x+4,87,0xFFEDDAFF,false);
			int order=menu.lesson()==1?2-i:i;
			g.text(font,BookArt.fit(font,menu.ready()?ui("input_card",order+1,trace(order)).getString():ui("hidden").getString(),72),x+4,102,current?0xFFB8F0ED:0xFFBCB9C8,false);
		}
		text(g, ui("progress", menu.progress()), 22, 122, 0xFFC9C6D0);
		Component reward = Component.translatable("item.jugcraft." + switch (menu.lesson()) { case 0 -> "lucid_imprint"; case 1 -> "pattern_imprint"; default -> "oneiric_imprint"; });
		text(g, menu.status() == 0 ? ui("requires_" + menu.lesson()) : menu.status() == 1 ? ui("trace_reward", reward) : ui("status_" + menu.status()), 20, 175, 0xFFD5D5DE);
		text(g, ui("cost"), 20, 194, 0xFFACA5B8);
	}
}
