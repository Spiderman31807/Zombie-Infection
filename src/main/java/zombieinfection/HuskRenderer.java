package zombieinfection;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.geom.ModelLayers;

import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public class HuskRenderer extends ZombieRenderer {
	private static final ResourceLocation HUSK_LOCATION = new ResourceLocation("textures/entity/zombie/husk.png");

	public HuskRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.HUSK, ModelLayers.HUSK_INNER_ARMOR, ModelLayers.HUSK_OUTER_ARMOR);
	}

	protected void scale(AbstractClientPlayer player, PoseStack pose, float scale) {
		float f = 1.0625F;
		pose.scale(1.0625F, 1.0625F, 1.0625F);
		super.scale(player, pose, scale);
	}

	public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
		return HUSK_LOCATION;
	}
}
