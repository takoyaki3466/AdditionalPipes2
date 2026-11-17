package com.takoy3466.additional_pipes2.client.screen;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.menu.TeleportPipeMenu;
import com.takoy3466.additional_pipes2.menu.TeleportPipeView;
import com.takoy3466.additional_pipes2.network.TeleportPipeActionPayload;
import com.takoy3466.additional_pipes2.network.TeleportPipeActionPayload.Action;
import com.takoy3466.additional_pipes2.teleport.FrequencyNameBook;
import com.takoy3466.additional_pipes2.teleport.TeleportSettings;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * テレポートパイプの設定画面。元実装: {@code GuiTeleportPipeNew} (配置・操作は旧版に合わせている)。
 * <ul>
 *   <li>周波数: 入力欄 + 「Set」、-1/+1/-10/+10/-100/+100 ボタン、入力欄の上でのマウスホイール</li>
 *   <li>周波数の名前: 名前欄 + 「Add」/「Remove」、名前の一覧 (クリックでその周波数へ切り替え)</li>
 *   <li>受信の切り替え、リンク数・所有者の表示</li>
 * </ul>
 * 操作は全てサーバーへ送り ({@link TeleportPipeActionPayload})、表示はサーバーから届いた状態で更新する。
 */
public class TeleportPipeScreen extends AbstractContainerScreen<TeleportPipeMenu> {
    private static final ResourceLocation TEXTURE = AdditionalPipes2.id("textures/gui/teleport_pipe.png");
    private static final int IMAGE_WIDTH = 244;
    private static final int IMAGE_HEIGHT = 116;
    private static final int LABEL_COLOR = 0x404040;
    private static final int FREQUENCY_DIGITS = 5;
    private static final int[] STEP_BUTTON_ROWS = {28, 52, 76};
    private static final int[] STEPS = {1, 10, 100};
    private static final String LANG = "gui." + AdditionalPipes2.MODID + ".teleport_pipe.";

    private EditBox frequencyBox;
    private EditBox nameBox;
    private Button addNameButton;
    private Button removeNameButton;
    private Button receiveButton;
    private FrequencyNameList nameList;
    /** 操作の基準にする周波数。サーバーの値が変わるたびに合わせる。 */
    private int selectedFrequency;
    private int shownFrequency = -1;

    public TeleportPipeScreen(TeleportPipeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = IMAGE_WIDTH;
        imageHeight = IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        frequencyBox = new EditBox(font, leftPos + 7, topPos + 7, 40, 16, Component.translatable(LANG + "frequency"));
        frequencyBox.setMaxLength(FREQUENCY_DIGITS);
        frequencyBox.setFilter(text -> text.chars().allMatch(Character::isDigit));
        addRenderableWidget(frequencyBox);
        addRenderableWidget(Button.builder(Component.translatable(LANG + "set"), button -> applyTypedFrequency())
                .bounds(leftPos + 50, topPos + 7, 22, 16).build());
        for (int i = 0; i < STEPS.length; i++) {
            int step = STEPS[i];
            int y = topPos + STEP_BUTTON_ROWS[i];
            addRenderableWidget(Button.builder(Component.literal("-" + step), button -> changeFrequency(-step))
                    .bounds(leftPos + 6, y, 30, 20).build());
            addRenderableWidget(Button.builder(Component.literal("+" + step), button -> changeFrequency(step))
                    .bounds(leftPos + 42, y, 30, 20).build());
        }

        nameBox = new EditBox(font, leftPos + 80, topPos + 29, 114, 16, Component.translatable(LANG + "name"));
        nameBox.setMaxLength(FrequencyNameBook.MAX_NAME_LENGTH);
        addRenderableWidget(nameBox);
        addNameButton = addRenderableWidget(Button.builder(Component.translatable(LANG + "add"), button -> addName())
                .bounds(leftPos + 196, topPos + 29, 42, 16).build());
        removeNameButton = addRenderableWidget(Button.builder(Component.translatable(LANG + "remove"), button -> removeName())
                .bounds(leftPos + 196, topPos + 29, 42, 16).build());
        receiveButton = addRenderableWidget(Button.builder(Component.empty(), button -> send(Action.TOGGLE_RECEIVE, 0))
                .bounds(leftPos + 204, topPos + 8, 34, 16).build());

        nameList = new FrequencyNameList(font, 156, 46, this::selectName);
        nameList.setPosition(leftPos + 80, topPos + 49);
        shownFrequency = -1;
        refreshFromServer();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshFromServer();
    }

