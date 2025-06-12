package gamelogic.level;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import gameengine.PhysicsObject;
import gameengine.graphics.Camera;
import gameengine.loaders.Mapdata;
import gameengine.loaders.Tileset;
import gamelogic.GameResources;
import gamelogic.Main;
import gamelogic.enemies.Enemy;
import gamelogic.player.Player;
import gamelogic.tiledMap.Map;
import gamelogic.tiles.Flag;
import gamelogic.tiles.Flower;
import gamelogic.tiles.Gas;
import gamelogic.tiles.Lava;
import gamelogic.tiles.SolidTile;
import gamelogic.tiles.Spikes;
import gamelogic.tiles.Tile;
import gamelogic.tiles.Water;

public class Level {

	private LevelData leveldata;
	private Map map;
	private Enemy[] enemies;
	public static Player player;
	private Camera camera;

	private boolean active;
	private boolean playerDead;
	private boolean playerWin;

	private ArrayList<Enemy> enemiesList = new ArrayList<>();
	private ArrayList<Flower> flowers = new ArrayList<>();
	private ArrayList<Water> waterList = new ArrayList<>();
	private ArrayList<Gas> gasList = new ArrayList<>();
	private ArrayList<Lava> lavaList = new ArrayList<>();

	private List<PlayerDieListener> dieListeners = new ArrayList<>();
	private List<PlayerWinListener> winListeners = new ArrayList<>();

	private Mapdata mapdata;
	private int width;
	private int height;
	private int tileSize;
	private Tileset tileset;
	public static float GRAVITY = 70;

	public Level(LevelData leveldata) {
		this.leveldata = leveldata;
		mapdata = leveldata.getMapdata();
		width = mapdata.getWidth();
		height = mapdata.getHeight();
		tileSize = mapdata.getTileSize();
		restartLevel();
	}

	public LevelData getLevelData(){
		return leveldata;
	}

