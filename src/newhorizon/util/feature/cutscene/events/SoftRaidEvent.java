package newhorizon.util.feature.cutscene.events;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.bullet.BulletType;
import mindustry.io.TypeIO;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.feature.cutscene.events.util.BulletHandler;

/**
 * The IO of {@link BulletType} is unstable as it uses content's id to save the type.<p>
 * This event is suggested to make only random event disappearing after trigger.<p><p>
 *
 * Construct a fixed event is more advisable.
 * */
public class SoftRaidEvent extends RaidEvent{
	public SoftRaidEvent(String name, BulletType type){
		super(name);
		bulletType = type;
	}
	
	@Override
	public void write(CutsceneEventEntity e, Writes writes){
		super.write(e, writes);
		
		TypeIO.writeBulletType(writes, bulletType);
		
		writes.i(number);
		writes.f(shootDelay);
		writes.f(inaccuracy);
		writes.f(sourceSpread);
		writes.i(BulletHandler.of(shootModifier));
	}
	
	@Override
	public void read(CutsceneEventEntity e, Reads reads){
		super.read(e, reads);
		
		bulletType = TypeIO.readBulletType(reads);
		
		number = reads.i();
		shootDelay = reads.f();
		inaccuracy = reads.f();
		sourceSpread = reads.f();
		shootModifier = BulletHandler.get(reads.i());
		
	}
}