    /** サーバーの周波数が変わったら入力欄と名前欄を合わせ、ボタンと一覧を更新する (旧 refresh / updateNames)。 */
    private void refreshFromServer() {
        TeleportPipeView view = menu.view();
        if (view.frequency() != shownFrequency) {
            shownFrequency = view.frequency();
            selectedFrequency = view.frequency();
            if (!frequencyBox.isFocused()) {
                frequencyBox.setValue(String.valueOf(view.frequency()));
            }
            nameBox.setValue(view.currentName());
        }
        receiveButton.setMessage(Component.translatable(LANG + (view.canReceive() ? "true" : "false")));
        updateNameControls(view);
    }

    private void updateNameControls(TeleportPipeView view) {
        String currentName = view.currentName();
        String typed = nameBox.getValue();
        boolean canAdd = currentName.isEmpty() || !currentName.equals(typed);
        boolean typedIsUsable = FrequencyNameBook.isValidName(typed) && !isNameUsed(view, typed);
        addNameButton.visible = canAdd;
        addNameButton.active = canAdd && typedIsUsable;
        removeNameButton.visible = !canAdd;
        removeNameButton.active = !canAdd;
        List<String> names = new ArrayList<>();
        for (FrequencyNameBook.Entry entry : view.names()) {
            if (canAdd || !entry.name().equals(typed)) {
                names.add(entry.name());
            }
        }
        nameList.update(names, canAdd ? typed : "");
    }

    private static boolean isNameUsed(TeleportPipeView view, String name) {
        return view.names().stream().anyMatch(entry -> entry.name().equals(name));
    }

    // ---- 操作 -------------------------------------------------------------------

    private void applyTypedFrequency() {
        String text = frequencyBox.getValue();
        if (!text.isEmpty()) {
            setFrequency(Integer.parseInt(text));
        }
    }

    private void changeFrequency(int delta) {
        setFrequency(selectedFrequency + delta);
    }

    private void setFrequency(int frequency) {
        selectedFrequency = TeleportSettings.clampFrequency(frequency);
        frequencyBox.setValue(String.valueOf(selectedFrequency));
        send(Action.SET_FREQUENCY, selectedFrequency);
    }

    private void addName() {
        PacketDistributor.sendToServer(TeleportPipeActionPayload.addName(menu.containerId, nameBox.getValue().strip()));
    }

    private void removeName() {
        send(Action.REMOVE_NAME, 0);
        nameBox.setValue("");
    }

    /** 一覧の名前を選ぶと、その名前の周波数へ切り替える (旧 selectSlotName)。 */
    private void selectName(String name) {
        for (FrequencyNameBook.Entry entry : menu.view().names()) {
            if (entry.name().equals(name)) {
                setFrequency(entry.frequency());
                return;
            }
        }
    }

    private void send(Action action, int value) {
        PacketDistributor.sendToServer(TeleportPipeActionPayload.of(menu.containerId, action, value));
    }

    // ---- 入力 -------------------------------------------------------------------

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            // 旧 selectNextField: 周波数欄と名前欄を行き来する。
            boolean toName = !nameBox.isFocused();
            setFocused(toName ? nameBox : frequencyBox);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (frequencyBox.isFocused()) {
                applyTypedFrequency();
                return true;
            }
            if (nameBox.isFocused() && addNameButton.visible && addNameButton.active) {
                addName();
                return true;
            }
        }
        if (frequencyBox.isFocused() || nameBox.isFocused()) {
            // 入力中はインベントリキーなどで画面を閉じない。
            EditBox focused = frequencyBox.isFocused() ? frequencyBox : nameBox;
            return focused.keyPressed(keyCode, scanCode, modifiers) || focused.canConsumeInput();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (nameList.mouseClicked(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** 一覧の上ではスクロール、周波数欄の上 (または入力中) では周波数を 1 ずつ変える (旧 handleMouseInput)。 */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (nameList.contains(mouseX, mouseY)) {
            nameList.scroll(scrollY);
            return true;
        }
        if (scrollY != 0.0 && (frequencyBox.isFocused() || frequencyBox.isMouseOver(mouseX, mouseY))) {
            changeFrequency(-(int) Math.signum(scrollY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    // ---- 描画 -------------------------------------------------------------------

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        nameList.render(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        TeleportPipeView view = menu.view();
        graphics.drawString(font, Component.translatable(LANG + "connected", view.connectedCount()), 80, 12, LABEL_COLOR, false);
        graphics.drawString(font, Component.translatable(LANG + "receive"), 160, 12, LABEL_COLOR, false);
        graphics.drawString(font, Component.translatable(LANG + "owner", view.ownerName()), 6, 102, LABEL_COLOR, false);
    }
}
