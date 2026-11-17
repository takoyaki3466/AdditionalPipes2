package com.takoy3466.additional_pipes2.client.screen;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.menu.AdvancedWoodMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 高度な木製パイプのフィルター画面。元実装: {@code GuiAdvancedWoodPipe}。
 * フィルター 9 枠と、「これらのアイテムが必要 / これらのアイテムを除外」を切り替えるボタン。
 */
public class AdvancedWoodScreen extends AbstractContainerScreen<AdvancedWoodMenu> {
    private static final ResourceLocation TEXTURE = AdditionalPipes2.id("textures/gui/advanced_wooden_pipe.png");
    private static final int IMAGE_WIDTH = 175;
    private static final int IMAGE_HEIGHT = 156;
    private static final int LABEL_COLOR = 0x404040;
    private static final int INVENTORY_LABEL_Y = 66;
    private static final String LANG = "gui." + AdditionalPipes2.MODID + ".advanced_wooden_pipe.";

    private Button modeButton;

    public AdvancedWoodScreen(AdvancedWoodMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = IMAGE_WIDTH;
        imageHeight = IMAGE_HEIGHT;
        inventoryLabelY = INVENTORY_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        modeButton = addRenderableWidget(Button.builder(modeText(), button -> toggleMode())
                .bounds(leftPos + 8, topPos + 40, 140, 20).build());
    }

    private void toggleMode() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, AdvancedWoodMenu.BUTTON_TOGGLE_EXCLUDE);
        }
    }

    private Component modeText() {
        return Component.translatable(LANG + (menu.isExclude() ? "excluded" : "required"));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        modeButton.setMessage(modeText());
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

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, LABEL_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);
    }
}
