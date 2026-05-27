package zombieinfection;

import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.ByIdMap;
import net.minecraft.sounds.SoundEvents;

import java.util.function.IntFunction;

public enum ZombieType {
	None(new ZombieInfo().disable().type(EntityType.PLAYER, false)),
	//Converted(new ZombieInfo().sprinting().swimming().renderer(null).clearSounds().type(EntityType.PLAYER, false)),
	Drowned(new ZombieInfo().type(EntityType.DROWNED).death(SoundEvents.DROWNED_DEATH).hurt(SoundEvents.DROWNED_HURT).step(SoundEvents.DROWNED_STEP).swimming().texture("drowned", false).texture("drowned_outer_layer", true)),
	Default(new ZombieInfo().refresh()),
	Husk(new ZombieInfo().type(EntityType.HUSK).drown(Default).noBurn().death(SoundEvents.HUSK_DEATH).hurt(SoundEvents.HUSK_HURT).step(SoundEvents.HUSK_STEP).texture("husk", false));

	private static final IntFunction<ZombieType> MappedID = ByIdMap.continuous(ZombieType::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	private final ZombieInfo info;
	private final int id;

	private ZombieType(ZombieInfo info) {
		this.id = this.ordinal();
		this.info = info;
	}

	public static ZombieType byId(int id) {
		return MappedID.apply(id);
	}

	public String getName() {
		return this.toString();
	}

	public ZombieInfo info() {
		return this.info;
	}

	public int getId() {
		return this.id;
	}
}
