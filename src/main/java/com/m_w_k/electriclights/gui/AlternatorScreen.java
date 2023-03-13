package com.m_w_k.electriclights.gui;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Quaternionf;
import org.joml.Vector2f;

import javax.annotation.Nonnull;

public class AlternatorScreen extends AbstractContainerScreen<AlternatorMenu> {
    private static final ResourceLocation ALTERNATOR_GUI_TEXTURES = new ResourceLocation(ElectricLightsMod.MODID, "textures/gui/menu/alternator_generator.png");
    private double rotation;

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
            this.blit(poseStack, guiLeft + 67, guiTop + 78 - progressionScaled, 177, 10 - progressionScaled, 15, 1 + progressionScaled);
        }
        this.blit(poseStack, guiLeft + 13, guiTop + 32, 176, 75, 48, 16);

        poseStack.pushPose();
        int a = (guiLeft + 99 + 31);
        int b = (guiTop + 11 + 31);
        poseStack.translate(a, b, 0);
        poseStack.mulPose(new Quaternionf().rotateZ((float) rotation));
        this.blit(poseStack, -31, -31, 176, 13, 62, 62);
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateZ((float) -rotation));
        poseStack.translate(-a, -b, 0);

        rotation = Math.toRadians((Math.toDegrees(rotation) + Math.sqrt(this.menu.getEnergyStored()) / 500f) % 360);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return mouseX < (double) guiLeft || mouseY < (double) guiTop || mouseX >= (double) (guiLeft + this.imageWidth) || mouseY >= (double) (guiTop + this.imageHeight);
    }
}
