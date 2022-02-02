const charge = extend(SimpleReloadEventClass, "charge", {
    updateEvent(e){
        this.super$updateEvent(e);

        if(e.reload / this.reloadTime > 0.52){
            runOnce("1/2 Strike", run(() => {
                NHSounds.alarm.play();

                NHFunc.spawnUnit(enemyTeam(), 1048, 2016, 270, 120, 300, 120, NHUnitTypes.guardian, 3);

                actionSeqMinor(
                    Actions.parallel(
                        UIActions.labelActSimple("[#" + enemyTeam().color + "]" + NHUnitTypes.guardian.localizedName + " [lightgray]Inbound."),
                        UIActions.cautionAt(1048, 2016, 8, 3, enemyTeam().color),
                    )
                );
            }));
        }

        if(e.reload / this.reloadTime > 0.72){
            runOnce("2/3 Strike", run(() => {
                NHSounds.alarm.play();

                NHFunc.spawnUnit(enemyTeam(), 1048, 2016, 270, 80, 300, 120, NHUnitTypes.sin, 1);

                actionSeqMinor(
                    Actions.parallel(
                        UIActions.labelActSimple("[#" + enemyTeam().color + "]" + NHUnitTypes.sin.localizedName + " [lightgray]Inbound."),
                        UIActions.cautionAt(1048, 2016, 8, 3, enemyTeam().color),
                    )
                );
            }));
        }

        if(e.reload / this.reloadTime > 0.375){
            runOnce("2/4 Strike", run(() => {
                NHSounds.alarm.play();
                WorldActions.raidPosMulti(null, enemyTeam(), NHBullets.arc_9000, 1256, 2460, 1056, 1560, 3, 30, 50, 2).run();
                WorldActions.raidPosMulti(null, enemyTeam(), NHBullets.arc_9000, 1256, 2460, 1392, 1560, 3, 30, 50, 2).run();
                WorldActions.raidPosMulti(null, enemyTeam(), NHBullets.airRaidMissile, 1256, 2460, 1056, 1560, 12, 5, 50, 2).run();
                WorldActions.raidPosMulti(null, enemyTeam(), NHBullets.airRaidMissile, 1256, 2460, 1392, 1560, 12, 5, 50, 2).run();
                actionSeqMinor(
                    Actions.sequence(
                        Actions.parallel(
                            UIActions.labelActSimple("[#ff7b69]Primary Turret Bases Attack!!"),
                            UIActions.cautionAt(1056, 1560, 8, 3, Pal.redderDust),
                            UIActions.cautionAt(1392, 1560, 8, 3, Pal.redderDust)
                        ),
                        UIActions.labelActSimple("[accent]Major defensive facilities were severely damaged."),
                        Actions.parallel(
                            UIActions.labelActSimple("[accent]Use the artillery matrix to defend against the enemies."),
                            UIActions.cautionAt(1032, 1152, 6, 3, Pal.accent)
                        )
                    )
                );
            }));
        }

        if(e.reload / this.reloadTime > 0.2){
            runOnce("1/5 Strike", run(() => {
                NHSounds.alarm.play();
                WorldActions.raidPosMulti(null, enemyTeam(), NHBullets.airRaidMissile, 2264, 1016, 1264, 816, 60, 2, 120, 2).run();
                actionSeqMinor(
                    Actions.parallel(
                        UIActions.cautionAt(1264, 816, 18, 3, Pal.redderDust),
                        Actions.sequence(
                            UIActions.labelActSimple("[#ff7b69]Main Generator Under Attack!!"),
                            UIActions.labelActSimple("Stay back from it. The splash of the Hyper Generator will tear the units near it.")
                        )
                    )
                );
            }));
        }
    }
});
charge.info = prov(() => "Hyperspace Charge")
charge.reloadTime = 14400;
charge.removeAfterTriggered = true;
charge.action = run(() => {
    bigEventLaunch([
        UIActions.moveToSimple(1224, 1328),
        UIActions.labelActSimple("Charge complete..."),
        UIActions.labelActSimple("Standby for hyperspace jump..."),
        Actions.run(run(() => {
            const core = allyTeam().core();
            NHSounds.hyperspace.play();
            NHFx.hyperSpace.at(core.x, core.y, core.block.size * 8, core.team.color);
            Time.run(NHFx.hyperSpace.lifetime / 2, () => {
                core.set(0, 0);
                Vars.state.teams.get(Vars.state.rules.defaultTeam).destroyToDerelict();
                core.kill();
                Time.run(420, () => {
                    Logic.gameOver(Vars.state.rules.waveTeam);
                    if(!Vars.headless && Vars.state.isCampaign()){
                        Core.settings.put("complete-primary-base", true);
                        NHSectorPresets.ruinedWarehouse.unlock();
                        NHSectorPresets.primaryBase.clearUnlock();
                        UIActions.forceEnd();
                    }
                })
            });
        })),
        Actions.delay(1),
        UIActions.labelActSimple("Initiating Self-destruct System...         "),
        Actions.delay(1.4),
        UIActions.labelActSimple("Good luck in your future campaigns..."),
        Actions.delay(100),
        Actions.run(run(() => {
            allyTeam().core().kill();
        }))
    ])
});

