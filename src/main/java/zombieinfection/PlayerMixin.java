package zombieinfection.mixin;

import zombieinfection.ZombieType;
import zombieinfection.Infector;
import zombieinfection.Infectable;

import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;

@Mixin(Player.class)
public abstract class PlayerMixin implements Infectable, Infector {
	@Unique
	private static final EntityDataAccessor<Integer> ZombieVariant = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);

	@Override
	public ZombieType getInfectType() {
		return this.getInfection();
	}

	@Override
	public ZombieType getInfection() {
		return ZombieType.byId(this.entity().getEntityData().get(ZombieVariant));
	}

	@Override
	public void setInfection(ZombieType type) {
		if(this.getInfection() == type)
			return;
			
		this.entity().getEntityData().set(ZombieVariant, type.getId());
		Infectable.super.setInfection(type);
	}

	private boolean isSunBurnTick() {
		if (this.entity().level().isDay() && !this.entity().level().isClientSide) {
			if (this.entity().isInWaterRainOrBubble() || this.entity().isInPowderSnow || this.entity().wasInPowderSnow)
				return false;
			if (!this.entity().level().canSeeSky(BlockPos.containing(this.entity().getX(), this.entity().getEyeY(), this.entity().getZ())))
				return false;
			float light = this.entity().getLightLevelDependentMagicValue();
			if (light > 0.5 && this.entity().getRandom().nextFloat() * 30 < (light - 0.4) * 2)
				return true;
		}
		return false;
	}

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void defineSynchedData() {
		this.entity().getEntityData().define(ZombieVariant, 0);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void readAdditionalSaveData(CompoundTag compound) {
		if (compound.contains("ZombieVariant"))
			this.setInfection(ZombieType.byId(compound.getInt("ZombieVariant")));
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void addAdditionalSaveData(CompoundTag compound) {
		compound.putInt("ZombieVariant", this.getInfection().getId());
	}

	@Inject(method = "aiStep", at = @At("HEAD"))
	private void aiStep() {
		if (this.entity().isAlive() && this.isSunSensitive() && this.isSunBurnTick()) {
			ItemStack helmet = this.entity().getItemBySlot(EquipmentSlot.HEAD);
			if (!helmet.isEmpty()) {
				if (helmet.isDamageableItem()) {
					helmet.setDamageValue(helmet.getDamageValue() + this.entity().getRandom().nextInt(2));
					if (helmet.getDamageValue() >= helmet.getMaxDamage()) {
						this.entity().broadcastBreakEvent(EquipmentSlot.HEAD);
						this.entity().setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
					}
				}
			} else {
				this.entity().setSecondsOnFire(8);
			}
		}
	}
}
