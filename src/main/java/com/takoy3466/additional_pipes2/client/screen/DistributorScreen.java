package com.takoy3466.additional_pipes2.client.screen;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.menu.DistributorMenu;
import com.takoy3466.additional_pipes2.pipe.behavior.DistributionCounts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 分配パイプの設定画面。元実装: {@code GuiDistributionPipe}。
 * 面ごと (下・上・北・南・西・東, 背景の色見本の順) に「-」「個数」「+」を並べる。
 */
public class DistributorScreen extends AbstractContainerScreen<DistributorMenu> {
    private static final ResourceLocation TEXTURE = AdditionalPipes2.id("textures/gui/distribution_pipe.png");
    private static final int IMAGE_WIDTH = 175;
    private static final int IMAGE_HEIGHT = 130;
    /** 旧版の配置: 画面左端 + 30、上端 - 7 を基準に、1 段目が +24、以降 18 ずつ下がる。 */
    private static final int ROW_LEFT = 30;
    private static final int FIRST_ROW_TOP = 17;
    private static final int ROW_PITCH = 18;
    private static final int BUTTON_HEIGHT = 17;
    private static final int SMALL_BUTTON_WIDTH = 20;
    private static final int VALUE_WIDTH = 30;

    private static final int VALUE_COLOR = 0x404040;
    private static final int VALUE_TEXT_OFFSET_Y = 5;

    public DistributorScreen(DistributorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = IMAGE_WIDTH;
        imageHeight = IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        for (int side = 0; side < DistributionCounts.SIDES; side++) {
            int x = leftPos + ROW_LEFT;
            int y = topPos + FIRST_ROW_TOP + side * ROW_PITCH;
            int decreaseId = DistributorMenu.buttonId(side, false);
            int increaseId = DistributorMenu.buttonId(side, true);
            addRenderableWidget(Button.builder(Component.literal("-"), button -> clickButton(decreaseId))
                    .bounds(x + 1, y, SMALL_BUTTON_WIDTH, BUTTON_HEIGHT).build());
            addRenderableWidget(Button.builder(Component.literal("+"), button -> clickButton(increaseId))
                    .bounds(x + 5 + SMALL_BUTTON_WIDTH + VALUE_WIDTH, y, SMALL_BUTTON_WIDTH, BUTTON_HEIGHT).build());
        }
    }

    private void clickButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    /** 各段の「-」と「+」の間に送出個数を描く (旧版は押せないボタンに数値を表示していた)。題名は描かない。 */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int centerX = ROW_LEFT + 3 + SMALL_BUTTON_WIDTH + VALUE_WIDTH / 2;
        for (int side = 0; side < DistributionCounts.SIDES; side++) {
            String value = String.valueOf(menu.amount(side));
            int y = FIRST_ROW_TOP + side * ROW_PITCH + VALUE_TEXT_OFFSET_Y;
            graphics.drawString(font, value, centerX - font.width(value) / 2, y, VALUE_COLOR, false);
        }
    }
}
