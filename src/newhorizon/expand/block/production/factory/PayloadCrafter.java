package newhorizon.expand.block.production.factory;

import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadConveyor;
import mindustry.world.blocks.units.UnitAssembler;
import newhorizon.content.blocks.ModuleBlock;
import newhorizon.expand.block.consumer.NHConsumeItemDynamic;
import newhorizon.expand.block.consumer.NHConsumeLiquidDynamic;
import newhorizon.expand.block.consumer.NHConsumePayloadDynamic;
import newhorizon.expand.block.consumer.NHConsumeShowStat;
import newhorizon.util.ui.ContentSelectionTable;

public class PayloadCrafter extends AdaptCrafter {
    public Seq<Block> filter = new Seq<>();
    public ObjectIntMap<Block> multiplier = new ObjectIntMap<>();
    public int payloadCapacity = 5;

    public PayloadCrafter(String name) {
        super(name);
        acceptsPayload = true;
        acceptsUnitPayloads = false;
        configurable = true;

        consume(new NHConsumeItemDynamic((PayloadCrafterBuild e) -> {
            if(e.recipeCost() != null) return e.recipeCost().itemReq.copy().toArray(ItemStack.class);
            return ItemStack.empty;
        }));
        consume(new NHConsumeLiquidDynamic((PayloadCrafterBuild e) -> {
            if(e.recipeCost() != null) return e.recipeCost().liquidReq.copy().toArray(LiquidStack.class);
            return LiquidStack.empty;
        }));
        consume(new NHConsumePayloadDynamic((PayloadCrafterBuild e) -> {
            if(e.recipeCost() != null) return e.recipeCost().payloadReq;
            return PayloadStack.list();
        }));
        consume(new NHConsumeShowStat(
            (PayloadCrafterBuild e) -> {
                if(e.recipeCost() != null) return e.recipeCost().itemReq.copy().toArray(ItemStack.class);
                return ItemStack.empty;
            },
            (PayloadCrafterBuild e) -> {
                if(e.recipeCost() != null) return e.recipeCost().liquidReq.copy().toArray(LiquidStack.class);
                return LiquidStack.empty;
            },
            (PayloadCrafterBuild e) -> {
                if(e.recipeCost() != null) return e.recipeCost().payloadReq.copy().toArray(PayloadStack.class);
                return PayloadStack.with();
            },
            PayloadCrafterBuild::getPayloads
        ));

        configClear((PayloadCrafterBuild tile) -> tile.recipe = null);
        config(Block.class, (PayloadCrafterBuild tile, Block block) -> {
            if (tile.recipe != block) tile.progress = 0f;
            if (filter.contains(block)) tile.recipe = block;
        });
        configClear((PayloadCrafterBuild tile) -> tile.recipe = null);
    }

    public class PayloadCrafterBuild extends AdaptCrafterBuild {
        public @Nullable Block recipe;
        public PayloadSeq payloads = new PayloadSeq();

        @Override
        public Object config(){
            return recipe;
        }

        @Override
        public PayloadSeq getPayloads() {
            return payloads;
        }

        public ModuleBlock.ModuleCost recipeCost(){
            if(recipe != null && ModuleBlock.moduleCosts.get(recipe) != null) return ModuleBlock.moduleCosts.get(recipe);
            return null;
        }

        @Override
        public void buildConfiguration(Table table) {
            ContentSelectionTable.buildModuleTable(PayloadCrafter.this, table, filter, () -> recipe, this::configure);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return recipeCost() != null
                    && recipeCost().itemReq.contains(stack -> stack.item == item)
                    && items.get(item) < recipeCost().itemReq.find(stack -> stack.item == item).amount * 2;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return recipeCost() != null
                    && recipeCost().liquidReq.contains(stack -> stack.liquid == liquid)
                    && liquids.get(liquid) < liquidCapacity;
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            return recipeCost() != null
                    && recipeCost().payloadReq.contains(stack -> stack.item == payload.content())
                    && payloads.get(payload.content()) < payloadCapacity;
        }

        @Override
        public boolean shouldConsume() {
            return super.shouldConsume() && recipe != null && payloads.get(recipe) < payloadCapacity;
        }

        @Override
        public float getProgressIncrease(float baseTime){
            baseTime = recipe == null ? baseTime : recipeCost().craftTime * 0.99f;
            if(ignoreLiquidFullness){
                return super.getProgressIncrease(baseTime);
            }

            //limit progress increase by maximum amount of liquid it can produce
            float scaling = 1f, max = 1f;
            if(outputLiquids != null){
                max = 0f;
                for(var s : outputLiquids){
                    float value = (liquidCapacity - liquids.get(s.liquid)) / (s.amount * edelta());
                    scaling = Math.min(scaling, value);
                    max = Math.max(max, value);
                }
            }

            //when dumping excess take the maximum value instead of the minimum.
            return super.getProgressIncrease(baseTime) * (dumpExtraLiquid ? Math.min(max, 1f) : scaling);
        }

        @Override
        public void craft(){
            consume();

            if(outputItems != null){
                for(var output : outputItems){
                    for(int i = 0; i < output.amount; i++){
                        offload(output.item);
                    }
                }
            }

            if(wasVisible){
                craftEffect.at(x, y);
            }

            payloads.add(recipe, multiplier.get(recipe, 1));
            progress %= 1f;
        }

        @Override
        public void dumpOutputs() {
            super.dumpOutputs();
            if (recipe != null) {
                BuildPayload payload = new BuildPayload(recipe, team);
                payload.set(x, y, 0);
                dumpPayload(payload);
            }
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            payloads.add(payload.content(), 1);
            Fx.payloadDeposit.at(source.x(), source.y(), source.angleTo(this), new UnitAssembler.YeetData(new Vec2(x, y), payload.content()));
        }

        @Override
        public boolean dumpPayload(Payload todump) {
            if (this.proximity.size != 0) {
                int dump = dumpIndex;
                for (int i = 0; i < linkProximityMap.size; ++i) {
                    int idx = (i + dump) % linkProximityMap.size;
                    Building[] pair = linkProximityMap.get(idx);
                    Building target = pair[0];
                    Building source = pair[1];
                    if (todump != null && payloads.get(todump.content()) > 0 && target.acceptPayload(source, todump)) {
                        target.handlePayload(this, todump);
                        payloads.remove(todump.content(), 1);
                        if (target instanceof PayloadConveyor.PayloadConveyorBuild){
                            Fx.payloadDeposit.at(x, y, this.angleTo(target), new UnitAssembler.YeetData(new Vec2(target.x, target.y), todump.content()));
                        }
                        incrementDumpIndex(linkProximityMap.size);
                        return true;
                    }
                    incrementDumpIndex(linkProximityMap.size);
                }

            }
            return false;
        }
    }
}
