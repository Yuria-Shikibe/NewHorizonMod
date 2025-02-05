package newhorizon.util.struct;

import arc.struct.Queue;
import arc.util.Log;
import mindustry.graphics.Layer;

/** A series of elements that run in sequence. */
public class TimeQueue<T extends TimeQueue.Timed>{
	public Queue<T> queue = new Queue<>();
	public T current;
	
	public TimeQueue(){}
	
	public void add(T item){
		queue.addFirst(item);
	}
	
	@SuppressWarnings("unchecked")
	public void addAll(T... items){
		for(T item : items){
			queue.addFirst(item);
		}
	}
	
	public void clear(){
		queue.clear();
		current = null;
	}
	
	public void update(){
		if (current == null && !queue.isEmpty()){
			current = queue.removeLast();
			current.begin();
		}

		if(current != null && current.complete()){
			current.end();
			current = null;
		}

		if (current != null && !current.complete()){
			current.update();
		}
	}
	
	public boolean complete(){
		return current == null && queue.isEmpty();
	}
	
	public interface Timed{
		void begin();
		void update();
		void end();
		boolean complete();
	}
}
