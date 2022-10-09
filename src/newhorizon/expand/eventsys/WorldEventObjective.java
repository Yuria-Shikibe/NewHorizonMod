package newhorizon.expand.eventsys;

import mindustry.game.MapObjectives;
import mindustry.graphics.MinimapRenderer;
import newhorizon.NHGroups;

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
		return "Events Detected";
	}
	
	/** @return Details that appear upon click. */
	@Override
	public String details(){
		return "[accent]" + NHGroups.events.size() + "[] events in total.";
	}
	
	/**
	 * @return The localized type-name of this objective, defaulting to the class simple name without the "Objective"
	 * prefix.
	 */
	@Override
	public String typeName(){
		return super.typeName();
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
