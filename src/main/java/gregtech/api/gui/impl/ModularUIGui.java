package gregtech.api.gui.impl;

import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.net.PacketUIWidgetUpdate;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

public class ModularUIGui extends GuiContainer {

    public static Queue<PacketUIWidgetUpdate> queuingWidgetUpdates = new ArrayDeque<>();

    private final ModularUI modularUI;

    public ModularUI getModularUI() {
        return modularUI;
    }

    public ModularUIGui(ModularUI modularUI) {
        super(new ModularUIContainer(modularUI));
        this.modularUI = modularUI;
    }

    @Override
    public void initGui() {
        xSize = modularUI.width;
        ySize = modularUI.height;
        super.initGui();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        processWidgetPackets();
        modularUI.guiWidgets.values().forEach(Widget::updateScreen);
    }

    private void processWidgetPackets() {
        PacketUIWidgetUpdate packet = queuingWidgetUpdates.poll();
        if(packet != null && packet.windowId == inventorySlots.windowId) {
            Widget widget = modularUI.guiWidgets.get(packet.widgetId);
            int discriminator = packet.updateData.readInt();
            if(widget != null) widget.readUpdateInfo(discriminator, packet.updateData);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    //for foreground gl state is already translated
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        modularUI.guiWidgets.values().stream().sorted()
            .forEach(widget -> {
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                widget.drawInForeground(mouseX - guiLeft, mouseY - guiTop);
                GlStateManager.popMatrix();
            });
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0.0);
        modularUI.backgroundPath.draw(0, 0, xSize, ySize);
        modularUI.guiWidgets.values().stream().sorted()
                .forEach(widget -> {
                    GlStateManager.pushMatrix();
                    GlStateManager.color(1.0f, 1.0f, 1.0f);
                    widget.drawInBackground(mouseX - guiLeft, mouseY - guiTop);
                    GlStateManager.popMatrix();
                });
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        modularUI.guiWidgets.values().forEach(widget -> widget.mouseClicked(mouseX - guiLeft, mouseY - guiTop, mouseButton));
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        modularUI.guiWidgets.values().forEach(widget -> widget.mouseDragged(mouseX - guiLeft, mouseY - guiTop, clickedMouseButton, timeSinceLastClick));
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        modularUI.guiWidgets.values().forEach(widget -> widget.mouseReleased(mouseX - guiLeft, mouseY - guiTop, state));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        modularUI.guiWidgets.values().forEach(widget -> widget.keyTyped(typedChar, keyCode));
    }

}
