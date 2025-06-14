package newhorizon.content;

import arc.Core;
import arc.files.Fi;
import arc.func.Cons;
import arc.func.Func;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.util.Log;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.game.MapObjectives;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
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
import newhorizon.expand.entities.UltFire;
import newhorizon.expand.game.MapMarker.RaidIndicator;
import newhorizon.expand.game.MapObjectives.ReuseObjective;
import newhorizon.expand.game.MapObjectives.TriggerObjective;
import newhorizon.expand.logic.DefaultRaid;
import newhorizon.expand.logic.ThreatLevel;
import newhorizon.expand.logic.statements.*;

import java.io.IOException;

import static mindustry.Vars.renderer;

public class NHContent extends Content {
    public static final float GRAVITY_TRAP_LAYER = Layer.light + 2.472f; // Making it wried
    public static final float XEN_LAYER = Layer.block - 0.003f;
    public static final float POWER_AREA = Layer.power + 0.114f;
    public static final float POWER_DYNAMIC = Layer.power + 0.514f;

    public static Fi scheDir;

    public static Schematic mLoadout, nhBaseLoadout;

    public static Texture smoothNoise, particleNoise, darkerNoise, armorTex/*, platingNoise*/;

    public static CacheLayer quantumLayer, armorLayer;

    public static TextureRegion
            crossRegion, sourceCenter, timeIcon, xenIcon,
            iconLevel, ammoInfo, arrowRegion, pointerRegion, icon, icon2, upgrade, upgrade2,
            linkArrow, activeBoost;

    public static TextureRegion //UI
            raid, objective, fleet, capture;

    public static TextureRegion khs0, khs1, ks1, ks2, ks3, ks4, ks5, ks6, ks7, ks8;

    public static Attribute quantum;

    public static LCategory nhwproc, nhwprocevent, nhcutscene, nhaction, nhcamera, nhcurtain, nhinfo, nhevent, nhsignal, nhui, nhalert;

    public static void loadPriority() {
        new NHContent().load();
    }

    public static void loadBeforeContentLoad() {
        CacheLayer.add(quantumLayer = new CacheLayer.ShaderLayer(NHShaders.quantum) {
        });
        CacheLayer.add(armorLayer = new CacheLayer.ShaderLayer(NHShaders.tiler) {
            @Override
            public void begin() {
                renderer.effectBuffer.begin();
                Core.graphics.clear(Color.clear);
                renderer.blocks.floor.beginc();
            }

            @Override
            public void end() {
                renderer.effectBuffer.end();

                NHShaders.tiler.texture = armorTex;
                renderer.effectBuffer.blit(shader);

                renderer.blocks.floor.beginc();
            }
        });

        quantum = Attribute.add("quantum");
    }

    public static void loadLast() {
        nhwproc = new LCategory("nh-wproc", Pal.heal.cpy().lerp(Pal.gray, 0.2f));
        nhcutscene = new LCategory("nh-cutscene", Pal.remove.cpy().lerp(Pal.gray, 0.3f));
        nhaction = new LCategory("nh-action", Pal.surge.cpy().lerp(Pal.gray, 0.3f));

        ThreatLevel.init();

        LAssembler.customParsers.put("gravitywell", GravityWell::new);
        LAssembler.customParsers.put("linetarget", LineTarget::new);
        LAssembler.customParsers.put("randspawn", RandomSpawn::new);
        LAssembler.customParsers.put("randtarget", RandomTarget::new);
        LAssembler.customParsers.put("teamthreat", TeamThreat::new);

        LAssembler.customParsers.put("raidcontrol", RaidControl::new);

        LogicIO.allStatements.addUnique(GravityWell::new);
        LogicIO.allStatements.addUnique(LineTarget::new);
        LogicIO.allStatements.addUnique(RandomSpawn::new);
        LogicIO.allStatements.addUnique(RandomTarget::new);
        LogicIO.allStatements.addUnique(TeamThreat::new);

        LogicIO.allStatements.addUnique(RaidControl::new);

        registerStatement("defaultraid", DefaultRaid::new, DefaultRaid::new);

        MapObjectives.registerObjective(ReuseObjective::new);
        MapObjectives.registerObjective(TriggerObjective::new);

        MapObjectives.registerMarker(RaidIndicator::new);
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

        scheDir = NewHorizon.MOD.root.child("schematics-bases");


        try {
            NHSchematic.load();

            mLoadout = Schematics.read(scheDir.child("init-loadout" + ".msch"));
            nhBaseLoadout = Schematics.readBase64("bXNjaAF4nI3QTQuCMBgH8L+VQhlEBnWtD7AYpXePHaIvEB2mPeSgtpiK0afPilCww3bYDs9vzxtm8PsYKHEjTA9U7bSRT632Wpx1WcA/U54aeS+kVgC8q0jomqN3PPWwUFSx7OtZqWTBZH2RwbwdUPQoc5ZqQ/V/H+8zcLBqk4soMjJxHIfRmtdPFPI1/8iRLe3DremyTU0mlW5kuPlKr1s/MUKlWUO3/Ec79f9TB0O4737H9snHtsmBifVwgf1yA9vlvgAW2YvD");
        } catch (IOException e) {
            Log.info(e);
        }

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

        khs0 = Core.atlas.find(NewHorizon.name("kill-streak-hs-0"));
        khs1 = Core.atlas.find(NewHorizon.name("kill-streak-hs-1"));
        ks1 = Core.atlas.find(NewHorizon.name("kill-streak-1"));
        ks2 = Core.atlas.find(NewHorizon.name("kill-streak-2"));
        ks3 = Core.atlas.find(NewHorizon.name("kill-streak-3"));
        ks4 = Core.atlas.find(NewHorizon.name("kill-streak-4"));
        ks5 = Core.atlas.find(NewHorizon.name("kill-streak-5"));
        ks6 = Core.atlas.find(NewHorizon.name("kill-streak-6"));
        ks7 = Core.atlas.find(NewHorizon.name("kill-streak-7"));
        ks8 = Core.atlas.find(NewHorizon.name("kill-streak-8"));

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

        armorTex = loadTex("armor", t -> {
            t.setFilter(Texture.TextureFilter.nearest);
            t.setWrap(Texture.TextureWrap.repeat);
        });
    }

    Texture loadTex(String name, Cons<Texture> modifier) {
        Texture tex = new Texture(NewHorizon.MOD.root.child("textures").child(name + (name.endsWith(".png") ? "" : ".png")));
        modifier.get(tex);

        return tex;
    }
}
