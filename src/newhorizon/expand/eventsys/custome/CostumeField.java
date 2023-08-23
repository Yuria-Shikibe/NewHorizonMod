package newhorizon.expand.eventsys.custome;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.util.serialization.Json;
import arc.util.serialization.JsonValue;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.vm.annotation.ForceInline;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class CostumeField implements Json.JsonSerializable{
	public final Field field;
	public Object context;
	
	public CostumeField(Field field){
		this.field = field;
	}

	public void buildTable(Table table){
	
	}
	
	public String desc(){
		return Core.bundle.getOrNull(field.getName());
	}
	
	public String name(){
		return field.getName();
	}
	
	public Class<?> getType(){
		return field.getType();
	}
	
	public boolean hasDesc(){
		return desc() != null;
	}
	
	public void apply(Object tgt, Object val) throws IllegalAccessException{
		field.set(tgt, val);
	}
	
	public boolean isPrimitive(){
		return getType().isPrimitive();
	}
	
	public boolean isNumber(){
		Class<?> T = getType();
		return (isPrimitive() && (!T.equals(boolean.class) && !T.equals(char.class))) || Number.class.isAssignableFrom(T);
	}
	
	@ForceInline
	@CallerSensitive
	public Object get(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return field.get(obj);
	}
	
	@ForceInline
	@CallerSensitive
	public boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return field.getBoolean(obj);
	}
	
	@ForceInline
	@CallerSensitive
	public byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return field.getByte(obj);
	}
	
	@ForceInline
	@CallerSensitive
	public char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return field.getChar(obj);
	}
	
	@ForceInline
	@CallerSensitive
	public short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return field.getShort(obj);
	}
	
	@ForceInline
	@CallerSensitive
	public int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return field.getInt(obj);
	}
	
	@ForceInline
	@CallerSensitive
	public long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return field.getLong(obj);
	}
	
	@ForceInline
	@CallerSensitive
	public float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return field.getFloat(obj);
	}
	
	@ForceInline
	@CallerSensitive
	public double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return field.getDouble(obj);
	}
	
	@ForceInline
	@CallerSensitive
	public void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException{
		field.set(obj, value);
	}
	
	@ForceInline
	@CallerSensitive
	public void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException{
		field.setBoolean(obj, z);
	}
	
	@ForceInline
	@CallerSensitive
	public void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException{
		field.setByte(obj, b);
	}
	
	@ForceInline
	@CallerSensitive
	public void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException{
		field.setChar(obj, c);
	}
	
	@ForceInline
	@CallerSensitive
	public void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException{
		field.setShort(obj, s);
	}
	
	@ForceInline
	@CallerSensitive
	public void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException{
		field.setInt(obj, i);
	}
	
	@ForceInline
	@CallerSensitive
	public void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException{
		field.setLong(obj, l);
	}
	
	@ForceInline
	@CallerSensitive
	public void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException{
		field.setFloat(obj, f);
	}
	
	@ForceInline
	@CallerSensitive
	public void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException{
		field.setDouble(obj, d);
	}
	
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass){
		return field.getAnnotation(annotationClass);
	}
	
	public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass){
		return field.getAnnotationsByType(annotationClass);
	}
	
	@Override
	public void write(Json json){
		json.writeArrayStart();
	}
	
	@Override
	public void read(Json json, JsonValue jsonData){
	
	}
}
