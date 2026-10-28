package io.github.jimbozoomer.jugcraft.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * The magic interfaces' screen (docs/features/magic-interfaces.md, spec 4.1): the skeleton the pact and roster screens
 * each wrote for themselves. Every control has a stable key that survives a rebuild (a state packet marks the screen
 * dirty; the next tick rebuilds it and gives the keyboard back to the same key); the body scrolls in a
 * {@link ComposerScroll}; the footer's buttons share their row by their labels' widths. The backdrop is the cosmos over
 * the dimmed world, then the subtitles (an in-game screen, as a chest's, so spirits' cues stay readable).
 * <p>
 * For the tests: {@link #focusedKey}, {@link #control}, {@link #inView}, {@link #outside} (every control, the arrows and
 * bookmarks beside the panel included) and {@link #drawn} (every string the screen draws without hovering, for the
 * word budgets of spec 1.1). Native widgets only: no text box, no GuiLib, no mixin.
 */
public abstract class MagicScreen extends Screen implements ScreenKit.Fitted {
	/** A widget that draws words: what it draws without being hovered (its message may be longer). */
	interface Drawn {
		String drawnText();
	}

	protected final Map<String, AbstractWidget> controls = new LinkedHashMap<>();
	protected final Set<String> footer = new LinkedHashSet<>();
	private final List<AbstractWidget> parts = new ArrayList<>();
	private final List<String> drewChrome = new ArrayList<>();
	protected @Nullable ComposerScroll body;
	protected MagicLayout layout = MagicLayout.pact(MagicLayout.PANEL_WIDTH, MagicLayout.FULL_FROM, 0);
	protected MagicTheme theme = MagicTheme.SCROLL;
	/** The key the keyboard is on (kept across rebuilds). */
	protected String focus = "";
	protected boolean dirty;
	/** The next free offset in the body. */
	protected int at;
	private double scrollTo = -1;

	protected MagicScreen(Component title) {
		super(title);
	}

	/** Builds this state's widgets: init calls it after the layout is chosen and before focus is given back. */
	protected abstract void build();

	/** The layout for this GUI size (by default the pact screen's, with nothing known of the body's words). */
	protected MagicLayout layout(int width, int height) {
		return MagicLayout.pact(width, height, 0);
	}

	/** Draws the panel's own art over the backdrop (header, scrolls); called from extractBackground. */
	protected void extractChrome(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
	}

	/** Whether the keyboard should stay where it is now (while an answer is awaited). */
	protected boolean holdsFocus() {
		return false;
	}

	@Override
	protected void init() {
		double scroll = body == null ? 0 : body.scrollAmount();
		controls.clear();
		footer.clear();
		parts.clear();
		drewChrome.clear();
		body = null;
		at = 0;
		scrollTo = -1;
		layout = layout(width, height);
		build();
		if (body != null) {
			body.arrange(scrollTo >= 0 ? scrollTo : scroll);
		}
		restore();
	}

	/** Opens the scrolling body over {@code area} (build calls it once; pinned lines sit outside it). */
	protected ComposerScroll openBody(ScreenRectangle area) {
		body = addRenderableWidget(new ComposerScroll(minecraft, area.left(), area.top(), area.width(), Math.max(24, area.height()), this::focused));
		return body;
	}

	/** Scrolls the body to {@code amount} after this build (a new state of things starts at its top). */
	protected void scrollTo(double amount) {
		scrollTo = amount;
	}

	/** Adds {@code widget} to the body at the next offset, under {@code key} when it has one; spacing follows it. */
	protected <T extends AbstractWidget> T inBody(String key, T widget, int spacing) {
		if (body == null) {
			throw new IllegalStateException("openBody first");
		}
		body.add(widget, at);
		at += widget.getHeight() + spacing;
		return keep(key, widget);
	}

	/** Adds {@code widget} to the body at {@code offset} (the next offset stays where it is), under {@code key} when it has one. */
	protected <T extends AbstractWidget> T inBodyAt(String key, T widget, int offset) {
		if (body == null) {
			throw new IllegalStateException("openBody first");
		}
		body.add(widget, offset);
		return keep(key, widget);
	}

