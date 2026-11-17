package com.takoy3466.additional_pipes2.client.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * 周波数の名前の一覧 (スクロールできる 1 列のリスト)。元実装: {@code GuiNameSlot} + {@code GuiScrollSlot}。
 * 入力中の名前で始まるものを先頭に、それ以外を後ろに、それぞれ大文字小文字を無視した名前順で並べる。
 * 名前をクリックすると {@code onSelect} に渡す。
 */
final class FrequencyNameList {
    private static final int ROW_HEIGHT = 11;
    private static final int TEXT_PADDING = 2;
    private static final int BACKGROUND_COLOR = 0xFF000000;
    private static final int BORDER_COLOR = 0xFFA0A0A0;
    private static final int HOVER_COLOR = 0xFF404040;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int MATCH_TEXT_COLOR = 0xFFFFFF80;

    private final Font font;
    private final Consumer<String> onSelect;
    private int x;
    private int y;
    private final int width;
    private final int height;
    private List<String> names = List.of();
    private String match = "";
    private int scrollRows;

    FrequencyNameList(Font font, int width, int height, Consumer<String> onSelect) {
        this.font = font;
        this.width = width;
        this.height = height;
        this.onSelect = onSelect;
    }

    void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** 表示する名前と、優先して並べる入力中の文字列を設定する。 */
    void update(List<String> newNames, String newMatch) {
        List<String> sorted = new ArrayList<>(newNames);
        sorted.sort(Comparator.comparing((String name) -> !matches(name, newMatch))
                .thenComparing(name -> name.toLowerCase(Locale.ROOT)));
        this.names = sorted;
        this.match = newMatch;
        scrollRows = Mth.clamp(scrollRows, 0, maxScroll());
    }

    /** 名前が入力中の文字列で始まるか (大文字小文字を区別しない, 旧 doesNameMatch)。 */
    static boolean matches(String name, String match) {
        return name.length() >= match.length() && name.regionMatches(true, 0, match, 0, match.length());
    }

    private int visibleRows() {
        return height / ROW_HEIGHT;
    }

    private int maxScroll() {
        return Math.max(0, names.size() - visibleRows());
    }

    boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    void scroll(double amount) {
        scrollRows = Mth.clamp(scrollRows - (int) Math.signum(amount), 0, maxScroll());
    }

    boolean mouseClicked(double mouseX, double mouseY) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }
        int index = scrollRows + (int) ((mouseY - y) / ROW_HEIGHT);
        if (index >= 0 && index < names.size()) {
            onSelect.accept(names.get(index));
            return true;
        }
        return false;
    }

    void render(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, BORDER_COLOR);
        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
        int rows = Math.min(visibleRows(), names.size() - scrollRows);
        for (int row = 0; row < rows; row++) {
            String name = names.get(scrollRows + row);
            int rowY = y + row * ROW_HEIGHT;
            if (mouseX >= x && mouseX < x + width && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                graphics.fill(x, rowY, x + width, rowY + ROW_HEIGHT, HOVER_COLOR);
            }
            int color = !match.isEmpty() && matches(name, match) ? MATCH_TEXT_COLOR : TEXT_COLOR;
            graphics.drawString(font, name, x + TEXT_PADDING, rowY + TEXT_PADDING, color, false);
        }
    }
}
