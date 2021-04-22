package newhorizon.func;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextButton;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Weather;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.content.NHLoader;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static mindustry.Vars.content;
import static mindustry.Vars.mobile;
import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;

public class TableTexDebugDialog extends BaseDialog{
	private BaseDialog
		buttonImage,
		buttonText,
		iconDialog,
		tableDialog;
	
	public TableTexDebugDialog(String title){
		this(title, Core.scene.getStyle(Dialog.DialogStyle.class));
	}
	
	public TableTexDebugDialog(String title, Dialog.DialogStyle style) {
		super(title, style);
	}
	
	public BaseDialog init(){
		addCloseButton();
		
		cont.button("Icons", () -> {
			iconDialog = new BaseDialog("ICONS"){{
				Object obj = new Icon();
				Class<?> c = obj.getClass();
				
				Field[] fields = c.getFields();
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(f.getType().getSimpleName().equals("TextureRegionDrawable")){
								if(index % 6 == 0) t.row();
								t.table(inner -> {
									try{
										inner.image((Drawable)f.get(obj)).pad(OFFSET / 3);
									}catch(IllegalAccessException err){
										throw new IllegalArgumentException(err);
									}
									inner.add(f.getName());
								}).size(LEN * 3, LEN).pad(OFFSET / 3);
								index++;
							}
						}catch(IllegalArgumentException err){
							throw new IllegalArgumentException(err);
						}
					}
				}).grow();
			}};
			iconDialog.addCloseListener();
			iconDialog.show();
		}).size(LEN * 3, LEN).pad(OFFSET / 2).disabled(b -> mobile);
			
		cont.button("TableTexes", () -> {
			tableDialog = new BaseDialog("ICONS"){{
				Object obj = new Tex();
				Class<?> c = obj.getClass();
				
				Field[] fields = c.getFields();
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(f.getType().getSimpleName().equals("TextureRegionDrawable") || f.getType().getSimpleName().equals("NinePatchDrawable")){
								if(index % 6 == 0) t.row();
								t.table(inner -> {
									try{
										inner.table((Drawable)f.get(obj), de -> de.add(f.getName())).size(LEN * 3, LEN).pad(OFFSET / 3);
									}catch(IllegalAccessException err){
										throw new IllegalArgumentException(err);
									}
								}).size(LEN * 3, LEN).pad(OFFSET / 3);
								index++;
							}
						}catch(IllegalArgumentException err){
							throw new IllegalArgumentException(err);
						}
					}
				}).grow();
			}};
			tableDialog.addCloseListener();
			tableDialog.show();
		}).size(LEN * 3, LEN).pad(OFFSET / 2).disabled(b -> mobile);
		
		cont.button("ButtonTexts", () -> {
			buttonText = new BaseDialog("ButtonTexts"){{
				Object obj = new Styles();
				Class<?> c = obj.getClass();
				
				Field[] fields = c.getFields();
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(f.getType().getSimpleName().equals("TextButtonStyle")){
								if(index % 6 == 0) t.row();
								t.table(inner -> {
									try{
										inner.button(f.getName(), (TextButton.TextButtonStyle)f.get(obj), () -> {}).size(LEN * 3, LEN).row();
										inner.button(f.getName(), (TextButton.TextButtonStyle)f.get(obj), () -> {}).size(LEN * 3, LEN).pad(OFFSET / 3).disabled(b -> true).row();
									}catch(IllegalAccessException err){
										throw new IllegalArgumentException(err);
									}
								}).grow().pad(OFFSET / 3);
								index++;
							}
						}catch(IllegalArgumentException err){
							throw new IllegalArgumentException(err);
						}
					}
				}).grow();
			}};
			buttonText.addCloseListener();
			buttonText.show();
		}).size(LEN * 3, LEN).pad(OFFSET / 2).disabled(b -> mobile);
		
		cont.button("ButtonImages", () -> {
			buttonImage = new BaseDialog("ButtonImages"){{
				
				Object obj = new Styles();
				Class<?> c = obj.getClass();
				
				Field[] fields = c.getFields();
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(f.getType().getSimpleName().equals("ImageButtonStyle")){
								if(index % 6 == 0) t.row();
								t.table(inner -> {
									inner.table(de ->{
										try{
											de.button(Icon.none, (ImageButton.ImageButtonStyle)f.get(obj), () -> {}).size(LEN * 3, LEN).row();
										}catch(IllegalAccessException err){
											throw new IllegalArgumentException(err);
										}
										de.add(f.getName()).pad(OFFSET / 3);
									}).row();
									inner.table(de ->{
										try{
											de.button(Icon.none, (ImageButton.ImageButtonStyle)f.get(obj), () -> {}).disabled(b -> true).size(LEN * 3, LEN).row();
										}catch(IllegalAccessException err){
											throw new IllegalArgumentException(err);
										}
										de.add(f.getName()).pad(OFFSET / 3);
									}).row();
								}).grow().pad(OFFSET / 3);
								index++;
							}
						}catch(IllegalArgumentException err){
							throw new IllegalArgumentException(err);
						}
					}
				}).grow();
			}};
			buttonImage.addCloseListener();
			buttonImage.show();
		}).size(LEN * 3, LEN).pad(OFFSET / 2).disabled(b -> mobile);
		
		cont.button("Images", () -> {
			buttonImage = new BaseDialog("Images"){{
				cont.pane(table -> {
					AtomicInteger index = new AtomicInteger();
					NHLoader.outlineTex.each( (arg, tex) -> {
						if(tex != null && tex.found()){
							if(index.get() % 8 == 0)table.row();
							table.table(t -> {
								float width = Mathf.clamp(tex.width, 0, LEN * 3);
								t.table(in -> in.image(tex).size(width, tex.height * width / tex.width)).size(LEN * 3).row();
								t.add(arg).size(LEN * 3, LEN / 2);
							});
							index.getAndIncrement();
						}
					});
					NHLoader.fullIconNeeds.each( (arg, iconSet) -> {
						TextureRegion tex = Core.atlas.find(arg + "-icon");
						if(tex != null && tex.found()){
							if(index.get() % 8 == 0)table.row();
							table.table(t -> {
								float width = Mathf.clamp(tex.width, 0, LEN * 3);
								t.table(in -> in.image(tex).size(width, tex.height * width / tex.width)).size(LEN * 3).row();
								t.add(arg).size(LEN * 3, LEN / 2);
							});
							index.getAndIncrement();
						}
					});
				}).fill();
			}};
			buttonImage.addCloseListener();
			buttonImage.show();
		}).size(LEN * 3, LEN).pad(OFFSET / 2).disabled(b -> mobile);
		
		cont.row();
		
		cont.button("Units", () -> {
			buttonImage = new BaseDialog("Units"){{
				cont.pane(table -> {
					AtomicInteger index = new AtomicInteger();
					
					content.units().forEach( (unit) -> {
						if(!unit.isHidden()){
							if(index.get() % 8 == 0) table.row();
							table.table(Tex.buttonEdge3, t -> {
								t.button(new TextureRegionDrawable(unit.shadowRegion), Styles.cleari,LEN * 3,() -> {
									BaseDialog d = new BaseDialog("info"){{
										cont.image(unit.shadowRegion);
									}};
									d.addCloseListener();
									d.show();
								}).grow().row();
								t.pane(in -> in.add(unit.localizedName).height(LEN).fillX()).height(LEN).growX();
							});
							index.getAndIncrement();
						}
					});
				}).fill();
			}};
			buttonImage.addCloseListener();
			buttonImage.show();
		}).size(LEN * 3, LEN).pad(OFFSET / 2).disabled(b -> mobile);
		
		cont.button("Unlock", () -> {
			for(UnlockableContent content : content.items()){
				content.unlock();
			}
			for(UnlockableContent content : content.liquids()){
				content.unlock();
			}
			for(UnlockableContent content : content.units()){
				content.unlock();
			}
			for(UnlockableContent content : content.blocks()){
				content.unlock();
			}
			for(UnlockableContent content : content.sectors()){
				content.unlock();
			}
		}).size(LEN * 3, LEN).pad(OFFSET / 2);
		
		cont.button("Settings", () -> {
			new SettingDialog().show();
		}).size(LEN * 3, LEN).pad(OFFSET / 2).disabled(b -> mobile);
		
		cont.button("Weathers", () -> {
			BaseDialog dialog = new BaseDialog("");
			dialog.cont.pane(t -> {
				t.add("Add").row();
				t.image().growX().height(OFFSET / 4).pad(OFFSET / 2).color(Pal.accent).row();
				t.pane(table -> {
					for(Content content : content.getBy(ContentType.weather)){
						Weather c = (Weather)content;
						table.button(c.localizedName, () -> {
							Groups.weather.add(c.create(5f));
						}).growX().fillY().row();
					}
				}).grow().row();
				t.image().growX().height(OFFSET / 4).pad(OFFSET / 2).color(Pal.accent).row();
				t.button("Remove", () -> Groups.weather.clear()).growX().height(LEN);
			}).grow();
			dialog.addCloseButton();
			dialog.show();
		}).size(LEN * 3, LEN).pad(OFFSET / 2);
		
		return this;
	}
}































