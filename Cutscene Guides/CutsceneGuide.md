# Custom Cutscene Guide

**Used to make map animation script.**

## FOREWORD

- Though the cutscene script has to do with javascript, it isn't so hard.
- Of course, you need some grammar to make your journey easier, however the cutscene script is something that likes Lego Bricks. You just need to buffer code blocks one by one then you can make cutscenes.
- So relax. **_You Can Do It_**
- Example: [Script For Map (@HC)Hostile HQ](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/assets/custom-cutscene/(%40HC)Hostile%20HQ-cutscene.js)
- Main Codes: [CutsceneScript.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/util/feature/cutscene/CutsceneScript.java)
- Important: 
  - [CutsceneEvent.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/util/feature/cutscene/CutsceneEvent.java)
  - [CutsceneEventEntity.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/feature/CutsceneEventEntity.java)
  - [UIActions.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/util/feature/cutscene/UIActions.java)
  - [WorldActions.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/util/feature/cutscene/WorldActions.java)

---

### Features

- Powered by **JavaScript**.
- Could be stored into the map file after you finish it, making it available without *external files*.
- Available for multiple-player games and headless servers.
- Has in-game debugger. You have to activate these mod settings to access it.
  - `Debug Mode`
  - `Tool Panel`

### Caution

- Unsafe because the js don't have **_SECURITY LIMIT_**.
- Difficult to debug, as the crashes often occurred without a report, and the tools are still in develop period currently.
- While a map has both packaged script & external script file, only the packaged on will be run.

## HOW TO USE

### PRE IMPORTER

```js
Log.info("Loaded Cutscene Class Vault");

let loader = Vars.mods.getMod("new-horizon").loader;

function loadContent(fullName){
     return loader.loadClass(fullName).newInstance(); //Garbage things
}

function loadClass(fullName){
     return loader.loadClass(fullName); //Garbage things
}

const UIActions = loadContent("newhorizon.util.feature.cutscene.UIActions");
const KeyFormat = loadContent("newhorizon.util.feature.cutscene.KeyFormat");
const WorldActions = loadContent("newhorizon.util.feature.cutscene.WorldActions");

const CutsceneEventClass = loadClass("newhorizon.util.feature.cutscene.CutsceneEvent");
const CutsceneEvent = CutsceneEventClass.newInstance();

const CutsceneEventEntity = loadContent("newhorizon.util.feature.cutscene.CutsceneEventEntity");
const CutsceneScript = loadContent("newhorizon.util.feature.cutscene.CutsceneScript");
const CCS_Scripts = CutsceneScript.scripts;
const EventSamples = loadContent("newhorizon.util.feature.cutscene.EventSamples");

const NHBlocks = loadContent("newhorizon.content.blocks.NHBlocks");
const NHBullets = loadContent("newhorizon.content.NHBullets");
const NHItems = loadContent("newhorizon.content.NHItems");
const NHLiquids = loadContent("newhorizon.content.NHLiquids");
const NHSounds = loadContent("newhorizon.content.NHSounds");
const NHWeathers = loadContent("newhorizon.content.NHWeathers");
const NHUnitTypes = loadContent("newhorizon.content.NHUnitTypes");
const NHStatusEffects = loadContent("newhorizon.content.NHStatusEffects");
const NHSectorPresets = loadContent("newhorizon.content.NHSectorPresets");
const NHFx = loadContent("newhorizon.content.NHFx");
const NHColor = loadContent("newhorizon.content.NHColor");
const NHPlanets = loadContent("newhorizon.content.NHPlanets");
const NHFunc = loadContent("newhorizon.util.func.NHFunc");
const DrawFunc = loadContent("newhorizon.util.graphic.DrawFunc");
const Tables = loadContent("newhorizon.util.ui.NHUIFunc");
const TableFunc = loadContent("newhorizon.util.ui.TableFunc");
const NHInterp = loadContent("newhorizon.util.func.NHInterp");
const PosLightning = loadContent("newhorizon.util.feature.PosLightning");

const FleetEventClass = loadClass("newhorizon.util.feature.cutscene.events.FleetEvent");
const ObjectiveEventClass = loadClass("newhorizon.util.feature.cutscene.events.ObjectiveEvent");
const RaidEventClass = loadClass("newhorizon.util.feature.cutscene.events.RaidEvent");
const SignalEventClass = loadClass("newhorizon.util.feature.cutscene.events.SignalEvent");
const DestroyObjectiveEventClass = loadClass("newhorizon.util.feature.cutscene.events.DestroyObjectiveEvent");

const OFFSET = 12;
const LEN = 60;

const state = Vars.state;
const tilesize = Vars.tilesize;
const world = Vars.world;

function newEvent(name, args){
    return extend(CutsceneEventClass, name, args);
}
```

