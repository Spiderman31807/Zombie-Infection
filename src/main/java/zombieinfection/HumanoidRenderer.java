package zombieinfection;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.HumanoidModel;

@OnlyIn(Dist.CLIENT)
public abstract class HumanoidRenderer<T extends LivingEntity, M extends HumanoidModel<T>> extends LivingEntityRenderer<T, M> {
	public HumanoidRenderer(EntityRendererProvider.Context context, M model, float p_174171_) {
		this(context, model, p_174171_, 1, 1, 1);
	}

	public HumanoidRenderer(EntityRendererProvider.Context context, M model, float p_174175_, float p_174176_, float p_174177_, float p_174178_) {
		super(context, model, p_174175_);
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), p_174176_, p_174177_, p_174178_, context.getItemInHandRenderer()));
		this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
		this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
	}
}
