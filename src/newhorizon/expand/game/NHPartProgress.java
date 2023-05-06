package newhorizon.expand.game;

import mindustry.entities.part.DrawPart;

public class NHPartProgress{
	public static final DrawPart.PartProgress recoilWarmup = p -> Math.max(0, p.warmup - p.recoil);
	public static final DrawPart.PartProgress recoilWarmupSep = p -> p.warmup - p.recoil;
}