	public void restartLevel() {
		int[][] values = mapdata.getValues();
		Tile[][] tiles = new Tile[width][height];

		for (int x = 0; x < width; x++) {
			int xPosition = x;
			for (int y = 0; y < height; y++) {
				int yPosition = y;

				tileset = GameResources.tileset;

				tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this);
				if (values[x][y] == 0)
					tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this); // Air
				else if (values[x][y] == 1)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid"), this);

				else if (values[x][y] == 2)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_DOWNWARDS, this);
				else if (values[x][y] == 3)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_UPWARDS, this);
				else if (values[x][y] == 4)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_LEFTWARDS, this);
				else if (values[x][y] == 5)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_RIGHTWARDS, this);
				else if (values[x][y] == 6)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Dirt"), this);
				else if (values[x][y] == 7)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Grass"), this);
				else if (values[x][y] == 8)
					enemiesList.add(new Enemy(xPosition*tileSize, yPosition*tileSize, this)); // TODO: objects vs tiles
				else if (values[x][y] == 9)
					tiles[x][y] = new Flag(xPosition, yPosition, tileSize, tileset.getImage("Flag"), this);
				else if (values[x][y] == 10) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower1"), this, 1);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 11) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower2"), this, 2);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 12)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_down"), this);
				else if (values[x][y] == 13)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_up"), this);
				else if (values[x][y] == 14)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_middle"), this);
				else if (values[x][y] == 15)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasOne"), this, 1);
				else if (values[x][y] == 16)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasTwo"), this, 2);
				else if (values[x][y] == 17)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasThree"), this, 3);
				else if (values[x][y] == 18)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Falling_water"), this, 0);
				else if (values[x][y] == 19)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Full_water"), this, 3);
				else if (values[x][y] == 20)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Half_water"), this, 2);
				else if (values[x][y] == 21)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Quarter_water"), this, 1);
				else if (values[x][y] == 22)
					tiles[x][y] = new Lava(xPosition, yPosition, tileSize, tileset.getImage("Lava"), this);

			}
		}

		enemies = new Enemy[enemiesList.size()];
		map = new Map(width, height, tileSize, tiles);
		camera = new Camera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, 0, map.getFullWidth(), map.getFullHeight());
		for (int i = 0; i < enemiesList.size(); i++) {
			enemies[i] = new Enemy(enemiesList.get(i).getX(), enemiesList.get(i).getY(), this);
		}

		player = new Player(leveldata.getPlayerX() * map.getTileSize(), leveldata.getPlayerY() * map.getTileSize(), this);
		camera.setFocusedObject(player);

		active = true;
		playerDead = false;
		playerWin = false;
	}

	//Precondition: none
	//Postcondition: Player returns to normal
	public void resetPlayerEffects() {
		player.jumpPower = 1350;
		player.walkSpeed = 400;
		Level.GRAVITY = 70;
	}

	public void onPlayerDeath() {
		active = false;
		playerDead = true;
		waterList.clear();
		gasList.clear();
		resetPlayerEffects();
		throwPlayerDieEvent();
	}

	public void onPlayerWin() {
		active = false;
		playerWin = true;
		throwPlayerWinEvent();
	}

	public void update(float tslf) {
		if (active) {
			// Update the player
			player.update(tslf);

			// Player death
			if (map.getFullHeight() + 100 < player.getY())
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.BOT] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.TOP] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.LEF] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.RIG] instanceof Spikes)
				onPlayerDeath();

			for (int i = 0; i < flowers.size(); i++) {
				if (flowers.get(i).getHitbox().isIntersecting(player.getHitbox())) {
					if(flowers.get(i).getType() == 1)
						water(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 3);
					else
						addGas(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 20, new ArrayList<Gas>());
					flowers.remove(i);
					i--;
				}
			}

			// Update the enemies
			for (int i = 0; i < enemies.length; i++) {
				enemies[i].update(tslf);
				if (player.getHitbox().isIntersecting(enemies[i].getHitbox())) {
					onPlayerDeath();
				}
			}

			//When in either gas or water, jump power is reduced
			ArrayList<Tile> liquids = new ArrayList<>();
			liquids.addAll(waterList);
			liquids.addAll(gasList);
			for (Tile t : liquids) {
				if (!t.getHitbox().isIntersecting(player.getHitbox())) {
					resetPlayerEffects();
				}
			}
			//The higher the if statement, the higher priority
			for (Tile t : liquids) {
				if (t instanceof Water && t.getHitbox().isIntersecting(player.getHitbox())) {
					player.jumpPower = 900;
					player.walkSpeed = 200;

				} else if (t instanceof Gas && t.getHitbox().isIntersecting(player.getHitbox())) {
					player.jumpPower = 675;
					Level.GRAVITY = 35;
				}
			}

			// Update the map
			map.update(tslf);

			// Update the camera
			camera.update(tslf);
		}
	}
	
	//Precondition: none
	//Postcondition: 1-20 new gas tiles are created
	private void addGas(int col, int row, Map map, int numSquaresToFill, ArrayList<Gas> placedThisRound) {
		//Records the new gas tile
		Gas g = new Gas(col, row, tileSize, tileset.getImage("GasOne"), this, 0);
		map.addTile(col, row, g);
		placedThisRound.add(g);
		gasList.add(g);

		//Removes one because this is the starting gas tile
		numSquaresToFill -= 1;
		
		//This loop allows us to look through every tile, plus all the new ones
		for (int a = 0; a < placedThisRound.size(); a++) {
			//Overwrites the original to use fewer variables and help navigate easier
			col = placedThisRound.get(a).getCol();
			row = placedThisRound.get(a).getRow();
			
			//A simple 2D Array to help navigate around all 8 spaces around the given tile in order
			int[][] checkTiles = {
				{col, row - 1}, // Top
				{col + 1, row - 1}, // Top Right
				{col - 1, row - 1}, // Top Left
				{col + 1, row}, // Right
				{col - 1, row}, // Left
				{col, row + 1}, // Bottom
				{col + 1, row + 1}, // Bottom Right
				{col - 1, row + 1} // Bottom Left
			};
			
			//This loops through the 2D Array to check all 8 tiles around the given tile
			for (int i = 0; i < checkTiles.length; i++) {
				int c = checkTiles[i][0];
				int r = checkTiles[i][1];
				
				//Checks to see if the given coordinates are within bounds and the tile at said coordinates is not solid nor a gas tile
				if ((c > 0 && c < map.getTiles().length) && (r > 0 && r < map.getTiles()[0].length) && !map.getTiles()[c][r].isSolid() && !(map.getTiles()[c][r] instanceof Gas) && numSquaresToFill > 0) {
					//Records the new gas tile
					Gas newGas = new Gas(c, r, tileSize, tileset.getImage("GasOne"), this, 0);
					map.addTile(c, r, newGas);
					gasList.add(newGas);
					placedThisRound.add(newGas);

					numSquaresToFill -= 1;
				}
			}
			
			//Ensures to end the loop when there are no more squares left to fill
			if (!(numSquaresToFill > 0)) {
				break;
			}
		}
	}

	//Preconditions: none
	//Postconditions: water spreads throughout the level as long as there are open spaces
	private void water(int col, int row, Map map, int fullness) {
		//This part is used so that when a new water tile is made, it takes the fullness and knows which costume from the costume map to use
		String waterLevel = "";
		if (fullness == 3) {
			waterLevel = "Full_water";
		} else if (fullness == 2) {
			waterLevel = "Half_water";
		} else if (fullness == 1) {
			waterLevel = "Quarter_water";
		} else if (fullness == 0) {
			waterLevel = "Falling_water";
		}

		//This records the new tile. Because the water method is recursive, we do not have to record water again.
		Water w = new Water(col, row, tileSize, tileset.getImage(waterLevel), this, fullness);
		map.addTile(col, row, w);
		waterList.add(w);

		//Check the tile below for an open space
		if (row + 1 < map.getTiles()[0].length && !map.getTiles()[col][row + 1].isSolid()) {
			water(col, row + 1, map, 0); //Place water in the tile below with the falling costume

		} else {
			if (row + 1 < map.getTiles()[0].length) {
				//If this tile is currently falling, and the tile above is also a water tile, become a full water tile
				if (fullness == 0 && row - 1 > 0 && (map.getTiles()[col][row - 1] instanceof Water)) { 
					water(col, row, map, 3); //Place water 
				}

				//If the tile below is solid, try to go left and right
				//Right
				if (col + 1 < map.getTiles().length - 1 && !(map.getTiles()[col + 1][row] instanceof Water) && !map.getTiles()[col + 1][row].isSolid()) {
					if (fullness > 1) {
						water(col + 1, row, map, fullness - 1);
					} else {
						water(col + 1, row, map, fullness);
					}
				}

				//Left
				if (col - 1 > 0 && !(map.getTiles()[col - 1][row] instanceof Water) && !map.getTiles()[col - 1][row].isSolid()) {
					if (fullness > 1) {
						water(col - 1, row, map, fullness - 1);
					} else {
						water(col - 1, row, map, fullness);
					}
				}
			}
		}
	}

	private void lava(int col, int row, Map map) {
		//This records the new tile. Because the water method is recursive, we do not have to record water again.
		Lava l = new Lava(col, row, tileSize, tileset.getImage("lava"), this);
		map.addTile(col, row, l);
		lavaList.add(l);

		//Check the tile below for an open space
		if (row + 1 < map.getTiles()[0].length && !map.getTiles()[col][row + 1].isSolid()) {
			lava(col, row + 1, map); //Place lava in the tile below with the falling costume

		} else {
			if (row + 1 < map.getTiles()[0].length) {
				//If the tile below is solid, try to go left and right
				//Right
				if (col + 1 < map.getTiles().length - 1 && !(map.getTiles()[col + 1][row] instanceof Lava) && !map.getTiles()[col + 1][row].isSolid()) {
					lava(col + 1, row, map);
				}

				//Left
				if (col - 1 > 0 && !(map.getTiles()[col - 1][row] instanceof Lava) && !map.getTiles()[col - 1][row].isSolid()) {
					lava(col - 1, row, map);
				}
			}
		}
	}

	public void draw(Graphics g) {
	   	g.translate((int) -camera.getX(), (int) -camera.getY());
	   	// Draw the map
	   	for (int x = 0; x < map.getWidth(); x++) {
	   		for (int y = 0; y < map.getHeight(); y++) {
	   			Tile tile = map.getTiles()[x][y];
	   			if (tile == null) {
	   				continue;
				}
	   			if (tile instanceof Gas) {
	   				int adjacencyCount =0;
	   				for (int i=-1; i<2; i++) {
	   					for (int j =-1; j<2; j++) {
	   						if (j!=0 || i!=0) {
	   							if ((x+i)>=0 && (x+i)<map.getTiles().length && (y+j)>=0 && (y+j)<map.getTiles()[x].length) {
	   								if (map.getTiles()[x+i][y+j] instanceof Gas) {
	   									adjacencyCount++;
	   								}
	   							}
	   						}
	   					}
	   				}
	   				if (adjacencyCount == 8) {
	   					((Gas)(tile)).setIntensity(2);
	   					tile.setImage(tileset.getImage("GasThree"));
	   				} else if (adjacencyCount >5) {
	   					((Gas)(tile)).setIntensity(1);
	   					tile.setImage(tileset.getImage("GasTwo"));
	   				} else {
	   					((Gas)(tile)).setIntensity(0);
	   					tile.setImage(tileset.getImage("GasOne"));
	   				}
	   			}

	   			if (camera.isVisibleOnCamera(tile.getX(), tile.getY(), tile.getSize(), tile.getSize())) {
	   				tile.draw(g);
				}
	   		}
	   	}

	   	// Draw the enemies
	   	for (int i = 0; i < enemies.length; i++) {
	   		enemies[i].draw(g);
	   	}

	   	 // Draw the player
	   	player.draw(g);

	   	 // used for debugging
	   	if (Camera.SHOW_CAMERA) {
	   		camera.draw(g);
		}
		g.translate((int) +camera.getX(), (int) +camera.getY());
	    }


	// --------------------------Die-Listener
	public void throwPlayerDieEvent() {
		for (PlayerDieListener playerDieListener : dieListeners) {
			playerDieListener.onPlayerDeath();
		}
	}

	public void addPlayerDieListener(PlayerDieListener listener) {
		dieListeners.add(listener);
	}

	// ------------------------Win-Listener
	public void throwPlayerWinEvent() {
		for (PlayerWinListener playerWinListener : winListeners) {
			playerWinListener.onPlayerWin();
		}
	}

	public void addPlayerWinListener(PlayerWinListener listener) {
		winListeners.add(listener);
	}

	// ---------------------------------------------------------Getters
	public boolean isActive() {
		return active;
	}

	public boolean isPlayerDead() {
		return playerDead;
	}

	public boolean isPlayerWin() {
		return playerWin;
	}

	public Map getMap() {
		return map;
	}

	public Player getPlayer() {
		return player;
	}
}