package zombieinfection;

import org.joml.Matrix4f;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.util.Mth;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.EntityModel;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public abstract class MobRenderer<M extends EntityModel<AbstractClientPlayer>> extends LivingEntityRenderer<AbstractClientPlayer, M> {
	public static final int LEASH_RENDER_STEPS = 24;

	public MobRenderer(EntityRendererProvider.Context p_174304_, M p_174305_, float p_174306_) {
		super(p_174304_, p_174305_, p_174306_);
	}

	protected boolean shouldShowName(AbstractClientPlayer player) {
		return super.shouldShowName(player) && player.shouldShowName();
	}

	public boolean shouldRender(AbstractClientPlayer p_115468_, Frustum p_115469_, double p_115470_, double p_115471_, double p_115472_) {
		if (super.shouldRender(p_115468_, p_115469_, p_115470_, p_115471_, p_115472_))
			return true;
		return false;
	}

	public void render(AbstractClientPlayer p_115455_, float p_115456_, float p_115457_, PoseStack p_115458_, MultiBufferSource p_115459_, int p_115460_) {
		super.render(p_115455_, p_115456_, p_115457_, p_115458_, p_115459_, p_115460_);
	}

	private static void addVertexPair(VertexConsumer p_174308_, Matrix4f p_254405_, float p_174310_, float p_174311_, float p_174312_, int p_174313_, int p_174314_, int p_174315_, int p_174316_, float p_174317_, float p_174318_, float p_174319_,
			float p_174320_, int p_174321_, boolean p_174322_) {
		float f = (float) p_174321_ / 24.0F;
		int i = (int) Mth.lerp(f, (float) p_174313_, (float) p_174314_);
		int j = (int) Mth.lerp(f, (float) p_174315_, (float) p_174316_);
		int k = LightTexture.pack(i, j);
		float f1 = p_174321_ % 2 == (p_174322_ ? 1 : 0) ? 0.7F : 1.0F;
		float f2 = 0.5F * f1;
		float f3 = 0.4F * f1;
		float f4 = 0.3F * f1;
		float f5 = p_174310_ * f;
		float f6 = p_174311_ > 0.0F ? p_174311_ * f * f : p_174311_ - p_174311_ * (1.0F - f) * (1.0F - f);
		float f7 = p_174312_ * f;
		p_174308_.vertex(p_254405_, f5 - p_174319_, f6 + p_174318_, f7 + p_174320_).color(f2, f3, f4, 1.0F).uv2(k).endVertex();
		p_174308_.vertex(p_254405_, f5 + p_174319_, f6 + p_174317_ - p_174318_, f7 - p_174320_).color(f2, f3, f4, 1.0F).uv2(k).endVertex();
	}
}
