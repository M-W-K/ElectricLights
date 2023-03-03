package net.minecraft.world.damagesource;

import net.minecraft.world.phys.Vec3;

public class PointDamageSource extends DamageSource {
   private final Vec3 damageSourcePosition;

   public PointDamageSource(String p_254254_, Vec3 p_254033_) {
      super(p_254254_);
      this.damageSourcePosition = p_254033_;
   }

   public Vec3 getSourcePosition() {
      return this.damageSourcePosition;
   }
}