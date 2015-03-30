package com.gdx.engine;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectArray;
import com.badlogic.gdx.physics.bullet.dynamics.InternalTickCallback;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

public class BulletTickCallback extends InternalTickCallback {	
	public static Vector3 tmpV1 = new Vector3();
	
	public BulletTickCallback(btDynamicsWorld dynamicsWorld) {
		super(dynamicsWorld, true);
	}
	
	@Override
	public void onInternalTick(btDynamicsWorld dynamicsWorld, float timeStep) {
//		btCollisionObjectArray objs = dynamicsWorld.getCollisionObjectArray();
//		dynamicsWorld.clearForces();
	}
}