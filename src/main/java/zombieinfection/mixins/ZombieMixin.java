package zombieinfection.mixins;

import zombieinfection.ZombieType;
import zombieinfection.Infector;
import zombieinfection.Infectable;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;

@Mixin(Zombie.class)
public abstract class ZombieMixin extends Monster implements Infector {
	private ZombieMixin(EntityType<? extends Monster> type, Level world) {
		super(type, world);
	}

	@Override
	public ZombieType getInfectType() {
		EntityType type = ((Entity) (Object) this).getType();
		if (type == EntityType.HUSK)
			return ZombieType.Husk;
		if (type == EntityType.DROWNED)
			return ZombieType.Drowned;
		if (type == EntityType.ZOMBIE)
			return ZombieType.Default;
		return ZombieType.Default;
	}

	@Override
	public boolean isAlliedTo(Entity entity) {
		if (entity instanceof Infectable infectable && infectable.isInfected())
			return true;
		return super.isAlliedTo(entity);
	}
}
