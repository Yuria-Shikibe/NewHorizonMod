package newhorizon.feature.cutscene;

import arc.graphics.Color;

/**
 * Search what is Key-Value pair map on the Internet if you cannot understand these things.
 *
 * @implNote Make the key to the value(event's data) more formatted.
 */
public class KeyFormat{
	public static final String SHOW_PREFIX = "<@Show>";
	public static final String SPLITTER = "<@Param>";
	
	/**
	 * Generate a formatted event name for bars saving.
	 *
	 * @param time Use tick format.
	 */
	public static String generateName(String name, Color color, float time){
		return SHOW_PREFIX + name + SPLITTER + color + SPLITTER + time;
	}
	
	/**
	 * Get the shorted name from a formatted event name.
	 */
	public static String getEventName(String key){
		String[] s = key.split(SPLITTER);
		return s[0].replaceFirst(SHOW_PREFIX, "");
	}
	
	/**
	 * Get the bar color from a formatted event name.
	 */
	public static Color getEventColor(String key){
		String[] s = key.split(SPLITTER);
		return s.length < 2 ? Color.white : Color.valueOf(s[1]);
	}
	
	/**
	 * Get the bar color(Hex format) from a formatted event name.
	 */
	public static String getEventColorHex(String key){
		String[] s = key.split(SPLITTER);
		return s.length < 2 ? Color.white.toString() : s[1];
	}
	
	/**
	 * Get the total time(tick format) from a formatted event name.
	 */
	public static float getEventTotalTime(String key){
		try{
			String[] s = key.split(SPLITTER);
			return Float.parseFloat(s[s.length - 1]);
		}catch(Exception e){
			return 0;
		}
	}
	
	public static final String ENEMY_CORE_DESTROYED_EVENT = "EnemyCoreDestroyedEvent", FLEET_RAID_EVENT_00 = "FleetRaidEvent00", FLEET_RAID_EVENT_01 = "FleetRaidEvent01", FLEET_RAID_EVENT_02 = "FleetRaidEvent02", FLEET_RAID_EVENT_03 = "FleetRaidEvent03";
}
