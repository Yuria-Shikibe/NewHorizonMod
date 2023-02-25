package newhorizon.expand.eventsys.custom;

import arc.func.Prov;
import arc.math.geom.Point2;
import arc.scene.event.Touchable;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.TextArea;
import arc.scene.ui.layout.Table;
import arc.struct.LongMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.gen.Tex;
import newhorizon.expand.eventsys.WorldEventType;
import newhorizon.expand.eventsys.annotation.Parserable;
import newhorizon.expand.eventsys.annotation.Pos;

import java.util.Arrays;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class ParserRegistry{
	public static final LongMap<Interpreter> interpreters = new LongMap<>();
	public static ObjectMap<Class<?>, Interpreter> primitiveIpt = new ObjectMap<>();
	
	public static final Seq<Runnable> applyOnExit = new Seq<>();
	
	public static WorldEventType getContext(){
		return Customizer.customizer.getContext();
	}
	
	public static void exitApply(Runnable runnable){
		applyOnExit.add(runnable);
	}
	
	static{
		//Pos Customizer
//		register(hash(ObjectMap.class, BulletType.class, ShootPattern.class), (c, table) -> {
//			table.table(Tex.pane, t -> {
//				TextArea textAreaX = new TextArea("0");
//				TextArea textAreaY = new TextArea("0");
//
//				t.table(x -> {
//					t.add("X:").padRight(OFFSET).fill();
//					t.add(textAreaX).grow();
//				}).growX().fillY().row();
//
//				t.table(x -> {
//					t.add("Y:").padRight(OFFSET).fill();
//					t.add(textAreaY).grow();
//				}).growX().fillY().row();
//
//
//			}).growX().fill().row();
//		});
		
		//Pos Customizer
		register(hash(Integer.class, Point2.class), (c, table) -> {
			table.table(Tex.pane, t -> {
				TextArea textAreaX = new TextArea("0");
				TextArea textAreaY = new TextArea("0");
				
				textAreaX.setPrefRows(1);
				textAreaY.setPrefRows(1);
				
				t.table(x -> {
					t.add("X:").padRight(OFFSET).fill();
					t.add(textAreaX).grow();
				}).growX().fillY().row();
				
				t.table(x -> {
					t.add("Y:").padRight(OFFSET).fill();
					t.add(textAreaY).grow();
				}).growX().fillY().row();
				
				
			}).growX().fill().row();
		});
		
		register(hash(String.class, Void.class), (c, table) -> {
			TextArea textArea = new TextArea("0");
			textArea.setPrefRows(1);
			
			Runnable setter = () -> c.set(textArea.getText(), getContext());
			exitApply(setter);
			
			table.table(Tex.pane, t -> {
				t.add(c.displayName).row();
				t.image().growX().height(4).pad(4f, 4f, OFFSET, LEN).row();
				t.add(textArea).growX().height(LEN);
				textArea.changed(setter);
			}).growX().fill().row();
		});
		
		primitiveIpt.put(int.class, (c, table) -> {
			Slider s = new Slider(c.numberParam.min(), c.numberParam.max(), c.numberParam.stepSize(), false);
			
			Runnable setter = () -> c.set((int)s.getValue(), getContext());
			exitApply(setter);
			
			s.released(setter);
			s.setValue(c.getNumber(getContext()).floatValue());
			
			table.table(Tex.pane,  t -> {
				t.left();
				t.defaults().growX().fillY().left();
				t.add(c.displayName).row();
				
				Label l = new Label(() -> c.numberParam.display().toDisplay.get(s.getValue()));
				l.touchable = Touchable.disabled;
				
				t.stack(s, l);
			}).growX().fill().row();
		});
		
		primitiveIpt.put(float.class, (c, table) -> {
			Slider s = new Slider(c.numberParam.min(), c.numberParam.max(), c.numberParam.stepSize(), false);
			
			Runnable setter = () -> c.set(s.getValue(), getContext());
			exitApply(setter);
			
			s.released(setter);
			s.setValue(c.getNumber(getContext()).floatValue());
			
			table.table(Tex.pane,  t -> {
				t.left();
				t.defaults().growX().fillY().left();
				t.add(c.displayName).row();
				
				Label l = new Label(() -> c.numberParam.display().toDisplay.get(s.getValue()));
				l.touchable = Touchable.disabled;
				
				t.stack(s, l);
			}).growX().fillY().row();
		});
		
		primitiveIpt.put(boolean.class, (c, table) -> {
			CheckBox box = new CheckBox(c.displayName);
			Runnable setter = () -> c.set(box.isChecked(), getContext());
			exitApply(setter);
			
			table.table(Tex.pane,  t -> {
				t.left();
				t.defaults().growX().fillY().left();
				
				box.left();
				box.update(() -> box.setChecked(c.getBool(getContext())));
				box.changed(setter);
				
				t.add(box).left();
			}).growX().fillY().row();
		});
	}
	
	public static Interpreter getInterpreter(Customizer.CustomParam param){
		Prov<Interpreter> nullI = () -> ((customParam, table) -> {
			table.add("Unregistered Type: " + customParam.getType().getSimpleName()).fill().row();
		});
		
		if(param.getType().isPrimitive() && !param.field.isAnnotationPresent(Pos.class)){
			return primitiveIpt.get(param.getType(), nullI);
		}else if(param.needParser()){
			Parserable p = param.field.getAnnotation(Parserable.class);
			long hash = hash(p.value(), p.params());
			return interpreters.get(hash, nullI.get());
		}else return nullI.get();
	}
	
	public static void register(Class<?> main, Class<?>[] params, Interpreter interpreter){
		long hash = hash(main, params);
		interpreters.put(hash, interpreter);
	}
	
	public static void register(long hash, Interpreter interpreter){
		interpreters.put(hash, interpreter);
	}
	
	public static long hash(Class<?> main, Class<?>... arr){
		return (long)Arrays.hashCode(arr) + main.hashCode() << 8;
	}
	//	public static final ObjectMap<Class?>
	
	public interface Interpreter{
		void build(Customizer.CustomParam customParam, Table table);
	}
}
