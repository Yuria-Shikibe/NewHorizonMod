Log.info("Loaded Cutscene Class Vault");

let loader = Vars.mods.getMod("new-horizon").loader;

function loadContent(fullName){
     return loader.loadClass(fullName).newInstance(); //Garbage things
}

function loadClass(fullName){
     return loader.loadClass(fullName); //Garbage things
}

const UIActions = loadContent("newhorizon.feature.cutscene.UIActions");
const KeyFormat = loadContent("newhorizon.feature.cutscene.KeyFormat");
const WorldActions = loadContent("newhorizon.feature.cutscene.WorldActions");

const CutsceneEventClass = loadClass("newhorizon.feature.cutscene.CutsceneEvent");
const CutsceneEvent = CutsceneEventClass.newInstance();

const CutsceneEventEntity = loadContent("newhorizon.feature.cutscene.CutsceneEventEntity");
const CutsceneScript = loadContent("newhorizon.feature.cutscene.CutsceneScript");
const CCS_Scripts = loadContent("newhorizon.feature.cutscene.CCS_Scripts");
const EventSamples = loadContent("newhorizon.feature.cutscene.EventSamples");


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
const NHFunc = loadContent("newhorizon.func.NHFunc");
const DrawFunc = loadContent("newhorizon.func.DrawFunc");
const Tables = loadContent("newhorizon.ui.Tables");
const TableFunc = loadContent("newhorizon.ui.TableFunc");
const NHInterp = loadContent("newhorizon.func.NHInterp");
const PosLightning = loadContent("newhorizon.feature.PosLightning");

const OFFSET = 12;
const LEN = 60;

const state = Vars.state;
const tilesize = Vars.tilesize;
const world = Vars.world;

function newEvent(name, args){
    return extend(CutsceneEventClass, name, args);
}