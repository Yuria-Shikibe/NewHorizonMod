package newhorizon.content;

import mindustry.content.Planets;
import mindustry.game.Schematics;
import mindustry.graphics.g3d.HexMesh;
import mindustry.graphics.g3d.PlanetGrid;
import mindustry.maps.generators.BlankPlanetGenerator;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.world.meta.Env;

public class NHPlanets{
	public static Planet midantha;
	
	public static void load(){
		midantha = new Planet("midantha", Planets.sun, 2){{
			sectors.add(new Sector(this, PlanetGrid.Ptile.empty));

			sectorApproxRadius = 1;

			visible = true;
			accessible = true;
			alwaysUnlocked = true;

			generator = new NHPlanetGenerator();
			meshLoader = () -> new HexMesh(this, 4);

			defaultEnv = Env.terrestrial;

			iconColor = NHColor.darkEnrColor;
		}};
	}

	public static class NHPlanetGenerator extends BlankPlanetGenerator {
		@Override
		public int getSectorSize(Sector sector){
			return 512;
		}

		@Override
		protected void generate() {
			int sx = width/2, sy = height/2;
			Schematics.placeLaunchLoadout(sx, sy);
		}
	}
}
