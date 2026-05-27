package zombieinfection;

public interface Infector {
	default ZombieType getInfectType() {
		return ZombieType.None;
	}
}
