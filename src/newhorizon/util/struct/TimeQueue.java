package newhorizon.util.struct;

import arc.func.Cons;
import arc.struct.Queue;

public class TimeQueue<T extends TimeQueue.Timed>{
	public Queue<T> queue = new Queue<>();
	public T current;
	
	public TimeQueue(){
	}
	
	@SafeVarargs
	public TimeQueue(T... items){
		addAll(items);
	}
	
	public void add(T item){
		queue.addFirst(item);
	}
	
	public boolean update(Cons<T> init){
		if(current == null){
			if(queue.isEmpty())return false;
			current = queue.removeLast();
			init.get(current);
		}
		
		current.update();
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public TimeQueue<T> addAll(T... items){
		queue = new Queue<>(items.length);
		
		for(T item : items){
			queue.addFirst(item);
		}
		
		current = queue.removeLast();
		
		return this;
	}
	
	public void clear(){
		current = null;
		queue.clear();
	}
	
	public boolean update(){
		if(current == null){
			if(queue.isEmpty())return false;
			current = queue.removeLast();
		}
		
		current.update();
		
		return true;
	}
	
	public float getDuration(){
		float sum = 0;
		for(T action : queue){
			sum += action.getDuration();
		}
		
		return sum;
	}
	
	public boolean valid(){
		return current != null;
	}
	
	public boolean complete(){
		return current == null && queue.isEmpty();
	}
	
	@Override
	public String toString(){
		return "TimeQueue{" + "queue=" + queue + '}';
	}
	
	public interface Timed{
		void update();
		float getDuration();
		void next();
	}
}
