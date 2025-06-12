package newhorizon.util.ui;

import arc.func.Boolp;
import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.math.Interp;
import arc.scene.Action;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.ArcRuntimeException;
import arc.util.Nullable;

public class DelayCollapser extends WidgetGroup {
    Table table;
    @Nullable
    Boolp collapsedFunc;
    private CollapseAction collapseAction = new CollapseAction();
    boolean collapsed, autoAnimate;
    boolean actionRunning;
    float currentHeight;
    float delayTime = 2f;
    float delayTimer;
    float seconds = 0.4f;

    public DelayCollapser(Cons<Table> cons, boolean collapsed){
        this(new Table(), collapsed);
        cons.get(table);
    }

    public DelayCollapser(Table table, boolean collapsed){
        this.table = table;
        this.collapsed = collapsed;
        setTransform(true);

        updateTouchable();
        addChild(table);
    }

    public DelayCollapser setDuration(float seconds){
        this.seconds = seconds;
        return this;
    }

    public DelayCollapser setCollapsed(Boolp collapsed){
        this.collapsedFunc = collapsed;
        return this;
    }

    public DelayCollapser setCollapsed(boolean autoAnimate, Boolp collapsed){
        this.collapsedFunc = collapsed;
        this.autoAnimate = autoAnimate;
        return this;
    }

    public void toggle(){
        setCollapsed(!isCollapsed());
    }

    public void toggle(boolean animated){
        setCollapsed(!isCollapsed(), animated);
    }

    public void setCollapsed(boolean collapse, boolean withAnimation){
        this.collapsed = collapse;
        updateTouchable();

        if(table == null) return;

        actionRunning = true;

        if(withAnimation){
            addAction(collapseAction);
        }else{
            if(collapse){
                currentHeight = 0;
                collapsed = true;
            }else{
                currentHeight = table.getPrefHeight();
                collapsed = false;
            }

            actionRunning = false;
            invalidateHierarchy();
        }
    }

    public void setCollapsed(boolean collapse){
        setCollapsed(collapse, true);
    }

    public boolean isCollapsed(){
        return collapsed;
    }

    private void updateTouchable(){
        Touchable touchable1 = collapsed ? Touchable.disabled : Touchable.enabled;
        this.touchable = touchable1;
    }

    @Override
    public void draw(){
        if(currentHeight > 1){
            Draw.flush();
            if(clipBegin(x, y, getWidth(), currentHeight)){
                super.draw();
                Draw.flush();
                clipEnd();
            }
        }
    }

    @Override
    public void act(float delta){
        super.act(delta);

        if(collapsedFunc != null){
            boolean col = collapsedFunc.get();
            if(col != collapsed){
                setCollapsed(col, autoAnimate);
            }
        }
    }

    @Override
    public void layout(){
        if(table == null) return;

        table.setBounds(0, 0, getWidth(), getHeight());

        if(!actionRunning){
            if(collapsed)
                currentHeight = 0;
            else
                currentHeight = table.getPrefHeight();
        }
    }

    @Override
    public float getPrefWidth(){
        return table == null ? 0 : table.getPrefWidth();
    }

    @Override
    public float getPrefHeight(){
        if(table == null) return 0;

        if(!actionRunning){
            if(collapsed)
                return 0;
            else
                return table.getPrefHeight();
        }

        return currentHeight;
    }

    public void setTable(Table table){
        this.table = table;
        clearChildren();
        addChild(table);
    }

    @Override
    public float getMinWidth(){
        return 0;
    }

    @Override
    public float getMinHeight(){
        return 0;
    }

    @Override
    protected void childrenChanged(){
        super.childrenChanged();
        if(getChildren().size > 1) throw new ArcRuntimeException("Only one actor can be added to CollapsibleWidget");
    }

    private class CollapseAction extends Action {
        CollapseAction(){
        }

        @Override
        public boolean act(float delta){
            if(collapsed){
                if (delayTimer <= delayTime){
                    delayTimer += delta;
                }else {
                    currentHeight -= delta * table.getPrefHeight() / seconds;
                    if(currentHeight <= 0){
                        currentHeight = 0;
                        actionRunning = false;
                    }

                    delayTime = 0f;
                }

            }else{
                currentHeight += delta * table.getPrefHeight() / seconds;
                if(currentHeight > table.getPrefHeight()){
                    currentHeight = table.getPrefHeight();
                    actionRunning = false;
                }
            }

            invalidateHierarchy();
            return !actionRunning;
        }
    }
}