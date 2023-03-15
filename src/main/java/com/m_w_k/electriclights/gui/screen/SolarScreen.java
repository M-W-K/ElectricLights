package com.m_w_k.electriclights.gui.screen;

import com.m_w_k.electriclights.ElectricLightsMod;
import com.m_w_k.electriclights.gui.menu.SolarMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class SolarScreen extends AbstractContainerScreen<SolarMenu> {
    private static final ResourceLocation SOLAR_GUI_TEXTURES = new ResourceLocation(ElectricLightsMod.MODID, "textures/gui/menu/solar_generator.png");

    public SolarScreen(SolarMenu container, Inventory inventory, Component name) {
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
        RenderSystem.setShaderTexture(0, SOLAR_GUI_TEXTURES);
        int guiLeft = this.leftPos;
        int guiTop = this.topPos;
        this.blit(poseStack, guiLeft, guiTop, 0, 0, this.imageWidth, this.imageHeight);

        int energyLevel = 74 - (int) (74 * this.menu.getEnergyScaled());
        this.blit(poseStack, guiLeft + 150, guiTop + 6 + energyLevel, 176, 30 + energyLevel, 20, 74 - energyLevel);

        int solarFactor = this.menu.getSolarFactor();
        int sol = solarFactor < 10 ? (solarFactor - 1) / 2 + 1 : (solarFactor - 9) * 25 / 90 + 5;
        this.blit(poseStack, guiLeft + 19, guiTop + 14, 0, 166, 122, sol);

        int extensions = (int) (125 - 125 / Math.log1p(this.menu.getExtensionsCount() + Math.E));
        this.blit(poseStack, guiLeft + 19, guiTop + 44, 0, 197, extensions, 29);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return mouseX < (double) guiLeft || mouseY < (double) guiTop || mouseX >= (double) (guiLeft + this.imageWidth) || mouseY >= (double) (guiTop + this.imageHeight);
    }
}