const hostile1 = extend(SimpleReloadEventClass, "hostile1", {});
hostile1.info = prov(() => "Hostile Incoming Units <Group-1>")
hostile1.color = prov(() => Pal.redderDust);
hostile1.reloadTime = 1200;
hostile1.action = run(() => {
    NHFunc.spawnUnit(enemyTeam(), 1384, 2032, 270, 240, 900, 45, NHUnitTypes.striker, 12);
});

const hostile2 = extend(SimpleReloadEventClass, "hostile2", {});
hostile2.info = prov(() => "Hostile Incoming Units <Group-2>")
hostile2.color = prov(() => Pal.redderDust);
hostile2.reloadTime = 900;
hostile2.action = run(() => {
    NHFunc.spawnUnit(enemyTeam(), 1384, 2032, 270, 280, 600, 25, NHUnitTypes.aliotiat, 24);
    NHFunc.spawnUnit(enemyTeam(), 1384, 2032, 270, 280, 600, 60, NHUnitTypes.tarlidor, 6);
});

const hostile3 = extend(SimpleReloadEventClass, "hostile3", {});
hostile3.info = prov(() => "Hostile Incoming Units <Group-3>")
hostile3.color = prov(() => Pal.redderDust);
hostile3.reloadTime = 1800;
hostile3.action = run(() => {
    NHFunc.spawnUnit(enemyTeam(), 1384, 2032, 270, 280, 300, 10, NHUnitTypes.branch, 18);
    NHFunc.spawnUnit(enemyTeam(), 1384, 2032, 270, 280, 600, 25, NHUnitTypes.warper, 12);
    NHFunc.spawnUnit(enemyTeam(), 1384, 2032, 270, 280, 900, 60, NHUnitTypes.destruction, 6);
});

const hostile4 = extend(SimpleReloadEventClass, "hostile4", {});
hostile4.info = prov(() => "Hostile Incoming Units <Group-4>")
hostile4.color = prov(() => Pal.redderDust);
hostile4.reloadTime = 2200;
hostile4.action = run(() => {
    NHFunc.spawnUnit(enemyTeam(), 928, 1896, 270, 280, 300, 32, UnitTypes.arkyid, 12);
    NHFunc.spawnUnit(enemyTeam(), 928, 1896, 270, 280, 600, 90, UnitTypes.toxopid, 6);
    NHFunc.spawnUnit(enemyTeam(), 928, 1896, 270, 280, 900, 120, NHUnitTypes.annihilation, 6);
});

