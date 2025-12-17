package newhorizon.content.blocks;

import mindustry.content.Blocks;
import mindustry.content.StatusEffects;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.MultiPacker;
import mindustry.world.Block;
import mindustry.world.blocks.environment.*;
import newhorizon.content.NHContent;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.environment.OreVein;
import newhorizon.expand.block.environment.TiledFloor;

public class EnvironmentBlock {
    public static Block metalFloorPlain, labFloorLight, labFloorDark;

    public static Floor armorClear;

    public static Block
            oreZeta, oreSilicon, oreSilicar,
            oreSmallTitanium, oreNormalTitanium, oreDenseTitanium, orePureTitanium,
            oreClusterTitanium,
            platingFloor1, platingFloor2, platingFloor3, platingFloor4,
            conglomerateWall, darkConglomerateWall, thoriumStoneWall,
            conglomerateBoulder, darkConglomerateBoulder,

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

        oreSmallTitanium = new OreVein("ore-small-titanium", NHItems.titanium, 0.5f);
        oreNormalTitanium = new OreVein("ore-normal-titanium", NHItems.titanium, 1f);
        oreDenseTitanium = new OreVein("ore-dense-titanium", NHItems.titanium, 2f);
        orePureTitanium = new OreVein("ore-pure-titanium", NHItems.titanium, 4f);
        oreClusterTitanium = new TallBlock("ore-cluster-titanium") {{
            itemDrop = NHItems.titanium;
            shadowOffset = -1f;
            variants = 3;

            attributes.set(NHContent.density, 1f);
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

        conglomerateBoulder = new Prop("conglomerate-boulder") {{
            variants = 4;
        }};
        darkConglomerateBoulder = new Prop("dark-conglomerate-boulder") {{
            variants = 4;
        }};

        conglomerateSparse = new Floor("conglomerate-sparse") {{
            variants = 6;
            wall = conglomerateWall;
            decoration = conglomerateBoulder;
        }};
        conglomerate = new Floor("conglomerate") {{
            variants = 6;
            wall = conglomerateWall;
            decoration = conglomerateBoulder;
        }
            @Override
            public void createIcons(MultiPacker packer) {
                super.createIcons(packer);
                mapColor.set(conglomerateSparse.mapColor).mul(1.05f);
            }
        };
        conglomerateDense = new Floor("conglomerate-dense") {{
            variants = 6;
            wall = conglomerateWall;
            decoration = conglomerateBoulder;
        }
            @Override
            public void createIcons(MultiPacker packer) {
                super.createIcons(packer);
                mapColor.set(conglomerateSparse.mapColor).mul(1.10f);
            }
        };
        darkConglomerateSparse = new Floor("dark-conglomerate-sparse") {{
            variants = 6;
            wall = darkConglomerateWall;
            decoration = darkConglomerateBoulder;
        }};
        darkConglomerate = new Floor("dark-conglomerate") {{
            variants = 6;
            wall = darkConglomerateWall;
            decoration = darkConglomerateBoulder;
        }
            @Override
            public void createIcons(MultiPacker packer) {
                super.createIcons(packer);
                mapColor.set(darkConglomerateSparse.mapColor).mul(1.05f);
            }
        };
        darkConglomerateDense = new Floor("dark-conglomerate-dense") {{
            variants = 6;
            wall = darkConglomerateWall;
            decoration = darkConglomerateBoulder;
        }
            @Override
            public void createIcons(MultiPacker packer) {
                super.createIcons(packer);
                mapColor.set(darkConglomerateSparse.mapColor).mul(1.10f);
            }
        };

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
        }
            @Override
            public void createIcons(MultiPacker packer) {
                super.createIcons(packer);
                mapColor.set(thoriumStoneSparse.mapColor).mul(1.05f);
            }
        };
        thoriumStoneDense = new Floor("thorium-stone-dense") {{
            variants = 4;
            wall = thoriumStoneWall;
        }
            @Override
            public void createIcons(MultiPacker packer) {
                super.createIcons(packer);
                mapColor.set(thoriumStoneSparse.mapColor).mul(1.10f);
            }
        };

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

        Blocks.vibrantCrystalCluster.itemDrop = NHItems.thorium;
        Blocks.crystalCluster.itemDrop = NHItems.thorium;
        Blocks.crystalOrbs.itemDrop = NHItems.beryllium;
        Blocks.whiteTree.itemDrop = NHItems.sporePod;
        Blocks.crystalBlocks.itemDrop = NHItems.sand;

        Blocks.vibrantCrystalCluster.attributes.set(NHContent.density, 1f);
        Blocks.crystalCluster.attributes.set(NHContent.density, 1f);
        Blocks.crystalOrbs.attributes.set(NHContent.density, 0.75f);
        Blocks.whiteTree.attributes.set(NHContent.density, 0.25f);
        Blocks.crystalBlocks.attributes.set(NHContent.density, 0.5f);
    }
}
