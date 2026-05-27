package zombieinfection;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.geom.ModelPart;

@OnlyIn(Dist.CLIENT)
public class ZombieModel extends AbstractZombieModel {
	public ZombieModel(ModelPart part) {
		super(part);
	}

	public boolean isAggressive(AbstractClientPlayer player) {
		return player instanceof Infectable infectable && infectable.isAggressive();
	}
}
