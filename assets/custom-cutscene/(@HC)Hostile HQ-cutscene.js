const eventNameFlagship = KeyFormat.generateName("Hostile Flagships Arriving", Pal.redderDust, 60 * 60 * 10);

CutsceneScript.curUpdater.add(run(() => {
    CutsceneScript.reload(KeyFormat.generateName("Hostile Reinforcements Arriving", Pal.ammo, 60 * 60 * 5), Time.delta, 60 * 60 * 5, prov(() => true), prov(() => Vars.state.rules.waveTeam.cores().size == 1), run(() => {
        const core = Vars.state.rules.waveTeam.core();
        UIActions.actionSeq(
            Actions.parallel(
                UIActions.cautionAt(core.x, core.y, core.block.size * Vars.tilesize / 2, 6, core.team.color),
                Actions.run(run(() => {
                    NHSounds.alarm.play();
                    NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(Vars.state.rules.defaultTeam.core()), 120, 300, 30, NHUnitTypes.destruction, 4);
                    NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(Vars.state.rules.defaultTeam.core()), 120, 240, 15, NHUnitTypes.striker, 6);
                    NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(Vars.state.rules.defaultTeam.core()), 120, 300, 60, NHUnitTypes.hurricane, 2);
                })),
                UIActions.labelAct(
                    "[accent]Caution[]: @@@Hostile Fleet Incoming."
                    , 0.75, 3.26, Interp.linear, cons(t => {
                        t.image(Icon.warning).padRight(OFFSET);
                    })
                )
            )
        );
    }));

    if(CutsceneScript.eventHasData(KeyFormat.ENEMY_CORE_DESTROYED_EVENT)){
        CutsceneScript.reload(KeyFormat.generateName("Air Raid", NHColor.darkEnrColor, 60 * 60 * 8), Time.delta, 60 * 60 * 8, prov(() => true), prov(() => Vars.state.rules.defaultTeam.cores().size > 1 && Vars.state.rules.waveTeam.cores().any()), run(() => {
            const core = Vars.state.teams.closestCore(Vars.world.unitWidth(), Vars.world.unitHeight(), Vars.state.rules.defaultTeam);
            const coreE = Vars.state.rules.waveTeam.cores().firstOpt();
            UIActions.actionSeq(
                Actions.parallel(
                    UIActions.cautionAt(core.x, core.y, core.block.size * Vars.tilesize / 3.5, 4, core.team.color),
                    Actions.run(run(() => {
                        NHSounds.alarm.play();
                        for(let i = 0; i < 35; i++){
                            Time.run(i * 4, WorldActions.raidPos(coreE, coreE.team, NHBullets.airRaid, coreE.x + Mathf.range(160), coreE.y + Mathf.range(160), core, cons(bu => {
                                bu.damage /= 4;
                                bu.vel.rotate(Mathf.range(1));
                                bu.type.shootEffect.at(bu.x, bu.y, bu.angleTo(core), bu.type.hitColor);
                                bu.type.smokeEffect.at(bu.x, bu.y, bu.angleTo(core), bu.type.hitColor);
                            })));
                        }
                    })),
                    UIActions.labelAct(
                        "[accent]Caution[]: @@@Raid Incoming."
                        , 0.75, 3.26, Interp.linear, cons(t => {
                            t.image(Icon.warning).padRight(OFFSET);
                        })
                    )
                )
            );
        }));
        CutsceneScript.reload(eventNameFlagship, Time.delta, 60 * 60 * 10, prov(() => !CutsceneScript.getBool("SpawnedBoss")), prov(() => true), run(() => {
            CutsceneScript.runEventOnce("SpawnedBoss", run(() => {
                            const sX = Vars.world.unitWidth() - 240, sY = Vars.world.unitHeight() - 240;

                            UIActions.actionSeq(
                                Actions.parallel(Actions.delay(2), UIActions.curtainIn(2, Interp.pow2Out)), Actions.run(run(() => UIActions.pauseCamera())),
                                UIActions.moveTo(sX - 360, sY - 120, 2, Interp.pow3),
                                Actions.parallel(
                                    UIActions.holdCamera(sX - 360, sY - 120, 13),
                                    Actions.sequence(
                                        Actions.parallel(
                                            UIActions.labelAct(
                                                "[accent]Caution[]: @@@Flagship Group Incoming.",
                                                0.75, 6.25, Interp.linear, cons(t => {
                                                    t.image(Icon.warning).padRight(OFFSET);
                                                })
                                            ),
                                            Actions.run(run(() => {
                                                Angles.randLenVectors(Time.millis(), 3, 400, new Floatc2(){get(i, j){
                                                    WorldActions.raidDirection(Vars.state.rules.waveTeam.cores().firstOpt(), Vars.state.rules.waveTeam, NHBullets.eternity,
                                                        sX + 480 + i, sY + 800 + j, 225, Mathf.dst(sX + 480 + i, sY + 800 + j, sX, sY) + Mathf.random(600, 900), cons(b => {})
                                                    ).run();
                                                }});
                                            }))
                                        ),
                                        Actions.parallel(
                                            UIActions.cautionAt(sX, sY, 30, 4.5, Pal.redderDust),
                                            Actions.run(run(() => {
                                                NHSounds.alarm.play();
                                                NHFunc.spawnUnit(Vars.state.rules.waveTeam, sX, sY, 225, 300, 180, 90, NHUnitTypes.collapser, 4);
                                            })),
                                            UIActions.labelActFull(
                                            "[accent]Caution[]: @@@Collapsers Approaching",
                                            0.75, 6.25, Interp.linear, Interp.one, cons(t => {
                                                t.image(Icon.warning).padRight(OFFSET);
                                            }))
                                        )
                                    )
                                ),
                                Actions.run(run(() => UIActions.resumeCamera())),
                                UIActions.curtainOut(1, Interp.pow2In)
                            );
            }));
        }));
    }
}));

