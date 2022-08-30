package newhorizon.expand.units;

import arc.math.Mathf;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Groups;
import mindustry.gen.TimedKillUnit;
import newhorizon.expand.entities.EntityRegister;

//It's too expensive to just solve a trail fade effect. Plz fix it.
public class AdaptedTimedKillUnit extends TimedKillUnit{
	@Override public int classId(){return EntityRegister.getID(getClass());}
	
	@Override
	public void remove(){
		if (this.added) {
			Groups.all.remove(this);
			Groups.unit.remove(this);
			Groups.sync.remove(this);
			Groups.draw.remove(this);
			this.added = false;
			if (Vars.net.client()) {
				Vars.netClient.addRemovedEntity(this.id());
			}
			
			this.team.data().updateCount(this.type, -1);
			this.controller.removed(this);
			if (this.trail != null && this.trail.size() > 0) {
				Fx.trailFade.at(this.x, this.y, (type.engineSize + Mathf.absin(Time.time, 2f, type.engineSize / 4f) * (type.useEngineElevation ? elevation : 1f)) * type.trailScl, this.type.trailColor == null ? this.team.color : this.type.trailColor, this.trail.copy());
			}
			
			for(WeaponMount mount : mounts){
				if(mount.weapon.continuous && mount.bullet != null && mount.bullet.owner == this){
					mount.bullet.time = mount.bullet.lifetime - 10.0F;
					mount.bullet = null;
				}
				
				if(mount.sound != null){
					mount.sound.stop();
				}
			}
			
		}
	}
}
