package com.themis.orchestration.state;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * State machine for workflow execution
 */
public interface WorkflowStateMachine {
    
    /**
     * Available workflow states
     */
    enum State {
        CREATED,
        RUNNING,
        PAUSED,
        COMPLETED,
        FAILED,
        CANCELLED
    }
    
    /**
     * Available workflow events
     */
    enum Event {
        START,
        PAUSE,
        RESUME,
        COMPLETE,
        FAIL,
        CANCEL,
        RESET
    }
    
    /**
     * State transition result
     */
    class TransitionResult {
        private final State fromState;
        private final State toState;
        private final Event event;
        private final boolean successful;
        private final String message;
        private final LocalDateTime timestamp;
        
        public TransitionResult(State fromState, State toState, Event event, boolean successful, String message) {
            this.fromState = fromState;
            this.toState = toState;
            this.event = event;
            this.successful = successful;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters
        public State getFromState() { return fromState; }
        public State getToState() { return toState; }
        public Event getEvent() { return event; }
        public boolean isSuccessful() { return successful; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    /**
     * Get current state
     */
    State getCurrentState();
    
    /**
     * Get allowed events for current state
     */
    Set<Event> getAllowedEvents();
    
    /**
     * Trigger an event and transition to new state
     */
    TransitionResult trigger(Event event);
    
    /**
     * Trigger an event with context
     */
    TransitionResult trigger(Event event, Map<String, Object> context);
    
    /**
     * Check if event is allowed in current state
     */
    boolean isEventAllowed(Event event);
    
    /**
     * Get target state for an event
     */
    State getTargetState(Event event);
    
    /**
     * Reset state machine to initial state
     */
    void reset();
    
    /**
     * Get state machine history
     */
    java.util.List<TransitionResult> getHistory();
}