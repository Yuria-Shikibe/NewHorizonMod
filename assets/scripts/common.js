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
NHWeathers.solarStorm.create(3,1500);

NHGroups.autoEventTriggers.each(cons(e=>Log.info(e.meet())))

NHWeathers.quantumStorm.instance().intensity+=0.5;
NHWeathers.quantumStorm.instance().intensity-=0.5;

NHGroups.gravityTraps.intersect(__*8,__*8,8,8,cons(e=>Log.info(e)));

NHGroups.event.clear();
NHGroups.event.getByID(ID)

var unit=NHUnitTypes.warper.create(Team.purple);unit.set(Vars.world.unitWidth()/2,Vars.world.unitHeight()/2);unit.add();

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

const event = extend(RaidEventClass, "custom-multi-raid-event", {
    triggered(e){
        if(e == null)return;
        e.reload = 0;
        const team = this.attackerTeamFunc.get(e);
        const source = this.sourceFunc.get(e);
        if(source == null)return;

        const vec2 = new Vec2().set(e);

        UIActions.actionSeqMinor(Actions.parallel(UIActions.cautionAt(e.getX(), e.getY(), 4, 1, team.color), Actions.run(run(() => {
            NHSounds.alarm.play();
            for(var i = 0; i < 10; i++){
                var finalI = i;
                Time.run(i * 10, WorldActions.raidPos(team.cores().firstOpt(), team, NHBullets.skyFrag, source.getX() + Mathf.randomSeedRange(e.id + finalI, this.sourceSpread), source.getY() + Mathf.randomSeedRange(e.id + 100 - finalI, this.sourceSpread), vec2.x, vec2.y, cons(b => {
                    b.vel.rotate(Mathf.randomSeedRange(e.id + 50 + finalI, inaccuracy));
                    if(b.type.shootEffect != null)
                        b.type.shootEffect.at(b.x, b.y, b.angleTo(source), b.type.hitColor);
                    if(b.type.smokeEffect != null)
                        b.type.smokeEffect.at(b.x, b.y, b.angleTo(source), b.type.hitColor);
                    shootModifier.get(b);
                })));
            }

            //复制下面这一段并修改由__*__包裹的数据，并取消/**/注释
            //BulletType即所需子弹，其他量的含义同底下的注释
            /*for(let i = 0; i < __number__; i++){
                let finalI = i;
                Time.run(i * __delay__, WorldActions.raidPos(team.cores().firstOpt(), team, __bulletType__, source.getX() + Mathf.randomSeedRange(e.id + finalI, sourceSpread), source.getY() + Mathf.randomSeedRange(e.id + 100 - finalI, sourceSpread), vec2.x, vec2.y, cons(b => {
                    b.vel.rotate(Mathf.randomSeedRange(e.id + 50 + finalI, inaccuracy));
                    if(b.type.shootEffect != null)
                        b.type.shootEffect.at(b.x, b.y, b.angleTo(source), b.type.hitColor);
                    if(b.type.smokeEffect != null)
                        b.type.smokeEffect.at(b.x, b.y, b.angleTo(source), b.type.hitColor);
                    shootModifier.get(b);
                })));
            }*/
        })), UIActions.labelAct("[accent]Caution[]: Raid Incoming.", 1, 1, Interp.linear, cons(t => {
            t.image(NHContent.raid).size(LEN).padRight(OFFSET);
        }))));

        e.set(this.targetFunc.get(e));
    }
});
event.number = 100; //Shots|发射数
event.shootDelay = 3; //Spacing between shots|每次发生间隔 [Unit:tick]|[单位:帧]
event.inaccuracy = 3; //散布/角度
event.sourceSpread = 200; //The spread range of shoot positions|射击源的散布范围
event.reloadTime = 60 * 60; //Interval between alert and raid|每次从警报到袭击的间隔 [Unit:tick]|[单位:帧]
handleEvent(event);