The way I used to import the `Class` is really garbage. If you are able to improve it, just **PULL REQUEST** .
This importer has imported most of the MOD Classes that you will need. If you need more, you can invoke the method `loadClass(<String> Class Full Name);` to load more classes.

### Main Process

1. Write `(@HC)`, which means `Annotation: Has Cutscene`, in your map's name.
> Like this
> ![](https://github.com/Yuria-Shikibe/NewHorizonMod/raw/main/github-pictures/guide/cutscene-guide-rename.png)
2. Save the map and open it.
3. The js file with a specific name will be automatically generated. If everything goes right, press `F8` to open the `last-log` and you will see this: ![](https://github.com/Yuria-Shikibe/NewHorizonMod/raw/main/github-pictures/guide/cutscene-js-auto-generated.png)
4. Write your script in the file generated by the mod.
5. Go through the tough debugging time.
6. Open the `Menu` in `Map Editor` (Tap `ESC` on your keyboard or `Home Button` on your phone, or click the button on the dialog's left top), Click button `Cutscene Scripts`: ![](https://github.com/Yuria-Shikibe/NewHorizonMod/raw/main/github-pictures/guide/cutscene-guide-package1.png)
7. Click button `Package Scripts`, then select the js file and confirm.
8. If you want to confirm, Click button `Read Scripts` to see whether the map has your script installed or not.
9. Save the map and the open the world, test your script.
10. If something wrong that didn't ever appear happens, try to repackage the script.
11. The tag `(@HC)` can be deleted after you finishing debug works, or you can keep it as a sign that tells others this map has a cutscene.
12. Publish your map.

### Core Method
```java
public static boolean actionSeq(Action... actions){
    boolean isPlaying = isPlayingCutscene;
    
    Action[] acts = new Action[actions.length + 1];
    System.arraycopy(actions, 0, acts, 0, actions.length);
    acts[acts.length - 1] = Actions.parallel(Actions.remove(), Actions.run(() -> currentActions = null));
    
    if(!isPlaying){
        isPlayingCutscene = true;
        currentActions = acts;
        Table filler = new Table(Tex.clear){
            {
                Core.scene.root.addChild(this);
                
                setFillParent(true);
                visible(UIActions::shown);
                
                keyDown(k -> {
                    if(k == KeyCode.escape) remove();
                });
            }
            
            @Override
            public void act(float delta){
                super.act(delta);
                if(Vars.state.isMenu()) remove();
            }
            
            @Override
            public boolean remove(){
                enableVanillaUI();
                
                if(waitingPool.any()){
                    Time.run(60f, () -> {
                        isPlayingCutscene = false;
                        actionSeq(waitingPool.pop());
                    });
                }else isPlayingCutscene = false;
                
                return super.remove();
            }
        };
        
        filler.actions(acts);
    }else{
        waitingPool.add(acts);
    }
    
    return isPlaying;
}
```

- Fill the method with `Action` one by one, follow time order.
- Cutscene is powered by `arc.scene.Action`.
- If you quit the world while a cutscene is playing, It won't be saved, which may causing some saving problems.
- If multiple actions are called overlapped, they will be acted one by one.

#### Caution
- All `Action`s' time are formatted into **Second** while other method are `tick(1 / 60 Sec)` format.
- Almost all methods uses **\*8** coordinates. 

### Examples & Introduction

#### How To Start A Cutscene?
```js
UIActions.actionSeq(
    UIActions.startCutsceneDefault(),
    Actions.delay(3),
    UIActions.endCutsceneDefault()
);
```
Here is a piece of `JS` code. Copy it to your cutscene script debugger and select them, then run `Run Selection`.
You can find your UI is hidden and curtains go into the screen.

So, use `UIActions.actionSeq(Action... actions)` to start a cutscene.

#### When To Start?
The method above only told you how to activate a cutscene manually. So how to activate them on specific time?
```js 
CutsceneScript.curIniter.add(run(() => {
    UIActions.actionSeq(
        UIActions.startCutsceneDefault(),
        Actions.delay(3),
        UIActions.endCutsceneDefault()
    );
}));
```
This piece adds the *Cutscene Activator* to the initializer, which means that the script will be run **every time** when the world is loaded. Copy them to your script debugger and **Save Them To Your Script File** and reload the world, see what will happen.

#### How To Limit Them?
Ok we just have learned how to use the initializer, but you want your cutscene will only be played while the first time loading the world. How to do?

Continue the code from above:
```js 
CutsceneScript.curIniter.add(run(() => {
    if(CutsceneScript.canInit())UIActions.actionSeq(
        UIActions.startCutsceneDefault(),
        Actions.delay(3),
        UIActions.endCutsceneDefault()
    );
}));
```
This piece adds the cutscene *Condition Determiner* to the initializer, making the cutscene after the `if` statement only run on the first time load the world. Meanwhile, this method will put data to the `Vars.state.rules.<StringMap>tag`, as a sign that the world has already made the initialization run. If you invoke `Vars.state.rules.tags.get("inited")` afterwards, you will receive a  `true` in `String`.

#### How To Move & Hold My Camera?
- See these methods in [CutsceneScript.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/feature/CutsceneScript.java) :
  - `UIActions.track(Position target, float duration)`
  - `UIActions.moveTo(float x, float y, float duration, Interp interpolation)`
  - `UIActions.holdCamera(float x, float y, float duration)`
- Before moving your camera, you have to invoke method `UIActions.pauseCamera()`; in addition, you have to invoke method `UIActions.resumeCamera()` after your cutscene movement has completed. But considered that most of the camera moving have to do with curtain stretch in, so the method `UIActions.startCutsceneDefault()` & `UIActions.endCutsceneDefault()`, two methods used at the beginning and the end respectively, include the method of pause & resume camera. So you can invoke them more convenient.
- The coordinate of the camera moving method all using _**\*8**_ format.
- > ![Coord Format](https://github.com/Yuria-Shikibe/NewHorizonMod/raw/main/github-pictures/guide/coord-format.png)
- Also, remember again: _ALL `Action` USE **SECOND** FORMAT._

Continue the code from above:

```js 
CutsceneScript.curIniter.add(run(() => {
    if(CutsceneScript.canInit())UIActions.actionSeq(
        UIActions.startCutsceneDefault(),
        UIActions.moveTo(80, 80, 1, Interp.pow3),
        UIActions.holdCamera(80, 80, 3),
        UIActions.endCutsceneDefault()
    );
}));
```

Or you can use this if you don't want to use the curtain stretch in effect.
```js 
CutsceneScript.curIniter.add(run(() => {
    if(CutsceneScript.canInit())UIActions.actionSeq(
        UIActions.pauseCamera(),
        UIActions.moveTo(80, 80, 1, Interp.pow3),
        UIActions.holdCamera(80, 80, 3),
        UIActions.resumeCamera()
    );
}));
```

So, in the code above, you will move your camera to (80, 80) (or (10, 10) as tile format) in 1 sec with a slow -> fast -> slow animation curve. And hold at the position for 3 sec.

#### How To Use Caution Mark?
- See the method in [CutsceneScript.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/feature/CutsceneScript.java) :
  - `cautionAt(float x, float y, float size, float duration, Color color)`
- Currently, the mark only has one style. I will develop more in the future.
- Param: `size` is relative to your screen size.

Continue the code from above:
```js 
CutsceneScript.curIniter.add(run(() => {
    if(CutsceneScript.canInit())UIActions.actionSeq(
        UIActions.startCutsceneDefault(),
        UIActions.moveTo(80, 80, 1, Interp.pow3),
        UIActions.holdCamera(80, 80, 3),
        UIActions.cautionAt(80, 80, 16, 2, Pal.accent),
        UIActions.endCutsceneDefault()
    );
}));
```

#### Something Goes Wrong!?
- If you have tried the code from above, you may find that your caution mark didn't appear immediately after your camera move to the destination, instead, it waited for about 3 sec.
- Yes, but not *about*, it waited for exactly 3s, the same param you writing in `UIActions.holdCamera(80, 80, 3)` .
- SO WHAT TO DO IF YOU WANT THE `holdCamera` AND `cautionAt` FUNCTION IN THE SAME TIME? Use `Actions.parallel(Action... actions)`.

See: [ParallelAction](https://github.com/Anuken/Arc/blob/0e99b0291f81d74d335dca8b0cf3bf26931f1197/arc-core/src/arc/scene/actions/ParallelAction.java)
See: [Action Invoke](https://github.com/Anuken/Arc/blob/0e99b0291f81d74d335dca8b0cf3bf26931f1197/arc-core/src/arc/scene/actions/Actions.java)

Sample:
```js 
CutsceneScript.curIniter.add(run(() => {
    if(CutsceneScript.canInit())UIActions.actionSeq(
        UIActions.startCutsceneDefault(),
        UIActions.moveTo(80, 80, 1, Interp.pow3),
        Actions.parallel(
          UIActions.holdCamera(80, 80, 3),
          UIActions.cautionAt(80, 80, 16, 2, Pal.accent)
        ),
        UIActions.endCutsceneDefault()
    );
}));
```

#### Text Pop Up
- See [RunnableAction](https://github.com/Anuken/Arc/blob/0e99b0291f81d74d335dca8b0cf3bf26931f1197/arc-core/src/arc/scene/actions/RunnableAction.java)
- See these methods in [CutsceneScript.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/feature/CutsceneScript.java) :
  - `labelAct(String text, float duration, float holdDuration)`
  - `labelAct(String text, float duration, float holdDuration, Interp interpolation, Cons<Table> modifier)`
- If you want to use other methods in an action, use `RunnableAction` to invoke the method.
- For some technical reasons currently, the text which has the fade in effect do not support *Color Mark* like `[accent]Text[]`.
- Remember again: _ALL GENERAL METHOD USE **TICK** FORMAT._


Sample:
```js 
CutsceneScript.curIniter.add(run(() => {
    if(CutsceneScript.canInit())UIActions.actionSeq(
        UIActions.startCutsceneDefault(),
        UIActions.moveTo(80, 80, 1, Interp.pow3),
        Actions.parallel(
            UIActions.holdCamera(80, 80, 3),
            UIActions.cautionAt(80, 80, 16, 2, Pal.accent),
            UIActions.labelAct(
                "[accent]Speaker[]: @@@Saying: BOTH YURIA AND NEW HORIZON MOD SUCKS"
                , 0.75, 2.25, Interp.linear, cons(t => {
                    t.image(Icon.warning).padRight(OFFSET);
                })
            )
        ),
        UIActions.endCutsceneDefault()
    );
}));
```

#### Units Jump In And Other Methods In An Action
- See these methods in [NHFunc.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/util/func/NHFunc.java) :
  - `spawnUnit(Team team, float x, float y, float angle, float spawnRange, float spawnReloadTime, float spawnDelay, UnitType type, int spawnNum)`
  - `spawnSingleUnit(Team team, float x, float y, float angle, float delay, UnitType type)`

Sample:
```js 
CutsceneScript.curIniter.add(run(() => {
    if(CutsceneScript.canInit())UIActions.actionSeq(
        UIActions.startCutsceneDefault(),
        UIActions.moveTo(80, 80, 1, Interp.pow3),
        Actions.parallel(
            UIActions.holdCamera(80, 80, 3),
            UIActions.cautionAt(80, 80, 16, 2, Pal.accent),
            UIActions.labelAct(
                "[accent]Speaker[]: @@@Saying: BOTH YURIA AND NEW HORIZON MOD SUCKS"
                , 0.75, 2.25, Interp.linear, cons(t => {
                    t.image(Icon.warning).padRight(OFFSET);
                })
            ),
            Actions.run(run(() => {
                NHFunc.spawnUnit(state.rules.waveTeam, 80, 80, 45, 120, 20, 30, NHUnitTypes.striker, 4);
            }))
        ),
        UIActions.endCutsceneDefault()
    );
}));
```

#### Custom Event
- See These:
  - [CutsceneEvent.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/util/feature/cutscene/CutsceneEvent.java)
  - [CutsceneEventEntity.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/feature/CutsceneEventEntity.java)

Sample: If all enemy Hyper Generator get destroyed, friendly reinforcements inbound.
```js 
const destroyReactors = extend(DestroyObjectiveEventClass, "destroyReactors", {});

destroyReactors.targets = func(e => {
    const buildings = new Seq();

    Groups.build.each(
        boolf(b => b.isValid() && b.team != Vars.state.rules.defaultTeam && b.block == NHBlocks.hyperGenerator),
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
```

#### Debug
Ok, finally you finished your first cutscene script.
Use `Remove World Data` in the debugger and retest the script.

#### Exceptions:
- If nothing was popped up, it was likely that you mistook the `Class` name of a `Field` or a `Method`.
- If a `NullPointerException` popped up, add `if` statements to make all things you are invoking are `NotNull`.

### Commonly Used Fields & Methods

#### Interpolation: `Interp` & `NHInterp`

![](https://github.com/Yuria-Shikibe/NewHorizonMod/raw/main/github-pictures/guide/interps.png)
- You can get these from class `arc.math.Interp`, `newhorizon.util.func.NHInterp`.
- These are use as animation curves, which can adjust the progress of animations.
- If you activated the `Tool Panel` & `Debug Mod` in `Mod Settings`, you can access this table from *Cheat Table -> Debug -> Interp*.
---

#### Action: `arc.scene.Action`

- Originally used for UI animations. Now particular of them are usable in the cutscene scripts.
  - `DelayAction`
  - `ParallelAction`
  - `SequenceAction`
  - `RunnableAction`
  - `ImportantRunnableAction`
  - `LabelAction`
  - `CameraMoveAction`
  - `CameraTrackerAction`
  - `CautionAction`
  - `AddAction`
  - `AddListenerAction`
  - `RemoveListenerAction`
  - `AfterAction`
  - `IntAction`
  - `FloatAction`
  - `TimeScaleAction`
  - `RepeatAction`

--- 

#### Fields & Methods From Class: `CutsceneScript`

##### Actor Vault

```java
public static final Seq<Runnable> curUpdater = new Seq<>(), curIniter = new Seq<>();
public static final Seq<Cons<Boolean>> curEnder = new Seq<>();
```

- `curUpdater` Used to store movements that is acted every update(Do not run during pause).
- `curIniter` Used to store movements that is acted when the world is loaded.
- `curEnder` Used to store movements that is acted when game over. Param `Boolean`: true -> win; false -> lose.

---

##### Block Destroy Listener

```java
public static final ObjectMap<Block, Cons<Building>> blockDestroyListener = new ObjectMap<>();
```

- Used to store movements that will be called when a specific type of block is destroyed

---

##### Timer

```java
public static Interval timer = new Interval(6);
```

- Used for events that have short spacing.
- Use *Method:* `reload(...)` for events that have long time spacing and need to be saved.

---

##### Methods:

###### addListener(Seq<Block> types, Cons<Building> actor)
```java
public static void addListener(Seq<Block> types, Cons<Building> actor){
    for(Block type : types)addListener(type, actor);
}
```
- Used for adding `Block Destroy Listener` for multiple block types at once

###### canInit()
```java
public static boolean canInit(){
    boolean b = !state.rules.tags.containsKey("inited") || !Boolean.parseBoolean(state.rules.tags.get("inited"));
    state.rules.tags.put("inited", "true");
    initHasRun = true;
    return b;
}
```
- Used for judging whether the mod hasn't running init cutscenes.
- Using it in a `if` statement, and write the initialization action in the following statement

###### eventHasData(String key)
```java
public static boolean eventHasData(String key){
    return state.rules.tags.containsKey(key);
}
```
- Used for checking whether an event has its data or not.
- Use it to know whether an event has happened or is going to happen.

###### run(String key, Boolf\<String> boolf, Runnable run)
```java
public static void run(String key, Boolf<String> boolf, Runnable run){
    if(state.rules.tags.containsKey(key) && boolf.get(state.rules.tags.get(key))){
        run.run();
    }
}
```
- Used for running an event when the data of the event is qualified.

###### getBool(String key)
```java
public static boolean getBool(String key){
    return state.rules.tags.containsKey(key) && Boolean.parseBoolean(state.rules.tags.get(key));
}
```
- Used for getting a `true` if an event's data equals `"true"`, or it will return `false`.

###### getFloat(String key)
```java
public static float getFloat(String key){
    return Float.parseFloat(state.rules.tags.get(key));
}
```
- Used for getting a `float` if an event's data in the string does not contain a parsable `float`, or it will throw an `Exception`.

###### getFloatOrNaN(String key)
```java
public static float getFloatOrNaN(String key){
    float f = Float.NaN;
    try{
        f = Float.parseFloat(state.rules.tags.get(key));
    }catch(Exception ignore){}
    return f;
}
```
- Used for getting a `float` if an event's data in the string does not contain a parsable `float`, or it will return `Float.NaN`.

~~Sorry I'm too tired, I may finish these in the future. Read [CutsceneScript.java](https://github.com/Yuria-Shikibe/NewHorizonMod/blob/main/src/newhorizon/feature/CutsceneScript.java) first~~

