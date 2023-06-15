package newhorizon.util.struct;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FloatCircularQueue implements Iterable<Float>{
	private final float[] queue;
	private int head;
	private int tail;
	private int size;
	private final int capacity;
	
	public FloatCircularQueue(int k) {
		this.queue = new float[k];
		this.head = -1;
		this.tail = -1;
		this.size = 0;
		this.capacity = k;
	}
	
	public int size(){
		return size;
	}
	
	public void each(QueueIterator itr){
		int s = head;
		for(int i = 0; i < size; i++){
			itr.get(i, queue[s]);
			s = (s + 1) % capacity;
		}
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public boolean isFull() {
		return size == capacity;
	}
	
	//Return the tail, if no-full, return def;
	public float push(float v, float def){
		float value = def;
		
		if(isEmpty()){
			head = tail = 0;
		}else{
			if(isFull()){
				value = queue[tail];
				tail = (tail - 1 + capacity) % capacity;
				size--;
			}
			head = (head - 1 + capacity) % capacity;
		}
		queue[head] = v;
		size++;
		
		return value;
	}
	
	public boolean enqueueFront(float value) {
		if (isFull())
			return false;
		
		if (isEmpty())
			head = tail = 0;
		else
			head = (head - 1 + capacity) % capacity;
		
		queue[head] = value;
		size++;
		return true;
	}
	
	public boolean enqueueRear(float value) {
		if (isFull())
			return false;
		
		if (isEmpty())
			head = 0;
		
		tail = (tail + 1) % capacity;
		queue[tail] = value;
		size++;
		return true;
	}
	
	public float dequeueFront() {
		if (isEmpty())
			return -1;
		
		float value = queue[head];
		if (head == tail)
			head = tail = -1;
		else
			head = (head + 1) % capacity;
		
		size--;
		return value;
	}
	
	public float dequeueRear() {
		if (isEmpty())
			return -1;
		
		float value = queue[tail];
		if (head == tail)
			head = tail = -1;
		else
			tail = (tail - 1 + capacity) % capacity;
		
		size--;
		return value;
	}
	
	public float front(){
		return queue[head];
	}
	
	public float rear(){
		return queue[tail];
	}
	
	public interface QueueIterator{
		void get(int i, float f);
	}
	
	public Iterator<Float> iterator() {
		return new CircularQueueIterator();
	}
	
	private class CircularQueueIterator implements Iterator<Float>{
		private int current;
		private int count;
		
		public CircularQueueIterator() {
			current = head;
			count = 0;
		}
		
		@Override
		public boolean hasNext() {
			return count < size;
		}
		
		@Override
		public Float next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			
			float element = queue[current];
			current = (current + 1) % capacity;
			count++;
			return element;
		}
	}
}