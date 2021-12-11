CutsceneEventEntity.events.each(cons(e => e.act()));

CutsceneEventEntity.events.clear();

EventSamples.waveTeamRaid.setup();

Time.run(120, run(() => UIActions.skip()));

Groups.build.each(boolf(b => b.team != Team.sharded), cons(b => b.kill()));

