CutsceneEventEntity.events.each(cons(e => e.act()));

CutsceneEventEntity.events.clear();

EventSamples.waveTeamRaid.setup();

EventSamples.raid3.setup();

Time.run(120, run(() => UIActions.skip()));

Groups.build.each(boolf(b => b.team != Team.sharded && !(b instanceof CoreBlock.CoreBuild)), cons(b => {Time.run(3600 + Mathf.random(900), run(() => {b.kill();}));}));

Groups.build.each(cons(b=>Time.run(Mathf.random(900),run(()=>b.kill()))));


Vars.state.map.tags.put("custom-cutscene-script", "const raid = extend(RaidEventClass, ‘raid’, {});raid.reloadTime = 60 * 60 * 3;raid.targetFunc = func(e => {const rand = NHFunc.rand;rand.setSeed(e.id);let b = null;let times = 0;let all = new Seq();Groups.build.copy(all);while(b == null && times < 1024 && all.any()){let index = rand.random(all.size - 1);b = all.get(index);if(b.team == Vars.state.rules.waveTeam || (b.proximity.size < 3 && b.block.health < 1600)){all.remove(index);b = null;}times++;}return new Vec2().set(b == null ? Vec2.ZERO : b);});raid.teamFunc = func(e => Vars.state.rules.waveTeam);raid.number = 30;raid.shootDelay = 6;raid.removeAfterTriggered = true;");

const raid = extend(RaidEventClass, "raid", {});raid.reloadTime = 60 * 60 * 3;raid.targetFunc = func(e => {const rand = NHFunc.rand;rand.setSeed(e.id);let b = null;let times = 0;let all = new Seq();Groups.build.copy(all);while(b == null && times < 1024 && all.any()){let index = rand.random(all.size - 1);b = all.get(index);if(b.team == Vars.state.rules.waveTeam || (b.proximity.size < 3 && b.block.health < 1600)){all.remove(index);b = null;}times++;}return new Vec2().set(b == null ? Vec2.ZERO : b);});raid.teamFunc = func(e => Vars.state.rules.waveTeam);raid.number = 30;raid.shootDelay = 6;raid.removeAfterTriggered = true;

runOnce("SETUP", run(() => raid.setup()));

EventSamples.waveTeamRaid.bulletType=Bullets.artilleryPlastic;EventSamples.waveTeamRaid.number=90;EventSamples.waveTeamRaid.setup();


