package com.gdx.engine;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class Assets {
	public static AssetManager manager = new AssetManager();
	public static Texture crosshair;
	public static Texture floor;
	public static Texture wall;
<<<<<<< HEAD
=======
	public static Texture stoneFloor;
>>>>>>> origin/test2
	public static TiledMap level;
	public static TiledMap level2;
	public static ModelBuilder modelBuilder;
	public static Material floorMat;
	public static Material wallMat;
<<<<<<< HEAD
=======
	public static Material stoneFloorMat;
>>>>>>> origin/test2
	public static Decal test;
	public static TextureRegion test1;
	public static Texture hole;
	
	public static void loadAssets() {
		hole = new Texture("hole.png");
		crosshair = new Texture("crosshair.png");
		test1 = new TextureRegion(hole);
		level = new TmxMapLoader().load("mymap.tmx");
		floor = new Texture("floor.png");
		wall = new Texture("wall.png");
<<<<<<< HEAD
		level2 = new TmxMapLoader().load("mymap2.tmx");
		modelBuilder = new ModelBuilder();
		floorMat = new Material(TextureAttribute.createDiffuse(Assets.floor));
		wallMat = new Material(TextureAttribute.createDiffuse(Assets.wall));
=======
		stoneFloor = new Texture("stoneFloor.png");
		level2 = new TmxMapLoader().load("mymap2.tmx");
		modelBuilder = new ModelBuilder();
		floorMat = new Material(TextureAttribute.createDiffuse(floor));
		wallMat = new Material(TextureAttribute.createDiffuse(wall));
		stoneFloorMat = new Material(TextureAttribute.createDiffuse(stoneFloor));
>>>>>>> origin/test2
	}
}
