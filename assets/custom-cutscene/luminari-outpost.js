const destroyReactors = extend(DestroyObjectiveEventClass, "destroyReactors", {});

destroyReactors.targets = func(e => {
    const buildings = new Seq();

    Groups.build.each(
        boolf(b => b.isValid() && b.team != Vars.state.rules.defaultTeam && b.block.flags.contains(BlockFlag.reactor)),
        cons(b => buildings.add(b))
    );

    return buildings;
});

const award = extend(FleetEventClass, "award", {});
award.teamFunc = func(e => Vars.state.rules.defaultTeam);
award.targetFunc = func(e => Vars.state.teams.get(award.teamFunc.get(e)).core());
award.removeAfterTriggered = true;
award.unitTypeMap = ObjectMap.of(NHUnitTypes.longinus, 6);

destroyReactors.action = cons(e => award.setup());

CutsceneScript.curIniter.add(run(() => {
    if(CutsceneScript.canInit())destroyReactors.setup();
}));