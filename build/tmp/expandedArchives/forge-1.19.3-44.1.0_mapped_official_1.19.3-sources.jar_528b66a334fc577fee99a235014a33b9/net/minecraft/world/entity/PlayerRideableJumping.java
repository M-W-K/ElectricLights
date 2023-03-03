package net.minecraft.world.entity;

import net.minecraft.world.entity.player.Player;

public interface PlayerRideableJumping extends PlayerRideable {
   void onPlayerJump(int p_21696_);

   boolean canJump(Player p_260002_);

   void handleStartJump(int p_21695_);

   void handleStopJump();

   default int getJumpCooldown() {
      return 0;
   }
}