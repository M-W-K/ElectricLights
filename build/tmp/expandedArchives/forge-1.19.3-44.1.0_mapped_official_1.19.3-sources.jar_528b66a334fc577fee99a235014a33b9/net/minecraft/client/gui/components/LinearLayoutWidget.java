package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LinearLayoutWidget extends AbstractContainerWidget {
   private final LinearLayoutWidget.Orientation orientation;
   private final List<LinearLayoutWidget.ChildContainer> children = new ArrayList<>();
   private final List<AbstractWidget> containedChildrenView = Collections.unmodifiableList(Lists.transform(this.children, (p_254146_) -> {
      return p_254146_.child;
   }));
   private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults();

   public LinearLayoutWidget(int p_253992_, int p_254410_, LinearLayoutWidget.Orientation p_253898_) {
      this(0, 0, p_253992_, p_254410_, p_253898_);
   }

   public LinearLayoutWidget(int p_253953_, int p_254179_, int p_254204_, int p_253837_, LinearLayoutWidget.Orientation p_254163_) {
      super(p_253953_, p_254179_, p_254204_, p_253837_, Component.empty());
      this.orientation = p_254163_;
   }

   public void pack() {
      if (!this.children.isEmpty()) {
         int i = 0;
         int j = this.orientation.getSecondaryLength(this);

         for(LinearLayoutWidget.ChildContainer linearlayoutwidget$childcontainer : this.children) {
            i += this.orientation.getPrimaryLength(linearlayoutwidget$childcontainer);
            j = Math.max(j, this.orientation.getSecondaryLength(linearlayoutwidget$childcontainer));
         }

         int k = this.orientation.getPrimaryLength(this) - i;
         int l = this.orientation.getPrimaryPosition(this);
         Iterator<LinearLayoutWidget.ChildContainer> iterator = this.children.iterator();
         LinearLayoutWidget.ChildContainer linearlayoutwidget$childcontainer1 = iterator.next();
         this.orientation.setPrimaryPosition(linearlayoutwidget$childcontainer1, l);
         l += this.orientation.getPrimaryLength(linearlayoutwidget$childcontainer1);
         LinearLayoutWidget.ChildContainer linearlayoutwidget$childcontainer2;
         if (this.children.size() >= 2) {
            for(Divisor divisor = new Divisor(k, this.children.size() - 1); divisor.hasNext(); l += this.orientation.getPrimaryLength(linearlayoutwidget$childcontainer2)) {
               l += divisor.nextInt();
               linearlayoutwidget$childcontainer2 = iterator.next();
               this.orientation.setPrimaryPosition(linearlayoutwidget$childcontainer2, l);
            }
         }

         int i1 = this.orientation.getSecondaryPosition(this);

         for(LinearLayoutWidget.ChildContainer linearlayoutwidget$childcontainer3 : this.children) {
            this.orientation.setSecondaryPosition(linearlayoutwidget$childcontainer3, i1, j);
         }

         this.orientation.setSecondaryLength(this, j);
      }
   }

   protected List<? extends AbstractWidget> getContainedChildren() {
      return this.containedChildrenView;
   }

   public LayoutSettings newChildLayoutSettings() {
      return this.defaultChildLayoutSettings.copy();
   }

   public LayoutSettings defaultChildLayoutSetting() {
      return this.defaultChildLayoutSettings;
   }

   public <T extends AbstractWidget> T addChild(T p_254554_) {
      return this.addChild(p_254554_, this.newChildLayoutSettings());
   }

   public <T extends AbstractWidget> T addChild(T p_253643_, LayoutSettings p_253652_) {
      this.children.add(new LinearLayoutWidget.ChildContainer(p_253643_, p_253652_));
      return p_253643_;
   }

   @OnlyIn(Dist.CLIENT)
   static class ChildContainer extends AbstractContainerWidget.AbstractChildWrapper {
      protected ChildContainer(AbstractWidget p_253998_, LayoutSettings p_254445_) {
         super(p_253998_, p_254445_);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Orientation {
      HORIZONTAL,
      VERTICAL;

      int getPrimaryLength(AbstractWidget p_254218_) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = p_254218_.getWidth();
               break;
            case VERTICAL:
               i = p_254218_.getHeight();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      int getPrimaryLength(LinearLayoutWidget.ChildContainer p_253875_) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = p_253875_.getWidth();
               break;
            case VERTICAL:
               i = p_253875_.getHeight();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      int getSecondaryLength(AbstractWidget p_254292_) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = p_254292_.getHeight();
               break;
            case VERTICAL:
               i = p_254292_.getWidth();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      int getSecondaryLength(LinearLayoutWidget.ChildContainer p_253622_) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = p_253622_.getHeight();
               break;
            case VERTICAL:
               i = p_253622_.getWidth();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      void setPrimaryPosition(LinearLayoutWidget.ChildContainer p_253675_, int p_254517_) {
         switch (this) {
            case HORIZONTAL:
               p_253675_.setX(p_254517_, p_253675_.getWidth());
               break;
            case VERTICAL:
               p_253675_.setY(p_254517_, p_253675_.getHeight());
         }

      }

      void setSecondaryPosition(LinearLayoutWidget.ChildContainer p_254172_, int p_254126_, int p_254113_) {
         switch (this) {
            case HORIZONTAL:
               p_254172_.setY(p_254126_, p_254113_);
               break;
            case VERTICAL:
               p_254172_.setX(p_254126_, p_254113_);
         }

      }

      int getPrimaryPosition(AbstractWidget p_254108_) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = p_254108_.getX();
               break;
            case VERTICAL:
               i = p_254108_.getY();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      int getSecondaryPosition(AbstractWidget p_253744_) {
         int i;
         switch (this) {
            case HORIZONTAL:
               i = p_253744_.getY();
               break;
            case VERTICAL:
               i = p_253744_.getX();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return i;
      }

      void setSecondaryLength(AbstractWidget p_254408_, int p_253958_) {
         switch (this) {
            case HORIZONTAL:
               p_254408_.height = p_253958_;
               break;
            case VERTICAL:
               p_254408_.width = p_253958_;
         }

      }
   }
}