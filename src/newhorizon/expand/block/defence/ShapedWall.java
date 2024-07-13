package newhorizon.expand.block.defence;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.world.blocks.defense.ShieldWall;
import mindustry.world.blocks.defense.Wall;
import newhorizon.content.NHFx;
import newhorizon.util.graphic.FloatPlatformDrawer;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class ShapedWall extends Wall {
	public TextureRegion[] orthogonalRegion;
	public TextureRegion[] diagonalRegion;

	protected static final Point2[] orthogonalPos = {
		new Point2(0, 1),
		new Point2(1, 0),
		new Point2(0, -1),
		new Point2(-1, 0),
	};

	protected static final Point2[][] diagonalPos = {
		new Point2[]{ new Point2(1, 0), new Point2(1, 1), new Point2(0, 1)},
		new Point2[]{ new Point2(1, 0), new Point2(1, -1), new Point2(0, -1)},
		new Point2[]{ new Point2(-1, 0), new Point2(-1, -1), new Point2(0, -1)},
		new Point2[]{ new Point2(-1, 0), new Point2(-1, 1), new Point2(0, 1)},
	};

	protected static final Point2[] proximityPos = {
		new Point2(0, 1),
		new Point2(1, 0),
		new Point2(0, -1),
		new Point2(-1, 0),

		new Point2(1, 1),
		new Point2(1, -1),
		new Point2(-1, -1),
		new Point2(-1, 1),
	};

	public float linkAlphaLerpDst = 24f;
	public float linkAlphaScl = 0.45f;
	public float minShareDamage = 70;

	public ShapedWall(String name){
		super(name);
		size = 1;
	}
	
	@Override
	public void load(){
		super.load();
		orthogonalRegion = new TextureRegion[16];
		diagonalRegion = new TextureRegion[4];
		for(int i = 0; i < 16; i++){
			orthogonalRegion[i] = new TextureRegion(Core.atlas.find(name + "-" + i));
		}
		for(int i = 0; i < 4; i++){
			diagonalRegion[i] = new TextureRegion(Core.atlas.find(name + "-edge-" + i));
		}
	}
	
	public class ShapeWallBuild extends Building{
		public Seq<ShapeWallBuild> connectedWalls = new Seq<>();
		public int orthogonalIndex = 0;
		public boolean[] diagonalIndex = new boolean[4];

		public void updateDrawRegion(){
			orthogonalIndex = 0;

			for(int i = 0; i < orthogonalPos.length; i++){
				Point2 pos = orthogonalPos[i];
				Building build = Vars.world.build(tileX() + pos.x, tileY() + pos.y);
				if (build instanceof ShapeWallBuild){
					orthogonalIndex += 1 << i;
				}
			}

			for(int i = 0; i < diagonalPos.length; i++){
				boolean diagonal = true;
				Point2[] posArray = diagonalPos[i];

                for (Point2 pos : posArray) {
                    Building build = Vars.world.build(tileX() + pos.x, tileY() + pos.y);
                    if (!(build instanceof ShapeWallBuild)) {
                        diagonal = false;
                        break;
                    }
                }

				diagonalIndex[i] = diagonal;
			}
		}

		public void updateProximityWall(){
			tmpTiles.clear();
			connectedWalls.clear();

			for (Point2 point : proximityPos) {
				Building other = world.build(tile.x + point.x, tile.y + point.y);
				if (other == null || other.team != team) continue;
				if (other instanceof ShapeWallBuild) {
					tmpTiles.add(other);
				}
			}
			for (Building tile : tmpTiles) {
				ShapeWallBuild shapeWall = (ShapeWallBuild)tile;
				connectedWalls.add(shapeWall);
			}

			updateDrawRegion();
		}
		
		public void drawTeam() {
			Draw.color(this.team.color);
			Fill.square(x, y, 1.015f, 45);
			Draw.color();
		}
		
		@Override
		public boolean collision(Bullet other){
			if(other.type.absorbable)other.absorb();
			return super.collision(other);
		}
		
		@Override
		public float handleDamage(float amount){
			if(amount > minShareDamage && hitTime <= 0){
				float maxHandOut = amount / 9;
				float haveHandOut = 0;
				
				for(ShapeWallBuild b : connectedWalls){
					float damageP = Math.max(maxHandOut, Mathf.curve(b.healthf(), 0.25f, 0.75f) * maxHandOut);
					haveHandOut += damageP;
					b.damage(team, damageP);
					if(damageP > 0.5f) NHFx.shareDamage.at(b.x, b.y, b.block.size * tilesize / 2f, team.color, damageP / Math.max(maxHandOut, minShareDamage));
				}
				
				NHFx.shareDamage.at(x, y, block.size * tilesize / 2f, team.color, 1f);
				hitTime = Math.max(1.5f, hitTime);
				return amount - haveHandOut;
			}else return super.handleDamage(amount);
		}
		
		@Override
		public void draw(){
			Draw.rect(orthogonalRegion[orthogonalIndex], x, y);
			for (int i = 0; i < diagonalIndex.length; i++){
				if (diagonalIndex[i]){
					Draw.rect(diagonalRegion[i], x, y);
				}
			}
		}
		
		@Override
		public void drawSelect(){
			super.drawSelect();
			Draw.color(team.color);
			for(Building b : connectedWalls){
				Draw.alpha((1 - b.dst(this) / linkAlphaLerpDst) * linkAlphaScl);
				Fill.square(b.x, b.y, b.block.size * tilesize / 2f);
			}
			
			Draw.alpha(linkAlphaScl);
			Fill.square(x, y, size * tilesize / 2f);
		}

		public void updateProximity() {
			super.updateProximity();

			updateProximityWall();
			for (ShapeWallBuild other : connectedWalls) {
				other.updateProximityWall();
			}
		}
		
		@Override
		public void onRemoved(){
			for (ShapeWallBuild other : connectedWalls) {
				other.updateProximityWall();
			}
			super.onRemoved();
		}
	}
}