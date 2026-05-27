package zombieinfection.mixins;

import zombieinfection.ZombieType;
import zombieinfection.Infector;
import zombieinfection.Infectable;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;

import net.minecraftforge.event.ForgeEventFactory;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.util.Mth;
import net.minecraft.tags.FluidTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.advancements.Advancement;

import java.util.UUID;
import net.minecraft.world.item.Items;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements Infectable, Infector {
	@Unique
	private static final EntityDataAccessor<Integer> ZombieVariant = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
	@Unique
	private static final EntityDataAccessor<Boolean> Curing = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
	@Unique
	private static final EntityDataAccessor<Boolean> Drowning = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
	@Unique
	private int inWaterTime;
	@Unique
	private int conversionTime;
	@Unique
	private int curingTime;
	@Unique
	private UUID curingUUID;
	@Shadow
	@Final
	private Abilities abilities;

	private PlayerMixin(EntityType<? extends LivingEntity> type, Level world) {
		super(type, world);
	}

	@Override
	public ZombieType getInfectType() {
		return this.getInfection();
	}

	@Override
	public ZombieType getInfection() {
		return ZombieType.byId(this.entityData.get(ZombieVariant));
	}

	@Override
	public void setInfection(ZombieType type) {
		if (this.getInfection() == type)
			return;

		this.entityData.set(ZombieVariant, type.getId());
		Infectable.super.setInfection(type);
	}

	private boolean isSunBurnTick() {
		if (this.level().isDay() && !this.level().isClientSide) {
			if (this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow)
				return false;
			if (!this.level().canSeeSky(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ())))
				return false;
			float light = this.getLightLevelDependentMagicValue();
			if (light > 0.5 && this.random.nextFloat() * 30 < (light - 0.4) * 2)
				return true;
		}
		return false;
	}

	@Override
	public boolean isConverting() {
		return this.entityData.get(Curing);
	}

	@Override
	public boolean isUnderWaterConverting() {
		return this.entityData.get(Drowning);
	}

	private void startUnderWaterConversion(int time) {
		this.conversionTime = time;
		this.entityData.set(Drowning, true);
	}

	private void doUnderWaterConversion() {
		this.inWaterTime = -1;
		this.entityData.set(Drowning, false);
		this.setInfection(this.getInfection().info().drownInto);
		if (!this.isSilent())
			this.level().levelEvent((Player) null, 1040, this.blockPosition(), 0);
	}

	private int getConversionProgress() {
		int advance = 1;
		if (this.random.nextFloat() < 0.01f) {
			int detected = 0;
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			for (int x = (int) this.getX() - 4; x < (int) this.getX() + 4 && detected < 14; ++x) {
				for (int y = (int) this.getY() - 4; y < (int) this.getY() + 4 && detected < 14; ++y) {
					for (int z = (int) this.getZ() - 4; z < (int) this.getZ() + 4 && detected < 14; ++z) {
						BlockState state = this.level().getBlockState(pos.set(x, y, z));
						if (state.is(Blocks.IRON_BARS) || state.getBlock() instanceof BedBlock) {
							if (this.random.nextFloat() < 0.3F)
								++advance;
							++detected;
						}
					}
				}
			}
		}
		return advance;
	}

	private void startConverting(UUID uuid, int time) {
		this.curingUUID = uuid;
		this.curingTime = time;
		this.entityData.set(Curing, true);
		this.removeEffect(MobEffects.WEAKNESS);
		this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, time, Math.min(this.level().getDifficulty().getId() - 1, 0)));
		this.level().broadcastEntityEvent(this, (byte) 16);
	}

	private void finishConversion(ServerLevel server) {
		if (this.curingUUID != null) {
			Player cureCause = server.getPlayerByUUID(this.curingUUID);
			if (cureCause instanceof ServerPlayer player) {
				Advancement advancement = player.server.getAdvancements().getAdvancement(new ResourceLocation("minecraft:story/cure_zombie_villager"));
				player.getAdvancements().award(advancement, "cured_zombie_villager");
			}
		}
		
		this.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
		server.levelEvent(null, 1027, this.blockPosition(), 0);
		this.entityData.set(Curing, false);
		this.setInfection(ZombieType.None);
		this.curingUUID = null;
	}

	@Override
	public void playStepSound(BlockPos pos, BlockState state) {
		SoundEvent stepSound = this.getInfection().info().stepSound;
		if (stepSound != null) {
			this.playSound(stepSound, 0.15f, 1f);
			return;
		}

		super.playStepSound(pos, state);
	}

	@Override
	public MobType getMobType() {
		return this.isInfected() ? MobType.UNDEAD : super.getMobType();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
		super.onSyncedDataUpdated(data);
		if (ZombieVariant.equals(data))
			this.refreshDimensions();
	}

	@Override
	protected void triggerItemUseEffects(ItemStack stack, int useTick) {
		if(this.isInfected() && !this.isConverting() && !this.level().isClientSide && stack.is(Items.GOLDEN_APPLE) && this.hasEffect(MobEffects.WEAKNESS)) {
            if (!this.abilities.instabuild)
               stack.shrink(1);
        	this.startConverting(this.getUUID(), this.random.nextInt(2401) + 3600);
		} else {
			super.triggerItemUseEffects(stack, useTick);
		}
	}

	//@Override
	//public void setSprinting(boolean sprint) {
		//super.setSprinting(sprint && this.canSprint());
	//}

	@Inject(method = "defineSynchedData()V", at = @At("TAIL"))
	private void defineSynchedData(CallbackInfo callback) {
		this.entityData.define(ZombieVariant, 0);
		this.entityData.define(Curing, false);
		this.entityData.define(Drowning, false);
	}

	@Inject(method = "handleEntityEvent(B)V", at = @At("HEAD"), cancellable = true)
	private void handleEntityEvent(byte event, CallbackInfo callback) {
		if (event != 16)
			return;

		if (!this.isSilent())
			this.level().playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, this.getSoundSource(), 1 + this.random.nextFloat(), this.random.nextFloat() * 0.7f + 0.3f, false);
		callback.cancel();
	}

	@Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	private void readAdditionalSaveData(CompoundTag compound, CallbackInfo callback) {
		if (compound.contains("ZombieVariant"))
			this.setInfection(ZombieType.byId(compound.getInt("ZombieVariant")));
		if (compound.contains("InWaterTime"))
			this.inWaterTime = compound.getInt("InWaterTime");
		if (compound.contains("DrownedConversionTime", 99) && compound.getInt("DrownedConversionTime") > -1)
			this.startUnderWaterConversion(compound.getInt("DrownedConversionTime"));
		if (compound.contains("ConversionTime", 99) && compound.getInt("ConversionTime") > -1)
			this.startConverting(compound.hasUUID("ConversionPlayer") ? compound.getUUID("ConversionPlayer") : null, compound.getInt("ConversionTime"));
	}

	@Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	private void addAdditionalSaveData(CompoundTag compound, CallbackInfo callback) {
		if (this.isInfected()) {
			compound.putInt("ZombieVariant", this.getInfection().getId());
			compound.putInt("InWaterTime", this.isInWater() ? this.inWaterTime : -1);
			compound.putInt("DrownedConversionTime", this.isUnderWaterConverting() ? this.conversionTime : -1);
			compound.putInt("ConversionTime", this.isConverting() ? this.curingTime : -1);
			if (this.curingUUID != null)
				compound.putUUID("ConversionPlayer", this.curingUUID);
		}
	}

	/*@Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("RETURN"))
	private void hurt(DamageSource source, float damage, CallbackInfoReturnable<Boolean> callback) {
		if (callback.getReturnValue() && this.level() instanceof ServerLevel server && source.getEntity() instanceof LivingEntity attacker) {
			int x = Mth.floor(this.getX());
			int y = Mth.floor(this.getY());
			int z = Mth.floor(this.getZ());
			if (attacker != null && this.level().getDifficulty() == Difficulty.HARD && (double) this.random.nextFloat() < this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).getValue()
					&& this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
				Zombie zombie = EntityType.ZOMBIE.create(this.level());
				for (int attempt = 0; attempt < 50; ++attempt) {
					int spawnX = x + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
					int spawnY = y + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
					int spawnZ = z + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
					BlockPos spawnPos = new BlockPos(spawnX, spawnY, spawnZ);
					EntityType<?> type = zombie.getType();
					if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.getPlacementType(type), this.level(), spawnPos, type) && SpawnPlacements.checkSpawnRules(type, server, MobSpawnType.REINFORCEMENT, spawnPos, this.level().random)) {
						zombie.setPos((double) spawnX, (double) spawnY, (double) spawnZ);
						if (!this.level().hasNearbyAlivePlayer((double) spawnX, (double) spawnY, (double) spawnZ, 7) && this.level().isUnobstructed(zombie) && this.level().noCollision(zombie)
								&& !this.level().containsAnyLiquid(zombie.getBoundingBox())) {
							zombie.setTarget(attacker);
							zombie.finalizeSpawn(server, this.level().getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.REINFORCEMENT, null, null);
							server.addFreshEntityWithPassengers(zombie);
							this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Zombie reinforcement caller charge", (double) -0.05F, AttributeModifier.Operation.ADDITION));
							zombie.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Zombie reinforcement callee charge", (double) -0.05F, AttributeModifier.Operation.ADDITION));
							break;
						}
					}
				}
			}
		}
	}*/

	@Inject(method = "doHurtTarget(Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
	private void doHurtTarget(Entity target, CallbackInfoReturnable<Boolean> callback) {
		if (callback.getReturnValue() == true && this.getMainHandItem().isEmpty()) {
			float difficulty = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
	      	if (this.getInfection() == ZombieType.Husk && target instanceof LivingEntity victim)
	         	victim.addEffect(new MobEffectInstance(MobEffects.HUNGER, 140 * (int)difficulty), this);
	      	
			if(this.isOnFire()) {
				if (this.random.nextFloat() < difficulty * 0.3f)
					target.setSecondsOnFire(2 * (int) difficulty);
			}
		}
	}

	@Inject(method = "getHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)Lnet/minecraft/sounds/SoundEvent;", at = @At("RETURN"), cancellable = true)
	private void getHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> callback) {
		if (this.getInfection() == ZombieType.Drowned && this.entity().isInWater())
			callback.setReturnValue(SoundEvents.DROWNED_HURT_WATER);
		SoundEvent hurtSound = this.getInfection().info().hurtSound;
		if (hurtSound != null)
			callback.setReturnValue(hurtSound);
	}

	@Inject(method = "getDeathSound()Lnet/minecraft/sounds/SoundEvent;", at = @At("RETURN"), cancellable = true)
	private void getDeathSound(CallbackInfoReturnable<SoundEvent> callback) {
		if (this.getInfection() == ZombieType.Drowned && this.entity().isInWater())
			callback.setReturnValue(SoundEvents.DROWNED_DEATH_WATER);
		SoundEvent deathSound = this.getInfection().info().deathSound;
		if (deathSound != null)
			callback.setReturnValue(deathSound);
	}

	@Inject(method = "getStandingEyeHeight(Lnet/minecraft/world/entity/Pose;Lnet/minecraft/world/entity/EntityDimensions;)F", at = @At("RETURN"), cancellable = true)
	private void getStandingEyeHeight(Pose pose, EntityDimensions hitbox, CallbackInfoReturnable<Float> callback) {
		if (this.isInfected() && this.getInfection().info().type != EntityType.PLAYER) {
			float eyeLevel = switch (pose) {
				default -> 1.74f;
				case CROUCHING -> 1.32f;
				case FALL_FLYING -> 0.42f;
				case SPIN_ATTACK -> 0.42f;
			};
			callback.setReturnValue(eyeLevel);
		}
	}

	@Inject(method = "getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;", at = @At("RETURN"), cancellable = true)
	private void getDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> callback) {
		if (this.isInfected() && this.getInfection().info().type != EntityType.PLAYER)
			callback.setReturnValue(dynamicHitbox(this.getInfection().info().type.getDimensions(), pose));
	}

	private static EntityDimensions dynamicHitbox(EntityDimensions hitbox, Pose pose) {
		float smallerSide = Math.min(hitbox.width, hitbox.height);
		EntityDimensions smallHitbox = EntityDimensions.scalable(smallerSide, smallerSide);
		EntityDimensions tinyHitbox = EntityDimensions.scalable(smallerSide * 0.666f, smallerSide * 0.666f);
		hitbox = switch (pose) {
			default -> hitbox;
			case FALL_FLYING -> smallHitbox;
			case SLEEPING -> tinyHitbox;
			case SPIN_ATTACK -> smallHitbox;
			case CROUCHING -> EntityDimensions.scalable(hitbox.width, hitbox.height * 0.833f);
			case DYING -> tinyHitbox;
		};
		return hitbox;
	}

	@Inject(method = "getMyRidingOffset()D", at = @At("RETURN"), cancellable = true)
	private void getMyRidingOffset(CallbackInfoReturnable<Double> callback) {
		if (this.isInfected())
			callback.setReturnValue(this.isBaby() ? 0 : -0.45);
	}

	@Inject(method = "canSprint()Z", at = @At("RETURN"), cancellable = true)
	private void canSprint(CallbackInfoReturnable<Boolean> callback) {
		if (!this.getInfection().info().allowSprint)
			callback.setReturnValue(false);
	}

	@Inject(method = "updateSwimming()V", at = @At("HEAD"), cancellable = true)
	private void updateSwimming(CallbackInfo callback) {
		if (this.abilities.flying)
			return;
		if (!this.getInfection().info().allowSwim) {
			this.setSwimming(false);
			callback.cancel();
			return;
		}
		if (!this.getInfection().info().allowSprint) {
			if (this.isSwimming()) {
				this.setSwimming((this.isInWater() || this.isInFluidType((fluidType, height) -> this.canSwimInFluidType(fluidType))) && !this.isPassenger());
			} else {
				this.setSwimming((this.isUnderWater() || this.canStartSwimming()) && !this.isPassenger());
			}
			callback.cancel();
			return;
		}
	}

	@Inject(method = "tick()V", at = @At("HEAD"))
	private void tick(CallbackInfo callback) {
		if (this.level() instanceof ServerLevel server && this.isAlive() && this.isConverting()) {
			int progress = this.getConversionProgress();
			this.curingTime -= progress;
			if (this.curingTime <= 0 && ForgeEventFactory.canLivingConvert(this, EntityType.VILLAGER, (timer) -> this.curingTime = timer))
				this.finishConversion(server);
		}

		if (!this.level().isClientSide && this.isAlive()) {
			if (this.isUnderWaterConverting()) {
				--this.conversionTime;
				if (this.conversionTime < 0 && ForgeEventFactory.canLivingConvert(this, EntityType.DROWNED, (timer) -> this.conversionTime = timer))
					this.doUnderWaterConversion();
			} else if (this.convertsInWater()) {
				if (this.isEyeInFluid(FluidTags.WATER)) {
					++this.inWaterTime;
					if (this.inWaterTime >= 600)
						this.startUnderWaterConversion(300);
				} else {
					this.inWaterTime = -1;
				}
			}
		}
	}

	@Inject(method = "aiStep()V", at = @At("HEAD"))
	private void aiStep(CallbackInfo callback) {
		if (this.isAlive() && this.getInfection().info().dayBurning && this.isSunBurnTick()) {
			ItemStack helmet = this.entity().getItemBySlot(EquipmentSlot.HEAD);
			if (!helmet.isEmpty()) {
				if (helmet.isDamageableItem()) {
					helmet.setDamageValue(helmet.getDamageValue() + this.getRandom().nextInt(2));
					if (helmet.getDamageValue() >= helmet.getMaxDamage()) {
						this.broadcastBreakEvent(EquipmentSlot.HEAD);
						this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
					}
				}
			} else {
				this.setSecondsOnFire(8);
			}
		}
	}
}
