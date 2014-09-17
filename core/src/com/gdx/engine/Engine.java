package com.gdx.engine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.physics.bullet.Bullet;

public class Engine extends Game {	
	@Override
	public void create () {
		Assets.loadAssets();
		Bullet.init();
		setScreen(new GameScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
}
