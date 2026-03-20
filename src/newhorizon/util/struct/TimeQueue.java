package newhorizon.util.struct;

import arc.struct.Queue;
import arc.util.Log;
import newhorizon.expand.logic.components.Action;

/**
 * A queue of timed elements that run sequentially.
 * Supports pause/resume, insertion, and state query.
 */
public class TimeQueue<T extends TimeQueue.Timed> {
    /** Pending items queue */
    public Queue<T> queue = new Queue<>();
    /** Currently executing item */
    public T current;

    public TimeQueue() {
    }

    /**
     * Add an item to the end of the queue.
     */
    public void add(T item) {
        if (item != null) {
            queue.addFirst(item);
        }
    }

    /**
     * Add multiple items to the queue.
     */
    @SuppressWarnings("unchecked")
    public void addAll(T... items) {
        for (T item : items) {
            if (item != null) {
                queue.addFirst(item);
            }
        }
    }

    /**
     * Insert an item at the front of the queue (will execute next).
     */
    public void insertNext(T item) {
        if (item != null) {
            queue.addLast(item);
        }
    }

    /**
     * Clear all pending items and stop current execution.
     */
    public void clear() {
        if (current != null) {
            current.end();
            current = null;
        }
        queue.clear();
    }

    /**
     * Update the queue. Should be called every frame.
     */
    public void update() {
        // Start next item if current is null
        if (current == null && !queue.isEmpty()) {
            Log.info("asdasd");
            current = queue.removeLast();
            current.begin();
        }

        // Check if current item is complete
        if (current != null && current.complete()) {
            current.end();
            current = null;
        }

        // Update current item
        if (current != null && !current.complete()) {
            current.update();
        }
    }

    /**
     * Skip the current item and move to next.
     */
    public void skipCurrent() {
        if (current != null) {
            current.skip();
            current.end();
            current = null;
        }
    }

    /**
     * Check if the queue has completed all items.
     */
    public boolean complete() {
        return current == null && queue.isEmpty();
    }

    /**
     * Check if the queue is currently executing an item.
     */
    public boolean isRunning() {
        return current != null;
    }

    /**
     * Get the number of pending items.
     */
    public int size() {
        return queue.size;
    }

    /**
     * Check if the queue is empty.
     */
    public boolean isEmpty() {
        return queue.isEmpty() && current == null;
    }

    /**
     * Unified interface for timed elements that can be executed sequentially.
     * Supports pause/resume and skip functionality.
     */
    public interface Timed {
        /** Called when the element starts execution */
        void begin();

        /** Called every frame while executing */
        void update();

        /** Called when the element finishes execution */
        void end();

        /** Check if the element has completed */
        boolean complete();

        /** Skip the element execution */
        default void skip() {
        }
    }
}
