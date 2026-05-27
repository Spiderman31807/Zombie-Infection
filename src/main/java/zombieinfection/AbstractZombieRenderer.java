package zombieinfection;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.player.AbstractClientPlayer;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractZombieRenderer<M extends ZombieModel> extends HumanoidRenderer<AbstractClientPlayer, M> {
	private static final ResourceLocation ZOMBIE_LOCATION = new ResourceLocation("textures/entity/zombie/zombie.png");

	protected AbstractZombieRenderer(EntityRendererProvider.Context p_173910_, M p_173911_, M p_173912_, M p_173913_) {
		super(p_173910_, p_173911_, 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this, p_173912_, p_173913_, p_173910_.getModelManager()));
	}

	public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
		return ZOMBIE_LOCATION;
	}

	protected boolean isShaking(AbstractClientPlayer player) {
		return player instanceof Infectable infectable && infectable.isShaking();
	}

	protected void setupRotations(AbstractClientPlayer p_117802_, PoseStack p_117803_, float p_117804_, float p_117805_, float p_117806_) {
		if (p_117802_.isFallFlying()) {
			super.setupRotations(p_117802_, p_117803_, p_117804_, p_117805_, p_117806_);
			float f1 = (float) p_117802_.getFallFlyingTicks() + p_117806_;
			float f2 = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
			if (!p_117802_.isAutoSpinAttack()) {
				p_117803_.mulPose(Axis.XP.rotationDegrees(f2 * (-90.0F - p_117802_.getXRot())));
			}
			Vec3 vec3 = p_117802_.getViewVector(p_117806_);
			Vec3 vec31 = p_117802_.getDeltaMovementLerped(p_117806_);
			double d0 = vec31.horizontalDistanceSqr();
			double d1 = vec3.horizontalDistanceSqr();
			if (d0 > 0.0D && d1 > 0.0D) {
				double d2 = (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d0 * d1);
				double d3 = vec31.x * vec3.z - vec31.z * vec3.x;
				p_117803_.mulPose(Axis.YP.rotation((float) (Math.signum(d3) * Math.acos(d2))));
			}
		} else {
			super.setupRotations(p_117802_, p_117803_, p_117804_, p_117805_, p_117806_);
		}
	}
}
