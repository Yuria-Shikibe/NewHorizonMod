package newhorizon.expand.block.special;

import arc.Core;
import arc.Input;
import arc.graphics.Color;
import arc.math.geom.Point2;
import arc.scene.ui.TextArea;
import arc.scene.ui.layout.Table;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.WorldLabel;
import mindustry.graphics.Drawf;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.MessageBlock;
import mindustry.world.meta.BuildVisibility;
import newhorizon.util.ui.TableFunc;

import static mindustry.Vars.mobile;
import static mindustry.Vars.tilesize;

public class LabelSpawner extends MessageBlock{
	public LabelSpawner(String name){
		super(name);
		
		canOverdrive = false;
		targetable = false;
		forceDark = true;
		privileged = true;
		
		maxTextLength = 1000;
		
		config(Integer.class, LabelSpawnerBuild::setTargetPos);
		
		category = Category.logic;
		buildVisibility = BuildVisibility.sandboxOnly;
		requirements = ItemStack.empty;
	}
	
	@Override
	public boolean checkForceDark(Tile tile){
		return !accessible();
	}
	
	@Override
	public boolean isHidden(){
		return !accessible();
	}
	
	@Override
	public boolean canBreak(Tile tile){
		return accessible();
	}
	
	@Override
	public boolean canBeBuilt(){
		return accessible();
	}
	
	@Override
	public boolean accessible(){
		return Vars.state.rules.editor;
	}
	
	public class LabelSpawnerBuild extends MessageBuild{
		public int targetPos = -1;
		
		@Override public void damage(Team source, float damage){}
		
		@Override
		public void buildConfiguration(Table table){
			if(!accessible()){
				deselect();
				return;
			}
			
			table.button(Icon.pencil, Styles.cleari, () -> {
				if(mobile){
					Core.input.getTextInput(new Input.TextInput(){{
						text = message.toString();
						multiline = true;
						maxLength = maxTextLength;
						accepted = str -> {
							if(!str.equals(text)) configure(str);
						};
					}});
				}else{
					BaseDialog dialog = new BaseDialog("@editmessage");
					dialog.setFillParent(false);
					TextArea a = dialog.cont.add(new TextArea(message.toString().replace("\r", "\n"))).size(380f, 160f).get();
					a.setFilter((textField, c) -> {
						if(c == '\n'){
							int count = 0;
							for(int i = 0; i < textField.getText().length(); i++){
								if(textField.getText().charAt(i) == '\n'){
									count++;
								}
							}
							return count < maxNewlines;
						}
						return true;
					});
					a.setMaxLength(maxTextLength);
					dialog.cont.row();
					dialog.cont.label(() -> a.getText().length() + " / " + maxTextLength).color(Color.lightGray);
					dialog.buttons.button("@ok", () -> {
						if(!a.getText().equals(message.toString())) configure(a.getText());
						dialog.hide();
					}).size(130f, 60f);
					dialog.update(() -> {
						if(tile.build != this){
							dialog.hide();
						}
					});
					dialog.closeOnBack();
					dialog.show();
				}
				deselect();
			}).size(50f);
			table.button(Icon.link, Styles.cleari, () -> {
				TableFunc.selectPos(table, p -> {
					targetPos = p.pack();
				});
			}).size(50f);
		}
		
		public void setTargetPos(int p){
			targetPos = p;
		}
		
		@Override public void onDestroyed(){}
		
		@Override public void afterDestroyed(){}
		
		@Override
		public void created(){
			Core.app.post(this::addLabel);
		}
		
		public void addLabel(){
			if(!Vars.net.client() && targetPos != -1){
				WorldLabel l = Pools.obtain(WorldLabel.class, WorldLabel::create);
				String s = message.toString();
				if(s.startsWith("@"))l.text(Core.bundle.get(s.replaceFirst("@", "")));
				else l.text(s);
				Tmp.p1.set(Point2.unpack(targetPos));
				l.set(Tmp.p1.x * tilesize, Tmp.p1.y * tilesize);
				l.add();
			}
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.i(targetPos);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			targetPos = read.i();
		}
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			
			if(targetPos != -1){
				Tmp.p1.set(Point2.unpack(targetPos));
				Drawf.square(Tmp.p1.x * tilesize, Tmp.p1.y * tilesize, 6, team.color);
			}
		}
		
		@Override
		public void drawSelect(){
			if(accessible())super.drawSelect();
		}
	}
}
