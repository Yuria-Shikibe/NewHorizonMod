package newhorizon.content;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Layer;
import mindustry.world.meta.Attribute;
import newhorizon.NewHorizon;
import newhorizon.expand.block.distribution.platform.FloatPlatformDrawer;
import newhorizon.expand.entities.UltFire;
import newhorizon.expand.logic.ThreatLevel;

public class NHContent{
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

    public static void loadPriority() {
        new NHContent().load();
    }

    public static void loadBeforeContentLoad() {
        CacheLayer.add(quantumLayer = new CacheLayer.ShaderLayer(NHShaders.quantum));
        quantum = Attribute.add("quantum");
        density = Attribute.add("density");
    }

    public static void loadLast() {

        ThreatLevel.init();

        //registerStatement("gravitywell", GravityWell::new, GravityWell::new);
        //registerStatement("linetarget", LineTarget::new, LineTarget::new);
        //registerStatement("randspawn", RandomSpawn::new, RandomSpawn::new);
        //registerStatement("randtarget", RandomTarget::new, RandomTarget::new);
        //registerStatement("teamthreat", TeamThreat::new, TeamThreat::new);
        //registerStatement("raidcontrol", RaidControl::new, RaidControl::new);
        //registerStatement("defaultraid", DefaultRaid::new, DefaultRaid::new);

        //MapObjectives.registerMarker(RaidIndicator::new);
    }

    public void load() {
        if (Vars.headless) return;

        Icon.icons.put("midantha", new TextureRegionDrawable(Core.atlas.find(NewHorizon.name("midantha"))));
        Icon.icons.put("nh", new TextureRegionDrawable(Core.atlas.find(NewHorizon.name("icon-2"))));

        UltFire.load();

        crossRegion = Core.atlas.find("cross");
        sourceCenter = Core.atlas.find(NewHorizon.name("source-center"));
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
