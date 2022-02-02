//--------------------------------------------
const event = extend(FleetEventClass, "custom-fleet-event", {});
event.unitTypeMap = ObjectMap.of(
    //UnitType Reference|单位可引用自类: NHUnitTypes, UnitTypes
    //格式|Format: <UnitType, Number>, <UnitType, Number> ...
    //Eg:
    NHUnitTypes.warper, 5, NHUnitTypes.branch, 5, NHUnitTypes.sharp, 10, UnitTypes.flare, 20
);
handleEvent(event);
//Copy Above as the constructor|复制以上代码作为构造器

//--------------------------------------------
//BulletType Reference|子弹可引用自类: NHBullets, Bullets
//格式|Format: <UnitType, Number>, <UnitType, Number> ...
//Eg:
const event = extend(RaidEventClass, "custom-raid-event", {});
event.number = 100; //Shots|发射数
event.shootDelay = 3; //Spacing between shots|每次发生间隔 [Unit:tick]|[单位:帧]
event.inaccuracy = 3; //散布/角度
event.sourceSpread = 200; //The spread range of shoot positions|射击源的散布范围
event.shootModifier = BulletHandler.spread1; //Modify to the bullet|对已发射子弹的修改 [Lambda Expression/Cons<Bullet>]
handleEvent(event);
//Copy Above as the constructor|复制以上代码作为构造器