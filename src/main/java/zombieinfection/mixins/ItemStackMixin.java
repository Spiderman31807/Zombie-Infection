package zombieinfection.mixins;

import zombieinfection.Infectable;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.Foods;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	public FoodProperties getFoodProperties(LivingEntity entity) {
		ItemStack stack = ((ItemStack) (Object) this);
		Item item = stack.getItem();
		
		if (entity instanceof Infectable infectable && infectable.isInfected()) {
			if (item == Items.ROTTEN_FLESH)
				return Foods.COOKED_BEEF;
				
			// Reverse Cooked / Raw Foods
			if (item == Items.COOKED_BEEF)
				return Foods.BEEF;
			if (item == Items.BEEF)
				return Foods.COOKED_BEEF;
			if (item == Items.COOKED_CHICKEN)
				return Foods.CHICKEN;
			if (item == Items.CHICKEN)
				return Foods.COOKED_CHICKEN;
			if (item == Items.COOKED_COD)
				return Foods.COD;
			if (item == Items.COD)
				return Foods.COOKED_COD;
			if (item == Items.COOKED_MUTTON)
				return Foods.MUTTON;
			if (item == Items.MUTTON)
				return Foods.COOKED_MUTTON;
			if (item == Items.COOKED_PORKCHOP)
				return Foods.PORKCHOP;
			if (item == Items.PORKCHOP)
				return Foods.COOKED_PORKCHOP;
			if (item == Items.COOKED_RABBIT)
				return Foods.RABBIT;
			if (item == Items.RABBIT)
				return Foods.COOKED_RABBIT;
			if (item == Items.COOKED_SALMON)
				return Foods.SALMON;
			if (item == Items.SALMON)
				return Foods.COOKED_SALMON;
		}
		
		return item.getFoodProperties(stack, entity);
	}
}
