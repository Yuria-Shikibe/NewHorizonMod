const event = extend(FleetEventClass, "fleet-event", {});
event.unitTypeMap = ObjectMap.of(
    //UnitType Reference|单位可引用自类: NHUnitTypes, UnitTypes
    //格式|Format: <UnitType, Number>, <UnitType, Number> ...
    //Eg:
    NHUnitTypes.warper, 5, NHUnitTypes.branch, 5, NHUnitTypes.sharp, 10
);
handleEvent(event);