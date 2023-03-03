package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.FrameWidget;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.LinearLayoutWidget;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.components.SpacerWidget;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.telemetry.TelemetryInfoScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.Difficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsScreen extends Screen {
   private static final Component SKIN_CUSTOMIZATION = Component.translatable("options.skinCustomisation");
   private static final Component SOUNDS = Component.translatable("options.sounds");
   private static final Component VIDEO = Component.translatable("options.video");
   private static final Component CONTROLS = Component.translatable("options.controls");
   private static final Component LANGUAGE = Component.translatable("options.language");
   private static final Component CHAT = Component.translatable("options.chat.title");
   private static final Component RESOURCEPACK = Component.translatable("options.resourcepack");
   private static final Component ACCESSIBILITY = Component.translatable("options.accessibility.title");
   private static final Component TELEMETRY = Component.translatable("options.telemetry");
   private static final int COLUMNS = 2;
   private final Screen lastScreen;
   private final Options options;
   private CycleButton<Difficulty> difficultyButton;
   private LockIconButton lockButton;

   public OptionsScreen(Screen p_96242_, Options p_96243_) {
      super(Component.translatable("options.title"));
      this.lastScreen = p_96242_;
      this.options = p_96243_;
   }

   protected void init() {
      GridWidget gridwidget = new GridWidget();
      gridwidget.defaultCellSetting().paddingHorizontal(5).paddingBottom(4).alignHorizontallyCenter();
      GridWidget.RowHelper gridwidget$rowhelper = gridwidget.createRowHelper(2);
      gridwidget$rowhelper.addChild(this.options.fov().createButton(this.minecraft.options, 0, 0, 150));
      gridwidget$rowhelper.addChild(this.createOnlineButton());
      gridwidget$rowhelper.addChild(SpacerWidget.height(26), 2);
      gridwidget$rowhelper.addChild(this.openScreenButton(SKIN_CUSTOMIZATION, () -> {
         return new SkinCustomizationScreen(this, this.options);
      }));
      gridwidget$rowhelper.addChild(this.openScreenButton(SOUNDS, () -> {
         return new SoundOptionsScreen(this, this.options);
      }));
      gridwidget$rowhelper.addChild(this.openScreenButton(VIDEO, () -> {
         return new VideoSettingsScreen(this, this.options);
      }));
      gridwidget$rowhelper.addChild(this.openScreenButton(CONTROLS, () -> {
         return new ControlsScreen(this, this.options);
      }));
      gridwidget$rowhelper.addChild(this.openScreenButton(LANGUAGE, () -> {
         return new LanguageSelectScreen(this, this.options, this.minecraft.getLanguageManager());
      }));
      gridwidget$rowhelper.addChild(this.openScreenButton(CHAT, () -> {
         return new ChatOptionsScreen(this, this.options);
      }));
      gridwidget$rowhelper.addChild(this.openScreenButton(RESOURCEPACK, () -> {
         return new PackSelectionScreen(this, this.minecraft.getResourcePackRepository(), this::updatePackList, this.minecraft.getResourcePackDirectory(), Component.translatable("resourcePack.title"));
      }));
      gridwidget$rowhelper.addChild(this.openScreenButton(ACCESSIBILITY, () -> {
         return new AccessibilityOptionsScreen(this, this.options);
      }));
      gridwidget$rowhelper.addChild(this.openScreenButton(TELEMETRY, () -> {
         return new TelemetryInfoScreen(this, this.options);
      }));
      gridwidget$rowhelper.addChild(Button.builder(CommonComponents.GUI_DONE, (p_96257_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).width(200).build(), 2, gridwidget$rowhelper.newCellSettings().paddingTop(6));
      gridwidget.pack();
      FrameWidget.alignInRectangle(gridwidget, 0, this.height / 6 - 12, this.width, this.height, 0.5F, 0.0F);
      this.addRenderableWidget(gridwidget);
   }

   private AbstractWidget createOnlineButton() {
      if (this.minecraft.level != null && this.minecraft.hasSingleplayerServer()) {
         this.difficultyButton = createDifficultyButton(0, 0, "options.difficulty", this.minecraft);
         if (!this.minecraft.level.getLevelData().isHardcore()) {
            this.lockButton = new LockIconButton(0, 0, (p_193857_) -> {
               this.minecraft.setScreen(new ConfirmScreen(this::lockCallback, Component.translatable("difficulty.lock.title"), Component.translatable("difficulty.lock.question", this.minecraft.level.getLevelData().getDifficulty().getDisplayName())));
            });
            this.difficultyButton.setWidth(this.difficultyButton.getWidth() - this.lockButton.getWidth());
            this.lockButton.setLocked(this.minecraft.level.getLevelData().isDifficultyLocked());
            this.lockButton.active = !this.lockButton.isLocked();
            this.difficultyButton.active = !this.lockButton.isLocked();
            LinearLayoutWidget linearlayoutwidget = new LinearLayoutWidget(150, 0, LinearLayoutWidget.Orientation.HORIZONTAL);
            linearlayoutwidget.addChild(this.difficultyButton);
            linearlayoutwidget.addChild(this.lockButton);
            linearlayoutwidget.pack();
            return linearlayoutwidget;
         } else {
            this.difficultyButton.active = false;
            return this.difficultyButton;
         }
      } else {
         return Button.builder(Component.translatable("options.online"), (p_261362_) -> {
            this.minecraft.setScreen(OnlineOptionsScreen.createOnlineOptionsScreen(this.minecraft, this, this.options));
         }).bounds(this.width / 2 + 5, this.height / 6 - 12 + 24, 150, 20).build();
      }
   }

   public static CycleButton<Difficulty> createDifficultyButton(int p_262051_, int p_261805_, String p_261598_, Minecraft p_261922_) {
      return CycleButton.builder(Difficulty::getDisplayName).withValues(Difficulty.values()).withInitialValue(p_261922_.level.getDifficulty()).create(p_262051_, p_261805_, 150, 20, Component.translatable(p_261598_), (p_193854_, p_193855_) -> {
         p_261922_.getConnection().send(new ServerboundChangeDifficultyPacket(p_193855_));
      });
   }

   private void updatePackList(PackRepository p_96245_) {
      List<String> list = ImmutableList.copyOf(this.options.resourcePacks);
      this.options.resourcePacks.clear();
      this.options.incompatibleResourcePacks.clear();

      for(Pack pack : p_96245_.getSelectedPacks()) {
         if (!pack.isFixedPosition()) {
            this.options.resourcePacks.add(pack.getId());
            if (!pack.getCompatibility().isCompatible()) {
               this.options.incompatibleResourcePacks.add(pack.getId());
            }
         }
      }

      this.options.save();
      List<String> list1 = ImmutableList.copyOf(this.options.resourcePacks);
      if (!list1.equals(list)) {
         this.minecraft.reloadResourcePacks();
      }

   }

   private void lockCallback(boolean p_96261_) {
      this.minecraft.setScreen(this);
      if (p_96261_ && this.minecraft.level != null) {
         this.minecraft.getConnection().send(new ServerboundLockDifficultyPacket(true));
         this.lockButton.setLocked(true);
         this.lockButton.active = false;
         this.difficultyButton.active = false;
      }

   }

   public void removed() {
      this.options.save();
   }

   public void render(PoseStack p_96249_, int p_96250_, int p_96251_, float p_96252_) {
      this.renderBackground(p_96249_);
      drawCenteredString(p_96249_, this.font, this.title, this.width / 2, 15, 16777215);
      super.render(p_96249_, p_96250_, p_96251_, p_96252_);
   }

    @Override
    public void onClose() {
        // We need to consider 2 potential parent screens here:
        // 1. From the main menu, in which case display the main menu
        // 2. From the pause menu, in which case exit back to game
        this.minecraft.setScreen(this.lastScreen instanceof PauseScreen ? null : this.lastScreen);
    }

   private Button openScreenButton(Component p_261565_, Supplier<Screen> p_262119_) {
      return Button.builder(p_261565_, (p_261361_) -> {
         this.minecraft.setScreen(p_262119_.get());
      }).build();
   }
}
