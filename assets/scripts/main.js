//Events.on(ClientLoadEvent, cons(et => {
//    var loadFailed = false;
//
//    const mod = Vars.mods.getMod("new-horizon");
//    if(mod == null || (mod.meta.name.equals("new-horizon") && mod.loader == null)){
//        loadFailed = true;
//    }
//
//    if(mod != null && loadFailed){
//        Log.err("Load Mod <New Horizon> Failed::Mod ClassLoader Missing");
//        const dl = new BaseDialog("Missing");
//        dl.addCloseButton();
//        dl.cont.pane(cons(t => {
//            t.align(Align.topLeft);
//            t.margin(60);
//            t.add("Failed to install [accent]<New Horizon>[] mod").pad(6);
//            t.image().growX().height(4).pad(4).color(Color.lightgray).row();
//            t.add("Please down load jar-packaged format mod file from GitHub or other places, or download this mod through [sky]Mod Browser[].");
//        })).grow();
//    }
//}));

