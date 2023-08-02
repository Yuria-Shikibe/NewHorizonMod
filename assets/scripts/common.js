

Core.app.setClipboardText("@CS-" + Vars.editor.tags.get("name"));

Vars.state.teams.get(Vars.state.rules.defaultTeam).units.each(cons(u => u.remove()));

Time.run(180, run(() => Vars.state.rules.tags.clear()))