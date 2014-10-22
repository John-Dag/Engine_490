package com.gdx.engine;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.gdx.DynamicEntities.Player;
import com.gdx.DynamicEntities.Projectile;
import com.gdx.DynamicEntities.Weapon;
import com.gdx.DynamicEntities.Enemy;

public class World {
	public static final float PLAYER_SIZE = 0.2f;
	public static Player player;
	//private Enemy enemy;
	//private Level level;
	private MeshLevel meshLevel;
	private Ray ray;
	private Array<BoundingBox> boxes;
	private float timer;
	private Vector3 position;
	private Vector3 out;
	public static ParticleManager particleManager;
    //private TiledMapTileLayer layer = (TiledMapTileLayer)Assets.level2.getLayers().get(0);
    DistanceTrackerMap distanceMap = new DistanceTrackerMap((TiledMapTileLayer)Assets.castle.getLayers().get(0), 400);
	
	public World() {
		player = new Player(this, 100, null, 2, true, true, new Vector3(2f, 1.5f, 2f), new Vector3(0, 0, 0), new Vector3(0, 0, 0), 
						    new Vector3(0, 0, 0), new Vector3(0, 0, 0), new ModelInstance(Assets.modelBuilder.createBox(1f, 1f, 1f, 
						    Assets.floorMat, Usage.Position | Usage.Normal | Usage.TextureCoordinates)));
		particleManager = new ParticleManager(this);
		meshLevel = new MeshLevel(Assets.castle, true);
		Entity.entityInstances.add(player);
		//meshLevel.getInstances().add(player.model);
		boxes = new Array<BoundingBox>();
		position = new Vector3();
		out = new Vector3();
	}
	
	public void update(float delta) {
		rayPick();
		updateEntities(delta);
		updateEnemies(delta);
		timer += delta;
	}
	
	public void initializeEntities() {
		for (Entity entity : Entity.entityInstances) {
			entity.initialize(this);
		}
	}
	
	private void updateEntities(float delta) {
		int size = Entity.entityInstances.size;
		
		for (int i = 0; i < size; i++) {
			Entity entity = Entity.entityInstances.get(i);
			
			if (entity.isActive()) {
				entity.update(delta);
			}
			
			else {
				//System.out.println("Removed: " + size);
				entity.removeEntity(i);
				size -= 1;
			}
		}
	}
	private void updateEnemies(float delta) {
		int size = Entity.entityInstances.size;
		
		for (int i = 0; i < size; i++) {
			Entity entity = Entity.entityInstances.get(i);
			if (entity instanceof Enemy) {
				final Enemy enemy = (Enemy)entity;
				GridPoint2 enemyPosition = new GridPoint2((int)enemy.getPosition().x, (int)enemy.getPosition().z);
				GridPoint2 playerPosition = new GridPoint2((int)player.camera.position.x, (int)player.camera.position.z);
				TiledMapTileLayer layer = (TiledMapTileLayer)getMeshLevel().getTiledMap().getLayers().get(0);
				int width = layer.getWidth();
				ArrayList<Integer> path;
				
				if (enemy.getStateMachine().Current == enemy.idle) {
					enemy.getAnimation().setAnimation("Idle", -1);
                    enemy.getVelocity().set(0,0,0);
				}

				else if (enemy.getStateMachine().Current == enemy.moving) {
                    try {
                        path = enemy.shortestPath(enemyPosition.x + width
                                * enemyPosition.y, playerPosition.x + width
                                * playerPosition.y, layer, distanceMap);
                    } catch (Exception ex) {
                        path = new ArrayList<Integer>();
                    }
                    if (path.size() == 0)
                        return;
                    enemy.getAnimation().setAnimation("Walking", -1);
                    Vector3 vel = new Vector3();
                    int y = path.get(0) / width;
                    int x = path.get(0) % width;
                    vel.x = x - enemyPosition.x;
                    vel.z = y - enemyPosition.y;

                    if (vel.x == 0 && vel.z == 0 && path.size() > 1) {
                        y = path.get(1) / width;
                        x = path.get(1 ) % width;
                        vel.x = x - enemyPosition.x;
                        vel.z = y - enemyPosition.y;
                    }
                    vel.y = 0;
                    vel.nor();
                    vel.scl(2f);
                    Vector2 angleVector = new Vector2(vel.z, vel.x);
                    enemy.getRotation().x = angleVector.angle();// +90 because
                    // model is
                    // originally 90
                    // degrees off
                    // when loaded
                    enemy.getVelocity().set(vel);
                    Vector3 collisionVector = getMeshLevel()
                            .checkCollision(enemy.getPosition(),
                                    enemy.getNewPosition(delta), 1.6f, 1.6f,
                                    1.6f);
                    enemy.getVelocity().set(enemy.getVelocity().x
                            * collisionVector.x, enemy.getVelocity().y
                            * collisionVector.y, enemy.getVelocity().z
                            * collisionVector.z);


                    float targetHeight = getMeshLevel().getHeightOffset()
                            + getMeshLevel().mapHeight(
                            enemy.getPosition().x, enemy.getPosition().z);
                    if (enemy.getPosition().y > targetHeight + 30 * delta) {
                        enemy.getPosition().y -= 30 * delta;

                    } else if (enemy.getPosition().y < targetHeight) {
                        enemy.getPosition().y = targetHeight;

                    } else {
                        enemy.getPosition().y = getMeshLevel()
                                .getHeightOffset()
                                + getMeshLevel().mapHeight(
                                enemy.getPosition().x,
                                enemy.getPosition().z);
                    }
				}
				
				else if (enemy.getStateMachine().Current == enemy.spawn) {
					enemy.getAnimation().setAnimation("Reincarnating", 1, new AnimationListener() {
						
						@Override
					public void onLoop(AnimationDesc animation) {
							// TODO Auto-generated method stub
							
					}
						
					@Override
					public void onEnd(AnimationDesc animation) {
						enemy.setSpawned(true);
						enemy.getStateMachine().Current = enemy.idle;
					}
				});
				}
				
				else if (enemy.getStateMachine().Current == enemy.attack){
					enemy.getAnimation().setAnimation("Attacking", -1);
					World.player.takeDamage(enemy.getDamage());
				}
				
				else {
					enemy.getVelocity().set(0, 0, 0);
					enemy.getAnimation().setAnimation("Dying", 1, new AnimationListener() {
							
							@Override
						public void onLoop(AnimationDesc animation) {
								// TODO Auto-generated method stub
								
						}
							
						@Override
						public void onEnd(AnimationDesc animation) {
							enemy.setIsActive(false);
						}
					});
				}
			}
		}
	}
	
