package lightning3d.Abilities;

import lightning3d.Engine.World;

import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector3;

public class PoisonCloud extends AOETarget {
	private final int ABILITY_DAMAGE = 1;
	private static final float ABILITY_DURATION = 15f;
	private final int ABILITY_SIZE = 3;
	private static final int ABILITY_COOLDOWN = 10;
	private static final float ABILITY_MAX_TARGET_DISTANCE = 20f;
	
	public PoisonCloud() {
		super();
	}
	
	public PoisonCloud(int id, boolean isActive, boolean isRenderable, Vector3 position, Decal decal) {
		super(id, isActive, isRenderable, position, ABILITY_DURATION, ABILITY_COOLDOWN, decal);
		this.setDamage(ABILITY_DAMAGE);
		this.setSize(ABILITY_SIZE);
		this.setMaxTargetingDistance(ABILITY_MAX_TARGET_DISTANCE);
		this.setParticleEffect(World.particleManager.poisonPool.obtain());
		this.setPoolRef(World.particleManager.poisonPool);
	}
}
