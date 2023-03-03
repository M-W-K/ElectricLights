package net.minecraft.data.advancements.packs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityType;

public class UpdateOneTwentyAdvancements extends VanillaHusbandryAdvancements {
   public void generate(HolderLookup.Provider p_262074_, Consumer<Advancement> p_261509_) {
      Advancement advancement = this.createRoot(p_261509_);
      Advancement advancement1 = this.createBreedAnAnimalAdvancement(advancement, p_261509_);
      this.createBreedAllAnimalsAdvancement(advancement1, p_261509_);
   }

   public EntityType<?>[] getBreedableAnimals() {
      EntityType<?>[] entitytype = super.getBreedableAnimals();
      List<EntityType<?>> list = Arrays.stream(entitytype).collect(Collectors.toList());
      list.add(EntityType.CAMEL);
      return list.toArray(entitytype);
   }
}