package lightning3d.Powerups;

import lightning3d.Engine.World;
import lightning3d.StaticEntities.PowerUp;
import lightning3d.StaticEntities.PowerUp.powerUpTypeEnum;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class SpeedPowerUp extends PowerUp {
	private float duration; // Length of time power-up lasts (in seconds).
	private final float SPEEDBOOST = 1.5f;
	private final float DEFAULTSPEED = 1f;
	private Model model;
	
	public SpeedPowerUp() {
		super();
		duration = 10;
	}
	
	public SpeedPowerUp(Vector3 position, int id, boolean isActive, boolean isRenderable, Model model, float duration, powerUpTypeEnum type) {
		super(position, id , isActive, isRenderable, model, duration, type);
		this.duration = duration;
		this.model = model;
		
		BoundingBox temp = new BoundingBox();
		this.getModel().calculateBoundingBox(temp);
		this.setBoundingBox(temp);
		this.getModel().transform.setToTranslation(this.getPosition());
		this.getModel().transform.scale(0.05f, 0.05f, 0.05f);
		this.getModel().transform.rotate(new Vector3(1,0,0), 90);
	}
	
	// Boosts player speed for duration.
	@Override
	public void effect() {
		World.player.setSpeedScalar(SPEEDBOOST);
		Timer.schedule(new Task() {
			public void run() { 
				World.player.setSpeedScalar(DEFAULTSPEED);
			}
		}, duration);
	}
	
	// Need to change model to some sort of power-up.
	@Override
	public PowerUp spawn() {
		SpeedPowerUp speedBoost = new SpeedPowerUp(this.getPosition().cpy(), 2, true, true, model, duration, powerUpTypeEnum.speedBoost);
		BoundingBox temp = new BoundingBox();
		speedBoost.getModel().calculateBoundingBox(temp);
		speedBoost.setBoundingBox(temp);
		speedBoost.getModel().transform.setToTranslation(speedBoost.getPosition());
		speedBoost.getModel().transform.scale(0.05f, 0.05f, 0.05f);
		speedBoost.getModel().transform.rotate(new Vector3(1,0,0), 90);
		return speedBoost;
	}
}