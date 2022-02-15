playerInit(run(() => {
    Time.run(30, run(() => {
    runOnce("set command", run(() => WorldActions.setToRally()));
        UIActions.actionSeqMinor(UIActions.labelActSimple("Capture [accent]Beacons[] to raise territory points to win.", cons(t => {
            t.image(NHContent.capture).size(LEN - OFFSET).padRight(OFFSET);
        })));
    }));
}));