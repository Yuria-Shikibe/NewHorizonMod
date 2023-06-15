package newhorizon.util.struct;

public class IntCircularQueue{
	public final int[] queue;
	public int head;
	public int tail;
	public final int k;
	
	public IntCircularQueue(int k) {
		this.k = k;
		this.queue = new int[k];
		this.head = -1;
		this.tail = -1;
	}
	
	public boolean isEmpty() {
		return head == -1;
	}
	
	public boolean isFull() {
		return ((tail + 1) % k) == head;
	}
	
	public boolean enqueueFront(int value) {
		if (isFull())
			return false;
		
		if (isEmpty())
			head = tail = 0;
		else
			head = (head - 1 + k) % k;
		
		queue[head] = value;
		return true;
	}
	
	public boolean enqueueRear(int value) {
		if (isFull())
			return false;
		
		if (isEmpty())
			head = 0;
		
		tail = (tail + 1) % k;
		queue[tail] = value;
		return true;
	}
	
	public int dequeueFront() {
		if (isEmpty())
			return -1;
		
		int value = queue[head];
		if (head == tail)
			head = tail = -1;
		else
			head = (head + 1) % k;
		
		return value;
	}
	
	public int dequeueRear() {
		if (isEmpty())
			return -1;
		
		int value = queue[tail];
		if (head == tail)
			head = tail = -1;
		else
			tail = (tail - 1 + k) % k;
		
		return value;
	}
}
