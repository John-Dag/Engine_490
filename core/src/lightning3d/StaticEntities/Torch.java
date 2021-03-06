package lightning3d.StaticEntities;

import lightning3d.Engine.Assets;
import lightning3d.Engine.SoundEffect;
import lightning3d.Engine.World;

import com.badlogic.gdx.math.Vector3;

public class Torch extends StaticEntity {
	char direction;
	
	public Torch() {
		super();
		this.direction = 'N';
	}

	public Torch(Vector3 position, char direction, int id, boolean isActive, boolean isRenderable) {
		super(position, id, isActive, isRenderable, false);
		this.setDirection(direction);
		this.setEffect(World.particleManager.getTorchPool().obtain());
		this.setSound(new SoundEffect(this.getPosition(), Assets.torchSound, 5f, 0.1f, true, true));
		World.soundManager.getSounds().add(this.getSound());
	}
	
	public void setRotations(char rotation) {
		switch (direction) {
			case('W'):
				this.getDecal().rotateZ(30f);
				break;
			case('E'):
				this.getDecal().rotateZ(-30f);
				break;
			case('N'):
				this.getDecal().rotateX(-30f);
				this.getDecal().rotateY(90f);
				break;
			case('S'):
				this.getDecal().rotateX(30f);
				this.getDecal().rotateY(90f);
				break;
		}
	}

	public char getDirection() {
		return direction;
	}

	public void setDirection(char direction) {
		this.direction = direction;
	}
}
