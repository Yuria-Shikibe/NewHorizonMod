NHGroups.event.each(cons(e=>e.reload+=10000000));
NHGroups.event.each(cons(e=>e.actNet()));
Draw.blend();
Draw.blend(NHBlending.test3);
Draw.blend(new Blending(Gl.blendSrcRgb, Gl.blendEquationRgb));

print(NHWeathers.quantumStorm.isActive())
Groups.weather.clear();
NHWeathers.solarStorm.create(3,1800);

PreMadeRaids.raid1.setup();

Core.settings.put("speed-scl",_);

NHWeathers.quantumStorm.create(Mathf.random(__,__),__);
Groups.weather.clear();
NHWeathers.quantumStorm.create(4,1800);

NHGroups.autoEventTriggers.each(cons(e=>Log.info(e.meet())))

NHWeathers.quantumStorm.instance().intensity+=0.5;
NHWeathers.quantumStorm.instance().intensity-=0.5;

NHGroups.gravityTraps.intersect(__*8,__*8,8,8,cons(e=>Log.info(e)));

NHGroups.event.clear();
NHGroups.event.getByID(ID)

EventSamples.waveTeamRaid.setup();
EventSamples.fleetInbound.setup()
EventSamples.jumpgateUnlock.setup()

PreMadeRaids.deadlyRaid3.setup()

CutsceneEvent.get("inbuilt-inbound-server-1").setup()
PreMadeRaids.raid2.setup().set(__*8,__*8)

for(var i=0;i<3;i++)PreMadeRaids.deadlyRaid2.setup();

Time.run(120, run(() => UIActions.skip()));

Groups.build.each(boolf(b => b.team != Team.sharded && !(b instanceof CoreBlock.CoreBuild)), cons(b => {Time.run(Mathf.random(900), run(() => {b.kill();}));}));

Groups.build.each(boolf(b =>!(b instanceof CoreBuild)),cons(b=>Time.run(Mathf.random(900),run(()=>b.kill())));

Groups.build.each(cons(b=>Time.run(Mathf.random(900),run(()=>b.kill()))));

TriggerGenerator.setToDefault(PreMadeRaids.standardRaid1);
TriggerGenerator.Item_50SurgeAlloy();

const destroyReactors = extend(DestroyObjectiveEventClass, "destroyReactors", {});

destroyReactors.targets = func(e => {
    const buildings = new Seq();

    Groups.build.each(
        boolf(b => b.isValid() && b.team != Vars.state.rules.defaultTeam && b.block == NHBlocks.hyperGenerator),
        cons(b => buildings.add(b))
    );

    return buildings;
});

handleEvent(destroyReactors);

const event = extend(RaidEventClass, "raid-custom", {});
handleEvent(event);




