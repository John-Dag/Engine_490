package com.gdx.DynamicEntities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gdx.engine.World;

public class Player extends Dynamic {
	private static final float ROTATION_SPEED = 0.2f;
	private static final float MOVEMENT_SPEED = 8.0f;
	private static final float CROUCH_SPEED = 2.0f;
	private static final float PLAYER_SIZE = 0.2f;
	private static final float PLAYER_HEIGHT = 0.5f;
	private static final float CROUCH_HEIGHT = 0.25f;
	private static final float PLAYER_HEIGHT_OFFSET = 0.5f;
	private static final float JUMP_SPEED = 10f;
	private static final float GRAVITY = 30f;
	private static final int MIN_HEALTH = 0;
	private static final int MAX_HEALTH = 100;
	public PerspectiveCamera camera;
	private boolean mouseLocked, mouseLeft, inCollision, isCrouching;
	public Vector3 temp;
	private World world;
	private Vector3 collisionVector;
	private Vector3 movementVector;
	private Vector3 newPos;
	private Vector3 oldPos;
	private boolean isJumping;
	private float jumpVelocity;
	private Vector3 v;
	private float currentHeightOffset;
	private float currentMovementSpeed;
	
	public Player() {
		super();
	}
	
	public Player(World world, int health, Weapon weapon, int id, boolean isActive, boolean isRenderable,
			      Vector3 position, Vector3 rotation, Vector3 scale,
			      Vector3 velocity, Vector3 acceleration, ModelInstance model) {
		super(health, weapon, id, isActive, isRenderable, position, rotation, scale, velocity,
			  acceleration, model);
		this.world = world;
		this.camera = new PerspectiveCamera();
		this.collisionVector = new Vector3(1,1,1);
		this.mouseLocked = false;
		this.temp = new Vector3();
		this.camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.camera.position.set(position.x, position.y, position.z);
		this.camera.lookAt(position.x + 1, position.y, position.z + 1);
		this.camera.near = 0.01f;
		this.camera.far = 30f;
		this.movementVector = new Vector3(0,0,0);
		this.newPos = new Vector3(0,0,0);
		this.oldPos = new Vector3(0,0,0);
		this.isJumping = false;
		this.inCollision = true;
		this.isCrouching = false;
		this.currentHeightOffset = PLAYER_HEIGHT_OFFSET;
		this.currentMovementSpeed = MOVEMENT_SPEED;
	}
	
	@Override
	public void update(float delta) {
		input(delta);
		// gets the height of the map at the players x,z location
		float heightValue = world.getMeshLevel().getHeightOffset() 
				+ currentHeightOffset 
				+ world.getMeshLevel().mapHeight(this.camera.position.x, this.camera.position.z);
		
		if(inCollision){
			// Jumping code
			if (isJumping) {
				float jumpAmt = jumpVelocity * delta;
				if (this.camera.position.y + jumpAmt > heightValue) {
					this.camera.position.y += jumpAmt;
					jumpVelocity -= GRAVITY * delta;
				}
				else {
					this.camera.position.y = heightValue;
					isJumping = false;
					jumpVelocity = 0f;
				}
			} else {
				// update height from ramps
				this.camera.position.y = heightValue;
			}
		}
		
		float movAmt = currentMovementSpeed * delta;
		movementVector.y = 0;	// jumping is done separately from the movementVector
		movementVector.nor();
		
		oldPos.set(this.camera.position);
		newPos.set(oldPos.x + movementVector.x * movAmt, oldPos.y + movementVector.y * movAmt, oldPos.z + movementVector.z * movAmt);
		
		// This makes it so that the player falls with gravity when running off ledges
		//if(world.getMeshLevel().getHeight(newPos) < world.getMeshLevel().getHeight(oldPos) &&
		if(world.getMeshLevel().mapHeight(newPos.x, newPos.z) < world.getMeshLevel().mapHeight(oldPos.x, oldPos.z) &&
				world.getMeshLevel().getHeight(newPos) != -1){
			isJumping = true;
		}

		// calculate collision vector (x, y, z) where 0 is collision, and 1 is no collision. This vector is then multiplied by the movementVector.
		if(inCollision){
			collisionVector = world.getMeshLevel().checkCollision(oldPos, newPos, PLAYER_SIZE, PLAYER_HEIGHT, PLAYER_SIZE);
			movementVector.set(movementVector.x * collisionVector.x,
					movementVector.y * collisionVector.y,
					movementVector.z * collisionVector.z);
		}

		this.camera.position.mulAdd(movementVector, movAmt);
		
		this.camera.update();
		this.getModel().transform.translate(this.camera.position.x, this.camera.position.y, this.camera.position.z);
	}
	
