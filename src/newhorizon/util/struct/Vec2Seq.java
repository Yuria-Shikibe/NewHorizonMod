package newhorizon.util.struct;

import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Floatc2;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import arc.util.Eachable;
import newhorizon.util.annotation.CacheBanned;

import java.util.Iterator;


/**
 * Use floats to restore the coords of a 2D vector to improve the performance. <p>
 * Used Mainly for {@link mindustry.entities.Effect} that need a cached sequence of vec2s to avoid constructing too much {@link Vec2} instances.
 *
 * Similar implemented in {@link mindustry.graphics.Trail#points};
 * @see FloatSeq
 * @see Vec2
 * */
public class Vec2Seq implements Iterable<Vec2>, Eachable<Vec2>{
	private final FloatSeq coordinates;
	
	private final Vec2 tmp = new Vec2();
	
	public Vec2Seq(){
		coordinates = new FloatSeq(true, 8);
	}
	
	public Vec2Seq(int length){
		coordinates = new FloatSeq(true, length * 2);
	}
	
	public Vec2Seq(Seq<Vec2> vec2s){
		this(vec2s.size / 2);
		addAll(vec2s);
	}
	
	public Vec2Seq(FloatSeq vec2s){
		coordinates = FloatSeq.with(vec2s.items.clone());
		if(coordinates.size % 2 == 1) coordinates.pop();
	}
	
	public Vec2Seq(float[] vec2s){
		this(vec2s.length / 2);
		coordinates.items = vec2s;
		coordinates.size = vec2s.length;
	}
	
	public int size(){
		return coordinates.size / 2;
	}
	
	public FloatSeq getCoordinates(){
		return coordinates;
	}
	
	public FloatSeq coordinates(){
		return coordinates;
	}
	
	public boolean any(){
		return size() > 0;
	}
	
	public void add(float x, float y){
		coordinates.add(x, y);
	}
	
	public void add(Vec2 vec2){
		add(vec2.x, vec2.y);
	}
	
	public void add(Position vec2){
		add(vec2.getX(), vec2.getY());
	}
	
	public void addAll(FloatSeq arr){
		for(int i = 0; i < arr.size / 2; i++){
			add(arr.get(i), arr.get(i + 1));
		}
	}
	
	public void addAll(float[] arr){
		for(int i = 0; i < arr.length / 2; i++){
			add(arr[i], arr[i + 1]);
		}
	}
	
	public void addAll(Iterable<Vec2> vecs){
		for(Vec2 v : vecs){
			add(v);
		}
	}
	
	
	//TODO should be faster.
	public void addAll(Vec2[] vec2s){
		for(Vec2 vec2 : vec2s){
			add(vec2);
		}
	}
	
	public Vec2Seq addAll(Vec2Seq vec2s){
		vec2s.each(((Floatc2)this::add));
		return this;
	}
	
	public Vec2Seq copy(){
		return new Vec2Seq(coordinates.items.clone());
	}
	
	public int count(Boolf<Vec2> bf){
		int i = 0;
		Vec2 vec2 = new Vec2();
		for(int j = 0; j < size(); j++){
			if(bf.get(setVec2(j, vec2)))i++;
		}
		
		return i;
	}
	
	public boolean contains(Boolf<Vec2> bf){
		Vec2 vec2 = new Vec2();
		for(int j = 0; j < size(); j++){
			setVec2(j, vec2);
			if(bf.get(vec2))return true;
		}
		
		return false;
	}
	
	public Seq<Vec2> asSeq(){
		Seq<Vec2> seq = new Seq<>(true, size());
		for(int j = 0; j < size(); j++){
			seq.add(newVec2(j));
		}
		return seq;
	}
	
	public Vec2 currentTmp(){
		return tmp;
	}
	
	@CacheBanned
	public Vec2 tmpVec2(int index){
		return tmp.set(coordinates.items[index * 2], coordinates.items[index * 2 + 1]);
	}
	
	public Vec2 setVec2(int index, Vec2 vec2){
		return vec2.set(coordinates.items[index * 2], coordinates.items[index * 2 + 1]);
	}
	
	public Vec2 newVec2(int index){
		return new Vec2(coordinates.items[index * 2], coordinates.items[index * 2 + 1]);
	}
	
	public void get(int index, Floatc2 operator){
		operator.get(coordinates.items[index * 2], coordinates.items[index * 2 + 1]);
	}
	
	public void remove(int index){
		coordinates.removeRange(index * 2, index * 2 + 1);
	}
	
	public void removeRange(int start, int end){
		coordinates.removeRange(start * 2, end * 2 + 1);
	}
	
	public void removeFirst(){
		remove(0);
	}
	
	public void removeLast(){
		remove(size() - 1);
	}
	
	public Vec2 firstTmp(){
		return tmpVec2(0);
	}
	
	public Vec2 peekTmp(){
		return tmpVec2(size() - 1);
	}
	
	public Vec2 popTmp(){
		peekTmp();
		
		removeLast();
		
		return tmp;
	}
	
	public void each(Floatc2 f){
		for(int i = 0; i < size(); i++){
			f.get(coordinates.items[i * 2], coordinates.items[i * 2 + 1]);
		}
	}
	
	public boolean check(){
		return coordinates.size % 2 == 0;
	}
	
	@Override
	public void each(Cons<? super Vec2> cons){
		for(int i = 0; i < size(); i++){
			cons.get(tmpVec2(i));
		}
	}
	
	
	
	/**
	 * Low Performance
	 *
	 * Returns an iterator over elements of type {@code T}.
	 *
	 * @return an Iterator.
	 */
	@Override
	public Iterator<Vec2> iterator(){
		return asSeq().iterator();
	}
}
