const loader = Vars.mods.getMod("new-horizon").loader;

function loadContent(fullName){
     return loader.loadClass(fullName).newInstance(); //Garbage things
}

function loadClass(fullName){
     return loader.loadClass(fullName); //Garbage things
}

const UIActions = loadContent("newhorizon.util.feature.cutscene.UIActions");
const KeyFormat = loadContent("newhorizon.util.feature.cutscene.KeyFormat");
const WorldActions = loadContent("newhorizon.util.feature.cutscene.WorldActions");

const CutsceneEventClass = loadClass("newhorizon.util.feature.cutscene.CutsceneEvent");
const CutsceneEvent = CutsceneEventClass.newInstance();

const CutsceneEventEntity = loadContent("newhorizon.util.feature.cutscene.CutsceneEventEntity");
const CutsceneScript = loadContent("newhorizon.util.feature.cutscene.CutsceneScript");
const CCS_Scripts = CutsceneScript.scripts;
const EventSamples = loadContent("newhorizon.util.feature.cutscene.EventSamples");

const NHBlocks = loadContent("newhorizon.content.NHBlocks");
const NHBullets = loadContent("newhorizon.content.NHBullets");
const NHItems = loadContent("newhorizon.content.NHItems");
const NHLiquids = loadContent("newhorizon.content.NHLiquids");
const NHSounds = loadContent("newhorizon.content.NHSounds");
const NHWeathers = loadContent("newhorizon.content.NHWeathers");
const NHUnitTypes = loadContent("newhorizon.content.NHUnitTypes");
const NHStatusEffects = loadContent("newhorizon.content.NHStatusEffects");
const NHSectorPresets = loadContent("newhorizon.content.NHSectorPresets");
const NHFx = loadContent("newhorizon.content.NHFx");
const NHColor = loadContent("newhorizon.content.NHColor");
const NHPlanets = loadContent("newhorizon.content.NHPlanets");
const NHFunc = loadContent("newhorizon.util.func.NHFunc");
const DrawFunc = loadContent("newhorizon.util.graphic.DrawFunc");
const Tables = loadContent("newhorizon.util.ui.Tables");
const TableFunc = loadContent("newhorizon.util.ui.TableFunc");
const NHInterp = loadContent("newhorizon.util.func.NHInterp");
const PosLightning = loadContent("newhorizon.util.feature.PosLightning");

const CCS_JsonHandler = loadContent("newhorizon.util.feature.cutscene.CCS_JsonHandler");

const BulletHandler = loadContent("newhorizon.util.feature.cutscene.events.util.BulletHandler");
const PreMadeRaids = loadContent("newhorizon.util.feature.cutscene.events.util.PreMadeRaids");
const AutoEventTrigger = loadContent("newhorizon.util.feature.cutscene.events.util.AutoEventTrigger");

const OV_Pair = loadContent("newhorizon.util.func.OV_Pair");

const FleetEventClass = loadClass("newhorizon.util.feature.cutscene.events.FleetEvent");
const ObjectiveEventClass = loadClass("newhorizon.util.feature.cutscene.events.ObjectiveEvent");
const RaidEventClass = loadClass("newhorizon.util.feature.cutscene.events.RaidEvent");
const SignalEventClass = loadClass("newhorizon.util.feature.cutscene.events.SignalEvent");
const DestroyObjectiveEventClass = loadClass("newhorizon.util.feature.cutscene.events.DestroyObjectiveEvent");
const SimpleReloadEventClass = loadClass("newhorizon.util.feature.cutscene.events.SimpleReloadEvent");
const ReachWaveObjectiveClass = loadClass("newhorizon.util.feature.cutscene.events.ReachWaveObjective");

const NHGroups = loadContent("newhorizon.expand.entities.NHGroups");
const NHBlending = loadContent("newhorizon.util.graphic.NHBlending");

const OFFSET = 12;
const LEN = 60;

const state = Vars.state;
const tilesize = Vars.tilesize;
const world = Vars.world;

function handleEvent(event){
    CutsceneEvent.eventHandled = event;
}

function newEvent(name, args){
    return extend(CutsceneEventClass, name, args);
}

function playerInit(run){
    CutsceneScript.curIniter.add(run);
}

function playerUpdate(run){
    CutsceneScript.curUpdater.add(run);
}

function playerEnd(run){
    CutsceneScript.curEnder.add(run);
}

function runOnce(tag, run){
    CutsceneScript.runEventOnce(tag, run);
}

function bigEventLaunch(actions){
    const events = new Seq(actions.length + 2);

    events.add(UIActions.startCutsceneDefault());
    events.addAll(actions);
    events.add(UIActions.endCutsceneDefault());

    UIActions.actionSeq(events.toArray(Action));
}

function actionSeq(actions){
    return UIActions.actionSeq(actions);
}

function actionSeqMinor(actions){
    return UIActions.actionSeqMinor(actions);
}

function enemyTeam(){return Vars.state.rules.waveTeam;}
function allyTeam(){return Vars.state.rules.defaultTeam;}

Log.info("Loaded Cutscene Class Vault");