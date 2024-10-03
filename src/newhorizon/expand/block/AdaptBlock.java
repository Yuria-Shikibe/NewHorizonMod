package newhorizon.expand.block;

import arc.util.Structs;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

import java.lang.reflect.Constructor;

/**used as the base of many blocks for NH.
 * @see AdaptBuilding
 *  */
public class AdaptBlock extends Block {


    public boolean isGraphEntity;
    //xen modules, similar to hasItem, hasLiquid
    public boolean hasXen;
    public boolean xenInput;
    public boolean xenOutput;
    public float xenArea;

    public AdaptBlock(String name) {
        super(name);

        solid = true;
        destructible = true;
        group = BlockGroup.walls;
        canOverdrive = false;
        envEnabled = Env.any;
    }

    @Override
    public void setBars() {
        super.setBars();

        if(hasXen){
            addBar("xen-frequency", entity -> {
                AdaptBuilding build = (AdaptBuilding)entity;
                return new Bar(build::getXenText, build::getXenSmoothColor, build::getXenFrac);
            });
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initBuilding() {
        try{
            Class<?> current = getClass();

            if(current.isAnonymousClass()){
                current = current.getSuperclass();
            }

            subclass = current;

            while(buildType == null && Block.class.isAssignableFrom(current)){
                //first class that is subclass of Building
                Class<?> type = Structs.find(current.getDeclaredClasses(), t -> Building.class.isAssignableFrom(t) && !t.isInterface());
                if(type != null){
                    //these are inner classes, so they have an implicit parameter generated
                    Constructor<? extends Building> cons = (Constructor<? extends Building>)type.getDeclaredConstructor(type.getDeclaringClass());
                    buildType = () -> {
                        try{
                            return cons.newInstance(this);
                        }catch(Exception e){
                            throw new RuntimeException(e);
                        }
                    };
                }

                //scan through every superclass looking for it
                current = current.getSuperclass();
            }

        }catch(Throwable ignored){}

        if(buildType == null){
            //assign default value
            buildType = AdaptBuilding::create;
        }
    }
}
