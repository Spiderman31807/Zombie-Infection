package zombieinfection;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.AnimationUtils;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractZombieModel extends HumanoidModel<AbstractClientPlayer> {
	protected AbstractZombieModel(ModelPart model) {
		super(model);
	}

	public void setupAnim(AbstractClientPlayer player, float p_102002_, float p_102003_, float p_102004_, float p_102005_, float p_102006_) {
		boolean flying = player.isFallFlying() || player.isAutoSpinAttack();
		this.crouching = !flying && player.isCrouching();
		super.setupAnim(player, p_102002_, p_102003_, p_102004_, p_102005_, p_102006_);
		if(!flying)
			AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, this.isAggressive(player), this.attackTime, p_102004_);
	}

	public abstract boolean isAggressive(AbstractClientPlayer player);
}
