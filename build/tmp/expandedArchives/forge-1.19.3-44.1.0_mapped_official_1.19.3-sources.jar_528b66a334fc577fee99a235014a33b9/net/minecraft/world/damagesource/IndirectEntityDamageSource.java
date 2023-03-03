package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class IndirectEntityDamageSource extends EntityDamageSource {
   @Nullable
   private final Entity cause;

   public IndirectEntityDamageSource(String p_19406_, Entity p_19407_, @Nullable Entity p_19408_) {
      super(p_19406_, p_19407_);
      this.cause = p_19408_;
   }

   @Nullable
   public Entity getDirectEntity() {
      return this.entity;
   }

   @Nullable
   public Entity getEntity() {
      return this.cause;
   }

   public Component getLocalizedDeathMessage(LivingEntity p_19410_) {
      Component component = this.cause == null ? this.entity.getDisplayName() : this.cause.getDisplayName();
      Entity $$5 = this.cause;
      ItemStack itemstack1;
      if ($$5 instanceof LivingEntity livingentity) {
         itemstack1 = livingentity.getMainHandItem();
      } else {
         itemstack1 = ItemStack.EMPTY;
      }

      ItemStack itemstack = itemstack1;
      String s = "death.attack." + this.msgId;
      if (!itemstack.isEmpty() && itemstack.hasCustomHoverName()) {
         String s1 = s + ".item";
         return Component.translatable(s1, p_19410_.getDisplayName(), component, itemstack.getDisplayName());
      } else {
         return Component.translatable(s, p_19410_.getDisplayName(), component);
      }
   }
}