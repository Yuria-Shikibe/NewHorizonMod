package newhorizon.func;

import arc.Core;
import arc.scene.style.Drawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextButton;
import arc.util.Log;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import java.lang.reflect.Field;

import static newhorizon.func.TableFuncs.LEN;
import static newhorizon.func.TableFuncs.OFFSET;

public class TableTexDebugDialog extends BaseDialog{
	private BaseDialog
		buttonImage,
		buttonText,
		iconDialog,
		tableDialog;
	
	static{
		TableFuncs.debugDialogs.add(new TableTexDebugDialog("default"));
	}
	
	public TableTexDebugDialog(String title){
		this(title, Core.scene.getStyle(Dialog.DialogStyle.class));
	}
	
	public TableTexDebugDialog(String title, Dialog.DialogStyle style) {
		super(title, style);
	}
	
	public BaseDialog init(){
		clear();
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
							if(f.getGenericType().toString().equals("class arc.scene.style.TextureRegionDrawable")){
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
		}).size(LEN * 3, LEN).pad(OFFSET / 2);
			
		cont.button("TableTexes", () -> {
			tableDialog = new BaseDialog("ICONS"){{
				Object obj = new Tex();
				Class<?> c = obj.getClass();
				
				Field[] fields = c.getFields();
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(f.getGenericType().toString().equals("class arc.scene.style.TextureRegionDrawable") || f.getGenericType().toString().equals("class arc.scene.style.NinePatchDrawable")){
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
		}).size(LEN * 3, LEN).pad(OFFSET / 2);
		
		cont.button("ButtonTexts", () -> {
			buttonText = new BaseDialog("ButtonTexts"){{
				Object obj = new Styles();
				Class<?> c = obj.getClass();
				
				Field[] fields = c.getFields();
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(f.getGenericType().toString().equals("class arc.scene.ui.TextButton$TextButtonStyle")){
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
		}).size(LEN * 3, LEN).pad(OFFSET / 2);
		
		cont.button("ButtonImages", () -> {
			buttonImage = new BaseDialog("ButtonImages"){{
				Object obj = new Styles();
				Class<?> c = obj.getClass();
				
				Field[] fields = c.getFields();
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(f.getGenericType().toString().equals("class arc.scene.ui.ImageButton$ImageButtonStyle")){
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
		}).size(LEN * 3, LEN).pad(OFFSET / 2);
		
		return this;
	}
}
