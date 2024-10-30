package newhorizon.expand.block.struct;


//aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
public class ConveyorQueue {
    /*
    public static final float FRAME_PERIOD = 60/3f;
    public Queue<AdaptItemBridgeBuild> conveyors = new Queue<>();

    public ConveyorQueue(){}

    public ConveyorQueue(AdaptItemBridgeBuild conveyor){
        conveyors.add(conveyor);
    }

    public void addConveyor(AdaptItemBridgeBuild previous, AdaptItemBridgeBuild current, AdaptItemBridgeBuild next){
        if (current == null) return;

        if (next != null && next.convQueue != current.convQueue){
            next.convQueue.mergeQueue(current.convQueue);
        }

        if (previous != null && current.convQueue != previous.convQueue){
            current.convQueue.mergeQueue(previous.convQueue);
        }

    }

    //attach a queue to current queue's last
    public void mergeQueue(ConveyorQueue last){
        last.conveyors.each(e -> {
            conveyors.addLast(e);
            e.convQueue = this;
        });
        last.clear();
    }

    public void clear(){
        conveyors.clear();
    }

    public void draw(){
        for (int i = 0; i < conveyors.size; i++){
            Draw.color(Pal.ammo);
            AdaptItemBridgeBuild e = conveyors.get(i);
            Lines.square(e.x, e.y, 4, 45);
            DrawUtil.drawText(i + "",e.x, e.y);
        }

        AdaptItemBridgeBuild first = conveyors.first();
        Draw.color(Pal.techBlue);
        Lines.square(first.x, first.y, 4, 45);
        DrawUtil.drawText("f",first.x, first.y);

        AdaptItemBridgeBuild last = conveyors.last();
        Draw.color(Pal.remove);
        Lines.square(last.x, last.y, 4, 45);
        DrawUtil.drawText("l",last.x, last.y);

        Draw.color();
    }

     */

}