	/** Adds {@code widget} to the screen itself (header, pinned lines, arrows, bookmarks, footer), under {@code key}. */
	protected <T extends AbstractWidget> T onScreen(String key, T widget) {
		addRenderableWidget(widget);
		return keep(key, widget);
	}

	private <T extends AbstractWidget> T keep(String key, T widget) {
		parts.add(widget);
		if (!key.isEmpty()) {
			controls.put(key, widget);
		}
		return widget;
	}

	/**
	 * One row of footer buttons, centred: each as wide as its label needs (MagicButton.labelWidth) and the row's spare
	 * room shared out, up to {@code widest} each (a plate with an icon alone, the narrow footer's page arrow, keeps its
	 * own width); their tips open below the footer or beside the button, never over the
	 * body (spec 2.8). A row that cannot hold every label keeps equal widths, and outside() names the cut label.
	 */
	protected void footer(int widest, String[] keys, MagicButton... buttons) {
		ScreenRectangle row = layout.footer();
		int gap = 5;
		int room = row.width() - gap * (buttons.length - 1);
		int needed = 0;
		int[] need = new int[buttons.length];
		for (int i = 0; i < buttons.length; i++) {
			need[i] = buttons[i].labelWidth(font);
			needed += need[i];
		}
		int spare = Math.max(0, room - needed);
		int[] widths = new int[buttons.length];
		int total = gap * (buttons.length - 1);
		for (int i = 0; i < buttons.length; i++) {
			widths[i] = needed > room ? Math.max(1, room / buttons.length)
					: buttons[i].look().width > 0 ? buttons[i].look().width : buttons[i].iconOnly() ? need[i]
					: Math.min(Math.max(need[i], widest), need[i] + spare / buttons.length);
			total += widths[i];
		}
		int x = row.left() + (row.width() - total) / 2;
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setRectangle(widths[i], buttons[i].getHeight(), x, row.top() + (row.height() - buttons[i].getHeight()) / 2);
			buttons[i].footer(row);
			onScreen(keys[i], buttons[i]);
			footer.add(keys[i]);
			x += widths[i] + gap;
		}
	}

	/** Records a string the chrome draws (the header's name and words), for drawn(). */
	protected void drew(String text) {
		if (!text.isEmpty()) {
			drewChrome.add(text);
		}
	}

	private void restore() {
		AbstractWidget widget = controls.get(focus);
		if (widget == null || !widget.active) {
			return;
		}
		if (body != null && body.children().contains(widget)) {
			setFocused(body);
			body.setFocused(widget);
		} else {
			setFocused(widget);
		}
	}

	protected void focused(@Nullable GuiEventListener target) {
		controls.forEach((key, widget) -> {
			if (widget == target) {
				focus = key;
			}
		});
	}

	@Override
	public void setFocused(@Nullable GuiEventListener target) {
		super.setFocused(target);
		focused(target);
	}

	@Override
	protected void setInitialFocus() {
		// A rebuild does not move the keyboard on, and while an answer is awaited the keyboard waits where it is.
		if (getFocused() == null && !holdsFocus()) {
			super.setInitialFocus();
		}
	}

	@Override
	public void tick() {
		super.tick();
		MagicPortraits.tick();
		if (dirty) {
			dirty = false;
			rebuildWidgets();
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		MagicArt.frame();
		// The world dimmed, then the cosmos centred on the panel (spec 2.1, 2.9: its frame by time, still under reduced motion).
		g.fill(0, 0, width, height, 0xA0080610);
		ScreenRectangle panel = layout.panel();
		MagicArt.cosmos(g, panel.left() + panel.width() / 2, panel.top() + panel.height() / 2);
		extractChrome(g, mouseX, mouseY, delta);
		// Spirits' subtitles over this screen (deferred to here, as vanilla's Screen.extractBackground does).
		minecraft.gui.hud.extractDeferredSubtitles();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}

	@Override
	public ScreenRectangle panelArea() {
		return layout.panelArea();
	}

	/** The key of the control the keyboard is on ("" for none), for tests. */
	public String focusedKey() {
		GuiEventListener leaf = getFocused();
		while (leaf instanceof ContainerEventHandler container && container.getFocused() != null) {
			leaf = container.getFocused();
		}
		for (Map.Entry<String, AbstractWidget> entry : controls.entrySet()) {
			if (entry.getValue() == leaf) {
				return entry.getKey();
			}
		}
		return "";
	}

	/** The control under {@code key}, or null, for tests. */
	public @Nullable AbstractWidget control(String key) {
		return controls.get(key);
	}

	/** Whether the control under {@code key} lies wholly inside the body's visible part (or is not in the body), for tests. */
	public boolean inView(String key) {
		AbstractWidget widget = controls.get(key);
		if (widget == null) {
			return false;
		}
		if (body == null || !body.children().contains(widget)) {
			return true;
		}
		return widget.getY() >= body.getY() && widget.getY() + widget.getHeight() <= body.getY() + body.getHeight();
	}

	/**
	 * What does not fit (tests: none): the panel off screen; the body over the footer or under 24 tall; any control
	 * outside the screen (one in the body outside the body's width); a footer button cutting its label.
	 */
	public List<String> outside() {
		List<String> out = new ArrayList<>();
		if (!layout.onScreen(layout.panelArea())) {
			out.add("panel");
		}
		if (body != null && (body.getY() + body.getHeight() > layout.footer().top() || body.getHeight() < 24)) {
			out.add("body");
		}
		for (Map.Entry<String, AbstractWidget> entry : controls.entrySet()) {
			AbstractWidget widget = entry.getValue();
			boolean inside = body != null && body.children().contains(widget);
			boolean off = inside ? widget.getX() < body.getX() || widget.getX() + widget.getWidth() > body.getX() + body.getWidth()
					: widget.getX() < 0 || widget.getY() < 0 || widget.getX() + widget.getWidth() > width || widget.getY() + widget.getHeight() > height;
			if (off) {
				out.add(entry.getKey());
			} else if (footer.contains(entry.getKey()) && widget instanceof MagicButton button && button.labelWidth(font) > button.getWidth()) {
				out.add(entry.getKey() + " (label cut)");
			}
		}
		return out;
	}

	/**
	 * Every string this screen draws without hovering: the chrome's and each visible part's, a part of the body only while
	 * some of it is scrolled into view (spec 1.1's budgets), for tests.
	 */
	public List<String> drawn() {
		List<String> out = new ArrayList<>(drewChrome);
		for (AbstractWidget part : parts) {
			boolean shown = body == null || !body.children().contains(part)
					|| part.getY() < body.getY() + body.getHeight() && part.getY() + part.getHeight() > body.getY();
			if (part.visible && shown && part instanceof Drawn drawn && !drawn.drawnText().isEmpty()) {
				out.add(drawn.drawnText());
			}
		}
		return out;
	}

	/** How many controls Tab stops at: every visible part that takes focus (spec 4.5: at most 20 on a terms page), for tests. */
	public int tabStops() {
		int stops = 0;
		for (AbstractWidget part : parts) {
			if (part.isActive()) {
				stops++;
			}
		}
		return stops;
	}

	/** The key of the control whose tip was drawn in the last whole frame ("" for none), for tests. */
	public String tipped() {
		AbstractWidget shown = MagicArt.tipped();
		for (Map.Entry<String, AbstractWidget> entry : controls.entrySet()) {
			if (entry.getValue() == shown) {
				return entry.getKey();
			}
		}
		return "";
	}

	/** Where the tip of the button under {@code key} opens for the keyboard (null for none), for tests: a footer's never covers the body. */
	public @Nullable ScreenRectangle tipArea(String key) {
		return controls.get(key) instanceof MagicButton button ? button.tipArea(width, height) : null;
	}

	/** The body's scroll as drawn (its painted margins included), for tests. */
	public ScreenRectangle bodyArea() {
		return layout.body();
	}

	/** Whether this GUI size draws the compact layout (the header a plate row), for tests. */
	public boolean compact() {
		return layout.compact();
	}

	/** How many words {@link #drawn} holds: whitespace tokens with a letter or digit (spec 1.1). */
	public int words() {
		int words = 0;
		for (String text : drawn()) {
			for (String token : text.split("\\s+")) {
				if (token.chars().anyMatch(Character::isLetterOrDigit)) {
					words++;
				}
			}
		}
		return words;
	}
}
