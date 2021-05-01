package newhorizon.func;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;
import newhorizon.block.special.JumpGate;
import newhorizon.bullets.EffectBulletType;
import newhorizon.content.NHBlocks;
import newhorizon.content.NHFx;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.tilesize;

public class FContents{
	public static class SpawnerData{
		@NotNull public final JumpGate.UnitSet set;
		@NotNull public final Color spawnColor;
		public final float spawnRange, spawnDelay;
		
		public SpawnerData(@NotNull JumpGate.UnitSet set, float spawnRange, float spawnDelay, @NotNull Color spawnColor){
			this.set = set;
			this.spawnRange = spawnRange;
			this.spawnDelay = spawnDelay;
			this.spawnColor = spawnColor;
		}
	}
	public static BulletType spawnUnitDrawer = new EffectBulletType(10){
		@Override
		public void init(Bullet b){
			if(!(b.data instanceof SpawnerData)){
				b.remove();
				return;
			}
			SpawnerData data = (SpawnerData)b.data;
			NHFx.spawnWave.at(b.x, b.y, data.spawnRange, data.spawnColor);
		}
		
		@Override
		public void draw(Bullet b){
			if(!(b.data instanceof SpawnerData)){
				b.remove();
				return;
			}
			SpawnerData data = (SpawnerData)b.data;
			
			Color spawnColor = data.spawnColor;
			TextureRegion
				pointerRegion = ((JumpGate)NHBlocks.jumpGate).pointerRegion,
				arrowRegion = ((JumpGate)NHBlocks.jumpGate).arrowRegion;
			
			float regSize = Functions.regSize(data.set.type);
			Draw.color(spawnColor);
			for(int i = 0; i < 4; i++){
				float sin = Mathf.absin(Time.time, 16f, tilesize);
				float length = (tilesize * 5 + sin) * b.fout() + tilesize;
				float signSize = regSize + 0.75f + Mathf.absin(Time.time + 8f, 8f, 0.15f);
				Tmp.v1.trns(i * 90, -length);
				Draw.rect(pointerRegion, b.x + Tmp.v1.x, b.y + Tmp.v1.y, pointerRegion.width * Draw.scl * signSize, pointerRegion.height * Draw.scl * signSize, i * 90 - 90);
			}
			
			for(int i = -4; i <= 4; i++){
				if(i == 0)continue;
				Tmp.v1.trns(b.rotation(), i * tilesize * 2);
				float f = (100 - (Time.time - 12.5f * i) % 100) / 100;
				Draw.rect(arrowRegion, b.x + Tmp.v1.x, b.y + Tmp.v1.y, arrowRegion.width * (regSize / 2f + Draw.scl) * f, arrowRegion.height * (regSize / 2f + Draw.scl) * f, b.rotation() - 90);
			}
			DrawFuncs.overlayText(Fonts.tech, String.valueOf(Mathf.ceil((b.lifetime - b.time) / 60f)), b.x, b.y, 0, 0,0.25f, spawnColor, false, true);
			Draw.reset();
		}
		
		@Override
		public void despawned(Bullet b){
			if(!(b.data instanceof SpawnerData)){
				b.remove();
				return;
			}
			SpawnerData data = (SpawnerData)b.data;
			
			UnitType type = data.set.type;
			float regSize = Functions.regSize(type);
			final Color spawnColor = data.spawnColor;
			
			NHFx.spawn.at(b.x, b.y, regSize, spawnColor, b);
			
			Unit unit = type.create(b.team());
			unit.set(b.x, b.y);
			unit.rotation = b.rotation();
			if(!Vars.net.client())unit.add();
			
			if(type.flying){
				NHFx.jumpTrail.at(unit.x, unit.y, b.rotation(), spawnColor, unit);
				unit.vel.add(Tmp.v1.trns(b.rotation(), type.speed * (1 + type.drag * 5) * 2).scl(type.drag + 2));
			}else{
				Fx.unitSpawn.at(unit.x, unit.y, b.rotation(), type);
				Time.run(Fx.unitSpawn.lifetime, () -> {
					for(int j = 0; j < 3; j++){
						Time.run(j * 8, () -> Fx.spawn.at(unit));
					}
					Effect.shake(type.hitSize / 2.4f, data.spawnDelay * 4, unit);
					NHFx.spawnGround.at(unit.x, unit.y, type.hitSize / tilesize * 3, spawnColor);
					NHFx.circle.at(unit.x, unit.y, type.hitSize * 4, spawnColor);
				});
			}
			Sounds.plasmaboom.at(unit.x, unit.y);
		}
	};
	
}
