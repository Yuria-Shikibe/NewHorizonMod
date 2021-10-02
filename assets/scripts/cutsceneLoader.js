Log.info("Loaded Cutscene Vault");

const loader = Vars.mods.getMod(modName).loader;

const loadClass = (fullName) => loader.loadClass(fullName).newInstance(); //Garbage things

const CutsceneScript = loadClass("newhorizon.feature.CutsceneScript");
const UIActions = loadClass("newhorizon.feature.CutsceneScript$UIActions");
const KeyFormat = loadClass("newhorizon.feature.CutsceneScript$KeyFormat");
const WorldActions = loadClass("newhorizon.feature.CutsceneScript$WorldActions");

const NHBlocks = loadClass("newhorizon.content.NHBlocks");
const NHBullets = loadClass("newhorizon.content.NHBullets");
const NHItems = loadClass("newhorizon.content.NHItems");
const NHLiquids = loadClass("newhorizon.content.NHLiquids");
const NHSounds = loadClass("newhorizon.content.NHSounds");
const NHWeathers = loadClass("newhorizon.content.NHWeathers");
const NHUnitTypes = loadClass("newhorizon.content.NHUnitTypes");
const NHStatusEffects = loadClass("newhorizon.content.NHStatusEffects");
const NHSectorPresets = loadClass("newhorizon.content.NHSectorPresets");
const NHFx = loadClass("newhorizon.content.NHFx");
const NHColor = loadClass("newhorizon.content.NHColor");
const NHPlanets = loadClass("newhorizon.content.NHPlanets");
const NHFunc = loadClass("newhorizon.func.NHFunc");
const DrawFunc = loadClass("newhorizon.func.DrawFunc");
const Tables = loadClass("newhorizon.func.Tables");
const TableFunc = loadClass("newhorizon.func.TableFunc");
const NHInterp = loadClass("newhorizon.func.NHInterp");
const PosLightning = loadClass("newhorizon.feature.PosLightning");

const OFFSET = 12, LEN = 60;

const state = Vars.state;
const tilesize = Vars.tilesize;
const world = Vars.world;