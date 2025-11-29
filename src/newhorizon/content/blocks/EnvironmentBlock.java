package newhorizon.content.blocks;

import mindustry.content.Liquids;
import mindustry.content.StatusEffects;
import mindustry.graphics.CacheLayer;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.StaticWall;
import newhorizon.content.*;
import newhorizon.expand.block.env.*;

public class EnvironmentBlock {
    public static Atlas_4_12_Floor metalFloorGroove, metalFloorGrooveDeep, metalFloorRidge, metalFloorRidgeHigh;
    public static Atlas_4_4_Floor armorAncient, armorAncientSub, armorQuantum;

    public static Atlas_4_12_Wall armorWall;
    public static Block metalFloorPlain, labFloorLight, labFloorDark;
    public static DataFloor lineMarkingFloor, lineMarkingFloorQuantum, lineMarkingFloorQuantumDark, lineMarkingFloorAncient, lineMarkingFloorAncientDark;

    public static Floor armorClear;
    public static OreBlock oreZeta, oreSilicon, oreSilicar;

    public static Block
            platingFloor1, platingFloor2, platingFloor3, platingFloor4,
            conglomerateWall, darkConglomerateWall, thoriumStoneWall,

            conglomerateSparse, conglomerate, conglomerateDense,
            darkConglomerateSparse, darkConglomerate, darkConglomerateDense,
            cryoniteSparse, cryonite,
            erodeRock, erodeRockDense,
            siliceoustone,
            thoriumStoneSparse, thoriumStone, thoriumStoneDense,
            ammoniaWater, ammoniaWaterDeep, ammoniaWaterShallow,
            zetaCrystalFloor;

