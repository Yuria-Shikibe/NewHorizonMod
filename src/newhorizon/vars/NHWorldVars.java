package newhorizon.vars;

import arc.Core;
import arc.graphics.Camera;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.struct.IntSeq;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import newhorizon.block.defence.GravityGully;
import newhorizon.block.special.CommandableBlock;
import newhorizon.block.special.UpgradeBlock;
import newhorizon.interfaces.BeforeLoadc;
import newhorizon.interfaces.ServerInitc;

import static mindustry.Vars.tilesize;

public class NHWorldVars{
	public transient static boolean serverLoaded = true;
	public transient static boolean worldLoaded = false;
	public transient static boolean load = false;
	
	public transient static final Seq<ServerInitc> serverLoad = new Seq<>();
	public transient static final Seq<BeforeLoadc> advancedLoad = new Seq<>();
	public transient static final Seq<UpgradeBlock.UpgradeBlockBuild> upgraderGroup = new Seq<>();
	public transient static final Seq<GravityGully.GravityGullyBuild> gravGullyGroup = new Seq<>();
	public transient static final ObjectMap<Tile, IntSeq> intercepted = new ObjectMap<>();
	
	public transient static final Seq<CommandableBlock.CommandableBlockBuild> commandables = new Seq<>();
	
	public transient static int ix, iy;
	public transient static int commandPos = -1;
	
	
	public static void clear(){
		intercepted.clear();
		upgraderGroup.clear();
		gravGullyGroup.clear();
		commandables.clear();
		
		ix = iy = 0;
		commandPos = -1;
	}
	
	public static void clearLast(){
		advancedLoad.clear();
		serverLoad.clear();
	}
	
	public static void drawGully(int teamIndex){
		float width = Core.graphics.getWidth();
		float height = Core.graphics.getHeight();
		
		Camera c = Core.camera;
		Tmp.r3.setSize(c.width + tilesize * 2, c.height + tilesize * 2).setCenter(c.position);
		
		for(Tile t : NHWorldVars.intercepted.keys()){
			if(!Tmp.r3.contains(t.drawx(), t.drawy()))continue;
			IntSeq teams = NHWorldVars.intercepted.get(t);
			
			int anyOther = teams.count(0);
			
			if(teams.get(teamIndex) > 0){
				if(anyOther < Team.all.length - 1) Draw.color(Pal.accent);
				else Draw.color(Pal.lancerLaser);
			}else if(anyOther < Team.all.length)Draw.color(Pal.ammo);
			else continue;
			
			Draw.alpha(0.45f);
			t.getBounds(Tmp.r1).getCenter(Tmp.v1);
			Fill.square(Tmp.v1.x, Tmp.v1.y, tilesize / 2f);
		}
	}
}
