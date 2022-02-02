//Since v1.10.7

// Only Simple Apis are listed here. If you want to know more, see the cutscene guide.
// 此处仅列出了简单 API。如果您想了解更多信息，请参阅过场动画指南。

//--------------------------------------------
//Fleet Inbound Event|舰队入场事件
const event = extend(FleetEventClass, "custom-fleet-event", {});
event.unitTypeMap = ObjectMap.of(
    //UnitType Reference|单位可引用自类: NHUnitTypes, UnitTypes
    //格式|Format: <UnitType, Number>, <UnitType, Number> ...
    //Eg:
    NHUnitTypes.warper, 5, NHUnitTypes.branch, 5, NHUnitTypes.sharp, 10, UnitTypes.flare, 20
);
event.reloadTime = 20 * 60; //Interval between alert and inbound|每次从警报到入场的间隔 [Unit:tick]|[单位:帧]
handleEvent(event);
//Copy Above as the constructor|复制以上代码作为构造器

//UnitTable|单位引用表

/*
NHUnitTypes.
    guardian, //Energy
    gather, saviour, rhino, //Air-Assist
    assaulter, anvil, collapser, //Air-2
    origin, thynomo, aliotiat, tarlidor, annihilation, sin, //Ground-1
    sharp, branch, warper, striker, naxos, destruction, longinus, hurricane, //Air-1
    relay, ghost, zarkov, declining; //Navy

UnitTypes.
    mace;
    dagger;
    crawler;
    fortress
    scepter;
    reign;
    vela;
    nova;
    pulsar;
    quasar;
    corvus;
    atrax;
    spiroct;
    arkyid;
    toxopid;
    flare;
    eclipse;
    horizon;
    zenith;
    antumbra
    mono;
    poly;
    mega;
    quad;
    oct;
    alpha;
    beta;
    gamma;
    risso;
    minke;
    bryde;
    sei;
    omura;
    retusa;
    oxynoe;
    cyerce;
    aegires;
    navanax;
*/

//--------------------------------------------
//Raid Event|空袭事件
const event = extend(RaidEventClass, "custom-raid-event", {});
event.bulletType = NHBullets.artilleryThermo; //BulletType Reference|子弹可引用自类: NHBullets, Bullets
event.number = 100; //Shots|发射数
event.shootDelay = 3; //Spacing between shots|每次发生间隔 [Unit:tick]|[单位:帧]
event.inaccuracy = 3; //散布/角度
event.sourceSpread = 200; //The spread range of shoot positions|射击源的散布范围
event.reloadTime = 60 * 60; //Interval between alert and raid|每次从警报到袭击的间隔 [Unit:tick]|[单位:帧]
handleEvent(event);
//Copy Above as the constructor|复制以上代码作为构造器

/*
    NHBullets.
        ultFireball,
        synchroZeta, synchroThermoPst, synchroFusion, synchroPhase,
        longRangeShoot, longRangeShootRapid, longRangeShootSplash, mineShoot,
        artilleryIrd, artilleryFusion, artilleryPlast, artilleryThermo, artilleryPhase, artilleryMissile,
        railGun1, railGun2, hurricaneType, polyCloud, missileTitanium, missileThorium, missileZeta, missile, missileStrike,
        strikeLaser, tear, skyFrag, hurricaneLaser, hyperBlast, hyperBlastLinker, huriEnergyCloud, warperBullet,
        none, supSky, darkEnrLightning, darkEnrlaser, decayLaser, longLaser, rapidBomb, airRaid,
        blastEnergyPst, blastEnergyNgt, curveBomb, strikeRocket, annMissile, collapserBullet, collapserLaserSmall, guardianBullet,
        strikeMissile, arc_9000, empFrag, empBlot2, empBlot3, antiAirSap, eternity, airRaidMissile, destructionRocket;

    Bullets.
        //artillery
        artilleryDense, artilleryPlastic, artilleryPlasticFrag, artilleryHoming, artilleryIncendiary, artilleryExplosive,

        //flak
        flakScrap, flakLead, flakGlass, flakGlassFrag,

        //frag (flak-like but hits ground)
        fragGlass, fragExplosive, fragPlastic, fragSurge, fragGlassFrag, fragPlasticFrag,

        //missiles
        missileExplosive, missileIncendiary, missileSurge,

        //standard
        standardCopper, standardDense, standardThorium, standardHoming, standardIncendiary,
        standardDenseBig, standardThoriumBig, standardIncendiaryBig,

        //liquid
        waterShot, cryoShot, slagShot, oilShot, heavyWaterShot, heavyCryoShot, heavySlagShot, heavyOilShot,

        //environment, misc.
        damageLightning, damageLightningGround, fireball, basicFlame, pyraFlame;
*/

//--------------------------------------------
//Countdown Event|倒计时事件
const event = extend(SimpleReloadEventClass, "custom-reload-event", {});
event.action = run(() => {
    //Apply JS Codes Here: What you want to do|请在此处加速关于你想做什么的JS代码
});
event.info = prov(() => "/*Text To Be Showed On HUD | 在HUD上显示的文本*/");
event.color = prov(() => Pal.head);//Color To Be Showed On HUD | 在HUD上显示的颜色
handleEvent(event);
//Copy Above as the constructor|复制以上代码作为构造器

//--------------------------------------------
//Signal Event|信号事件
const event = extend(SignalEventClass, "custom-signal-event", {});
event.action = run(() => {
    //Apply JS Codes Here: What you want to do|请在此处加速关于你想做什么的JS代码
});
event.range = 1200;//The max radius begin to hint the signal | 提示信号的最大半径
event.position = new Vec2(/*xCoord*/0, /*yCoord*/0); //The position of the source of the signal | 信号源坐标
handleEvent(event);
//Copy Above as the constructor|复制以上代码作为构造器



//Complex Below|下方的事件比较复杂，接口没有完全列出，建议查看源码

//--------------------------------------------
//Objective Event|任务事件
const event = extend(ObjectiveEventClass, "custom-mission-event", {});
event.action = cons(e => {
    //Apply JS Codes Here: What you want to do|请在此处加速关于你想做什么的JS代码
});
//Complex, js knowledge are required|较复杂，需要js知识
//Check the fields in the class: ObjectiveEvent
handleEvent(event);
//Copy Above as the constructor|复制以上代码作为构造器

//--------------------------------------------
//Destroy Blocks Event|击毁建筑任务事件
const event = extend(DestroyObjectiveEventClass, "custom-destroy-mission-event", {});
event.targetBlock = NHBlocks.jumpGate;//BulletType Reference|子弹可引用自类: NHBlocks, Blocks
event.targets = func(e => {
    //return Seq<Building>
    //Targets|目标
});
event.action = cons(e => {
    //Apply JS Codes Here: What you want to do|请在此处加速关于你想做什么的JS代码
});
handleEvent(event);
//Copy Above as the constructor|复制以上代码作为构造器