CutsceneScript.curIniter.add(run(() => {
    CutsceneScript.addListener(Blocks.coreNucleus, cons(b => {
        if(b.team != Vars.state.rules.waveTeam)return;

        CutsceneScript.runEventOnce(KeyFormat.ENEMY_CORE_DESTROYED_EVENT, run(() => {
            const core = Vars.state.teams.cores(Vars.state.rules.waveTeam).first();

            UIActions.reloadBarDelay("Hostile Flagships Arriving", 60 * 60 * 10, Pal.redderDust);
            UIActions.reloadBarDelay("Hostile Reinforcements Arriving", 60 * 60 * 5, Pal.ammo);
            UIActions.reloadBarDelay("Air Raid", 60 * 60 * 8, NHColor.darkEnrColor);

            UIActions.actionSeq(
                Actions.parallel(Actions.delay(2), UIActions.curtainIn(2, Interp.pow2Out)), Actions.run(run(() => UIActions.pauseCamera())),
                UIActions.moveTo(core.x, core.y, 2, Interp.pow3),
                Actions.parallel(
                    UIActions.holdCamera(core.x, core.y, 8),
                    Actions.sequence(
                        UIActions.labelAct(
                                "[accent]Caution[]: @@@Reinforcements Incoming."
                                , 0.75, 3.25, Interp.linear, cons(t => {
                                    t.image(Icon.warning).padRight(OFFSET);
                                })
                        ),
                        Actions.parallel(
                            UIActions.cautionAt(core.x, core.y, core.block.size / 3 * Vars.tilesize, 3.5, Vars.state.rules.waveTeam.color),
                            Actions.run(run(() => {
                                NHSounds.alarm.play();
                                NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(Vars.state.rules.defaultTeam.core()), 120, 60, 30, NHUnitTypes.longinus, 6);
                                NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(Vars.state.rules.defaultTeam.core()), 240, 60, 30, NHUnitTypes.guardian, 2);
                            })),
                            UIActions.labelActFull(
                                "[accent]Caution[]: @@@Multiple hostile units detected."
                                , 0.75, 3.25, Interp.linear, Interp.one, cons(t => {
                                    t.image(Icon.warning).padRight(OFFSET);
                                })
                            )
                        )
                    )
                ),
                Actions.run(run(() => UIActions.resumeCamera())),
                UIActions.curtainOut(1, Interp.pow2In)
            );
        }));
    }));

    if(CutsceneScript.canInit()){
        Time.run(30, run(() => {
            const cores = Vars.state.teams.cores(Vars.state.rules.waveTeam);
            const actions = new Seq(cores.size * 2);

            actions.add(Actions.delay(2), UIActions.curtainIn(2, Interp.pow2Out), Actions.run(run(() => UIActions.pauseCamera())));

            actions.addAll(
                UIActions.moveTo(1512, 1968, 2, Interp.pow3),
                Actions.parallel(
                    UIActions.holdCamera(1512, 1968, 5),
                    UIActions.cautionAt(1416, 1968, Vars.tilesize / 2, 3.5, Pal.heal),
                    UIActions.cautionAt(1512, 2040, Vars.tilesize / 2, 3.5, Pal.heal),
                    UIActions.labelAct(
                        "[accent]Caution[]: @@@Don't destroy these [accent]Power Voids[] unless you have sufficient military power."
                        , 0.75, 4.26, Interp.linear, cons(t => {
                            t.image(Icon.warning).padRight(OFFSET);
                        })
                    )
                ),
                UIActions.moveTo(1680, 1984, 1, Interp.pow3),
                Actions.parallel(
                    UIActions.holdCamera(1680, 1984, 4),
                    UIActions.cautionAt(1864, 2032, Vars.tilesize * 3 / 2, 3.5, Pal.power),
                    UIActions.cautionAt(1512, 2040, Vars.tilesize / 2, 3.5, Pal.heal),
                    UIActions.labelAct(
                        "[accent]Caution[]: @@@These [accent]Power Voids[] are linked to specific [sky]Jump Gates[]\nDestroy these voids will make these gates start to spawn units."
                        , 0.75, 3.25, Interp.linear, cons(t => {
                            t.image(NHBlocks.disposePowerVoid.fullIcon).size(LEN - OFFSET);
                            t.image(Icon.rightOpen).size(LEN - OFFSET);
                            t.image(NHBlocks.jumpGatePrimary.fullIcon).size(LEN - OFFSET).padRight(OFFSET);
                        })
                    )
                )
            );

            for(let i = 0; i < cores.size; i++){
                const core = cores.get(i);
                actions.add(UIActions.moveTo(core.x, core.y, 2, Interp.smooth2));
                actions.add(Actions.parallel(
                    UIActions.holdCamera(core.x, core.y, 3),
                    UIActions.labelAct(
                        "Team<[#" + core.team.color +  "]" + core.team.name.toUpperCase() +
                        "[]> : @@@" +
                        core.block.localizedName + " [[" + core.tileX() + ", " + core.tileY() + "]", 0.5, 2.5, Interp.linear, cons(t => {})
                    ),
                    Actions.run(run(() => {
                        NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(Vars.state.rules.defaultTeam.core()), 120, 60, 15, NHUnitTypes.naxos, 4);
                    }))
                ));
            }

            actions.add(Actions.run(run(() => UIActions.resumeCamera())), UIActions.curtainOut(1, Interp.pow2In));


            UIActions.actionSeq(actions.toArray(Action));
        }));
    }
}));