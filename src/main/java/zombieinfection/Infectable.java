package zombieinfection;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;

public interface Infectable {
	abstract void setHealth(float health);

	abstract float getMaxHealth();

	default LivingEntity entity() {
		return (LivingEntity) this;
	}

	default boolean isInfected() {
		ZombieType type = this.getInfection();
		return type != null && type != ZombieType.None;
	}

	default ZombieType getInfection() {
		return ZombieType.None;
	}

	default void setInfection(ZombieType type) {
		this.handleAttributes(this.entity().level().getCurrentDifficultyAt(this.entity().blockPosition()).getSpecialMultiplier());
		if (type != ZombieType.None)
			this.clearZombieAttackers();
	}

	default void clearZombieAttackers() {
		for (Zombie zombie : this.entity().level().getEntitiesOfClass(Zombie.class, this.entity().getBoundingBox().inflate(32))) {
			if(zombie.getTarget() == this.entity())
				zombie.setTarget(null);
		}
	}

	default boolean infect(Infector infector) {
		if (this.isInfected())
			return false;
		ZombieType type = infector.getInfectType();
		if (type == null || type == ZombieType.None)
			return false;
		this.setHealth(this.getMaxHealth());
		this.setInfection(type);
		return true;
	}

	default boolean isUnderWaterConverting() {
		return false;
	}

	default boolean isConverting() {
		return false;
	}

	default boolean isAggressive() {
		return false;
	}

	default boolean isShaking() {
		return this.isUnderWaterConverting() || this.isConverting();
	}

	default boolean convertsInWater() {
		return !this.isConverting() && this.getInfection().info().drownInto != null;
	}

	default boolean isSunSensitive() {
		return this.getInfection().info().dayBurning;
	}

	default void handleAttributes(float specialMultiplier) {
		AttributeSupplier supplier = (this.getInfection() != ZombieType.None ? Zombie.createAttributes() : Player.createAttributes()).build();
		AttributeMap attributes = this.entity().getAttributes();
		for (Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
			if (!attributes.hasAttribute(attribute) || !supplier.hasAttribute(attribute))
				continue;
			double value = supplier.getBaseValue(attribute);
			if (this.getInfection() != ZombieType.None && attribute == Attributes.MOVEMENT_SPEED)
				value /= 4;
			attributes.getInstance(attribute).setBaseValue(value);
		}

		if (!this.isInfected())
			return;
		this.randomizeReinforcementsChance();
		attributes.getInstance(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(new AttributeModifier("Random spawn bonus", this.entity().getRandom().nextDouble() * (double) 0.05f, AttributeModifier.Operation.ADDITION));
		if (this.entity().getRandom().nextFloat() < specialMultiplier * 0.05f) {
			//attributes.getInstance(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.entity().getRandom().nextDouble() * 0.25 + 0.5, AttributeModifier.Operation.ADDITION));
			attributes.getInstance(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.entity().getRandom().nextDouble() * 3 + 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
		}
	}

	default void randomizeReinforcementsChance() {
		//this.entity().getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(this.entity().getRandom().nextDouble() * ForgeConfig.SERVER.zombieBaseSummonChance.get());
	}
}
