package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;

public class UpdateActivityFromSchedule {
   public static BehaviorControl<LivingEntity> create() {
      return BehaviorBuilder.create((p_259429_) -> {
         return p_259429_.point((p_259250_, p_259187_, p_260251_) -> {
            p_259187_.getBrain().updateActivityFromSchedule(p_259250_.getDayTime(), p_259250_.getGameTime());
            return true;
         });
      });
   }
}