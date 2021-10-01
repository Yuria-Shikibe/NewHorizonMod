CutsceneScript.curUpdaters.add(run(() => {
    if(Vars.state.rules.waveTeam.cores().size == 1 && CutsceneScript.timer.get(0, 1800)){
        const core = Vars.state.rules.waveTeam.core();
        UIActions.actionSeq(
            Actions.parallel(
                UIActions.cautionAt(core.x, core.y, core.block.size * Vars.tilesize / 2, 6, core.team.color),
                Actions.run(run(() => {
                    NHSounds.alarm.play();
                    NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(Vars.player.team().core()), 120, 300, 30, NHUnitTypes.destruction, 4);
                    NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(Vars.player.team().core()), 120, 240, 15, NHUnitTypes.striker, 6);
                    NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(Vars.player.team().core()), 120, 300, 60, NHUnitTypes.hurricane, 2);
                })),
                UIActions.labelAct(
                    "[accent]Caution[]: @@@Hostile Fleet Incoming."
                    , 0.75, 3.26, false, Interp.linear, cons(t => {
                        t.image(Icon.warning).padRight(12);
                    })
                )
            )
        );
    }
}));
			
CutsceneScript.curIniter.add(run(() => {
    CutsceneScript.addListener(Blocks.coreNucleus, cons(b => {
        if(b.team != Vars.state.rules.waveTeam)return;
        
        CutsceneScript.runEventOnce(CommonEventNames.ENEMY_CORE_DESTROYED_EVENT, run(() => {
            const core = Vars.state.teams.cores(Vars.state.rules.waveTeam).first();
            
            UIActions.actionSeq(
                Actions.parallel(Actions.delay(2), UIActions.curtainIn(2, Interp.pow2Out)), Actions.run(run(() =>UIActions.pauseCamera())),
                UIActions.moveTo(core.x, core.y, 2, Interp.pow3),
                Actions.parallel(
                    UIActions.holdCamera(core.x, core.y, 4),
                    UIActions.labelAct(
                        "[accent]Caution[]: @@@Multiple hostile units detected."
                        , 0.75, 3.25, false, Interp.linear, cons(t => {
                            t.image(Icon.warning).padRight(12);
                        })
                    ),
                    UIActions.cautionAt(core.x, core.y, core.block.size / 3 * Vars.tilesize, 3, Vars.state.rules.waveTeam.color),
                    Actions.run(run(() => {
                        NHSounds.alarm.play();
                        NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(Vars.player.core()), 120, 60, 30, NHUnitTypes.longinus, 6);
                        NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(Vars.player.core()), 240, 60, 30, NHUnitTypes.guardian, 2);
                    }))
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
            
            actions.add(Actions.delay(2), Actions.run(run(() =>UIActions.pauseCamera())));
            
            actions.addAll(
                UIActions.moveTo(1512, 1968, 2, Interp.pow3),
                Actions.parallel(
                    UIActions.holdCamera(1512, 1968, 5),
                    UIActions.cautionAt(1416, 1968, Vars.tilesize / 2, 3.5, Pal.heal),
                    UIActions.cautionAt(1512, 2040, Vars.tilesize / 2, 3.5, Pal.heal),
                    UIActions.labelAct(
                        "[accent]Caution[]: @@@Don't destroy these [accent]Power Voids[] unless you have sufficient military power."
                        , 0.75, 4.26, false, Interp.linear, cons(t => {
                            t.image(Icon.warning).padRight(12);
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
                        , 0.75, 3.25, false, Interp.linear, cons(t => {
                            t.image(NHBlocks.disposePowerVoid.fullIcon).size(48);
                            t.image(Icon.rightOpen).size(48);
                            t.image(NHBlocks.jumpGatePrimary.fullIcon).size(48).padRight(12);
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
                                    core.block.localizedName + " [[" + core.tileX() + ", " + core.tileY() + "]", 0.5, 2.5, false, Interp.linear, cons(t => {
                            })
                    ),
                    Actions.run(run(() => {
                        NHFunc.spawnUnit(core.team, core.x, core.y, core.angleTo(Vars.player.team().core()), 120, 60, 15, NHUnitTypes.naxos, 4);
                    }))
                ));
            }
            
            actions.add(Actions.run(run(() => UIActions.resumeCamera() )));
            
            UIActions.screenHold(2, actions.size * 2, 1, Interp.fastSlow, Interp.slowFast, 0);
            
            UIActions.actionSeq(actions.toArray(Action));
        }));
    }
}));