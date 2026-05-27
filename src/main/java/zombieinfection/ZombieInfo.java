package zombieinfection;

import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.entity.EntityType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import com.google.common.base.Function;

public class ZombieInfo {
	public EntityType type = EntityType.ZOMBIE;
	public ZombieType drownInto = ZombieType.Drowned;
	public SoundEvent deathSound = SoundEvents.ZOMBIE_DEATH;
	public SoundEvent hurtSound = SoundEvents.ZOMBIE_HURT;
	public SoundEvent stepSound = SoundEvents.ZOMBIE_STEP;
	public Function<Object, Object> rendererGetter = null;
	public Object hand;
	public String textureId = "zombie";
	public String outerTextureId = null;
	public boolean dayBurning = true;
	public boolean allowSprint = false;
	public boolean allowSwim = false;

	public ZombieInfo() {
	}

	public ZombieInfo disable() {
		this.clearSounds();
		this.noBurn();
		this.sprinting();
		this.swimming();
		if (FMLEnvironment.dist != Dist.DEDICATED_SERVER)
			this.renderer((Function) null);
		this.drown(null);
		return this;
	}

	public ZombieInfo refresh() {
		if (FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
			this.renderer(this.type);
			this.refreshHandData();
		}
		return this;
	}

	public ZombieInfo type(EntityType type) {
		return this.type(type, true);
	}

	public ZombieInfo type(EntityType type, boolean changeRenderer) {
		this.type = type;
		if (changeRenderer && FMLEnvironment.dist != Dist.DEDICATED_SERVER)
		    this.renderer(type);
		return this;
	}

	public ZombieInfo drown(ZombieType type) {
		this.drownInto = type;
		return this;
	}

	public ZombieInfo noBurn() {
		this.dayBurning = false;
		return this;
	}

	public ZombieInfo sprinting() {
		this.allowSprint = true;
		return this;
	}

	public ZombieInfo swimming() {
		this.allowSwim = true;
		return this;
	}

	@OnlyIn(Dist.CLIENT)
	private ZombieInfo renderer(EntityType type) {
		if (type == EntityType.ZOMBIE)
			this.renderer((context) -> new ZombieRenderer((EntityRendererProvider.Context) context));
		if (type == EntityType.HUSK)
			this.renderer((context) -> new HuskRenderer((EntityRendererProvider.Context) context));
		if (type == EntityType.DROWNED)
			this.renderer((context) -> new DrownedRenderer((EntityRendererProvider.Context) context));
		return this;
	}

	@OnlyIn(Dist.CLIENT)
	private ZombieInfo renderer(Function<Object, Object> renderFunc) {
		this.rendererGetter = renderFunc;
		return this;
	}

	public ZombieInfo texture(String id, boolean outer) {
		if (!outer)
			this.textureId = id;
		if (outer)
			this.outerTextureId = id;
		if (FMLEnvironment.dist != Dist.DEDICATED_SERVER)
		    this.refreshHandData();
		return this;
	}

	public ZombieInfo clearSounds() {
		this.deathSound = null;
		this.hurtSound = null;
		this.stepSound = null;
		return this;
	}

	public ZombieInfo death(SoundEvent sound) {
		this.deathSound = sound;
		return this;
	}

	public ZombieInfo hurt(SoundEvent sound) {
		this.hurtSound = sound;
		return this;
	}

	public ZombieInfo step(SoundEvent sound) {
		this.stepSound = sound;
		return this;
	}

	@OnlyIn(Dist.CLIENT)
	public AbstractZombieRenderer createRenderer(EntityRendererProvider.Context context) {
		return this.rendererGetter == null ? null : (AbstractZombieRenderer) this.rendererGetter.apply(context);
	}

	public ResourceLocation getTexture(boolean outerLayer) {
		if (outerLayer)
			return this.outerTextureId == null ? null : new ResourceLocation("textures/entity/zombie/" + this.outerTextureId + ".png");
		return new ResourceLocation("textures/entity/zombie/" + this.textureId + ".png");
	}

	@OnlyIn(Dist.CLIENT)
	private ZombieInfo refreshHandData() {
		this.hand = this.getHandData();
		return this;
	}

	@OnlyIn(Dist.CLIENT)
	public HandData getHand() {
		return (HandData) hand;
	}

	@OnlyIn(Dist.CLIENT)
	private HandData getHandData() {
		HandData hand = new HandData();
		hand.texture = this.getTexture(false);
		hand.outerTexture = this.getTexture(true);
		hand.setScale(1f, 1f, 1f);
		hand.setPosition(5.25f, 21.5f, -1.1f);
		hand.setRotation(3.2f, 1.6f, 0.15f);
		return hand;
	}
}
