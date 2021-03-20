package newhorizon.block.special;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.scene.ui.layout.Table;
import arc.struct.BoolSeq;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.blocks.defense.Wall;

import static newhorizon.func.TableFs.LEN;

public class ShapedWall extends Wall{
	protected static int[][] tileKey = {
		{5, 1, 4},
		{2,    0},
		{6, 3, 7}
	};
	
	protected static int[][] traverseKey = {
		{1, 0}, {0, 1},
		{1, 1}, {-1, -1},
		{-1, 0}, {0, -1},
		{1, -1}, {-1, 1}
	};
	
	public final ObjectMap<String, TextureRegion> sprites = new ObjectMap<>();
	
	public ShapedWall(String name){
		super(name);
		configurable = true;
		size = 1;
		update = true;
		config(Point2.class, ShapeWallBuild::computePoint);
	}
	
//	@Override
//	public void load(){
//		super.load();
//		sprites.put("00000000", Core.atlas.find(name));
//		for(int i = 0; i < 256; i ++){
//			String key = Integer.toBinaryString(i);
//			StringBuilder builder = new StringBuilder();
//			builder.append("0".repeat(8 - key.length()));
//			key = builder.append(key).toString();
//			if(key.startsWith("0000"))continue;
//			Log.info(key);
//			sprites.put(key, Core.atlas.find(name + "-" + key));
//		}
//	}
	
	public class ShapeWallBuild extends Building{
		public BoolSeq drawid = new BoolSeq(8);
		protected String key = "";
		
		public void updateKey(){
			StringBuilder builder = new StringBuilder();
			for(boolean bool : drawid.items){
				Log.info(bool);
				builder.append(Mathf.num(bool));
			}
			key = builder.toString();
			if(key.startsWith("0000"))key = "00000000";
			Log.info(key);
		}
		
		public void computePoint(Point2 point){
			/*
				5 1 4
				2 x 0
				6 3 7
			 */
			int x = point.x, y = point.y, index;
			if(x == 1 && y == 0){
				index = 0;
			}else if(x == 0 && y == 1){
				index = 1;
			}else if(x == -1 && y == 0){
				index = 2;
			}else if(x == 0 && y == -1){
				index = 3;
			}else if(x == 1 && y == 1){
				index = 4;
			}else if(x == -1 && y == 1){
				index = 5;
			}else if(x == -1 && y == -1){
				index = 6;
			}else if(x == 1 && y == -1){
				index = 7;
			}else index = -1;
			
			if(index != -1)drawid.set(index, !drawid.get(index));
			updateKey();
		}
	
		public void updateDraw(){
			//Log.info(proximity);
			//updateProximity();
			//Log.info(proximity);
			for(int[] index : traverseKey){
				Building build = Vars.world.build(tileX() + index[0], tileY() + index[1]);
				if(build instanceof ShapeWallBuild){
					computePoint(Tmp.p1.set(build.tileX() - tileX(), build.tileY() - tileY()));
					build.configure(Tmp.p1.set(tileX() - build.tileX(), tileY() - build.tileY()));
				}
			}
			
		}
		
		@Override
		public void draw(){
			/*
				5 1 4
				2 x 0
				6 3 7
			 */
			
			
			
			super.draw();
		}
		
		@Override
		public void placed(){
			super.placed();
			
			for(int i = 0; i < 8; i++){
				drawid.add(false);
			}
			
			updateDraw();
		}
		
		@Override
		public void remove(){
			super.remove();
			updateDraw();
		}
		
		@Override
		public void onDestroyed(){
			super.onDestroyed();
			updateDraw();
		}
		
		@Override
		public void buildConfiguration(Table table){
			super.buildConfiguration(table);
			
			table.table(t -> {
				for(int[] i : tileKey){
					for(int j : i){
						t.image().size(LEN).color(drawid.get(j) ? Color.green : Pal.redderDust);
						if(j == 2)t.image().size(LEN).color(Pal.gray);
					}
					t.row();
				}
			}).fill().row();
			
			table.add(key).color(Pal.redderDust);

		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			for(boolean bool : drawid.items){
				write.bool(bool);
			}
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			for(boolean bool : drawid.items){
				drawid.add(read.bool());
			}
			
			updateKey();
		}
	}
}
