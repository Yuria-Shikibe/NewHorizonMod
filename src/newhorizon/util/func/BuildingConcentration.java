package newhorizon.util.func;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.GridBits;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.gen.Building;
import org.jetbrains.annotations.NotNull;

public class BuildingConcentration{
	public static Seq<Building> iterated = new Seq<>();
	
	public static Seq<Building> edgeBuilding = new Seq<>();
	public static Seq<Building> lastEdgeBuilding = new Seq<>();
	
	public static Seq<Building> tmpBuildings = new Seq<>();
	
	public static Seq<Complex> tmpComplexes = new Seq<>();
	
	public static final Vec2 tmpVec2 = new Vec2();
	
	public static final int MAX_SIZE = 128;
	
	public static Seq<Building> puddle(Building source){
		iterated.clear();
		edgeBuilding.clear();
		lastEdgeBuilding.clear();
		
		edgeBuilding.add(source);
		lastEdgeBuilding.add(source);
		iterated.add(source);
		
		while(iterated.size <= MAX_SIZE){
			for(Building building : edgeBuilding){
				tmpBuildings.addAll(building.proximity());
			}
			
			tmpBuildings.removeAll(lastEdgeBuilding);
			iterated.add(lastEdgeBuilding);
			
			lastEdgeBuilding.clear();
			lastEdgeBuilding.addAll(edgeBuilding);
			
			edgeBuilding.clear();
			edgeBuilding.addAll(tmpBuildings);
		}
		
		return iterated;
	}
	
	public static Vec2 center(Seq<Building> buildings){
		tmpVec2.setZero();
		
		for(Building b : buildings){
			tmpVec2.add(b.x, b.y);
		}
		
		return tmpVec2.scl(1f / buildings.size);
	}
	
	public static Seq<Complex> getComplexes(int minSize, Seq<Building> buildings){
		tmpComplexes.clear();
		
		while(buildings.any()){
			Complex complex = new Complex(buildings.pop());
			buildings.removeAll(complex.buildings);
			
			if(complex.totalTileSize >= minSize)tmpComplexes.add(complex);
		}
		
		return tmpComplexes;
	}
	
	public static class Complex implements Comparable<Complex>{
		public Seq<Building> buildings;
		
		public Building pop(){
			return buildings.pop();
		}
		
		public Building peek(){
			return buildings.peek();
		}
		
		/** Returns the first item. */
		public Building first(){
			return buildings.first();
		}
		
		@Nullable
		public Building firstOpt(){
			return buildings.firstOpt();
		}
		
		/** Returns true if the array is empty. */
		public boolean isEmpty(){
			return buildings.isEmpty();
		}
		
		public boolean any(){
			return buildings.any();
		}
		
		public Seq<Building> clear(){
			return buildings.clear();
		}
		
		public int size(){
			return buildings.size;
		}
		
		public int maxSize;
		public int totalTileSize;
		
		//Delta To Center Pos
		public float maxDeltaX;
		public float maxDeltaY;
		
		//Variance
		//The variance of the distance from the building to the center
		public float degreeOfDispersion = 0;
		
		//Expectation Center
		public float centerX, centerY;
		
		public float getVariance(){
			return degreeOfDispersion;
		}
		
		public Complex setVariance(float degreeOfDispersion){
			this.degreeOfDispersion = degreeOfDispersion;
			return this;
		}
		
		public Complex(Building source){
			this(64, source);
		}
		
		public void initData(){
			maxDeltaX = maxDeltaY = Float.MIN_VALUE;
			degreeOfDispersion = 0;
			totalTileSize = 0;
		}
		
		public void calculateData(){
			initData();
			Vec2 tmp = center(buildings);
			centerX = tmp.x;
			centerY = tmp.y;
			
			for(Building b : buildings){
				maxDeltaX = Math.max(maxDeltaX, Math.abs(b.x - centerX));
				maxDeltaY = Math.max(maxDeltaY, Math.abs(b.y - centerY));
				
				degreeOfDispersion += Mathf.dst(centerX, centerY, b.x, b.y) / b.block.size;
				totalTileSize += b.block.size * b.block.size;
			}
			
			degreeOfDispersion /= buildings.size;
		}
		
		private void getConnectedBuildingsRecursive(Building self, Seq<Building> connectedBuildings, GridBits visited, int maxSize){
			if(connectedBuildings.size >= maxSize)return;
			connectedBuildings.add(self);
			for (Building building : self.proximity){
				if (!visited.get(building.tileX(), building.tileY())) {
					visited.set(building.tileX(), building.tileY());
					getConnectedBuildingsRecursive(building, connectedBuildings, visited, maxSize);
				}
			}
		}
		
		public Complex(int maxSize, Building source){
			this.maxSize = maxSize;
			
			GridBits visited = new GridBits(Vars.world.width(), Vars.world.height());
			
			getConnectedBuildingsRecursive(source,(buildings = new Seq<>(maxSize)), visited, maxSize);
			
			if(buildings.size > maxSize){
				buildings.truncate(maxSize);
			}
			
			calculateData();
		}
		
		@Override
		public int compareTo(@NotNull Complex complex){
			return Float.compare(complex.getVariance(), getVariance());
		}
	}
}
