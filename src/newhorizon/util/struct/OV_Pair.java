package newhorizon.util.struct;

import arc.func.Cons2;
import arc.func.Func;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.io.TypeIO;
import mindustry.type.Item;
import mindustry.type.ItemStack;

public class OV_Pair<T>{
	public T item;
	public int value;
	
	public OV_Pair(){}
	
	public OV_Pair(T item, int value){
		this.item = item;
		this.value = value;
	}
	
	public ItemStack convert(){
		if(Item.class.isAssignableFrom(item.getClass())){
			return new ItemStack((Item)item, value);
		}else return null;
	}
	
	public static <T> OV_Pair<T>[] with(Object... items){
		OV_Pair<T>[] stacks = new OV_Pair[items.length / 2];
		for(int i = 0; i < items.length; i += 2){
			stacks[i / 2] = new OV_Pair<>((T)items[i], ((Number)items[i + 1]).intValue());
		}
		return stacks;
	}
	
	public static <T> Seq<OV_Pair<T>> seqWith(Object... items){
		Seq<OV_Pair<T>> stacks = new Seq<>(items.length / 2);
		for(int i = 0; i < items.length; i += 2){
			stacks.add(new OV_Pair<>((T)items[i], ((Number)items[i + 1]).intValue()));
		}
		return stacks;
	}
	
	public int getValue(){
		return value;
	}
	
	public OV_Pair<T> setValue(int value){
		this.value = value;
		
		return this;
	}
	
	public OV_Pair(T item){
		this.item = item;
	}
	
	public T getItem(){
		return item;
	}
	
	public void setItem(T item){
		this.item = item;
	}
	
	public boolean has(OV_Pair<T> other){
		return other.value >= value;
	}
	
	public boolean below(OV_Pair<T> other){
		return other.value < value;
	}
	
	public boolean positive(){
		return value > 0;
	}
	
	public boolean nonNegative(){
		return value >= 0;
	}
	
	public OV_Pair<T> sum(OV_Pair<T> other){
		return setValue(value + other.value);
	}
	
	public OV_Pair<T> sub(OV_Pair<T> other){
		return setValue(value - other.value);
	}
	
	@Override
	public boolean equals(Object o){
		if(this == o) return true;
		if(!(o instanceof OV_Pair)) return false;
		
		OV_Pair<?> that = (OV_Pair<?>)o;
		
		if(value != that.value) return false;
		return item.equals(that.item);
	}
	
	@Override
	public int hashCode(){
		int result = item.hashCode();
		result = 31 * result + value;
		return result;
	}
	
	public void write(Writes writes){
		TypeIO.writeObject(writes, item);
		writes.i(value);
	}
	
	public void read(Reads reads){
		item = (T)TypeIO.readObject(reads);
		value = reads.i();
	}
	
	public static <E> OV_Pair<E> readToNew(Reads reads){
		return new OV_Pair<>((E)TypeIO.readObject(reads), reads.i());
	}
	
	public static void writeArr(OV_Pair<?>[] pairs, Writes writes){
		writes.i(pairs.length);
		for(OV_Pair<?> pair : pairs){
			pair.write(writes);
		}
	}
	
	public static <T> void writeArr(Seq<OV_Pair<T>> pairs, Writes writes){
		writes.i(pairs.size);
		for(OV_Pair<T> pair : pairs){
			pair.write(writes);
		}
	}
	
	public static <T> void writeArr(Seq<OV_Pair<T>> pairs, Writes writes, Cons2<Writes, OV_Pair<T>> writer){
		writes.i(pairs.size);
		Log.info("Write ");
		for(OV_Pair<T> pair : pairs){
			writer.get(writes, pair);
		}
	}
	
	public static <T> Seq<OV_Pair<T>> readSeq(Reads reads){
		int length = reads.i();
		Seq<OV_Pair<T>> out = new Seq<>(length);
		for(int i = 0; i < length; i++)out.add(readToNew(reads));
		
		return out;
	}
	
	public static <T> Seq<OV_Pair<T>> readSeq(Reads reads, Func<Reads, OV_Pair<T>> reader){
		int length = reads.i();
		Seq<OV_Pair<T>> out = new Seq<>(length);
		for(int i = 0; i < length; i++)out.add(reader.get(reads));
		
		return out;
	}
	
	public static <E> OV_Pair<E>[] readArr(Reads reads){
		int length = reads.i();
		OV_Pair<E>[] out = new OV_Pair[length];
		for(int i = 0; i < length; i++)out[i] = readToNew(reads);
		
		return out;
	}
	
	@Override
	public String toString(){
		return "[" + item + ": " + value + ']';
	}
}