	//Bounding boxes used for frustum culling
	public void createBoundingBoxes() {
		int size = meshLevel.getInstances().size;
		
		for (int i = 0; i < size; i++) {
			BoundingBox box = new BoundingBox();
			meshLevel.getInstances().get(i).calculateBoundingBox(box);
			boxes.add(box);
		}
	}
	
	//Temporary
	public void rayPick() {
		if (player.isMouseLeft() && player.getWeapon() != null && 
			timer >= player.getWeapon().getFiringDelay()) {
			ray = player.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
			timer = 0;
			rayPickLevel();
			rayPickEntities();
			fireWeapon();
		}
	}
	
	public void rayPickLevel() {
		int size = meshLevel.getInstances().size;
		int result = -1;
		float distance = -1;
		
		if (ray != null) {
			for (int i = 0; i < size; i++) {
				ModelInstance model = meshLevel.getInstances().get(i);
				
				model.transform.getTranslation(position);
				position.add(boxes.get(i).getCenter());
				float dist2 = ray.origin.dst2(position);
				
				if (distance >= 0f && dist2 > distance)
					continue;
		
				if (Intersector.intersectRayBoundsFast(ray, boxes.get(i))) {
					result = i;
					distance = dist2;
					
					if (result > -1) {
						Intersector.intersectRayBounds(ray, boxes.get(i), out);
					}
				}
			}
		}
	}
	
	public void rayPickEntities() {
		int size = Entity.entityInstances.size;
		int result = -1;
		float distance = -1;
		Enemy enemy;
		
		if (ray != null) {
			for (int i = 0; i < size; i++) {
				Entity entity = Entity.entityInstances.get(i);
				if (entity instanceof Enemy) {
					enemy = (Enemy)entity;
					enemy.getModel().transform.getTranslation(position);
					position.add(enemy.getTransformedBoundingBox().getCenter());
					float dist2 = ray.origin.dst2(position);
					
					if (distance >= 0f && dist2 > distance)
						continue;
			
					if (Intersector.intersectRayBoundsFast(ray, enemy.getTransformedBoundingBox())) {
						result = i;
						distance = dist2;
						
						if (result > -1) {
							Intersector.intersectRayBounds(ray, enemy.getTransformedBoundingBox(), out);
						}
					}
				}
			}
		}
	}
	
	//Temporary
	public void fireWeapon() {
		if (player.getWeapon().isParticleWeapon()) {
			Vector3 rotation = new Vector3(0, 0, 0);
			Vector3 scale = new Vector3(0, 0, 0);
			
			//position, rotation, scale, angVelocity, velocity, angAccel, acceleration, active, index, collision
			Projectile projectile = new Projectile(6, true, true, player.camera.position.cpy(), 
												   rotation, scale, player.camera.direction.cpy(), player.camera.direction.cpy(), 
												   10, 0.1f, particleManager.mistPool.obtain(), this);
			Entity.entityInstances.add(projectile);
		}
	}
	
	/*
	public void setDecals() {
		Decal decal = Decal.newDecal(Assets.test1, true);
		decal.setPosition(out);
		decal.lookAt(player.camera.position, player.camera.position.cpy().nor());
		decal.setScale(0.001f);
		decal.value = 6;
		decalInstances.add(decal);
	}
	*/
	
	public Player getPlayer() {
		return player;
	}
	
	public Array<BoundingBox> getBoundingBoxes() {
		return boxes;
	}
	
	public MeshLevel getMeshLevel() {
		return meshLevel;
	}
	
	public Ray getRay() {
		return ray;
	}
	
	public Vector3 getOut() {
		return out;
	}
}
