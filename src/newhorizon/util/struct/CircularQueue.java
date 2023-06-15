package newhorizon.util.struct;

public class CircularQueue<T> {
	private final T[] queue;
	private int head;
	private int tail;
	private final int k;
	
	public CircularQueue(int k) {
		this.k = k;
		this.queue = (T[]) new Object[k];
		this.head = -1;
		this.tail = -1;
	}
	
	public boolean isEmpty() {
		return head == -1;
	}
	
	public boolean isFull() {
		return ((tail + 1) % k) == head;
	}
	
	public boolean enqueueFront(T value) {
		if (isFull())
			return false;
		
		if (isEmpty())
			head = tail = 0;
		else
			head = (head - 1 + k) % k;
		
		queue[head] = value;
		return true;
	}
	
	public boolean enqueueRear(T value) {
		if (isFull())
			return false;
		
		if (isEmpty())
			head = 0;
		
		tail = (tail + 1) % k;
		queue[tail] = value;
		return true;
	}
	
	public T dequeueFront() {
		if (isEmpty())
			return null;
		
		T value = queue[head];
		if (head == tail)
			head = tail = -1;
		else
			head = (head + 1) % k;
		
		return value;
	}
	
	public T dequeueRear() {
		if (isEmpty())
			return null;
		
		T value = queue[tail];
		if (head == tail)
			head = tail = -1;
		else
			tail = (tail - 1 + k) % k;
		
		return value;
	}
}
