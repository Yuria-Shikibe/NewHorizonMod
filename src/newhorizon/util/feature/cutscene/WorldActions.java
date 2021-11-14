package newhorizon.util.feature.cutscene;

import arc.audio.Sound;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.scene.actions.Actions;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Entityc;
import mindustry.gen.Icon;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.content.NHSounds;
import newhorizon.util.feature.cutscene.actions.CautionAction;
import newhorizon.util.func.NHFunc;

import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class WorldActions{
	public static void signal(float x, float y, float size, float cautionRange, float inaccuracyScl, float time, Color color, Sound sound){
		if(headless)return;
		float f = player.dst(x, y) / cautionRange;
		
		if(player == null || f > 1)return;
		
		f = Mathf.curve(f, 0.05f, 1);
		
		UIActions.actionSeqMinor(Actions.parallel(
			Actions.run(sound::play),
			UIActions.customCautionAt(x + Mathf.range(inaccuracyScl * cautionRange) * f, y + Mathf.range(inaccuracyScl * cautionRange) * f, size, time, color, CautionAction.MarkStyles.shake)
		));
	}
	
	public static void signalDef(float x, float y, float cautionRange, Color color){
		signal(x, y, 2, cautionRange, 0.75f, 0.5f, color, NHSounds.signal);
	}
	
	public static void signalTriggered(float x, float y, float range, Runnable runnable){
		if(headless)return;
		if(player.within(x, y, range))runnable.run();
	}
	
	public static boolean signalNearby(float x, float y, float range){
		if(headless)return false;
		return player.within(x, y, range);
	}
	
	/**
	 * Use a certain {@link BulletType} bullet to attack a certain place. The {@code lifetime} of the bullet will be
	 * scaled.
	 *
	 * @param type     The bullet type to be shot.
	 * @param owner    The owner of the bullet. Usually use a {@link mindustry.world.blocks.storage.CoreBlock.CoreBuild}
	 *                 core of a certain team.
	 * @param team     The team of the owner of the bullet.
	 * @param modifier A {@link Cons} modifier that used to modify the bullet entity {@link Bullet}.
	 * @param x        Use *8 format.
	 * @param y        Use *8 format.
	 * @param toX      Use *8 format.
	 * @param toY      Use *8 format.
	 *
	 * @return Returns a {@link Runnable} runnable function. Use {@code run()} to act it.
	 *
	 * @see mindustry.content.Bullets
	 * @see newhorizon.content.NHBullets
	 * @see BulletType
	 * @see Cons
	 * @see Entityc
	 * @see Bullet
	 * @see Team
	 */
	public static Runnable raidPos(Entityc owner, Team team, BulletType type, float x, float y, float toX, float toY, Cons<Bullet> modifier){
		float scl = NHFunc.scaleBulletLifetime(type, x, y, toX, toY);
		
		
		return () -> {
			modifier.get(type.create(owner, team, x, y, Angles.angle(x, y, toX, toY), 1, scl));
		};
	}
	
	public static Runnable raidPos(Entityc owner, Team team, BulletType type, float x, float y, Position target, Cons<Bullet> modifier){
		float scl = NHFunc.scaleBulletLifetime(type, x, y, target.getX(), target.getY());
		
		return () -> {
			modifier.get(type.create(owner, team, x, y, Angles.angle(x, y, target.getX(), target.getY()), 1, scl));
		};
	}
	
	public static Runnable raidDirection(Entityc owner, Team team, BulletType type, float x, float y, float angle, float distance, Cons<Bullet> modifier){
		return raidPos(owner, team, type, x, y, CutsceneScript.v1.trns(angle, distance).add(x, y), modifier);
	}
	
	public static void raidFromCoreToCore(Team from, Team target, BulletType ammo, int number, float shootDelay, float randP, float inaccuracy){
		CoreBlock.CoreBuild coreFrom = from.cores().firstOpt();
		CoreBlock.CoreBuild coreTarget = state.teams.closestCore(coreFrom.x, coreFrom.y, target);
		
		UIActions.actionSeq(Actions.parallel(UIActions.cautionAt(coreTarget.x, coreTarget.y, coreTarget.block.size * tilesize / 3.5f, number * shootDelay / 60f, coreTarget.team.color), Actions.run(() -> {
			NHSounds.alarm.play();
			for(int i = 0; i < number; i++){
				Time.run(i * shootDelay, WorldActions.raidPos(coreFrom, coreFrom.team, ammo, coreFrom.x + Mathf.range(randP), coreFrom.y + Mathf.range(randP), coreTarget, b -> {
					b.vel.rotate(Mathf.range(inaccuracy));
					if(b.type.shootEffect != null)
						b.type.shootEffect.at(b.x, b.y, b.angleTo(coreTarget), b.type.hitColor);
					if(b.type.smokeEffect != null)
						b.type.smokeEffect.at(b.x, b.y, b.angleTo(coreTarget), b.type.hitColor);
				}));
			}
		}), UIActions.labelAct("[accent]Caution[]: @@@Raid Incoming.", 0.75f, number * shootDelay / 60f, Interp.linear, t -> {
			t.image(Icon.warning).padRight(OFFSET);
		})));
	}
	
	public static void raidFromCoreToCoreDefault(BulletType ammo, int number, float shootDelay, float randP, float inaccuracy){
		raidFromCoreToCore(state.rules.waveTeam, state.rules.defaultTeam, ammo, number, shootDelay, randP, inaccuracy);
	}
}
