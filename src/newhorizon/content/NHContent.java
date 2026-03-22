package newhorizon.content;

import arc.Core;
import arc.func.Cons;
import arc.func.Func;
import arc.func.Prov;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.util.Log;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.game.MapObjectives;
import mindustry.gen.Icon;
import mindustry.gen.LogicIO;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LStatement;
import mindustry.world.meta.Attribute;
import newhorizon.NewHorizon;
import newhorizon.expand.block.distribution.platform.FloatPlatformDrawer;
import newhorizon.expand.entities.UltFire;
import newhorizon.expand.game.MapMarker.RaidIndicator;
import newhorizon.expand.game.MapObjectives.ReuseObjective;
import newhorizon.expand.game.MapObjectives.TriggerObjective;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ThreatLevel;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.CutsceneControl;
import newhorizon.expand.logic.components.action.*;
import newhorizon.expand.logic.cutscene.action.*;
import newhorizon.expand.logic.cutscene.actionBus.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NHContent extends Content {
    public static final float GRAVITY_TRAP_LAYER = Layer.light + 2.472f;
    public static final float HEX_SHIELD_LAYER = 56.172f;
    public static final float QUANTUM_LAYER = Layer.blockOver + 0.1919f;
    public static final float POWER_AREA = Layer.power + 0.114f;
    public static final float POWER_DYNAMIC = Layer.power + 0.514f;

    public static Texture smoothNoise, particleNoise, darkerNoise, noise;

    public static CacheLayer quantumLayer, armorLayer;

    public static TextureRegion
            crossRegion, sourceCenter, timeIcon, xenIcon,
            iconLevel, ammoInfo, arrowRegion, pointerRegion, icon, icon2, upgrade, upgrade2,
            linkArrow, activeBoost,
            beamLaser, beamLaserEnd, beamLaserInner, beamLaserInnerEnd;

    public static TextureRegion //UI
            raid, objective, fleet, capture;

    public static Attribute quantum, density;

    public static LCategory
            nhwproc, nhcutscene, nhaction,
            actionCameraControl, actionInputControl, actionCurtainControl, actionFlowControl;

    public static void loadPriority() {
        new NHContent().load();
    }

    public static void loadBeforeContentLoad() {
        CacheLayer.add(quantumLayer = new CacheLayer.ShaderLayer(NHShaders.quantum));
        quantum = Attribute.add("quantum");
        density = Attribute.add("density");
    }

    public static void loadLast() {
        nhwproc = new LCategory("nh-wproc", Pal.heal.cpy().lerp(Pal.gray, 0.2f));
        nhcutscene = new LCategory("nh-cutscene", Pal.remove.cpy().lerp(Pal.gray, 0.3f));

        nhaction = new LCategory("nh-action", Pal.surge.cpy().lerp(Pal.gray, 0.3f));

        actionFlowControl = new LCategory("nh-action-flow-control", Pal.surge.cpy().lerp(Pal.gray, 0.3f).shiftHue(0f));
        actionCameraControl = new LCategory("nh-action-camera-control", Pal.surge.cpy().lerp(Pal.gray, 0.3f).shiftHue(20f));
        actionInputControl = new LCategory("nh-action-input-control", Pal.surge.cpy().lerp(Pal.gray, 0.3f).shiftHue(40f));
        actionCurtainControl = new LCategory("nh-action-curtain-control", Pal.surge.cpy().lerp(Pal.gray, 0.3f).shiftHue(60f));

        ThreatLevel.init();

        //registerStatement("gravitywell", GravityWell::new, GravityWell::new);
        //registerStatement("linetarget", LineTarget::new, LineTarget::new);
        //registerStatement("randspawn", RandomSpawn::new, RandomSpawn::new);
        //registerStatement("randtarget", RandomTarget::new, RandomTarget::new);
        //registerStatement("teamthreat", TeamThreat::new, TeamThreat::new);
        //registerStatement("raidcontrol", RaidControl::new, RaidControl::new);
        //registerStatement("defaultraid", DefaultRaid::new, DefaultRaid::new);

        registerStatement(InitActons.class);
        registerStatement(SaveActions.class);
        registerStatement(GetActions.class);
        registerStatement(RunMainBus.class);
        registerStatement(RunSubBus.class);

        CutsceneControl.registerAction(NullAction.class);

        loadActions();

        MapObjectives.registerObjective(ReuseObjective::new);
        MapObjectives.registerObjective(TriggerObjective::new);
        MapObjectives.registerMarker(RaidIndicator::new);
    }

    public static void loadActions() {
        registerAction(Wait.class, WaitAction.class);

        registerAction(CurtainFadeIn.class, CurtainFadeInAction.class);
        registerAction(CurtainFadeOut.class, CurtainFadeOutAction.class);

        registerAction(CameraControl.class, CameraControlAction.class);
        registerAction(CameraZoom.class, CameraZoomAction.class);
        registerAction(CameraReset.class, CameraResetAction.class);

        registerAction(InputLock.class, InputLockAction.class);
        registerAction(InputUnlock.class, InputUnlockAction.class);
    }

    public static void registerAction(Class<? extends ActionLStatement> lstatement, Class<? extends Action> actionClass) {
        registerStatement(lstatement);
        CutsceneControl.registerAction(actionClass);
    }

    public static void registerStatement(Class<? extends ActionLStatement> lstatement){
        try {
            Constructor<? extends ActionLStatement> parserCons = lstatement.getDeclaredConstructor(String[].class);
            Constructor<? extends ActionLStatement> defaultCons = lstatement.getDeclaredConstructor();
            Method getNameMethod = lstatement.getDeclaredMethod("getLStatementName");

            parserCons.setAccessible(true);
            defaultCons.setAccessible(true);
            getNameMethod.setAccessible(true);

            String name;

            ActionLStatement tempInstance = defaultCons.newInstance();
            name = (String) getNameMethod.invoke(tempInstance);

            LAssembler.customParsers.put(name, (tokens) -> {
                try {
                    return parserCons.newInstance((Object) tokens);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to create instance of " + lstatement.getSimpleName() + " with tokens", e);
                }
            });
            LogicIO.allStatements.addUnique(() -> {
                try {
                    return defaultCons.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to create default instance of " + lstatement.getSimpleName(), e);
                }
            });
        }catch (Exception e){
            Log.err(e);
        }
    }

    public static void registerStatement(String name, Func<String[], LStatement> func, Prov<LStatement> prov) {
        LAssembler.customParsers.put(name, func);
        LogicIO.allStatements.addUnique(prov);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.error;
    }

    public void load() {
        if (Vars.headless) return;

        Icon.icons.put("midantha", new TextureRegionDrawable(Core.atlas.find(NewHorizon.name("midantha"))));
        Icon.icons.put("nh", new TextureRegionDrawable(Core.atlas.find(NewHorizon.name("icon-2"))));
        UltFire.load();

        crossRegion = Core.atlas.find("cross");
        sourceCenter = Core.atlas.find(NewHorizon.name("source-center"));
        timeIcon = Core.atlas.find(NewHorizon.name("time-icon"));
        xenIcon = Core.atlas.find(NewHorizon.name("xen-icon"));
        upgrade = Core.atlas.find(NewHorizon.name("upgrade"));
        upgrade2 = Core.atlas.find(NewHorizon.name("upgrade2"));
        arrowRegion = Core.atlas.find(NewHorizon.name("jump-gate-arrow"));
        ammoInfo = Core.atlas.find(NewHorizon.name("upgrade-info"));
        iconLevel = Core.atlas.find(NewHorizon.name("level-up"));
        pointerRegion = Core.atlas.find(NewHorizon.name("jump-gate-pointer"));
        icon = Core.atlas.find(NewHorizon.name("icon-white"));
        icon2 = Core.atlas.find(NewHorizon.name("icon-2"));

        raid = Core.atlas.find(NewHorizon.name("raid"));
        objective = Core.atlas.find(NewHorizon.name("objective"));
        fleet = Core.atlas.find(NewHorizon.name("fleet"));
        capture = Core.atlas.find(NewHorizon.name("capture"));

        linkArrow = Core.atlas.find(NewHorizon.name("linked-arrow"));
        activeBoost = Core.atlas.find(NewHorizon.name("active-boost"));

        beamLaser = Core.atlas.find(NewHorizon.name("stream-beam"));
        beamLaserEnd = Core.atlas.find(NewHorizon.name("stream-beam-end"));
        beamLaserInner = Core.atlas.find(NewHorizon.name("stream-beam-inner"));
        beamLaserInnerEnd = Core.atlas.find(NewHorizon.name("stream-beam-inner-end"));

        FloatPlatformDrawer.load();

        smoothNoise = loadTex("smooth-noise", t -> {
            t.setFilter(Texture.TextureFilter.linear);
            t.setWrap(Texture.TextureWrap.repeat);
        });

        particleNoise = loadTex("particle-noise", t -> {
            t.setFilter(Texture.TextureFilter.linear);
            t.setWrap(Texture.TextureWrap.repeat);
        });

        darkerNoise = loadTex("darker-noise", t -> {
            t.setFilter(Texture.TextureFilter.linear);
            t.setWrap(Texture.TextureWrap.repeat);
        });

        noise = loadTex("noise", t -> {
            t.setFilter(Texture.TextureFilter.linear);
            t.setWrap(Texture.TextureWrap.repeat);
        });
    }

    Texture loadTex(String name, Cons<Texture> modifier) {
        Texture tex = new Texture(NewHorizon.MOD.root.child("textures").child(name + (name.endsWith(".png") ? "" : ".png")));
        modifier.get(tex);

        return tex;
    }
}
