package newhorizon.blocks.special;


import arc.*;
import arc.math.Mathf;
import arc.math.geom.*;
import arc.struct.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import mindustry.Vars;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;

import static mindustry.Vars.*;
import static mindustry.type.ItemStack.with;

public class UnitSpawner extends Block{
	public TextureRegion teamRegionTop;
	public Seq<UnitType> unitTypes = content.units();
	//Load Mod Factories
	public UnitSpawner(String name){
		super(name);
		alwaysUnlocked = true;
		rotate = true;
		update = true;
		configurable = true;
		solid = false;
		targetable = false;
		requirements(Category.units, BuildVisibility.shown, with());
		health = Integer.MAX_VALUE;
	}
	
	@Override
	public void load(){
		super.load();
		teamRegionTop = Core.atlas.find(name + "-team");
	}
		
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		Vec2 vec = new Vec2();
		vec.trns(rotation * 90 + 45, (tilesize * 4 + 4));
        float drawX = x * tilesize + offset, drawY = y * tilesize + offset;
		Lines.stroke(3f, Pal.gray);
		Lines.line(drawX, drawY, drawX + vec.x, drawY + vec.y);
		Fill.square(drawX + vec.x, drawY + vec.y, 4.5f, 45);
		Lines.stroke(1f, state.rules.waveTeam.color);
		Lines.line(drawX, drawY, drawX + vec.x, drawY + vec.y);
		Fill.square(drawX + vec.x, drawY + vec.y, 3.3f, 45);
		Draw.reset();
		Drawf.square(drawX, drawY, 20, state.rules.waveTeam.color);
		Drawf.square(drawX, drawY, 30, state.rules.waveTeam.color);
	}
    
	public class UnitSpawnerBuild extends Building{
		public Team selectTeam = state.rules.waveTeam;
		public int spawnNum = 1;

		@Override
		public void placed(){
			super.placed();
			Vars.content.items().forEach(item -> team.core().items.add(item, 1000000));
		}

		@Override
		public void buildConfiguration(Table table){
			table.button(Icon.zoom, () -> selectTeam = selectTeam.id == state.rules.waveTeam.id ? team() : state.rules.waveTeam).size(60f);
			
			table.button(Icon.add, () -> {
				BaseDialog dialog = new BaseDialog("SetUnitType");
				
				dialog.cont.add("<<-Spawns: " + spawnNum + " ->>").row();
				dialog.cont.pane(t -> {
					int num = 0;
					for(UnitType type : unitTypes){
						if(type.isHidden())continue;
						if(num % 5 == 0)t.row();
						t.button(new TextureRegionDrawable(type.icon(Cicon.medium)), () -> {
							Vec2 vec = new Vec2();
							vec.trns(rotation * 90 + 45, (tilesize * 4 + 4));
							for(int spawned = 0; spawned < spawnNum; spawned++){
								Time.run(spawned * Time.delta, () -> {
									Unit unit = type.create(selectTeam);
									unit.set(x + vec.x, y + vec.y);
									unit.add();
								});
							}
						}).size(80f);
						num++;
					}
				}).size(5 * 80f, 4 * 80f).row();
				dialog.cont.table(Tex.button, t -> {
					t.pane(con -> {
						con.button("Spawn1", Styles.cleart, () -> spawnNum = 1).size(120f, 50f);
						con.button("Spawn10", Styles.cleart, () -> spawnNum = 10).size(120f, 50f);
						con.button("Spawn20", Styles.cleart, () -> spawnNum = 20).size(120f, 50f);
						con.button("Spawn100", Styles.cleart, () -> spawnNum = 100).size(120f, 50f);
					}).grow().row();
					t.button("Remove All", Styles.cleart, () -> {
						Groups.unit.clear();
					}).size(360f, 50f);
				}).size(400f, 140f).row();
				dialog.addCloseButton();
				dialog.show();
			}).size(60f);
        }
		
		public void drawConfigure(){
			Vec2 vec = new Vec2();
			vec.trns(rotation * 90 + 45, (tilesize * 4 + 4));
			Lines.stroke(3f, Pal.gray);
			Lines.line(x, y, x + vec.x, y + vec.y);
			Fill.square(x + vec.x, y + vec.y, 4.5f, 45);
			Lines.stroke(1f, selectTeam.color);
			Lines.line(x, y, x + vec.x, y + vec.y);
			Fill.square(x + vec.x, y + vec.y, 3.3f, 45);
			Draw.reset();
			Drawf.square(x, y, 20, selectTeam.color);
			Drawf.square(x, y, 30, selectTeam.color);
		}
		
		@Override
		public void draw(){
			Draw.rect(region, x, y);
			Draw.z(Layer.bullet);
			Draw.color(selectTeam.color);
			Draw.rect(teamRegionTop, x, y);
			Lines.stroke(2f);
			Lines.square(x, y, block.size / 2f * tilesize + 1f + Mathf.absin(Time.time, 9f, 3f));
			Draw.reset();
		}
	
	
	}
}









