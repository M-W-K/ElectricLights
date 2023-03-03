package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.ScreenNarrationCollector;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector2ic;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class Screen extends AbstractContainerEventHandler implements Renderable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Set<String> ALLOWED_PROTOCOLS = Sets.newHashSet("http", "https");
   private static final int EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE = 2;
   private static final Component USAGE_NARRATION = Component.translatable("narrator.screen.usage");
   protected final Component title;
   private final List<GuiEventListener> children = Lists.newArrayList();
   private final List<NarratableEntry> narratables = Lists.newArrayList();
   @Nullable
   protected Minecraft minecraft;
   protected ItemRenderer itemRenderer;
   public int width;
   public int height;
   public final List<Renderable> renderables = Lists.newArrayList();
   public boolean passEvents;
   protected Font font;
   @Nullable
   private URI clickedLink;
   private static final long NARRATE_SUPPRESS_AFTER_INIT_TIME = TimeUnit.SECONDS.toMillis(2L);
   private static final long NARRATE_DELAY_NARRATOR_ENABLED = NARRATE_SUPPRESS_AFTER_INIT_TIME;
   private static final long NARRATE_DELAY_MOUSE_MOVE = 750L;
   private static final long NARRATE_DELAY_MOUSE_ACTION = 200L;
   private static final long NARRATE_DELAY_KEYBOARD_ACTION = 200L;
   private final ScreenNarrationCollector narrationState = new ScreenNarrationCollector();
   private long narrationSuppressTime = Long.MIN_VALUE;
   private long nextNarrationTime = Long.MAX_VALUE;
   @Nullable
   private NarratableEntry lastNarratable;
   @Nullable
   private Screen.DeferredTooltipRendering deferredTooltipRendering;

   protected Screen(Component p_96550_) {
      this.title = p_96550_;
   }

   public Component getTitle() {
      return this.title;
   }

   public Component getNarrationMessage() {
      return this.getTitle();
   }

   public final void renderWithTooltip(PoseStack p_259694_, int p_260150_, int p_259877_, float p_259252_) {
      this.render(p_259694_, p_260150_, p_259877_, p_259252_);
      if (this.deferredTooltipRendering != null) {
         this.renderTooltip(p_259694_, this.deferredTooltipRendering, p_260150_, p_259877_);
         this.deferredTooltipRendering = null;
      }

   }

   public void render(PoseStack p_96562_, int p_96563_, int p_96564_, float p_96565_) {
      for(Renderable renderable : this.renderables) {
         renderable.render(p_96562_, p_96563_, p_96564_, p_96565_);
      }

   }

   public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
      if (p_96552_ == 256 && this.shouldCloseOnEsc()) {
         this.onClose();
         return true;
      } else if (p_96552_ == 258) {
         boolean flag = !hasShiftDown();
         if (!this.changeFocus(flag)) {
            this.changeFocus(flag);
         }

         return false;
      } else {
         return super.keyPressed(p_96552_, p_96553_, p_96554_);
      }
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   public void onClose() {
      this.minecraft.popGuiLayer();
   }

   protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T p_169406_) {
      this.renderables.add(p_169406_);
      return this.addWidget(p_169406_);
   }

   protected <T extends Renderable> T addRenderableOnly(T p_254514_) {
      this.renderables.add(p_254514_);
      return p_254514_;
   }

   protected <T extends GuiEventListener & NarratableEntry> T addWidget(T p_96625_) {
      this.children.add(p_96625_);
      this.narratables.add(p_96625_);
      return p_96625_;
   }

   protected void removeWidget(GuiEventListener p_169412_) {
      if (p_169412_ instanceof Renderable) {
         this.renderables.remove((Renderable)p_169412_);
      }

      if (p_169412_ instanceof NarratableEntry) {
         this.narratables.remove((NarratableEntry)p_169412_);
      }

      this.children.remove(p_169412_);
   }

   protected void clearWidgets() {
      this.renderables.clear();
      this.children.clear();
      this.narratables.clear();
   }

   private Font tooltipFont = null;
   private ItemStack tooltipStack = ItemStack.EMPTY;
   protected void renderTooltip(PoseStack p_96566_, ItemStack p_96567_, int p_96568_, int p_96569_) {
      tooltipStack = p_96567_;
      this.renderTooltip(p_96566_, this.getTooltipFromItem(p_96567_), p_96567_.getTooltipImage(), p_96568_, p_96569_);
      tooltipStack = ItemStack.EMPTY;
   }

   public void renderTooltip(PoseStack poseStack, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, int x, int y, ItemStack stack) {
      this.renderTooltip(poseStack, textComponents, tooltipComponent, x, y, null, stack);
   }
   public void renderTooltip(PoseStack poseStack, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, int x, int y, @Nullable Font font) {
      this.renderTooltip(poseStack, textComponents, tooltipComponent, x, y, font, ItemStack.EMPTY);
   }
   public void renderTooltip(PoseStack poseStack, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, int x, int y, @Nullable Font font, ItemStack stack) {
      this.tooltipFont = font;
      this.tooltipStack = stack;
      this.renderTooltip(poseStack, textComponents, tooltipComponent, x, y);
      this.tooltipFont = null;
      this.tooltipStack = ItemStack.EMPTY;
   }
   public void renderTooltip(PoseStack p_169389_, List<Component> p_169390_, Optional<TooltipComponent> p_169391_, int p_169392_, int p_169393_) {
      List<ClientTooltipComponent> list = net.minecraftforge.client.ForgeHooksClient.gatherTooltipComponents(this.tooltipStack, p_169390_, p_169391_, p_169392_, width, height, this.tooltipFont, this.font);
      this.renderTooltipInternal(p_169389_, list, p_169392_, p_169393_, DefaultTooltipPositioner.INSTANCE);
   }

   public List<Component> getTooltipFromItem(ItemStack p_96556_) {
      return p_96556_.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
   }

   public void renderTooltip(PoseStack p_96603_, Component p_96604_, int p_96605_, int p_96606_) {
      this.renderTooltip(p_96603_, Arrays.asList(p_96604_.getVisualOrderText()), p_96605_, p_96606_);
   }

   public void renderComponentTooltip(PoseStack p_96598_, List<Component> p_96599_, int p_96600_, int p_96601_) {
      List<ClientTooltipComponent> components = net.minecraftforge.client.ForgeHooksClient.gatherTooltipComponents(this.tooltipStack, p_96599_, p_96600_, width, height, this.tooltipFont, this.font);
      this.renderTooltipInternal(p_96598_, components, p_96600_, p_96601_, DefaultTooltipPositioner.INSTANCE);
   }
   public void renderComponentTooltip(PoseStack poseStack, List<? extends net.minecraft.network.chat.FormattedText> tooltips, int mouseX, int mouseY, ItemStack stack) {
      this.renderComponentTooltip(poseStack, tooltips, mouseX, mouseY, null, stack);
   }
   public void renderComponentTooltip(PoseStack poseStack, List<? extends net.minecraft.network.chat.FormattedText> tooltips, int mouseX, int mouseY, @Nullable Font font) {
      this.renderComponentTooltip(poseStack, tooltips, mouseX, mouseY, font, ItemStack.EMPTY);
   }

   public void renderComponentTooltip(PoseStack poseStack, List<? extends net.minecraft.network.chat.FormattedText> tooltips, int mouseX, int mouseY, @Nullable Font font, ItemStack stack) {
      this.tooltipFont = font;
      this.tooltipStack = stack;
      List<ClientTooltipComponent> components = net.minecraftforge.client.ForgeHooksClient.gatherTooltipComponents(stack, tooltips, mouseX, width, height, this.tooltipFont, this.font);
      this.renderTooltipInternal(poseStack, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE);
      this.tooltipFont = null;
      this.tooltipStack = ItemStack.EMPTY;
   }

   public void renderTooltip(PoseStack p_96618_, List<? extends FormattedCharSequence> p_96619_, int p_96620_, int p_96621_) {
      this.renderTooltipInternal(p_96618_, p_96619_.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), p_96620_, p_96621_, DefaultTooltipPositioner.INSTANCE);
   }

   public void renderTooltip(PoseStack poseStack, List<? extends FormattedCharSequence> lines, int x, int y, Font font) {
      this.tooltipFont = font;
      this.renderTooltip(poseStack, lines, x, y);
      this.tooltipFont = null;
   }

   private void renderTooltip(PoseStack p_263080_, Screen.DeferredTooltipRendering p_262968_, int p_263034_, int p_263076_) {
      this.renderTooltipInternal(p_263080_, p_262968_.tooltip().stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), p_263034_, p_263076_, p_262968_.positioner());
   }

   private void renderTooltipInternal(PoseStack p_263064_, List<ClientTooltipComponent> p_262990_, int p_263065_, int p_262996_, ClientTooltipPositioner p_262920_) {
      if (!p_262990_.isEmpty()) {
         net.minecraftforge.client.event.RenderTooltipEvent.Pre preEvent = net.minecraftforge.client.ForgeHooksClient.onRenderTooltipPre(this.tooltipStack, p_263064_, p_263065_, p_262996_, width, height, p_262990_, this.tooltipFont, this.font);
         if (preEvent.isCanceled()) return;
         int i = 0;
         int j = p_262990_.size() == 1 ? -2 : 0;

         for(ClientTooltipComponent clienttooltipcomponent : p_262990_) {
            int k = clienttooltipcomponent.getWidth(preEvent.getFont());
            if (k > i) {
               i = k;
            }

            j += clienttooltipcomponent.getHeight();
         }

         int k1 = preEvent.getX() + 12;
         int l1 = preEvent.getY() - 12;
         Vector2ic vector2ic = p_262920_.positionTooltip(this, k1, l1, i, j);
         k1 = vector2ic.x();
         l1 = vector2ic.y();
         p_263064_.pushPose();
         int l = 400;
         float f = this.itemRenderer.blitOffset;
         this.itemRenderer.blitOffset = 400.0F;
         Tesselator tesselator = Tesselator.getInstance();
         BufferBuilder bufferbuilder = tesselator.getBuilder();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
         Matrix4f matrix4f = p_263064_.last().pose();
         // TODO 1.19.3: Tooltip rendering changed, rethink the color event.
         // net.minecraftforge.client.event.RenderTooltipEvent.Color colorEvent = net.minecraftforge.client.ForgeHooksClient.onRenderTooltipColor(this.tooltipStack, p_263064_, k1, l1, preEvent.getFont(), p_262990_);
         TooltipRenderUtil.renderTooltipBackground((p_262872_, p_262873_, p_262874_, p_262875_, p_262876_, p_262877_, p_262878_, p_262879_, p_262880_) -> {
            GuiComponent.fillGradient(p_262872_, p_262873_, p_262874_, p_262875_, p_262876_, p_262877_, p_262878_, p_262879_, p_262880_);
         }, matrix4f, bufferbuilder, k1, l1, i, j, 400);
         RenderSystem.enableDepthTest();
         RenderSystem.disableTexture();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         BufferUploader.drawWithShader(bufferbuilder.end());
         RenderSystem.disableBlend();
         RenderSystem.enableTexture();
         MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
         p_263064_.translate(0.0F, 0.0F, 400.0F);
         int i1 = l1;

         for(int j1 = 0; j1 < p_262990_.size(); ++j1) {
            ClientTooltipComponent clienttooltipcomponent1 = p_262990_.get(j1);
            clienttooltipcomponent1.renderText(preEvent.getFont(), k1, i1, matrix4f, multibuffersource$buffersource);
            i1 += clienttooltipcomponent1.getHeight() + (j1 == 0 ? 2 : 0);
         }

         multibuffersource$buffersource.endBatch();
         p_263064_.popPose();
         i1 = l1;

         for(int i2 = 0; i2 < p_262990_.size(); ++i2) {
            ClientTooltipComponent clienttooltipcomponent2 = p_262990_.get(i2);
            clienttooltipcomponent2.renderImage(preEvent.getFont(), k1, i1, p_263064_, this.itemRenderer, 400);
            i1 += clienttooltipcomponent2.getHeight() + (i2 == 0 ? 2 : 0);
         }

         this.itemRenderer.blitOffset = f;
      }
   }

   protected void renderComponentHoverEffect(PoseStack p_96571_, @Nullable Style p_96572_, int p_96573_, int p_96574_) {
      if (p_96572_ != null && p_96572_.getHoverEvent() != null) {
         HoverEvent hoverevent = p_96572_.getHoverEvent();
         HoverEvent.ItemStackInfo hoverevent$itemstackinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ITEM);
         if (hoverevent$itemstackinfo != null) {
            this.renderTooltip(p_96571_, hoverevent$itemstackinfo.getItemStack(), p_96573_, p_96574_);
         } else {
            HoverEvent.EntityTooltipInfo hoverevent$entitytooltipinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ENTITY);
            if (hoverevent$entitytooltipinfo != null) {
               if (this.minecraft.options.advancedItemTooltips) {
                  this.renderComponentTooltip(p_96571_, hoverevent$entitytooltipinfo.getTooltipLines(), p_96573_, p_96574_);
               }
            } else {
               Component component = hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);
               if (component != null) {
                  this.renderTooltip(p_96571_, this.minecraft.font.split(component, Math.max(this.width / 2, 200)), p_96573_, p_96574_);
               }
            }
         }

      }
   }

   protected void insertText(String p_96587_, boolean p_96588_) {
   }

   public boolean handleComponentClicked(@Nullable Style p_96592_) {
      if (p_96592_ == null) {
         return false;
      } else {
         ClickEvent clickevent = p_96592_.getClickEvent();
         if (hasShiftDown()) {
            if (p_96592_.getInsertion() != null) {
               this.insertText(p_96592_.getInsertion(), false);
            }
         } else if (clickevent != null) {
            if (clickevent.getAction() == ClickEvent.Action.OPEN_URL) {
               if (!this.minecraft.options.chatLinks().get()) {
                  return false;
               }

               try {
                  URI uri = new URI(clickevent.getValue());
                  String s = uri.getScheme();
                  if (s == null) {
                     throw new URISyntaxException(clickevent.getValue(), "Missing protocol");
                  }

                  if (!ALLOWED_PROTOCOLS.contains(s.toLowerCase(Locale.ROOT))) {
                     throw new URISyntaxException(clickevent.getValue(), "Unsupported protocol: " + s.toLowerCase(Locale.ROOT));
                  }

                  if (this.minecraft.options.chatLinksPrompt().get()) {
                     this.clickedLink = uri;
                     this.minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, clickevent.getValue(), false));
                  } else {
                     this.openLink(uri);
                  }
               } catch (URISyntaxException urisyntaxexception) {
                  LOGGER.error("Can't open url for {}", clickevent, urisyntaxexception);
               }
            } else if (clickevent.getAction() == ClickEvent.Action.OPEN_FILE) {
               URI uri1 = (new File(clickevent.getValue())).toURI();
               this.openLink(uri1);
            } else if (clickevent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
               this.insertText(SharedConstants.filterText(clickevent.getValue()), true);
            } else if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
               String s1 = SharedConstants.filterText(clickevent.getValue());
               if (s1.startsWith("/")) {
                  if (!this.minecraft.player.connection.sendUnsignedCommand(s1.substring(1))) {
                     LOGGER.error("Not allowed to run command with signed argument from click event: '{}'", (Object)s1);
                  }
               } else {
                  LOGGER.error("Failed to run command without '/' prefix from click event: '{}'", (Object)s1);
               }
            } else if (clickevent.getAction() == ClickEvent.Action.COPY_TO_CLIPBOARD) {
               this.minecraft.keyboardHandler.setClipboard(clickevent.getValue());
            } else {
               LOGGER.error("Don't know how to handle {}", (Object)clickevent);
            }

            return true;
         }

         return false;
      }
   }

   public final void init(Minecraft p_96607_, int p_96608_, int p_96609_) {
      this.minecraft = p_96607_;
      this.itemRenderer = p_96607_.getItemRenderer();
      this.font = p_96607_.font;
      this.width = p_96608_;
      this.height = p_96609_;
      java.util.function.Consumer<GuiEventListener> add = (b) -> {
         if (b instanceof Renderable r)
            this.renderables.add(r);
         if (b instanceof NarratableEntry ne)
            this.narratables.add(ne);
         children.add(b);
      };
      if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.Init.Pre(this, this.children, add, this::removeWidget))) {
      this.rebuildWidgets();
      this.triggerImmediateNarration(false);
      this.suppressNarration(NARRATE_SUPPRESS_AFTER_INIT_TIME);
      }
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.Init.Post(this, this.children, add, this::removeWidget));
   }

   protected void rebuildWidgets() {
      this.clearWidgets();
      this.setFocused((GuiEventListener)null);
      this.init();
   }

   public List<? extends GuiEventListener> children() {
      return this.children;
   }

   protected void init() {
   }

   public void tick() {
   }

   public void removed() {
   }

   public void renderBackground(PoseStack p_96557_) {
      this.renderBackground(p_96557_, 0);
   }

   public void renderBackground(PoseStack p_96559_, int p_96560_) {
      if (this.minecraft.level != null) {
         this.fillGradient(p_96559_, 0, 0, this.width, this.height, -1072689136, -804253680);
         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.BackgroundRendered(this, p_96559_));
      } else {
         this.renderDirtBackground(p_96560_);
      }

   }

   public void renderDirtBackground(int p_96627_) {
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      float f = 32.0F;
      bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      bufferbuilder.vertex(0.0D, (double)this.height, 0.0D).uv(0.0F, (float)this.height / 32.0F + (float)p_96627_).color(64, 64, 64, 255).endVertex();
      bufferbuilder.vertex((double)this.width, (double)this.height, 0.0D).uv((float)this.width / 32.0F, (float)this.height / 32.0F + (float)p_96627_).color(64, 64, 64, 255).endVertex();
      bufferbuilder.vertex((double)this.width, 0.0D, 0.0D).uv((float)this.width / 32.0F, (float)p_96627_).color(64, 64, 64, 255).endVertex();
      bufferbuilder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, (float)p_96627_).color(64, 64, 64, 255).endVertex();
      tesselator.end();
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.BackgroundRendered(this, new PoseStack()));
   }

   public boolean isPauseScreen() {
      return true;
   }

   private void confirmLink(boolean p_96623_) {
      if (p_96623_) {
         this.openLink(this.clickedLink);
      }

      this.clickedLink = null;
      this.minecraft.setScreen(this);
   }

   private void openLink(URI p_96590_) {
      Util.getPlatform().openUri(p_96590_);
   }

   public static boolean hasControlDown() {
      if (Minecraft.ON_OSX) {
         return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 343) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 347);
      } else {
         return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 341) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 345);
      }
   }

   public static boolean hasShiftDown() {
      return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
   }

   public static boolean hasAltDown() {
      return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 342) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 346);
   }

   public static boolean isCut(int p_96629_) {
      return p_96629_ == 88 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static boolean isPaste(int p_96631_) {
      return p_96631_ == 86 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static boolean isCopy(int p_96633_) {
      return p_96633_ == 67 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static boolean isSelectAll(int p_96635_) {
      return p_96635_ == 65 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public void resize(Minecraft p_96575_, int p_96576_, int p_96577_) {
      this.init(p_96575_, p_96576_, p_96577_);
   }

   public static void wrapScreenError(Runnable p_96580_, String p_96581_, String p_96582_) {
      try {
         p_96580_.run();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, p_96581_);
         CrashReportCategory crashreportcategory = crashreport.addCategory("Affected screen");
         crashreportcategory.setDetail("Screen name", () -> {
            return p_96582_;
         });
         throw new ReportedException(crashreport);
      }
   }

   protected boolean isValidCharacterForName(String p_96584_, char p_96585_, int p_96586_) {
      int i = p_96584_.indexOf(58);
      int j = p_96584_.indexOf(47);
      if (p_96585_ == ':') {
         return (j == -1 || p_96586_ <= j) && i == -1;
      } else if (p_96585_ == '/') {
         return p_96586_ > i;
      } else {
         return p_96585_ == '_' || p_96585_ == '-' || p_96585_ >= 'a' && p_96585_ <= 'z' || p_96585_ >= '0' && p_96585_ <= '9' || p_96585_ == '.';
      }
   }

   public boolean isMouseOver(double p_96595_, double p_96596_) {
      return true;
   }

   public void onFilesDrop(List<Path> p_96591_) {
   }

   public Minecraft getMinecraft() {
      return this.minecraft;
   }

   private void scheduleNarration(long p_169381_, boolean p_169382_) {
      this.nextNarrationTime = Util.getMillis() + p_169381_;
      if (p_169382_) {
         this.narrationSuppressTime = Long.MIN_VALUE;
      }

   }

   private void suppressNarration(long p_169379_) {
      this.narrationSuppressTime = Util.getMillis() + p_169379_;
   }

   public void afterMouseMove() {
      this.scheduleNarration(750L, false);
   }

   public void afterMouseAction() {
      this.scheduleNarration(200L, true);
   }

   public void afterKeyboardAction() {
      this.scheduleNarration(200L, true);
   }

   private boolean shouldRunNarration() {
      return this.minecraft.getNarrator().isActive();
   }

   public void handleDelayedNarration() {
      if (this.shouldRunNarration()) {
         long i = Util.getMillis();
         if (i > this.nextNarrationTime && i > this.narrationSuppressTime) {
            this.runNarration(true);
            this.nextNarrationTime = Long.MAX_VALUE;
         }
      }

   }

   public void triggerImmediateNarration(boolean p_169408_) {
      if (this.shouldRunNarration()) {
         this.runNarration(p_169408_);
      }

   }

   private void runNarration(boolean p_169410_) {
      this.narrationState.update(this::updateNarrationState);
      String s = this.narrationState.collectNarrationText(!p_169410_);
      if (!s.isEmpty()) {
         this.minecraft.getNarrator().sayNow(s);
      }

   }

   protected void updateNarrationState(NarrationElementOutput p_169396_) {
      p_169396_.add(NarratedElementType.TITLE, this.getNarrationMessage());
      p_169396_.add(NarratedElementType.USAGE, USAGE_NARRATION);
      this.updateNarratedWidget(p_169396_);
   }

   protected void updateNarratedWidget(NarrationElementOutput p_169403_) {
      ImmutableList<NarratableEntry> immutablelist = this.narratables.stream().filter(NarratableEntry::isActive).collect(ImmutableList.toImmutableList());
      Screen.NarratableSearchResult screen$narratablesearchresult = findNarratableWidget(immutablelist, this.lastNarratable);
      if (screen$narratablesearchresult != null) {
         if (screen$narratablesearchresult.priority.isTerminal()) {
            this.lastNarratable = screen$narratablesearchresult.entry;
         }

         if (immutablelist.size() > 1) {
            p_169403_.add(NarratedElementType.POSITION, Component.translatable("narrator.position.screen", screen$narratablesearchresult.index + 1, immutablelist.size()));
            if (screen$narratablesearchresult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
               p_169403_.add(NarratedElementType.USAGE, Component.translatable("narration.component_list.usage"));
            }
         }

         screen$narratablesearchresult.entry.updateNarration(p_169403_.nest());
      }

   }

   @Nullable
   public static Screen.NarratableSearchResult findNarratableWidget(List<? extends NarratableEntry> p_169401_, @Nullable NarratableEntry p_169402_) {
      Screen.NarratableSearchResult screen$narratablesearchresult = null;
      Screen.NarratableSearchResult screen$narratablesearchresult1 = null;
      int i = 0;

      for(int j = p_169401_.size(); i < j; ++i) {
         NarratableEntry narratableentry = p_169401_.get(i);
         NarratableEntry.NarrationPriority narratableentry$narrationpriority = narratableentry.narrationPriority();
         if (narratableentry$narrationpriority.isTerminal()) {
            if (narratableentry != p_169402_) {
               return new Screen.NarratableSearchResult(narratableentry, i, narratableentry$narrationpriority);
            }

            screen$narratablesearchresult1 = new Screen.NarratableSearchResult(narratableentry, i, narratableentry$narrationpriority);
         } else if (narratableentry$narrationpriority.compareTo(screen$narratablesearchresult != null ? screen$narratablesearchresult.priority : NarratableEntry.NarrationPriority.NONE) > 0) {
            screen$narratablesearchresult = new Screen.NarratableSearchResult(narratableentry, i, narratableentry$narrationpriority);
         }
      }

      return screen$narratablesearchresult != null ? screen$narratablesearchresult : screen$narratablesearchresult1;
   }

   public void narrationEnabled() {
      this.scheduleNarration(NARRATE_DELAY_NARRATOR_ENABLED, false);
   }

   public void setTooltipForNextRenderPass(List<FormattedCharSequence> p_259937_) {
      this.setTooltipForNextRenderPass(p_259937_, DefaultTooltipPositioner.INSTANCE, true);
   }

   public void setTooltipForNextRenderPass(List<FormattedCharSequence> p_262939_, ClientTooltipPositioner p_263078_, boolean p_263107_) {
      if (this.deferredTooltipRendering == null || p_263107_) {
         this.deferredTooltipRendering = new Screen.DeferredTooltipRendering(p_262939_, p_263078_);
      }

   }

   protected void setTooltipForNextRenderPass(Component p_259986_) {
      this.setTooltipForNextRenderPass(Tooltip.splitTooltip(this.minecraft, p_259986_));
   }

   public void setTooltipForNextRenderPass(Tooltip p_262992_, ClientTooltipPositioner p_262980_, boolean p_262988_) {
      this.setTooltipForNextRenderPass(p_262992_.toCharSequence(this.minecraft), p_262980_, p_262988_);
   }

   protected static void hideWidgets(AbstractWidget... p_202377_) {
      for(AbstractWidget abstractwidget : p_202377_) {
         abstractwidget.visible = false;
      }

   }

   @OnlyIn(Dist.CLIENT)
   static record DeferredTooltipRendering(List<FormattedCharSequence> tooltip, ClientTooltipPositioner positioner) {
   }

   @OnlyIn(Dist.CLIENT)
   public static class NarratableSearchResult {
      public final NarratableEntry entry;
      public final int index;
      public final NarratableEntry.NarrationPriority priority;

      public NarratableSearchResult(NarratableEntry p_169424_, int p_169425_, NarratableEntry.NarrationPriority p_169426_) {
         this.entry = p_169424_;
         this.index = p_169425_;
         this.priority = p_169426_;
      }
   }
}
