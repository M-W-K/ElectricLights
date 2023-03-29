package com.m_w_k.electriclights.gui.screen;

import com.m_w_k.electriclights.gui.menu.AlternatorMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public class AlternatorScreen extends AbstractGeneratorScreen<AlternatorMenu> {
    private double rotation;

    public AlternatorScreen(AlternatorMenu container, Inventory inventory, Component name) {
        super(container, inventory, name, "textures/gui/menu/alternator_generator.png");
    }

    @Override
    protected void renderBg(@Nonnull PoseStack poseStack, float partialTicks, int x, int y) {
        super.renderBg(poseStack, partialTicks, x, y);
        int guiLeft = this.leftPos;
        int guiTop = this.topPos;
        this.blit(poseStack, guiLeft, guiTop, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isLit()) {
            int progressionScaled = this.menu.getLitTimeRemainingScaled();
            this.blit(poseStack, guiLeft + 67, guiTop + 78 - progressionScaled, 177, 10 - progressionScaled, 15, 2 + progressionScaled);
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
}
