NHGroups.events.each(cons(e=>e.act()));

Draw.blend();
Draw.blend(NHBlending.test3);
Draw.blend(new Blending(Gl.blendSrcRgb, Gl.blendEquationRgb));

print(NHWeathers.quantumStorm.isActive())
Groups.weather.clear();
NHWeathers.quantumField.create(Mathf.random(1, 2), 300);
NHWeathers.quantumStorm.instance().windVector.rotate(5);

NHWeathers.quantumStorm.create(Mathf.random(1, 2), 300);

NHWeathers.quantumStorm.instance().intensity += 0.5;
NHWeathers.quantumStorm.instance().intensity -= 0.5;


NHGroups.events.clear();

EventSamples.waveTeamRaid.setup();

NHGroups.events.getByID(ID)

EventSamples.jumpgateUnlock.setup()
PreMadeRaids.standardRaid1.setup();

for(var i=0;i<20;i++)PreMadeRaids.deadlyRaid2.setup();

Time.run(120, run(() => UIActions.skip()));

Groups.build.each(boolf(b => b.team != Team.sharded && !(b instanceof CoreBlock.CoreBuild)), cons(b => {Time.run(Mathf.random(900), run(() => {b.kill();}));}));

Groups.build.each(boolf(b => !(b instanceof CoreBlock.CoreBuild)), cons(b => {Time.run(Mathf.random(900), run(() => {b.kill();}));}));

Groups.build.each(cons(b=>Time.run(Mathf.random(900),run(()=>b.kill()))));

TriggerGenerator.setToDefault(PreMadeRaids.standardRaid1);
TriggerGenerator.Item_50SurgeAlloy();



Vars.state.map.tags.put("custom-cutscene-script", "const raid = extend(RaidEventClass, ‘raid’, {});raid.reloadTime = 60 * 60 * 3;raid.targetFunc = func(e => {const rand = NHFunc.rand;rand.setSeed(e.id);let b = null;let times = 0;let all = new Seq();Groups.build.copy(all);while(b == null && times < 1024 && all.any()){let index = rand.random(all.size - 1);b = all.get(index);if(b.team == Vars.state.rules.waveTeam || (b.proximity.size < 3 && b.block.health < 1600)){all.remove(index);b = null;}times++;}return new Vec2().set(b == null ? Vec2.ZERO : b);});raid.teamFunc = func(e => Vars.state.rules.waveTeam);raid.number = 30;raid.shootDelay = 6;raid.removeAfterTriggered = true;");

const raid = extend(RaidEventClass, "raid", {});raid.reloadTime = 60 * 60 * 3;raid.targetFunc = func(e => {const rand = NHFunc.rand;rand.setSeed(e.id);let b = null;let times = 0;let all = new Seq();Groups.build.copy(all);while(b == null && times < 1024 && all.any()){let index = rand.random(all.size - 1);b = all.get(index);if(b.team == Vars.state.rules.waveTeam || (b.proximity.size < 3 && b.block.health < 1600)){all.remove(index);b = null;}times++;}return new Vec2().set(b == null ? Vec2.ZERO : b);});raid.teamFunc = func(e => Vars.state.rules.waveTeam);raid.number = 30;raid.shootDelay = 6;raid.removeAfterTriggered = true;

runOnce("SETUP", run(() => raid.setup()));

EventSamples.waveTeamRaid.bulletType=Bullets.artilleryPlastic;EventSamples.waveTeamRaid.number=90;EventSamples.waveTeamRaid.setup();


