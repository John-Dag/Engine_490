package com.gdx.Network;

import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.gdx.DynamicEntities.Projectile;
import com.gdx.Network.NetClientEvent.ChatMessage;
import com.gdx.Network.NetClientEvent.CreatePlayer;
import com.gdx.Network.NetClientEvent.CreatePlayerProjectile;
import com.gdx.Network.NetClientEvent.CreateProjectile;
import com.gdx.Network.NetClientEvent.ProjectileCollision;
import com.gdx.Network.NetClientEvent.RemovePlayer;
import com.gdx.Network.NetClientEvent.PowerUpConsumed;
import com.gdx.Network.NetClientEvent.WeaponPickedUp;
import com.gdx.engine.Entity;
import com.gdx.engine.World;

public class NetClientEventManager {
	private Array<NetClientEvent> events;
	private World world;
	
	public NetClientEventManager(World world) {
		setEvents(new Array<NetClientEvent>());
		this.world = world;
	}
	
	public void processEvents() {
		synchronized (events) {
			for (NetClientEvent event : events) {
				event.handleEvent(world);
			}
		}
		
		events.clear();
	}
	
	public void addNetEvent(NetClientEvent event) {
		synchronized (events) {
			events.add(event);
		}
	}

	public Array<NetClientEvent> getEvents() {
		return events;
	}

	public void setEvents(Array<NetClientEvent> events) {
		this.events = events;
	}
}