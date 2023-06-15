package newhorizon.expand.cutscene.stateoverride;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import newhorizon.util.struct.Vec2Seq;

public class PathPoint{
	public Vec2Seq basePoints = new Vec2Seq();
	public Vec2Seq restPoints = new Vec2Seq();
	
	public PathPoint addAll(Vec2... vec2s){
		basePoints.addAll(vec2s);
		restPoints = basePoints.copy();
		return this;
	}
	
	public Vec2 current(){
		return restPoints.firstTmp();
	}
	
	public boolean complete(float x, float y, float approximateDst){
		int vecI = getNearestRest(x, y);
		if(restPoints.tmpVec2(vecI).within(x, y, approximateDst)){
			restPoints.remove(vecI);
			return true;
		}else return false;
	}
	
	public int getNearestBase(float x, float y){
		return getNearest(x, y, basePoints);
	}
	
	public int getNearestRest(float x, float y){
		return getNearest(x, y, restPoints);
	}
	
	public int getNearest(float x, float y, Vec2Seq seq){
		float dst2 = Float.MAX_VALUE;
		int index = 0;
		
		for(int i = 0; i < seq.size(); i++){
			Vec2 v = seq.tmpVec2(i);
			float dst2_cal = Mathf.dst2(x, y, v.x, v.y);
			if(dst2_cal < dst2){
				index = i;
				dst2 = dst2_cal;
			}
		}
		
		return index;
	}
}
