package client.application;

import client.scotlandyard.*;

import java.util.concurrent.*;

/**
 * A class to allow communication between Threads.
 */

public class ThreadCommunicator {
    
    private BlockingQueue<Object> eventQueue;
    private BlockingQueue<Object> updateQueue;
    
    /**
     * Constructor for ThreadCommunicator, creates BlockingQueues.
     */
    public ThreadCommunicator() {
        eventQueue = new ArrayBlockingQueue<Object>(1024);
        updateQueue = new ArrayBlockingQueue<Object>(1024);
    }
    
    /**
     * Returns the object from the top of the event queue
     * if there is one available, waits otherwise.
     *
     * @return the object from the top of the event queue
     * if there is one available, waits otherwise.
     */
    public Object takeEvent() {
        Object object = null;
        try {
            object = eventQueue.take();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        return object;
    }
    
    /**
     * Returns the object from the top of the update queue
     * if there is one available, waits otherwise.
     *
     * @return the object from the top of the update queue
     * if there is one available, waits otherwise.
     */
    public Object takeUpdate() {
        Object object = null;
        try {
            object = updateQueue.take();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        return object;
    }

    /**
     * Puts an event id and object onto the event queue
     * in accordance with our protocol.
     *
     * @param id the String id of this event
     * @param object the object for this event
     */
    public void putEvent(String id, Object object) {
        try {
            eventQueue.put(id);
            eventQueue.put(object);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
    
    /**
     * Puts an update id and object onto the update queue
     * in accordance with our protocol.
     *
     * @param id the String id of this update
     * @param object the object for this update
     */
    public void putUpdate(String id, Object object) {
        try {
            updateQueue.put(id);
            updateQueue.put(object);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
    
    /**
     * Clears the event queue
     */
    public void clearEvents() {
        eventQueue.clear();
    }
    
    /**
     * Clears the update queue
     */
    public void clearUpdates() {
        updateQueue.clear();
    }
    
}