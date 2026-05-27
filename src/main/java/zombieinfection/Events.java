package zombieinfection;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.vertex.PoseStack;

@Mod.EventBusSubscriber
public class Events {
	@OnlyIn(Dist.CLIENT)
	private static EntityRendererProvider.Context getContext() {
		Minecraft minecraft = Minecraft.getInstance();
		return new EntityRendererProvider.Context(minecraft.getEntityRenderDispatcher(), minecraft.getItemRenderer(), minecraft.getBlockRenderer(), minecraft.gameRenderer.itemInHandRenderer, minecraft.getResourceManager(),
				minecraft.getEntityModels(), minecraft.font);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void playerRender(RenderPlayerEvent.Pre event) {
		Player player = event.getEntity();
		if (player instanceof Infectable infectable) {
			ZombieType infection = infectable.getInfection();
			if (infection == null || infection.info().rendererGetter == null)
				return;

			AbstractZombieRenderer renderer = infection.info().createRenderer(getContext());
			renderer.render((AbstractClientPlayer) player, 0, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void armRender(RenderArmEvent event) {
		Player player = event.getPlayer();
		if (player instanceof Infectable infectable) {
			ZombieType infection = infectable.getInfection();
			if (infection == null || infection.info().getHand() == null)
				return;
			PoseStack pose = event.getPoseStack();
			int packedLight = event.getPackedLight();
			MultiBufferSource buffer = event.getMultiBufferSource();
			EntityRendererProvider.Context context = getContext();
			HandRenderer renderer = new HandRenderer(context, new HandModel(context.bakeLayer(ModelLayers.ZOMBIE), false));
			renderer.render((AbstractClientPlayer) player, 0, 0, pose, buffer, packedLight);
			if (infection.info().getHand().outerTexture != null) {
				HandRenderer outer = new HandRenderer(context, new HandModel(context.bakeLayer(ModelLayers.ZOMBIE), true));
				outer.render((AbstractClientPlayer) player, 0, 0, pose, buffer, packedLight);
			}
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void attacked(LivingHurtEvent event) {
		if (event.getEntity() instanceof Infectable infectable && infectable.entity().getHealth() <= event.getAmount()) {
			if (event.getSource().getEntity() instanceof LivingEntity attacker) {
				if (attacker.getMainHandItem().isEmpty() && attacker instanceof Infector infector && infectable.infect(infector))
					event.setCanceled(true);
			}
		}
	}
}
