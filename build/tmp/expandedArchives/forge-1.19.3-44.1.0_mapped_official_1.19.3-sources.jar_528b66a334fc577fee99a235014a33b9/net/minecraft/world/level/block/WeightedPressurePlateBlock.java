package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WeightedPressurePlateBlock extends BasePressurePlateBlock {
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   private final int maxWeight;
   private final SoundEvent soundOff;
   private final SoundEvent soundOn;

   public WeightedPressurePlateBlock(int p_250012_, BlockBehaviour.Properties p_249299_, SoundEvent p_251068_, SoundEvent p_249071_) {
      super(p_249299_);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
      this.maxWeight = p_250012_;
      this.soundOff = p_251068_;
      this.soundOn = p_249071_;
   }

   protected int getSignalStrength(Level p_58213_, BlockPos p_58214_) {
      int i = Math.min(p_58213_.getEntitiesOfClass(Entity.class, TOUCH_AABB.move(p_58214_)).size(), this.maxWeight);
      if (i > 0) {
         float f = (float)Math.min(this.maxWeight, i) / (float)this.maxWeight;
         return Mth.ceil(f * 15.0F);
      } else {
         return 0;
      }
   }

   protected void playOnSound(LevelAccessor p_58205_, BlockPos p_58206_) {
      p_58205_.playSound((Player)null, p_58206_, this.soundOn, SoundSource.BLOCKS);
   }

   protected void playOffSound(LevelAccessor p_58216_, BlockPos p_58217_) {
      p_58216_.playSound((Player)null, p_58217_, this.soundOff, SoundSource.BLOCKS);
   }

   protected int getSignalForState(BlockState p_58220_) {
      return p_58220_.getValue(POWER);
   }

   protected BlockState setSignalForState(BlockState p_58208_, int p_58209_) {
      return p_58208_.setValue(POWER, Integer.valueOf(p_58209_));
   }

   protected int getPressedTime() {
      return 10;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_58211_) {
      p_58211_.add(POWER);
   }
}