package net.minecraft.world.level.block.state.properties;

import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class RotationSegment {
   private static final int MAX_SEGMENT_INDEX = 15;
   private static final int NORTH_0 = 0;
   private static final int EAST_90 = 4;
   private static final int SOUTH_180 = 8;
   private static final int WEST_270 = 12;

   public static int getMaxSegmentIndex() {
      return 15;
   }

   public static int convertToSegment(Direction p_249634_) {
      return p_249634_.getAxis().isVertical() ? 0 : p_249634_.getOpposite().get2DDataValue() * 4;
   }

   public static int convertToSegment(float p_249057_) {
      return Mth.floor((double)((180.0F + p_249057_) * 16.0F / 360.0F) + 0.5D) & 15;
   }

   public static Optional<Direction> convertToDirection(int p_250978_) {
      Direction direction1;
      switch (p_250978_) {
         case 0:
            direction1 = Direction.NORTH;
            break;
         case 4:
            direction1 = Direction.EAST;
            break;
         case 8:
            direction1 = Direction.SOUTH;
            break;
         case 12:
            direction1 = Direction.WEST;
            break;
         default:
            direction1 = null;
      }

      Direction direction = direction1;
      return Optional.ofNullable(direction);
   }

   public static float convertToDegrees(int p_250653_) {
      return (float)p_250653_ * 22.5F;
   }
}