    public static void load() {
        oreZeta = new OreBlock("ore-zeta") {{
            oreDefault = true;
            variants = 3;
            oreThreshold = 0.95F;
            oreScale = 20.380953F;
            itemDrop = NHItems.zeta;
            localizedName = itemDrop.localizedName;
            mapColor.set(itemDrop.color);
            useColor = true;
        }};
        oreSilicar = new OreBlock("ore-silicar") {{
            oreDefault = true;
            variants = 3;
            oreThreshold = 0.95F;
            oreScale = 20.380953F;
            itemDrop = NHItems.silicar;
            localizedName = itemDrop.localizedName;
            mapColor.set(itemDrop.color);
            useColor = true;
        }};

        conglomerateWall = new StaticWall("conglomerate-wall") {{
            variants = 3;
        }};

        darkConglomerateWall = new StaticWall("dark-conglomerate-wall") {{
            variants = 3;
        }};

        thoriumStoneWall = new StaticWall("thorium-stone-wall") {{
            variants = 3;
        }};

        conglomerateSparse = new Floor("conglomerate-sparse") {{
            variants = 6;
            wall = conglomerateWall;
        }};
        conglomerate = new Floor("conglomerate") {{
            variants = 6;
            wall = conglomerateWall;
        }};
        conglomerateDense = new Floor("conglomerate-dense") {{
            variants = 6;
            wall = conglomerateWall;
        }};
        darkConglomerateSparse = new Floor("dark-conglomerate-sparse") {{
            variants = 6;
            wall = darkConglomerateWall;
        }};
        darkConglomerate = new Floor("dark-conglomerate") {{
            variants = 6;
            wall = darkConglomerateWall;
        }};
        darkConglomerateDense = new Floor("dark-conglomerate-dense") {{
            variants = 6;
            wall = darkConglomerateWall;
        }};

        cryoniteSparse = new Floor("cryonite-sparse") {{
            variants = 3;
        }};
        cryonite = new Floor("cryonite") {{
            variants = 3;
        }};

        erodeRock = new Floor("erode-rock") {{
            variants = 3;
        }};
        erodeRockDense = new Floor("erode-rock-dense") {{
            variants = 3;
        }};

        siliceoustone = new Floor("siliceoustone") {{
            variants = 4;
        }};

        thoriumStoneSparse = new Floor("thorium-stone-sparse") {{
            variants = 4;
            wall = thoriumStoneWall;
        }};
        thoriumStone = new Floor("thorium-stone") {{
            variants = 4;
            wall = thoriumStoneWall;
        }};
        thoriumStoneDense = new Floor("thorium-stone-dense") {{
            variants = 4;
            wall = thoriumStoneWall;
        }};

        zetaCrystalFloor = new Floor("zeta-crystal-floor") {{
            variants = 3;
        }};
        ammoniaWater = new Floor("ammonia-water") {{
            variants = 0;
            albedo = 0.9f;
            isLiquid = true;
            speedMultiplier = 0.75f;
            status = StatusEffects.wet;
            cacheLayer = CacheLayer.water;
            liquidDrop = NHLiquids.ammonia;
        }};

        ammoniaWaterDeep = new Floor("ammonia-water-deep") {{
            variants = 0;
            albedo = 0.9f;
            isLiquid = true;
            drownTime = 300f;
            speedMultiplier = 0.6f;
            status = StatusEffects.wet;
            cacheLayer = CacheLayer.water;
            liquidDrop = NHLiquids.ammonia;
        }};

        ammoniaWaterShallow = new Floor("ammonia-water-shallow") {{
            variants = 3;
            albedo = 0.9f;
            isLiquid = true;
            speedMultiplier = 0.9f;
            status = StatusEffects.wet;
            cacheLayer = CacheLayer.water;
            liquidDrop = NHLiquids.ammonia;
        }};

        /*
        metalFloorGroove = new Atlas_4_12_Floor("metal-floor-groove", true);
        metalFloorGrooveDeep = new Atlas_4_12_Floor("metal-floor-deep-groove", true);
        metalFloorRidge = new Atlas_4_12_Floor("metal-floor-ridge");
        metalFloorRidgeHigh = new Atlas_4_12_Floor("metal-floor-high-ridge");

         */

        armorWall = new Atlas_4_12_Wall("armor-wall");

        armorAncient = new Atlas_4_4_Floor("armor-ancient") {{
            lightColor = NHColor.ancient.cpy().a(0.7f);
            lightRadius = 15f;
            emitLight = true;
        }};
        armorAncientSub = new Atlas_4_4_Floor("armor-ancient-sub") {{
            lightColor = NHColor.ancient.cpy().a(0.7f);
            lightRadius = 15f;
            emitLight = true;
        }};
        armorQuantum = new Atlas_4_4_Floor("armor-quantum") {{
            lightColor = NHColor.darkEnrColor.cpy().a(0.7f);
            lightRadius = 15f;
            emitLight = true;
        }};

        platingFloor1 = new TiledFloor("plating-floor-1") {{
            //useTiles = false;
            tileName = "plating-floor";

            autotile = true;
            drawEdgeOut = false;
            drawEdgeIn = false;
        }};

        //platingFloor2 = new TiledFloor("plating-floor-2") {{
        //    useTiles = false;
        //    //tileName = "plating-floor";
        //    autotile = true;
        //    drawEdgeOut = false;
        //    drawEdgeIn = false;
        //}};

        platingFloor3 = new TiledFloor("plating-floor-3") {{
            useTiles = false;
            autotileVariants = 2;
            cacheLayer = NHContent.quantumLayer;

            autotile = true;
            drawEdgeOut = false;
            drawEdgeIn = false;
        }

            @Override
            public int variant(int x, int y, int max) {
                return (x + y) % 2;
            }
        };

        platingFloor4 = new TiledFloor("plating-floor-4") {{
            useTiles = false;
            cacheLayer = NHContent.quantumLayer;

            autotile = true;
            drawEdgeOut = false;
            drawEdgeIn = false;
        }};

        metalFloorPlain = new TiledFloor("plating-metal-floor"){{
            tileName = "plating-floor";

            autotile = true;
            drawEdgeOut = false;
            drawEdgeIn = false;
        }};
        //labFloorLight = new TiledFloor("lab-floor-light", 8, 1);
        //labFloorDark = new TiledFloor("lab-floor-dark", 8, 1);
        //armorClear = new Floor("armor-clear", 0) {{
        //    tilingVariants = 512 / 32;
        //}};

        //metalFloorGroove.baseFloor = metalFloorPlain;
        //metalFloorGrooveDeep.baseFloor = metalFloorPlain;
        //metalFloorRidge.baseFloor = metalFloorPlain;
        //metalFloorRidgeHigh.baseFloor = metalFloorPlain;

        armorAncient.blendFloors.add(armorAncientSub);
        armorAncientSub.blendFloors.add(armorAncient);

        armorWall.baseBlock = NHBlocks.metalWallQuantum;
    }
}
