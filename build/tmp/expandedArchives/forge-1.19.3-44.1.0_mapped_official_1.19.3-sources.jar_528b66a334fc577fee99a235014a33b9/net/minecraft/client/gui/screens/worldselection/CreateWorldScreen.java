package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class CreateWorldScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String TEMP_WORLD_PREFIX = "mcworld-";
   private static final Component GAME_MODEL_LABEL = Component.translatable("selectWorld.gameMode");
   private static final Component SEED_LABEL = Component.translatable("selectWorld.enterSeed");
   private static final Component SEED_INFO = Component.translatable("selectWorld.seedInfo");
   private static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
   private static final Component OUTPUT_DIR_INFO = Component.translatable("selectWorld.resultFolder");
   private static final Component COMMANDS_INFO = Component.translatable("selectWorld.allowCommands.info");
   private static final Component PREPARING_WORLD_DATA = Component.translatable("createWorld.preparing");
   @Nullable
   private final Screen lastScreen;
   private EditBox nameEdit;
   String resultFolder;
   private CreateWorldScreen.SelectedGameMode gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
   @Nullable
   private CreateWorldScreen.SelectedGameMode oldGameMode;
   private Difficulty difficulty = Difficulty.NORMAL;
   private boolean commands;
   private boolean commandsChanged;
   public boolean hardCore;
   protected WorldDataConfiguration dataConfiguration;
   @Nullable
   private Path tempDataPackDir;
   @Nullable
   private PackRepository tempDataPackRepository;
   private boolean worldGenSettingsVisible;
   private Button createButton;
   private CycleButton<CreateWorldScreen.SelectedGameMode> modeButton;
   private CycleButton<Difficulty> difficultyButton;
   private Button moreOptionsButton;
   private Button gameRulesButton;
   private Button dataPacksButton;
   private CycleButton<Boolean> commandsButton;
   private Component gameModeHelp1;
   private Component gameModeHelp2;
   private String initName;
   private GameRules gameRules = new GameRules();
   public final WorldGenSettingsComponent worldGenSettingsComponent;

   public static void openFresh(Minecraft p_232897_, @Nullable Screen p_232898_) {
      queueLoadScreen(p_232897_, PREPARING_WORLD_DATA);
      PackRepository packrepository = new PackRepository(new ServerPacksSource());
      net.minecraftforge.fml.ModLoader.get().postEvent(new net.minecraftforge.event.AddPackFindersEvent(net.minecraft.server.packs.PackType.SERVER_DATA, packrepository::addPackFinder));
      WorldLoader.InitConfig worldloader$initconfig = createDefaultLoadConfig(packrepository, new WorldDataConfiguration(new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of()), FeatureFlags.DEFAULT_FLAGS)); // FORGE: Load vanilla fallback with vanilla datapacks.
      CompletableFuture<WorldCreationContext> completablefuture = WorldLoader.load(worldloader$initconfig, (p_247792_) -> {
         return new WorldLoader.DataLoadOutput<>(new CreateWorldScreen.DataPackReloadCookie(new WorldGenSettings(WorldOptions.defaultWithRandomSeed(), WorldPresets.createNormalWorldDimensions(p_247792_.datapackWorldgen())), p_247792_.dataConfiguration()), p_247792_.datapackDimensions());
      }, (p_247798_, p_247799_, p_247800_, p_247801_) -> {
         p_247798_.close();
         return new WorldCreationContext(p_247801_.worldGenSettings(), p_247800_, p_247799_, p_247801_.dataConfiguration());
      }, Util.backgroundExecutor(), p_232897_);
      p_232897_.managedBlock(completablefuture::isDone);
      // FORGE: Force load mods' datapacks after setting screen and ensure datapack selection reverts to vanilla if invalid.
      CreateWorldScreen createWorldScreen = new CreateWorldScreen(p_232898_, new WorldDataConfiguration(new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of()), FeatureFlags.DEFAULT_FLAGS), new WorldGenSettingsComponent(completablefuture.join(), Optional.of(WorldPresets.NORMAL), OptionalLong.empty()));
      p_232897_.setScreen(createWorldScreen);
      createWorldScreen.tryApplyNewDataPacks(packrepository);
   }

   public static CreateWorldScreen createFromExisting(@Nullable Screen p_249522_, LevelSettings p_249091_, WorldCreationContext p_249742_, @Nullable Path p_252076_) {
      CreateWorldScreen createworldscreen = new CreateWorldScreen(p_249522_, p_249742_.dataConfiguration(), new WorldGenSettingsComponent(p_249742_, WorldPresets.fromSettings(p_249742_.selectedDimensions().dimensions()), OptionalLong.of(p_249742_.options().seed())));
      createworldscreen.initName = p_249091_.levelName();
      createworldscreen.commands = p_249091_.allowCommands();
      createworldscreen.commandsChanged = true;
      createworldscreen.difficulty = p_249091_.difficulty();
      createworldscreen.gameRules.assignFrom(p_249091_.gameRules(), (MinecraftServer)null);
      if (p_249091_.hardcore()) {
         createworldscreen.gameMode = CreateWorldScreen.SelectedGameMode.HARDCORE;
      } else if (p_249091_.gameType().isSurvival()) {
         createworldscreen.gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
      } else if (p_249091_.gameType().isCreative()) {
         createworldscreen.gameMode = CreateWorldScreen.SelectedGameMode.CREATIVE;
      }

      createworldscreen.tempDataPackDir = p_252076_;
      return createworldscreen;
   }

   private CreateWorldScreen(@Nullable Screen p_249549_, WorldDataConfiguration p_250099_, WorldGenSettingsComponent p_249670_) {
      super(Component.translatable("selectWorld.create"));
      this.lastScreen = p_249549_;
      this.initName = I18n.get("selectWorld.newWorld");
      this.dataConfiguration = p_250099_;
      this.worldGenSettingsComponent = p_249670_;
   }

   public void tick() {
      this.nameEdit.tick();
      this.worldGenSettingsComponent.tick();
   }

   protected void init() {
      this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, Component.translatable("selectWorld.enterName")) {
         protected MutableComponent createNarrationMessage() {
            return CommonComponents.joinForNarration(super.createNarrationMessage(), Component.translatable("selectWorld.resultFolder")).append(" ").append(CreateWorldScreen.this.resultFolder);
         }
      };
      this.nameEdit.setValue(this.initName);
      this.nameEdit.setResponder((p_232916_) -> {
         this.initName = p_232916_;
         this.createButton.active = !this.nameEdit.getValue().isEmpty();
         this.updateResultFolder();
      });
      this.addWidget(this.nameEdit);
      int i = this.width / 2 - 155;
      int j = this.width / 2 + 5;
      this.modeButton = this.addRenderableWidget(CycleButton.builder(CreateWorldScreen.SelectedGameMode::getDisplayName).withValues(CreateWorldScreen.SelectedGameMode.SURVIVAL, CreateWorldScreen.SelectedGameMode.HARDCORE, CreateWorldScreen.SelectedGameMode.CREATIVE).withInitialValue(this.gameMode).withCustomNarration((p_232940_) -> {
         return AbstractWidget.wrapDefaultNarrationMessage(p_232940_.getMessage()).append(CommonComponents.NARRATION_SEPARATOR).append(this.gameModeHelp1).append(" ").append(this.gameModeHelp2);
      }).create(i, 100, 150, 20, GAME_MODEL_LABEL, (p_232910_, p_232911_) -> {
         this.setGameMode(p_232911_);
      }));
      this.difficultyButton = this.addRenderableWidget(CycleButton.builder(Difficulty::getDisplayName).withValues(Difficulty.values()).withInitialValue(this.getEffectiveDifficulty()).create(j, 100, 150, 20, Component.translatable("options.difficulty"), (p_232907_, p_232908_) -> {
         this.difficulty = p_232908_;
      }));
      this.commandsButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.commands && !this.hardCore).withCustomNarration((p_232905_) -> {
         return CommonComponents.joinForNarration(p_232905_.createDefaultNarrationMessage(), Component.translatable("selectWorld.allowCommands.info"));
      }).create(i, 151, 150, 20, Component.translatable("selectWorld.allowCommands"), (p_232913_, p_232914_) -> {
         this.commandsChanged = true;
         this.commands = p_232914_;
      }));
      this.dataPacksButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.dataPacks"), (p_232947_) -> {
         this.openDataPackSelectionScreen();
      }).bounds(j, 151, 150, 20).build());
      this.gameRulesButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.gameRules"), (p_170188_) -> {
         this.minecraft.setScreen(new EditGameRulesScreen(this.gameRules.copy(), (p_232929_) -> {
            this.minecraft.setScreen(this);
            p_232929_.ifPresent((p_232892_) -> {
               this.gameRules = p_232892_;
            });
         }));
      }).bounds(i, 185, 150, 20).build());
      this.worldGenSettingsComponent.init(this, this.minecraft, this.font);
      this.moreOptionsButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.moreWorldOptions"), (p_170158_) -> {
         this.toggleWorldGenSettingsVisibility();
      }).bounds(j, 185, 150, 20).build());
      this.createButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.create"), (p_232938_) -> {
         this.onCreate();
      }).bounds(i, this.height - 28, 150, 20).build());
      this.createButton.active = !this.initName.isEmpty();
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_232903_) -> {
         this.popScreen();
      }).bounds(j, this.height - 28, 150, 20).build());
      this.refreshWorldGenSettingsVisibility();
      this.setInitialFocus(this.nameEdit);
      this.setGameMode(this.gameMode);
      this.updateResultFolder();
   }

   private Difficulty getEffectiveDifficulty() {
      return this.gameMode == CreateWorldScreen.SelectedGameMode.HARDCORE ? Difficulty.HARD : this.difficulty;
   }

   private void updateGameModeHelp() {
      this.gameModeHelp1 = Component.translatable("selectWorld.gameMode." + this.gameMode.name + ".line1");
      this.gameModeHelp2 = Component.translatable("selectWorld.gameMode." + this.gameMode.name + ".line2");
   }

   private void updateResultFolder() {
      this.resultFolder = this.nameEdit.getValue().trim();
      if (this.resultFolder.isEmpty()) {
         this.resultFolder = "World";
      }

      try {
         this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
      } catch (Exception exception1) {
         this.resultFolder = "World";

         try {
            this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
         } catch (Exception exception) {
            throw new RuntimeException("Could not create save folder", exception);
         }
      }

   }

   private static void queueLoadScreen(Minecraft p_232900_, Component p_232901_) {
      p_232900_.forceSetScreen(new GenericDirtMessageScreen(p_232901_));
   }

   private void onCreate() {
      WorldCreationContext worldcreationcontext = this.worldGenSettingsComponent.settings();
      WorldDimensions.Complete worlddimensions$complete = worldcreationcontext.selectedDimensions().bake(worldcreationcontext.datapackDimensions());
      LayeredRegistryAccess<RegistryLayer> layeredregistryaccess = worldcreationcontext.worldgenRegistries().replaceFrom(RegistryLayer.DIMENSIONS, worlddimensions$complete.dimensionsRegistryAccess());
      Lifecycle lifecycle = FeatureFlags.isExperimental(worldcreationcontext.dataConfiguration().enabledFeatures()) ? Lifecycle.experimental() : Lifecycle.stable();
      Lifecycle lifecycle1 = layeredregistryaccess.compositeAccess().allRegistriesLifecycle();
      Lifecycle lifecycle2 = lifecycle1.add(lifecycle);
      WorldOpenFlows.confirmWorldCreation(this.minecraft, this, lifecycle2, () -> {
         this.createNewWorld(worlddimensions$complete.specialWorldProperty(), layeredregistryaccess, lifecycle2);
      });
   }

   private void createNewWorld(PrimaryLevelData.SpecialWorldProperty p_250577_, LayeredRegistryAccess<RegistryLayer> p_249152_, Lifecycle p_249994_) {
      queueLoadScreen(this.minecraft, PREPARING_WORLD_DATA);
      Optional<LevelStorageSource.LevelStorageAccess> optional = this.createNewWorldDirectory();
      if (!optional.isEmpty()) {
         this.removeTempDataPackDir();
         boolean flag = p_250577_ == PrimaryLevelData.SpecialWorldProperty.DEBUG;
         WorldCreationContext worldcreationcontext = this.worldGenSettingsComponent.settings();
         WorldOptions worldoptions = this.worldGenSettingsComponent.createFinalOptions(flag, this.hardCore);
         LevelSettings levelsettings = this.createLevelSettings(flag);
         WorldData worlddata = new PrimaryLevelData(levelsettings, worldoptions, p_250577_, p_249994_);
         this.minecraft.createWorldOpenFlows().createLevelFromExistingSettings(optional.get(), worldcreationcontext.dataPackResources(), p_249152_, worlddata);
      }
   }

   private LevelSettings createLevelSettings(boolean p_205448_) {
      String s = this.nameEdit.getValue().trim();
      if (p_205448_) {
         GameRules gamerules = new GameRules();
         gamerules.getRule(GameRules.RULE_DAYLIGHT).set(false, (MinecraftServer)null);
         return new LevelSettings(s, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, gamerules, WorldDataConfiguration.DEFAULT);
      } else {
         return new LevelSettings(s, this.gameMode.gameType, this.hardCore, this.getEffectiveDifficulty(), this.commands && !this.hardCore, this.gameRules, this.dataConfiguration);
      }
   }

   private void toggleWorldGenSettingsVisibility() {
      this.setWorldGenSettingsVisible(!this.worldGenSettingsVisible);
   }

   private void setGameMode(CreateWorldScreen.SelectedGameMode p_100901_) {
      if (!this.commandsChanged) {
         this.commands = p_100901_ == CreateWorldScreen.SelectedGameMode.CREATIVE;
         this.commandsButton.setValue(this.commands);
      }

      if (p_100901_ == CreateWorldScreen.SelectedGameMode.HARDCORE) {
         this.hardCore = true;
         this.commandsButton.active = false;
         this.commandsButton.setValue(false);
         this.worldGenSettingsComponent.switchToHardcore();
         this.difficultyButton.setValue(Difficulty.HARD);
         this.difficultyButton.active = false;
      } else {
         this.hardCore = false;
         this.commandsButton.active = true;
         this.commandsButton.setValue(this.commands);
         this.worldGenSettingsComponent.switchOutOfHardcode();
         this.difficultyButton.setValue(this.difficulty);
         this.difficultyButton.active = true;
      }

      this.gameMode = p_100901_;
      this.updateGameModeHelp();
   }

   public void refreshWorldGenSettingsVisibility() {
      this.setWorldGenSettingsVisible(this.worldGenSettingsVisible);
   }

   private void setWorldGenSettingsVisible(boolean p_170197_) {
      this.worldGenSettingsVisible = p_170197_;
      this.modeButton.visible = !p_170197_;
      this.difficultyButton.visible = !p_170197_;
      if (this.worldGenSettingsComponent.isDebug()) {
         this.dataPacksButton.visible = false;
         this.modeButton.active = false;
         if (this.oldGameMode == null) {
            this.oldGameMode = this.gameMode;
         }

         this.setGameMode(CreateWorldScreen.SelectedGameMode.DEBUG);
         this.commandsButton.visible = false;
      } else {
         this.modeButton.active = true;
         if (this.oldGameMode != null) {
            this.setGameMode(this.oldGameMode);
         }

         this.commandsButton.visible = !p_170197_;
         this.dataPacksButton.visible = !p_170197_;
      }

      this.worldGenSettingsComponent.setVisibility(p_170197_);
      this.nameEdit.setVisible(!p_170197_);
      if (p_170197_) {
         this.moreOptionsButton.setMessage(CommonComponents.GUI_DONE);
      } else {
         this.moreOptionsButton.setMessage(Component.translatable("selectWorld.moreWorldOptions"));
      }

      this.gameRulesButton.visible = !p_170197_;
   }

   public boolean keyPressed(int p_100875_, int p_100876_, int p_100877_) {
      if (super.keyPressed(p_100875_, p_100876_, p_100877_)) {
         return true;
      } else if (p_100875_ != 257 && p_100875_ != 335) {
         return false;
      } else {
         this.onCreate();
         return true;
      }
   }

   public void onClose() {
      if (this.worldGenSettingsVisible) {
         this.setWorldGenSettingsVisible(false);
      } else {
         this.popScreen();
      }

   }

   public void popScreen() {
      this.minecraft.setScreen(this.lastScreen);
      this.removeTempDataPackDir();
   }

   public void render(PoseStack p_100890_, int p_100891_, int p_100892_, float p_100893_) {
      this.renderBackground(p_100890_);
      drawCenteredString(p_100890_, this.font, this.title, this.width / 2, 20, -1);
      if (this.worldGenSettingsVisible) {
         drawString(p_100890_, this.font, SEED_LABEL, this.width / 2 - 100, 47, -6250336);
         drawString(p_100890_, this.font, SEED_INFO, this.width / 2 - 100, 85, -6250336);
         this.worldGenSettingsComponent.render(p_100890_, p_100891_, p_100892_, p_100893_);
      } else {
         drawString(p_100890_, this.font, NAME_LABEL, this.width / 2 - 100, 47, -6250336);
         drawString(p_100890_, this.font, Component.empty().append(OUTPUT_DIR_INFO).append(" ").append(this.resultFolder), this.width / 2 - 100, 85, -6250336);
         this.nameEdit.render(p_100890_, p_100891_, p_100892_, p_100893_);
         drawString(p_100890_, this.font, this.gameModeHelp1, this.width / 2 - 150, 122, -6250336);
         drawString(p_100890_, this.font, this.gameModeHelp2, this.width / 2 - 150, 134, -6250336);
         if (this.commandsButton.visible) {
            drawString(p_100890_, this.font, COMMANDS_INFO, this.width / 2 - 150, 172, -6250336);
         }
      }

      super.render(p_100890_, p_100891_, p_100892_, p_100893_);
   }

   protected <T extends GuiEventListener & NarratableEntry> T addWidget(T p_100948_) {
      return super.addWidget(p_100948_);
   }

   protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T p_170199_) {
      return super.addRenderableWidget(p_170199_);
   }

   @Nullable
   private Path getTempDataPackDir() {
      if (this.tempDataPackDir == null) {
         try {
            this.tempDataPackDir = Files.createTempDirectory("mcworld-");
         } catch (IOException ioexception) {
            LOGGER.warn("Failed to create temporary dir", (Throwable)ioexception);
            SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
            this.popScreen();
         }
      }

      return this.tempDataPackDir;
   }

   private void openDataPackSelectionScreen() {
      Pair<Path, PackRepository> pair = this.getDataPackSelectionSettings();
      if (pair != null) {
         this.minecraft.setScreen(new PackSelectionScreen(this, pair.getSecond(), this::tryApplyNewDataPacks, pair.getFirst(), Component.translatable("dataPack.title")));
      }

   }

   private void tryApplyNewDataPacks(PackRepository p_100879_) {
      List<String> list = ImmutableList.copyOf(p_100879_.getSelectedIds());
      List<String> list1 = p_100879_.getAvailableIds().stream().filter((p_232927_) -> {
         return !list.contains(p_232927_);
      }).collect(ImmutableList.toImmutableList());
      WorldDataConfiguration worlddataconfiguration = new WorldDataConfiguration(new DataPackConfig(list, list1), this.dataConfiguration.enabledFeatures());
      if (list.equals(this.dataConfiguration.dataPacks().getEnabled())) {
         this.dataConfiguration = worlddataconfiguration;
      } else {
         FeatureFlagSet featureflagset = p_100879_.getRequestedFeatureFlags();
         if (FeatureFlags.isExperimental(featureflagset)) {
            this.minecraft.tell(() -> {
               this.minecraft.setScreen(new ConfirmExperimentalFeaturesScreen(p_100879_.getSelectedPacks(), (p_247796_) -> {
                  if (p_247796_) {
                     this.applyNewPackConfig(p_100879_, worlddataconfiguration);
                  } else {
                     this.openDataPackSelectionScreen();
                  }

               }));
            });
         } else {
            this.applyNewPackConfig(p_100879_, worlddataconfiguration);
         }

      }
   }

   private void applyNewPackConfig(PackRepository p_251497_, WorldDataConfiguration p_249845_) {
      this.minecraft.tell(() -> {
         this.minecraft.setScreen(new GenericDirtMessageScreen(Component.translatable("dataPack.validation.working")));
      });
      WorldLoader.InitConfig worldloader$initconfig = createDefaultLoadConfig(p_251497_, p_249845_);
      WorldLoader.<CreateWorldScreen.DataPackReloadCookie, WorldCreationContext>load(worldloader$initconfig, (p_247793_) -> {
         if (p_247793_.datapackWorldgen().registryOrThrow(Registries.WORLD_PRESET).size() == 0) {
            throw new IllegalStateException("Needs at least one world preset to continue");
         } else if (p_247793_.datapackWorldgen().registryOrThrow(Registries.BIOME).size() == 0) {
            throw new IllegalStateException("Needs at least one biome continue");
         } else {
            WorldCreationContext worldcreationcontext = this.worldGenSettingsComponent.settings();
            DynamicOps<JsonElement> dynamicops = RegistryOps.create(JsonOps.INSTANCE, worldcreationcontext.worldgenLoadContext());
            DataResult<JsonElement> dataresult = WorldGenSettings.encode(dynamicops, worldcreationcontext.options(), worldcreationcontext.selectedDimensions()).setLifecycle(Lifecycle.stable());
            DynamicOps<JsonElement> dynamicops1 = RegistryOps.create(JsonOps.INSTANCE, p_247793_.datapackWorldgen());
            WorldGenSettings worldgensettings = dataresult.flatMap((p_232895_) -> {
               return WorldGenSettings.CODEC.parse(dynamicops1, p_232895_);
            }).getOrThrow(false, Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error));
            return new WorldLoader.DataLoadOutput<>(new CreateWorldScreen.DataPackReloadCookie(worldgensettings, p_247793_.dataConfiguration()), p_247793_.datapackDimensions());
         }
      }, (p_247788_, p_247789_, p_247790_, p_247791_) -> {
         p_247788_.close();
         return new WorldCreationContext(p_247791_.worldGenSettings(), p_247790_, p_247789_, p_247791_.dataConfiguration());
      }, Util.backgroundExecutor(), this.minecraft).thenAcceptAsync((p_261374_) -> {
         this.dataConfiguration = p_261374_.dataConfiguration();
         this.worldGenSettingsComponent.updateSettings(p_261374_);
         this.rebuildWidgets();
      }, this.minecraft).handle((p_232918_, p_232919_) -> {
         if (p_232919_ != null) {
            LOGGER.warn("Failed to validate datapack", p_232919_);
            this.minecraft.tell(() -> {
               this.minecraft.setScreen(new ConfirmScreen((p_232949_) -> {
                  if (p_232949_) {
                     this.openDataPackSelectionScreen();
                  } else {
                     this.dataConfiguration = new WorldDataConfiguration(new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of()), FeatureFlags.DEFAULT_FLAGS); // FORGE: Revert to *actual* vanilla data
                     this.minecraft.setScreen(this);
                  }

               }, Component.translatable("dataPack.validation.failed"), CommonComponents.EMPTY, Component.translatable("dataPack.validation.back"), Component.translatable("dataPack.validation.reset")));
            });
         } else {
            this.minecraft.tell(() -> {
               this.minecraft.setScreen(this);
            });
         }

         return null;
      });
   }

   private static WorldLoader.InitConfig createDefaultLoadConfig(PackRepository p_251829_, WorldDataConfiguration p_251555_) {
      WorldLoader.PackConfig worldloader$packconfig = new WorldLoader.PackConfig(p_251829_, p_251555_, false, true);
      return new WorldLoader.InitConfig(worldloader$packconfig, Commands.CommandSelection.INTEGRATED, 2);
   }

   private void removeTempDataPackDir() {
      if (this.tempDataPackDir != null) {
         try (Stream<Path> stream = Files.walk(this.tempDataPackDir)) {
            stream.sorted(Comparator.reverseOrder()).forEach((p_232942_) -> {
               try {
                  Files.delete(p_232942_);
               } catch (IOException ioexception1) {
                  LOGGER.warn("Failed to remove temporary file {}", p_232942_, ioexception1);
               }

            });
         } catch (IOException ioexception) {
            LOGGER.warn("Failed to list temporary dir {}", (Object)this.tempDataPackDir);
         }

         this.tempDataPackDir = null;
      }

   }

   private static void copyBetweenDirs(Path p_100913_, Path p_100914_, Path p_100915_) {
      try {
         Util.copyBetweenDirs(p_100913_, p_100914_, p_100915_);
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to copy datapack file from {} to {}", p_100915_, p_100914_);
         throw new UncheckedIOException(ioexception);
      }
   }

   private Optional<LevelStorageSource.LevelStorageAccess> createNewWorldDirectory() {
      try {
         LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.minecraft.getLevelSource().createAccess(this.resultFolder);
         if (this.tempDataPackDir == null) {
            return Optional.of(levelstoragesource$levelstorageaccess);
         }

         try {
            Optional optional;
            try (Stream<Path> stream = Files.walk(this.tempDataPackDir)) {
               Path path = levelstoragesource$levelstorageaccess.getLevelPath(LevelResource.DATAPACK_DIR);
               FileUtil.createDirectoriesSafe(path);
               stream.filter((p_232921_) -> {
                  return !p_232921_.equals(this.tempDataPackDir);
               }).forEach((p_232945_) -> {
                  copyBetweenDirs(this.tempDataPackDir, path, p_232945_);
               });
               optional = Optional.of(levelstoragesource$levelstorageaccess);
            }

            return optional;
         } catch (UncheckedIOException | IOException ioexception) {
            LOGGER.warn("Failed to copy datapacks to world {}", this.resultFolder, ioexception);
            levelstoragesource$levelstorageaccess.close();
         }
      } catch (UncheckedIOException | IOException ioexception1) {
         LOGGER.warn("Failed to create access for {}", this.resultFolder, ioexception1);
      }

      SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
      this.popScreen();
      return Optional.empty();
   }

   @Nullable
   public static Path createTempDataPackDirFromExistingWorld(Path p_100907_, Minecraft p_100908_) {
      MutableObject<Path> mutableobject = new MutableObject<>();

      try (Stream<Path> stream = Files.walk(p_100907_)) {
         stream.filter((p_232924_) -> {
            return !p_232924_.equals(p_100907_);
         }).forEach((p_232933_) -> {
            Path path = mutableobject.getValue();
            if (path == null) {
               try {
                  path = Files.createTempDirectory("mcworld-");
               } catch (IOException ioexception1) {
                  LOGGER.warn("Failed to create temporary dir");
                  throw new UncheckedIOException(ioexception1);
               }

               mutableobject.setValue(path);
            }

            copyBetweenDirs(p_100907_, path, p_232933_);
         });
      } catch (UncheckedIOException | IOException ioexception) {
         LOGGER.warn("Failed to copy datapacks from world {}", p_100907_, ioexception);
         SystemToast.onPackCopyFailure(p_100908_, p_100907_.toString());
         return null;
      }

      return mutableobject.getValue();
   }

   @Nullable
   private Pair<Path, PackRepository> getDataPackSelectionSettings() {
      Path path = this.getTempDataPackDir();
      if (path != null) {
         if (this.tempDataPackRepository == null) {
            this.tempDataPackRepository = ServerPacksSource.createPackRepository(path);
            net.minecraftforge.resource.ResourcePackLoader.loadResourcePacks(this.tempDataPackRepository, net.minecraftforge.server.ServerLifecycleHooks::buildPackFinder);
            this.tempDataPackRepository.reload();
         }

         this.tempDataPackRepository.setSelected(this.dataConfiguration.dataPacks().getEnabled());
         return Pair.of(path, this.tempDataPackRepository);
      } else {
         return null;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static record DataPackReloadCookie(WorldGenSettings worldGenSettings, WorldDataConfiguration dataConfiguration) {
   }

   @OnlyIn(Dist.CLIENT)
   static enum SelectedGameMode {
      SURVIVAL("survival", GameType.SURVIVAL),
      HARDCORE("hardcore", GameType.SURVIVAL),
      CREATIVE("creative", GameType.CREATIVE),
      DEBUG("spectator", GameType.SPECTATOR);

      final String name;
      final GameType gameType;
      private final Component displayName;

      private SelectedGameMode(String p_101035_, GameType p_101036_) {
         this.name = p_101035_;
         this.gameType = p_101036_;
         this.displayName = Component.translatable("selectWorld.gameMode." + p_101035_);
      }

      public Component getDisplayName() {
         return this.displayName;
      }
   }
}
