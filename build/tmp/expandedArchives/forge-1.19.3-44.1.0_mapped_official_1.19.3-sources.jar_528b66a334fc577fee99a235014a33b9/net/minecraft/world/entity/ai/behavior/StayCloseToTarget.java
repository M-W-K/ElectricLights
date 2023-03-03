package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class StayCloseToTarget {
   public static BehaviorControl<LivingEntity> create(Function<LivingEntity, Optional<PositionTracker>> p_259150_, int p_260151_, int p_260087_, float p_259454_) {
      return BehaviorBuilder.create((p_259273_) -> {
         return p_259273_.group(p_259273_.registered(MemoryModuleType.LOOK_TARGET), p_259273_.absent(MemoryModuleType.WALK_TARGET)).apply(p_259273_, (p_259832_, p_259483_) -> {
            return (p_260054_, p_260069_, p_259517_) -> {
               Optional<PositionTracker> optional = p_259150_.apply(p_260069_);
               if (optional.isEmpty()) {
                  return false;
               } else {
                  PositionTracker positiontracker = optional.get();
                  if (p_260069_.position().closerThan(positiontracker.currentPosition(), (double)p_260087_)) {
                     return false;
                  } else {
                     PositionTracker positiontracker1 = optional.get();
                     p_259832_.set(positiontracker1);
                     p_259483_.set(new WalkTarget(positiontracker1, p_259454_, p_260151_));
                     return true;
                  }
               }
            };
         });
      });
   }
}