	public void input(float delta) {
		//Lock the cursor with right mouse button
		if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
			Gdx.input.setCursorCatched(true);
			mouseLocked = true;
		}
		//ESC cancels cursor lock
		else if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gdx.input.setCursorCatched(false);
			mouseLocked = false;
		}
		
		else if (Gdx.input.isButtonPressed(Buttons.LEFT) && mouseLocked) {
			mouseLeft = true;
		} else {
			mouseLeft = false;
		}
		
		movementVector.set(0,0,0);
		
		// camera rotation based on mouse looking
		if (mouseLocked) {
			camera.direction.rotate(camera.up, -Gdx.input.getDeltaX() * ROTATION_SPEED);
			
			temp.set(camera.direction).crs(camera.up).nor();
			v = camera.direction;
			float pitch = (float)((Math.atan2(Math.sqrt(v.x * v.x + v.z * v.z), v.y) * MathUtils.radiansToDegrees));
			float pr = -Gdx.input.getDeltaY() * 0.1f;
			
			if (pitch - pr > 165) {
				pr = -(165 - pitch);
			}
			else if (pitch - pr < 15) {
				pr = pitch - 15;
			}
			
			camera.direction.rotate(temp, pr);
		}
		
		//Keyboard input
		if (Gdx.input.isKeyPressed(Keys.W)) {
			movementVector.add(camera.direction);
		}
		if (Gdx.input.isKeyPressed(Keys.S)) {
			movementVector.sub(camera.direction);
		}
		if (Gdx.input.isKeyPressed(Keys.D)) {
			temp.set(camera.direction).crs(camera.up);
			movementVector.add(temp);
		}
		if (Gdx.input.isKeyPressed(Keys.A)) {
			temp.set(camera.up).crs(camera.direction);
			movementVector.add(temp);
		}
		if (Gdx.input.isKeyPressed(Keys.SPACE)){
			if(!isJumping){
				isJumping = true;
				jumpVelocity = JUMP_SPEED;
			}
		}
		if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
			isCrouching = true;
			currentHeightOffset = CROUCH_HEIGHT;
			currentMovementSpeed = CROUCH_SPEED;
		}
		if (!Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)){
			isCrouching = false;
			currentHeightOffset = PLAYER_HEIGHT_OFFSET;
			currentMovementSpeed = MOVEMENT_SPEED;
		}
		// This is temporary, but useful for testing. Press 'O' if you ever get stuck.
		if (Gdx.input.isKeyPressed(Keys.O)){
			Vector2 tileCenter = world.getMeshLevel().getTileCenter(camera.position.x, camera.position.z);
			Vector2 camPosition = new Vector2(camera.position.x, camera.position.z);
			Vector2 movVect = new Vector2(0,0);
			movVect = tileCenter.sub(camPosition);
			camera.position.add(movVect.x * delta, 0, movVect.y * delta);
		}
	}
	
	public void decreaseViewDistance(){
		
	}
	
	public void increaseViewDistance(){
		
	}
	
	// input world coordinates to get tile coords
	public GridPoint2 getPlayerTileCoords(){
		return getTileCoords(camera.position.x, camera.position.z);
	}
	
	// This take in x and z from world coordinates and returns the tile position (tile index)
	// Note: The coords are flipped because of Tiled Map Editor and LibGDX being slightly inconsistent there.
	private GridPoint2 getTileCoords(float x, float z){
		int tileX = (int)z;
		int tileY = (int)x;
		return new GridPoint2(tileX, tileY);
	}
	
	public boolean isMouseLocked() {
		return mouseLocked;
	}

	public boolean isMouseLeft() {
		return mouseLeft;
	}

	public boolean isInCollision() {
		return inCollision;
	}

	public boolean isCrouching() {
		return isCrouching;
	}

	public boolean isJumping() {
		return isJumping;
	}

	public float getJumpVelocity() {
		return jumpVelocity;
	}

	public void setMouseLocked(boolean mouseLocked) {
		this.mouseLocked = mouseLocked;
	}

	public void setMouseLeft(boolean mouseLeft) {
		this.mouseLeft = mouseLeft;
	}

	public void setInCollision(boolean inCollision) {
		this.inCollision = inCollision;
	}

	public void setCrouching(boolean isCrouching) {
		this.isCrouching = isCrouching;
	}

	public void setJumping(boolean isJumping) {
		this.isJumping = isJumping;
	}

	public void setJumpVelocity(float jumpVelocity) {
		this.jumpVelocity = jumpVelocity;
	}
}