package com.m_w_k.electriclights.gui;

import com.m_w_k.electriclights.gui.AlternatorMenu;
import com.m_w_k.electriclights.ElectricLightsMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import javax.annotation.Nonnull;

public class AlternatorScreen extends AbstractContainerScreen<AlternatorMenu> {
    private static final ResourceLocation ALTERNATOR_GUI_TEXTURES = new ResourceLocation(ElectricLightsMod.MODID, "textures/gui/menu/alternator_generator.png");

    public AlternatorScreen(AlternatorMenu container, Inventory inventory, Component name) {
        super(container, inventory, name);
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@Nonnull PoseStack poseStack, float partialTicks, int x, int y) {
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ALTERNATOR_GUI_TEXTURES);
        int guiLeft = this.leftPos;
        int guiTop = this.topPos;
        this.blit(poseStack, guiLeft, guiTop, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isLit()) {
            int progressionScaled = this.menu.getLitTimeRemainingScaled();
            this.blit(poseStack, guiLeft + 103, guiTop + 70 - progressionScaled, 179, 70 - progressionScaled, 10, progressionScaled);

        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return mouseX < (double) guiLeft || mouseY < (double) guiTop || mouseX >= (double) (guiLeft + this.imageWidth) || mouseY >= (double) (guiTop + this.imageHeight);
    }
}