const goal = extend(ObjectiveEventClass, "buildUnits", {});
goal.info = func(e => "[gray]Build [accent]" + Vars.state.teams.get(Vars.state.rules.defaultTeam).countType(NHUnitTypes.destruction) + " / 8 [lightgray]" + NHUnitTypes.destruction.localizedName + "[].");
goal.trigger = func(e => Vars.state.teams.get(Vars.state.rules.defaultTeam).countType(NHUnitTypes.destruction) >= 8);
goal.action = cons(e => {
    actionSeq([
        UIActions.labelActSimple("Well done, that's must be enough to..."),
        UIActions.startCutsceneDefault(),
        UIActions.moveToSimple(1440, 2120),
//        Actions.delay(0.75),
        Actions.run(WorldActions.raidPosMulti(null, enemyTeam(), NHBullets.collapserBullet, 2532, 2748, 1400, 2100, 100, 0.8, 300, 0.75)),
        Actions.delay(5),
        Actions.parallel(
            UIActions.cautionAt(1344, 2048, 18, 3, Vars.state.rules.waveTeam.color),
            Actions.run(run(() => {
                NHSounds.alarm.play();
                NHFunc.spawnUnit(enemyTeam(), 1400, 2100, 260, 240, 600, 10, NHUnitTypes.striker, 18);
                NHFunc.spawnUnit(enemyTeam(), 1400, 2100, 260, 160, 600, 20, NHUnitTypes.destruction, 6);
            }))
        ),
        UIActions.endCutsceneDefault(),
        UIActions.labelActSimple("We are under attack!"),
        UIActions.labelActSimple("The gravity trap frontier was badly damaged."),
        UIActions.labelActSimple("An direct conflict was no way to avoid."),
        UIActions.labelActSimple("Scanner instructed that more powerful hostile units are approaching this sector."),
        UIActions.labelActSimple("The outpost is to weak to resist, we are now charging the hyperspace module for emergency eject."),
        UIActions.labelActSimple("Survive until the charge complete."),
        Actions.run(run(() => {
            charge.setup();
            hostile1.setup();
            hostile2.setup();
            hostile3.setup();
            hostile4.setup();
        }))
    ]);
});

const unlocker = extend(SignalEventClass, "unlock-units", {});
unlocker.action = run(() => {
    if(Vars.state.isCampaign()){
        NHUnitTypes.destruction.clearUnlock();
        NHUnitTypes.destruction.unlock();
    }

    bigEventLaunch([
        UIActions.moveToSimple(776, 1096),
        Actions.parallel(
            UIActions.cautionAt(780, 1140, 8, 3, Pal.accent),
            UIActions.cautionAt(780, 1052, 8, 3, Pal.accent),
            UIActions.labelActSimple("Use these [accent]Jump Gates[] to build 8 [sky]Destruction[]."),
        ),
        UIActions.labelActSimple("While the units is being built, please build some other defensive buildings to defend against potential enemies.")
    ]);
    goal.setup();
});
unlocker.position = new Vec2(792, 760);
unlocker.reloadTime = 200;

playerInit(run(() => {
    if(CutsceneScript.canInit()){
        Time.run(30, run(() => {
            bigEventLaunch([
                UIActions.moveToSimple(1432, 2096),
                UIActions.labelActSimple("This is an outpost on planet [sky]Midantha[]."),
                UIActions.moveToSimple(624, 1288),
                UIActions.labelActSimple("According to new intelligence, multiple hostile units are approaching this area from the north. Rally the units back to the [accent]Command Center[] to avoid being raided is recommended."),
                UIActions.moveToSimple(1392, 656),
                UIActions.labelActSimple("Build some units to defend the enemies."),
                UIActions.moveToSimple(1728, 1312),
                UIActions.labelActSimple("Found and transform to the signal source to receive unit blueprint data."),
                Actions.run(run(() => unlocker.setup()))
            ])
        }));

        for(let i = 0; i < Vars.content.items().size; i++){
            Vars.state.rules.defaultTeam.core().items.add(Vars.content.items().get(i), Vars.state.rules.defaultTeam.core().storageCapacity);
        }
    }
}));