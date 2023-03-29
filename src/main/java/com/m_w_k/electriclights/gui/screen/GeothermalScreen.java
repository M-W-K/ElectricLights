package com.m_w_k.electriclights.gui.screen;

import com.m_w_k.electriclights.gui.menu.GeothermalMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class GeothermalScreen extends AbstractGeneratorScreen<GeothermalMenu> {

    public GeothermalScreen(GeothermalMenu container, Inventory inventory, Component name) {
        super(container, inventory, name, "textures/gui/menu/geothermal_generator.png");
    }

    @Override
    protected void renderBg(@Nonnull PoseStack poseStack, float partialTicks, int x, int y) {
        super.renderBg(poseStack, partialTicks, x, y);
        int guiLeft = this.leftPos;
        int guiTop = this.topPos;
        this.blit(poseStack, guiLeft, guiTop, 0, 0, this.imageWidth, this.imageHeight);

        int extensions = (int) (126 - 126 / Math.log1p(this.menu.getExtensionsCount() + Math.E));
        this.blit(poseStack, guiLeft + 12, guiTop + 32, 1, 167, extensions, 47);

        int lava = this.menu.getLavaFactor() / 9;
        this.blit(poseStack, guiLeft + 11, guiTop + 62 + 16 - lava, 1, 219 + 16 - lava, 128, lava);

        this.blit(poseStack, guiLeft + 104, guiTop + 10, 177, 1, 55, 12);
    }
}
