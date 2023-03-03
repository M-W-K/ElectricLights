package net.minecraft.data.recipes.packs;

import java.util.function.Consumer;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class UpdateOneTwentyRecipeProvider extends RecipeProvider {
   public UpdateOneTwentyRecipeProvider(PackOutput p_249171_) {
      super(p_249171_);
   }

   protected void buildRecipes(Consumer<FinishedRecipe> p_250771_) {
      generateForEnabledBlockFamilies(p_250771_, FeatureFlagSet.of(FeatureFlags.UPDATE_1_20));
      threeByThreePacker(p_250771_, RecipeCategory.BUILDING_BLOCKS, Blocks.BAMBOO_BLOCK, Items.BAMBOO);
      planksFromLogs(p_250771_, Blocks.BAMBOO_PLANKS, ItemTags.BAMBOO_BLOCKS, 2);
      mosaicBuilder(p_250771_, RecipeCategory.DECORATIONS, Blocks.BAMBOO_MOSAIC, Blocks.BAMBOO_SLAB);
      woodenBoat(p_250771_, Items.BAMBOO_RAFT, Blocks.BAMBOO_PLANKS);
      chestBoat(p_250771_, Items.BAMBOO_CHEST_RAFT, Items.BAMBOO_RAFT);
      hangingSign(p_250771_, Items.OAK_HANGING_SIGN, Blocks.STRIPPED_OAK_LOG);
      hangingSign(p_250771_, Items.SPRUCE_HANGING_SIGN, Blocks.STRIPPED_SPRUCE_LOG);
      hangingSign(p_250771_, Items.BIRCH_HANGING_SIGN, Blocks.STRIPPED_BIRCH_LOG);
      hangingSign(p_250771_, Items.JUNGLE_HANGING_SIGN, Blocks.STRIPPED_JUNGLE_LOG);
      hangingSign(p_250771_, Items.ACACIA_HANGING_SIGN, Blocks.STRIPPED_ACACIA_LOG);
      hangingSign(p_250771_, Items.DARK_OAK_HANGING_SIGN, Blocks.STRIPPED_DARK_OAK_LOG);
      hangingSign(p_250771_, Items.MANGROVE_HANGING_SIGN, Blocks.STRIPPED_MANGROVE_LOG);
      hangingSign(p_250771_, Items.BAMBOO_HANGING_SIGN, Items.STRIPPED_BAMBOO_BLOCK);
      hangingSign(p_250771_, Items.CRIMSON_HANGING_SIGN, Blocks.STRIPPED_CRIMSON_STEM);
      hangingSign(p_250771_, Items.WARPED_HANGING_SIGN, Blocks.STRIPPED_WARPED_STEM);
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.CHISELED_BOOKSHELF).define('#', ItemTags.PLANKS).define('X', ItemTags.WOODEN_SLABS).pattern("###").pattern("XXX").pattern("###").unlockedBy("has_book", has(Items.BOOK)).save(p_250771_);
   }
}