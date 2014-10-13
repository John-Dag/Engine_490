package com.gdx.StaticEntities;

import com.badlogic.gdx.math.Vector3;
import com.gdx.engine.World;

public class Torch extends Static {
	char direction;
	
	public Torch() {
		super();
		this.direction = 'N';
	}

	public Torch(Vector3 position, char direction, int id, boolean isActive, boolean isRenderable) {
		super(position, id, isActive, isRenderable, false);
		this.setDirection(direction);
		this.setEffect(World.particleManager.getTorchPool().obtain());
	}
	
	public void setRotations(char rotation) {
		switch (direction) {
			case('W'):
				this.getDecal().rotateX(30f);
				this.getDecal().rotateY(-90f);
				break;
			case('E'):
				this.getDecal().rotateX(-30f);
				this.getDecal().rotateY(90f);
				break;
			case('N'):
				this.getDecal().rotateZ(30f);
				break;
			case('S'):
				this.getDecal().rotateZ(-30f);
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
