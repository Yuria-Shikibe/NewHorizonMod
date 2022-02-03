package newhorizon.util.feature.cutscene.events.util;

import arc.math.Mathf;
import arc.struct.Seq;
import newhorizon.NewHorizon;
import newhorizon.content.NHBullets;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.feature.cutscene.events.RaidEvent;

import java.lang.reflect.Field;

public class PreMadeRaids{
	public static final RaidEvent
	raid1 = new RaidEvent(NewHorizon.name("raid1")){{
		reloadTime = 60 * 60 * 4;
		
		number = 40;
		shootDelay = 4f;
		bulletType = NHBullets.blastEnergyNgt;
		
		removeAfterTriggered = cannotBeRemove = true;
	}}, raid2 = new RaidEvent(NewHorizon.name("raid2")){{
		reloadTime = 60 * 60 * 4;
		
		
		number = 60;
		shootDelay = 2f;
		bulletType = NHBullets.skyFrag;
		
		removeAfterTriggered = cannotBeRemove = true;
	}}, raid3 = new RaidEvent(NewHorizon.name("raid3")){{
		reloadTime = 60 * 60 * 4;
		
		
		number = 50;
		shootDelay = 3f;
		bulletType = NHBullets.hyperBlast;
		
		shootModifier = BulletHandler.spread2;
		
		removeAfterTriggered = cannotBeRemove = true;
	}}, quickRaid1 = new RaidEvent(NewHorizon.name("quickRaid1")){{
		reloadTime = 60 * 60 * 2;
		
		
		number = 50;
		shootDelay = 2f;
		bulletType = NHBullets.hyperBlast;
		
		shootModifier = BulletHandler.spread2;
		
		removeAfterTriggered = cannotBeRemove = true;
	}}, deadlyRaid1 = new RaidEvent(NewHorizon.name("deadlyRaid1")){{
		reloadTime = 60 * 60 * 4;
		
		number = 140;
		shootDelay = 18f;
		bulletType = NHBullets.synchroThermoPst;
		
		shootModifier = BulletHandler.spread2;
		
		removeAfterTriggered = cannotBeRemove = true;
	}}, deadlyRaid2 = new RaidEvent(NewHorizon.name("deadlyRaid2")){{
		reloadTime = 60 * 60 * 3;
		
		number = 160;
		shootDelay = 10f;
		sourceSpread = 360f;
		bulletType = NHBullets.destructionRocket;
		
		
		
		shootModifier = b -> {
			b.lifetime(b.lifetime() * (0.95f + Mathf.range(0.1f)));
			b.drag = (Mathf.random(0.00075f));
		};
		
		removeAfterTriggered = cannotBeRemove = true;
	}}, deadlyRaid3 = new RaidEvent(NewHorizon.name("deadlyRaid3")){{
		reloadTime = 60 * 60 * 3;
		
		number = 20;
		shootDelay = 15f;
		sourceSpread = 360f;
		bulletType = NHBullets.airRaidMissile;
		
		shootModifier = BulletHandler.spread2;
		
		removeAfterTriggered = cannotBeRemove = true;
	}}, standardRaid1 = new RaidEvent(NewHorizon.name("standardRaid1")){{
		reloadTime = 60 * 60 * 2;
		
		number = 70;
		shootDelay = 4.5f;
		sourceSpread = 160f;
		bulletType = NHBullets.destructionRocket;
		
		shootModifier = b -> {
			b.lifetime(b.lifetime() * (0.95f + Mathf.range(0.1f)));
			b.drag = (Mathf.random(0.00075f));
		};
		
		removeAfterTriggered = cannotBeRemove = true;
	}}, standardRaid2 = new RaidEvent(NewHorizon.name("standardRaid2")){{
		reloadTime = 60 * 60 * 2;
		
		number = 80;
		shootDelay = 3f;
		sourceSpread = 60f;
		bulletType = NHBullets.synchroZeta;
		
		shootModifier = BulletHandler.spread2;
		
		removeAfterTriggered = cannotBeRemove = true;
	}}
	;
	
	public static final Seq<RaidEvent> all = new Seq<>();
	
	public static int of(RaidEvent event){return all.indexOf(event);}
	
	public static CutsceneEvent get(int id){
		return id < 0 || id >= all.size ? CutsceneEvent.NULL_EVENT : all.get(id);
	}
	
	static{
		Field[] fields = PreMadeRaids.class.getFields();
		
		for(Field f : fields){
			if(RaidEvent.class.isAssignableFrom(f.getType())){
				try{
					all.add((RaidEvent)f.get(null));
				}catch(IllegalAccessException e){
					e.printStackTrace();
				}
			}
		}
	}
}
