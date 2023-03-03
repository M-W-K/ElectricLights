package net.minecraft.client.gui.screens.telemetry;

import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Path;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CenteredStringWidget;
import net.minecraft.client.gui.components.FrameWidget;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TelemetryInfoScreen extends Screen {
   private static final int PADDING = 8;
   private static final String FEEDBACK_URL = "https://aka.ms/javafeedback?ref=game";
   private static final Component TITLE = Component.translatable("telemetry_info.screen.title");
   private static final Component DESCRIPTION = Component.translatable("telemetry_info.screen.description").withStyle(ChatFormatting.GRAY);
   private static final Component BUTTON_GIVE_FEEDBACK = Component.translatable("telemetry_info.button.give_feedback");
   private static final Component BUTTON_SHOW_DATA = Component.translatable("telemetry_info.button.show_data");
   private final Screen lastScreen;
   private final Options options;
   private TelemetryEventWidget telemetryEventWidget;
   private double savedScroll;

   public TelemetryInfoScreen(Screen p_261720_, Options p_262019_) {
      super(TITLE);
      this.lastScreen = p_261720_;
      this.options = p_262019_;
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(super.getNarrationMessage(), DESCRIPTION);
   }

   protected void init() {
      FrameWidget framewidget = new FrameWidget(0, 0, this.width, this.height);
      framewidget.defaultChildLayoutSetting().padding(8);
      framewidget.setMinHeight(this.height);
      GridWidget gridwidget = framewidget.addChild(new GridWidget(), framewidget.newChildLayoutSettings().align(0.5F, 0.0F));
      gridwidget.defaultCellSetting().alignHorizontallyCenter().paddingBottom(8);
      GridWidget.RowHelper gridwidget$rowhelper = gridwidget.createRowHelper(1);
      gridwidget$rowhelper.addChild(new CenteredStringWidget(this.getTitle(), this.font));
      gridwidget$rowhelper.addChild(MultiLineTextWidget.createCentered(this.width - 16, this.font, DESCRIPTION));
      GridWidget gridwidget1 = this.twoButtonContainer(Button.builder(BUTTON_GIVE_FEEDBACK, this::openFeedbackLink).build(), Button.builder(BUTTON_SHOW_DATA, this::openDataFolder).build());
      gridwidget$rowhelper.addChild(gridwidget1);
      GridWidget gridwidget2 = this.twoButtonContainer(this.createTelemetryButton(), Button.builder(CommonComponents.GUI_DONE, this::openLastScreen).build());
      framewidget.addChild(gridwidget2, framewidget.newChildLayoutSettings().align(0.5F, 1.0F));
      gridwidget.pack();
      framewidget.pack();
      this.telemetryEventWidget = new TelemetryEventWidget(0, 0, this.width - 40, gridwidget2.getY() - (gridwidget1.getY() + gridwidget1.getHeight()) - 16, this.minecraft.font);
      this.telemetryEventWidget.setScrollAmount(this.savedScroll);
      this.telemetryEventWidget.setOnScrolledListener((p_262168_) -> {
         this.savedScroll = p_262168_;
      });
      this.setInitialFocus(this.telemetryEventWidget);
      gridwidget$rowhelper.addChild(this.telemetryEventWidget);
      gridwidget.pack();
      framewidget.pack();
      FrameWidget.alignInRectangle(framewidget, 0, 0, this.width, this.height, 0.5F, 0.0F);
      this.addRenderableWidget(framewidget);
   }

   private AbstractWidget createTelemetryButton() {
      AbstractWidget abstractwidget = this.options.telemetryOptInExtra().createButton(this.options, 0, 0, 150, (p_261857_) -> {
         this.telemetryEventWidget.onOptInChanged(p_261857_);
      });
      abstractwidget.active = this.minecraft.extraTelemetryAvailable();
      return abstractwidget;
   }

   private void openLastScreen(Button p_261672_) {
      this.minecraft.setScreen(this.lastScreen);
   }

   private void openFeedbackLink(Button p_261531_) {
      this.minecraft.setScreen(new ConfirmLinkScreen((p_261567_) -> {
         if (p_261567_) {
            Util.getPlatform().openUri("https://aka.ms/javafeedback?ref=game");
         }

         this.minecraft.setScreen(this);
      }, "https://aka.ms/javafeedback?ref=game", true));
   }

   private void openDataFolder(Button p_261840_) {
      Path path = this.minecraft.getTelemetryManager().getLogDirectory();
      Util.getPlatform().openUri(path.toUri());
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   public void render(PoseStack p_262137_, int p_262014_, int p_261607_, float p_261637_) {
      this.renderDirtBackground(0);
      super.render(p_262137_, p_262014_, p_261607_, p_261637_);
   }

   private GridWidget twoButtonContainer(AbstractWidget p_262100_, AbstractWidget p_261856_) {
      GridWidget gridwidget = new GridWidget();
      gridwidget.defaultCellSetting().alignHorizontallyCenter().paddingHorizontal(4);
      gridwidget.addChild(p_262100_, 0, 0);
      gridwidget.addChild(p_261856_, 0, 1);
      gridwidget.pack();
      return gridwidget;
   }
}