package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class EntityDamageSource extends DamageSource {
   protected final Entity entity;
   private boolean isThorns;

   public EntityDamageSource(String p_19394_, Entity p_19395_) {
      super(p_19394_);
      this.entity = p_19395_;
   }

   public EntityDamageSource setThorns() {
      this.isThorns = true;
      return this;
   }

   public boolean isThorns() {
      return this.isThorns;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public Component getLocalizedDeathMessage(LivingEntity p_19397_) {
      Entity entity = this.entity;
      ItemStack itemstack1;
      if (entity instanceof LivingEntity livingentity) {
         itemstack1 = livingentity.getMainHandItem();
      } else {
         itemstack1 = ItemStack.EMPTY;
      }

      ItemStack itemstack = itemstack1;
      String s = "death.attack." + this.msgId;
      return !itemstack.isEmpty() && itemstack.hasCustomHoverName() ? Component.translatable(s + ".item", p_19397_.getDisplayName(), this.entity.getDisplayName(), itemstack.getDisplayName()) : Component.translatable(s, p_19397_.getDisplayName(), this.entity.getDisplayName());
   }

   public boolean scalesWithDifficulty() {
      return this.entity instanceof LivingEntity && !(this.entity instanceof Player);
   }

   @Nullable
   public Vec3 getSourcePosition() {
      return this.entity.position();
   }

   public String toString() {
      return "EntityDamageSource (" + this.entity + ")";
   }
}