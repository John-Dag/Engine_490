package lightning3d.Network;

import lightning3d.DynamicEntities.Player;
import lightning3d.DynamicEntities.Projectile;
import lightning3d.Engine.Assets;
import lightning3d.Engine.ClientEvent;
import lightning3d.Engine.Entity;
import lightning3d.Engine.EntityManager;
import lightning3d.Engine.MeshLevel;
import lightning3d.Engine.ParticleManager;
import lightning3d.Engine.SoundManager;
import lightning3d.Engine.World;
import lightning3d.Network.Net.PlayerPacket;
import lightning3d.Shaders.ColorMultiplierEntityShader;
import lightning3d.Weapons.RocketLauncher;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class NetWorld extends World {
	public NetWorld() {
		Assets.loadModels();
		player = new Player(this, 100, null, 2, true, true, startVector, new Vector3(0, 0, 0), new Vector3(0, 0, 0), 
							new Vector3(0, 0, 0), new Vector3(0, 0, 0), null);
		setPlayer(player);
		//playerInstances.add(player);
		particleManager = new ParticleManager(this);
		player.initAbilities();
		player.initWeapons();
		setMeshLevel(new MeshLevel(Assets.rift, true, this));
		Entity.entityInstances.add(player);
		//distanceMap = new DistanceTrackerMap(getMeshLevel(), 2 + 32 * 2);
		entityManager = new EntityManager(this);
		setNetEventManager(new NetClientEventManager(this));
		setServerEventManager(new NetServerEventManager(this));
//		RocketLauncher launcher = (RocketLauncher) new RocketLauncher().spawn(getPlayer().getPosition());
//		Weapon noWeapon = new Weapon();
//		getPlayer().setWeapon(noWeapon);
	}

	@Override
	public void updatePlayers(PlayerPacket packet) {
		for (int i = 0; i < playerInstances.size; i++) {
			if (this.playerInstances.get(i).getNetId() == packet.id) {
				//System.out.println(packet.id);
				playerInstances.get(i).camera.position.set(packet.position);
				playerInstances.get(i).camera.direction.set(packet.direction);
				//Check this out later. 
				playerInstances.get(i).setPosition(playerInstances.get(i).camera.position);
				playerInstances.get(i).setTarget(playerInstances.get(i).getTarget().idt());
				playerInstances.get(i).setTarget(playerInstances.get(i).getTarget().translate(playerInstances.get(i).getPosition()).translate(0, .5f, 0));
				playerInstances.get(i).getBulletObject().setWorldTransform(playerInstances.get(i).getTarget());
			}
		}
	}
	
	@Override
	public void updateProjectiles(Net.ProjectilePacket packet) {
		for (int i = 0; i < Entity.entityInstances.size; i++) {
			if (Entity.entityInstances.get(i) instanceof Projectile) {
				Projectile projectile = (Projectile)Entity.entityInstances.get(i);
				//System.out.println(" " + projectile.getNetId() + " " + packet.id);
				if (projectile.getNetId() == packet.id) {
					projectile.getPosition().set(packet.position);
					//System.out.println(packet.position);
				}
			}
		}
	}

	@Override
	public void addProjectile(Net.NewProjectile packet) {
		Projectile projectile = NetWorld.entityManager.projectilePool.obtain();
		projectile.reset();
		projectile.setDamage(RocketLauncher.DAMAGE);
		projectile.setPosition(packet.position);
		projectile.setNetId(packet.id);
		projectile.setOriginID(packet.originID);
		projectile.getBulletBody().setWorldTransform(projectile.calculateTarget(packet.cameraPos));
		projectile.getBulletBody().setContactCallbackFilter(World.PLAYER_FLAG);
		Ray ray = new Ray(packet.rayOrigin, packet.rayDirection);
		projectile.getBulletBody().applyCentralImpulse(ray.direction.scl(RocketLauncher.PROJECTILE_SCALAR));
		ClientEvent.CreateEntity event = new ClientEvent.CreateEntity(projectile);
		getClientEventManager().addEvent(event);
	}
	
	@Override
	public void sendProjectilePositionUpdate(Projectile projectile) {
		Net.ProjectilePacket packet = new Net.ProjectilePacket();
		packet.position = projectile.getPosition();
		packet.id = projectile.getNetId();
		this.getServer().updateProjectiles(packet);
	}
	
	@Override
	public void addPlayer(Net.NewPlayer playerPacket) {
		try {
			Player player1 = new Player(this, 100, null, 2, true, true, new Vector3(2f, 1.5f, 2f), new Vector3(0, 0, 0), new Vector3(0, 0, 0), 
					new Vector3(0, 0, 0), new Vector3(0, 0, 0), new ModelInstance(Assets.manager.get("zombie_fast.g3db", Model.class)));
			player1.setAnimation(new AnimationController(player1.getModel()));
			GridPoint2 playerPos = new GridPoint2();
			playerPos.set(getMeshLevel().getStartingPoint());
			player1.camera.position.set(playerPacket.position.x, playerPacket.position.y - .5f, playerPacket.position.z);
			player1.getModel().transform.setToTranslation(player1.getPosition());
			player1.setNetId(playerPacket.id);
			player1.setNetName(playerPacket.name);
			player1.getAnimation().setAnimation("Walking", -1);
			ColorMultiplierEntityShader es=new ColorMultiplierEntityShader();
			es.multiplier.y=(float)Math.random();
			es.multiplier.x=(float)Math.random();
			es.multiplier.z=(float)Math.random();
			player1.setShader(es);
			player1.getBulletObject().setUserValue(playerInstances.size);
			playerInstances.add(player1);
		}
		catch (Exception e) {
			System.err.println(e);
		}
	}
	
	//Collisions between projectile/player are checked by the server only.
//	@Override
//	public void checkProjectileCollisions(Projectile projectile) {
//		for (Player player : playerInstances) {
//			if (projectile.getBoundingBox().intersects(player.getTransformedBoundingBox()) && projectile.getOriginID() != player.getNetId()
//				&& !projectile.hasDealtDamage()) {
//				createCollisionEvent(projectile, player);
//			}
//		}
//	}
	
	@Override
	public void checkProjectileCollisionsServer(Projectile projectile) {
		boolean collision = false;
		
		for (Player player : playerInstances) {
			if (projectile.getOriginID() != player.getNetId() && !projectile.hasDealtDamage()) {
				collision = checkCollision(player.getBulletObject(), projectile.getBulletObject());
				
				if (collision)
					createCollisionEvent(projectile, player);
			}
		}
	}
	
	public void createCollisionEvent(Projectile projectile, Player player) {
		Net.CollisionPacket packet = new Net.CollisionPacket();
		packet.projectileID = projectile.getNetId();
		packet.playerID = player.getNetId();
		packet.playerOriginID = projectile.getOriginID();
		packet.position = projectile.getPosition();
		packet.damage = projectile.getDamage();
		NetServerEvent.ProjectileCollision event = new NetServerEvent.ProjectileCollision(packet);
		this.getServerEventManager().addNetEvent(event);
		projectile.setDealtDamage(true);
	}
	
	public void createExplosionEffect(Net.CollisionPacket packet) {
		for (int i = 0; i < Entity.entityInstances.size; i++) {
			Entity entity = Entity.entityInstances.get(i);
			if (entity instanceof Projectile) {
				Projectile projectile = (Projectile)entity;
				if (projectile.getNetId() == packet.projectileID) {
					Matrix4 temp = new Matrix4();
					projectile.setTarget(temp.translate(packet.position));
					projectile.setMoving(false);
					Assets.hitSound.play(0.1f);
				}
			}
		}
	}
}
