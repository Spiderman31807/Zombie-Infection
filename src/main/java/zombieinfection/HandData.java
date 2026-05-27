package zombieinfection;

import org.joml.Vector3f;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.resources.ResourceLocation;

@OnlyIn(Dist.CLIENT)
public class HandData {
	public boolean enabled = false;
	public ResourceLocation texture;
	public ResourceLocation outerTexture;
	public Vector3f position = new Vector3f();
	public Vector3f rotation = new Vector3f();
	public Vector3f scale = new Vector3f(1);

	public HandData() {
	}

	public void setPosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
	}

	public void setRotation(float x, float y, float z) {
		rotation.x = x;
		rotation.y = y;
		rotation.z = z;
	}

	public void setScale(float x, float y, float z) {
		scale.x = x;
		scale.y = y;
		scale.z = z;
	}
}
