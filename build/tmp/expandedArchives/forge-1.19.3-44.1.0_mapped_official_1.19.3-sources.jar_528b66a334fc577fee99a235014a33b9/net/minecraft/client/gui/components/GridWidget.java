package net.minecraft.client.gui.components;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GridWidget extends AbstractContainerWidget {
   private final List<AbstractWidget> children = new ArrayList<>();
   private final List<GridWidget.CellInhabitant> cellInhabitants = new ArrayList<>();
   private final LayoutSettings defaultCellSettings = LayoutSettings.defaults();

   public GridWidget() {
      this(0, 0);
   }

   public GridWidget(int p_253876_, int p_253928_) {
      this(p_253876_, p_253928_, Component.empty());
   }

   public GridWidget(int p_253963_, int p_254096_, Component p_253791_) {
      super(p_253963_, p_254096_, 0, 0, p_253791_);
   }

   public void pack() {
      int i = 0;
      int j = 0;

      for(GridWidget.CellInhabitant gridwidget$cellinhabitant : this.cellInhabitants) {
         i = Math.max(gridwidget$cellinhabitant.getLastOccupiedRow(), i);
         j = Math.max(gridwidget$cellinhabitant.getLastOccupiedColumn(), j);
      }

      int[] aint = new int[j + 1];
      int[] aint1 = new int[i + 1];

      for(GridWidget.CellInhabitant gridwidget$cellinhabitant1 : this.cellInhabitants) {
         Divisor divisor = new Divisor(gridwidget$cellinhabitant1.getHeight(), gridwidget$cellinhabitant1.occupiedRows);

         for(int k = gridwidget$cellinhabitant1.row; k <= gridwidget$cellinhabitant1.getLastOccupiedRow(); ++k) {
            aint1[k] = Math.max(aint1[k], divisor.nextInt());
         }

         Divisor divisor1 = new Divisor(gridwidget$cellinhabitant1.getWidth(), gridwidget$cellinhabitant1.occupiedColumns);

         for(int l = gridwidget$cellinhabitant1.column; l <= gridwidget$cellinhabitant1.getLastOccupiedColumn(); ++l) {
            aint[l] = Math.max(aint[l], divisor1.nextInt());
         }
      }

      int[] aint2 = new int[j + 1];
      int[] aint3 = new int[i + 1];
      aint2[0] = 0;

      for(int k1 = 1; k1 <= j; ++k1) {
         aint2[k1] = aint2[k1 - 1] + aint[k1 - 1];
      }

      aint3[0] = 0;

      for(int l1 = 1; l1 <= i; ++l1) {
         aint3[l1] = aint3[l1 - 1] + aint1[l1 - 1];
      }

      for(GridWidget.CellInhabitant gridwidget$cellinhabitant2 : this.cellInhabitants) {
         int i2 = 0;

         for(int i1 = gridwidget$cellinhabitant2.column; i1 <= gridwidget$cellinhabitant2.getLastOccupiedColumn(); ++i1) {
            i2 += aint[i1];
         }

         gridwidget$cellinhabitant2.setX(this.getX() + aint2[gridwidget$cellinhabitant2.column], i2);
         int j2 = 0;

         for(int j1 = gridwidget$cellinhabitant2.row; j1 <= gridwidget$cellinhabitant2.getLastOccupiedRow(); ++j1) {
            j2 += aint1[j1];
         }

         gridwidget$cellinhabitant2.setY(this.getY() + aint3[gridwidget$cellinhabitant2.row], j2);
      }

      this.width = aint2[j] + aint[j];
      this.height = aint3[i] + aint1[i];
   }

   public <T extends AbstractWidget> T addChild(T p_254508_, int p_254211_, int p_254200_) {
      return this.addChild(p_254508_, p_254211_, p_254200_, this.newCellSettings());
   }

   public <T extends AbstractWidget> T addChild(T p_254111_, int p_253708_, int p_253918_, LayoutSettings p_253611_) {
      return this.addChild(p_254111_, p_253708_, p_253918_, 1, 1, p_253611_);
   }

   public <T extends AbstractWidget> T addChild(T p_254063_, int p_253923_, int p_254490_, int p_254378_, int p_253716_) {
      return this.addChild(p_254063_, p_253923_, p_254490_, p_254378_, p_253716_, this.newCellSettings());
   }

   public <T extends AbstractWidget> T addChild(T p_254015_, int p_253893_, int p_254284_, int p_253678_, int p_253681_, LayoutSettings p_254124_) {
      if (p_253678_ < 1) {
         throw new IllegalArgumentException("Occupied rows must be at least 1");
      } else if (p_253681_ < 1) {
         throw new IllegalArgumentException("Occupied columns must be at least 1");
      } else {
         this.cellInhabitants.add(new GridWidget.CellInhabitant(p_254015_, p_253893_, p_254284_, p_253678_, p_253681_, p_254124_));
         this.children.add(p_254015_);
         return p_254015_;
      }
   }

   protected List<? extends AbstractWidget> getContainedChildren() {
      return this.children;
   }

   public LayoutSettings newCellSettings() {
      return this.defaultCellSettings.copy();
   }

   public LayoutSettings defaultCellSetting() {
      return this.defaultCellSettings;
   }

   public GridWidget.RowHelper createRowHelper(int p_262003_) {
      return new GridWidget.RowHelper(p_262003_);
   }

   @OnlyIn(Dist.CLIENT)
   static class CellInhabitant extends AbstractContainerWidget.AbstractChildWrapper {
      final int row;
      final int column;
      final int occupiedRows;
      final int occupiedColumns;

      CellInhabitant(AbstractWidget p_254100_, int p_253740_, int p_254404_, int p_254488_, int p_253646_, LayoutSettings p_254333_) {
         super(p_254100_, p_254333_.getExposed());
         this.row = p_253740_;
         this.column = p_254404_;
         this.occupiedRows = p_254488_;
         this.occupiedColumns = p_253646_;
      }

      public int getLastOccupiedRow() {
         return this.row + this.occupiedRows - 1;
      }

      public int getLastOccupiedColumn() {
         return this.column + this.occupiedColumns - 1;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public final class RowHelper {
      private final int columns;
      private int index;

      RowHelper(int p_261793_) {
         this.columns = p_261793_;
      }

      public <T extends AbstractWidget> T addChild(T p_262087_) {
         return this.addChild(p_262087_, 1);
      }

      public <T extends AbstractWidget> T addChild(T p_261506_, int p_262059_) {
         return this.addChild(p_261506_, p_262059_, this.defaultCellSetting());
      }

      public <T extends AbstractWidget> T addChild(T p_261990_, LayoutSettings p_262075_) {
         return this.addChild(p_261990_, 1, p_262075_);
      }

      public <T extends AbstractWidget> T addChild(T p_262090_, int p_261501_, LayoutSettings p_262060_) {
         int i = this.index / this.columns;
         int j = this.index % this.columns;
         if (j + p_261501_ > this.columns) {
            ++i;
            j = 0;
            this.index = Mth.roundToward(this.index, this.columns);
         }

         this.index += p_261501_;
         return GridWidget.this.addChild(p_262090_, i, j, 1, p_261501_, p_262060_);
      }

      public LayoutSettings newCellSettings() {
         return GridWidget.this.newCellSettings();
      }

      public LayoutSettings defaultCellSetting() {
         return GridWidget.this.defaultCellSetting();
      }
   }
}