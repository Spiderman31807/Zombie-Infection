package zombieinfection;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.geom.ModelLayers;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public class DrownedRenderer extends AbstractZombieRenderer<DrownedModel> {
	private static final ResourceLocation DROWNED_LOCATION = new ResourceLocation("textures/entity/zombie/drowned.png");

	public DrownedRenderer(EntityRendererProvider.Context context) {
		super(context, new DrownedModel(context.bakeLayer(ModelLayers.DROWNED)), new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_INNER_ARMOR)), new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_OUTER_ARMOR)));
		this.addLayer(new DrownedOuterLayer(this, context.getModelSet()));
	}

	public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
		return DROWNED_LOCATION;
	}

	protected void setupRotations(AbstractClientPlayer player, PoseStack pose, float p_114111_, float p_114112_, float p_114113_) {
		super.setupRotations(player, pose, p_114111_, p_114112_, p_114113_);
		float f = player.getSwimAmount(p_114113_);
		if (f > 0.0F) {
			float f1 = -10.0F - player.getXRot();
			float f2 = Mth.lerp(f, 0.0F, f1);
			pose.rotateAround(Axis.XP.rotationDegrees(f2), 0.0F, player.getBbHeight() / 2.0F, 0.0F);
		}
	}
}
