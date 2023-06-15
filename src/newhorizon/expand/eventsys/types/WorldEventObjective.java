package newhorizon.expand.eventsys.types;

import arc.Core;
import mindustry.game.MapObjectives;
import mindustry.graphics.MinimapRenderer;
import mindustry.ui.IntFormat;
import newhorizon.NHGroups;

import static mindustry.Vars.*;

public class WorldEventObjective extends MapObjectives.MapObjective{
	static {
//		MapObjectives.registerObjective(WorldEventObjective::new);
//		MapObjectives.registerMarker(WorldEventMarker::new);
	}
	
	public WorldEventObjective(){
		markers = new MapObjectives.ObjectiveMarker[]{new WorldEventMarker()};
	}
	
	
	
	/** @return Whether this objective should run at all. */
	@Override
	public boolean qualified(){
		return !NHGroups.events.isEmpty();
	}
	
	/** @return Basic mission display text. If null, falls back to standard text. */
	@Override
	public String text(){
		StringBuilder ibuild = new StringBuilder();
		
		IntFormat
				wavef = new IntFormat("wave"),
				wavefc = new IntFormat("wave.cap"),
				enemyf = new IntFormat("wave.enemy"),
				enemiesf = new IntFormat("wave.enemies"),
				enemycf = new IntFormat("wave.enemycore"),
				enemycsf = new IntFormat("wave.enemycores"),
				waitingf = new IntFormat("wave.waiting", i -> {
					ibuild.setLength(0);
					int m = i/60;
					int s = i % 60;
					if(m > 0){
						ibuild.append(m);
						ibuild.append(":");
						if(s < 10){
							ibuild.append("0");
						}
					}
					ibuild.append(s);
					return ibuild.toString();
				});
		
		
		StringBuilder builder = new StringBuilder();
		
		if(state.rules.mission != null){
			builder.append(state.rules.mission);
			return builder.toString();
		}
		
		if(!state.rules.waves && state.rules.attackMode){
			int sum = Math.max(state.teams.present.sum(t -> t.team != player.team() ? t.cores.size : 0), 1);
			builder.append(sum > 1 ? enemycsf.get(sum) : enemycf.get(sum));
			return builder.toString();
		}
		
		if(!state.rules.waves && state.isCampaign()){
			builder.append("[lightgray]").append(Core.bundle.get("sector.curcapture"));
		}
		
		if(!state.rules.waves){
			return builder.toString();
		}
		
		if(state.rules.winWave > 1 && state.rules.winWave >= state.wave && state.isCampaign()){
			builder.append(wavefc.get(state.wave, state.rules.winWave));
		}else{
			builder.append(wavef.get(state.wave));
		}
		builder.append("\n");
		
		if(state.enemies > 0){
			if(state.enemies == 1){
				builder.append(enemyf.get(state.enemies));
			}else{
				builder.append(enemiesf.get(state.enemies));
			}
			builder.append("\n");
		}
		
		if(state.rules.waveTimer){
			builder.append((logic.isWaitingWave() ? Core.bundle.get("wave.waveInProgress") : (waitingf.get((int)(state.wavetime/60)))));
		}else if(state.enemies == 0){
			builder.append(Core.bundle.get("waiting"));
		}
		
		return builder.toString();
	}
	
	/** @return Details that appear upon click. */
	@Override
	public String details(){
		return "[accent]" + NHGroups.events.size() + "[] events in total.";
	}
	
	/** @return True if this objective is done and should be removed from the executor. */
	@Override
	public boolean update(){
		return false;
	}
	
	public static class WorldEventMarker extends MapObjectives.ObjectiveMarker{
		@Override
		public void drawMinimap(MinimapRenderer minimap){
			NHGroups.events.each(e -> e.type.minimapMarkable, e -> {
				e.type.drawMinimap(e, minimap);
			});
		}
	}
}
