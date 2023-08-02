package newhorizon.expand.block.ancient;

import arc.Core;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.world.Tile;
import newhorizon.content.NHFx;
import newhorizon.expand.block.turrets.ShootMatchTurret;

import static mindustry.Vars.*;

public class CaptureableTurret extends ShootMatchTurret{
	public float captureInvicibility = 60f * 7f;
	
	public float shakeOnDestroy = -1;
	public boolean cheatIfEnemy = true;
	public Effect replaceEffect = NHFx.crossBlastArrow45;
	public Item fallbackItem = null;
	
	public CaptureableTurret(String name){
		super(name);
		
		replaceable = false;
	}
	
	@Override
	public void init(){
		super.init();
		
		if(shakeOnDestroy < 0){
			shakeOnDestroy = size;
		}
		
		fallbackItem = ammoTypes.keys().toSeq().first();
	}
	
	@Override
	public boolean isVisible(){
		return state.rules.editor || state.rules.infiniteResources;
	}
	
	@Override
	public boolean canBreak(Tile tile){
		return state.rules.editor || state.rules.infiniteResources;
	}
	
	public class CaptureableTurretBuild extends ShootMatchTurretBuild{
		public Team lastDamage = Team.derelict;
		public float iframes = -1f;
		
		@Override
		public boolean canControl(){
			return playerControllable && !logicShooting;
		}
		
		@Override
		public void damage(@Nullable Team source, float damage){
			if(iframes > 0) return;
			
			if(source != null && source != team){
				lastDamage = source;
			}
			super.damage(source, damage);
		}
		
		@Override
		public void updateTile(){
			super.updateTile();
			
			iframes -= Time.delta;
			
			if(cheating() && acceptItem(null, fallbackItem)){
				handleItem(null, fallbackItem);
			}
		}
		
		@Override
		public void changeTeam(Team next){
			super.changeTeam(next);
		}
		
		@Override
		public boolean cheating(){
			return super.cheating() || (cheatIfEnemy && team == state.rules.waveTeam);
		}
		
		@Override
		public boolean canPickup(){
			return false;
		}
		
		@Override
		public void onDestroyed(){
			Effect.shake(shakeOnDestroy, shakeOnDestroy, x, y);
			
			NHFx.square45_8_45.at(x, y, 0, lastDamage.color);
			NHFx.square45_6_45_Charge.at(x, y, 0, lastDamage.color);
			NHFx.circleOut.at(x, y, tilesize * size, lastDamage.color);
			replaceEffect.at(x, y, lightRadius, lastDamage.color);
		}
		
		
		
		@Override
		public void afterDestroyed(){
			float rot = rotation;
			float warmup = warmupHold;
			
			if(!net.client()){
				tile.setBlock(block, lastDamage);
				CaptureableTurretBuild b = (CaptureableTurretBuild)tile.build;
				b.rotation = rot;
				b.warmupHold = warmup;
			}
			
			//delay so clients don't destroy it afterwards
			Core.app.post(() -> {
				tile.setNet(block, lastDamage, 0);
			});
			
			//building does not exist on client yet
			if(!net.client()){
				//core is invincible for several seconds to prevent recapture
				((CaptureableTurretBuild)tile.build).iframes = captureInvicibility;
			}
		}
	}
}
