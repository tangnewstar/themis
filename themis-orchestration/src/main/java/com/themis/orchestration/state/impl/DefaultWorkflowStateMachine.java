package com.themis.orchestration.state.impl;

import com.themis.orchestration.state.WorkflowStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of WorkflowStateMachine
 */
public class DefaultWorkflowStateMachine implements WorkflowStateMachine {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultWorkflowStateMachine.class);
    
    private volatile State currentState;
    private final Map<State, Set<Event>> stateTransitions;
    private final Map<StateEventPair, State> targetStates;
    private final List<TransitionResult> history;
    
    public DefaultWorkflowStateMachine() {
        this.currentState = State.CREATED;
        this.stateTransitions = initializeStateTransitions();
        this.targetStates = initializeTargetStates();
        this.history = Collections.synchronizedList(new ArrayList<>());
    }
    
    private Map<State, Set<Event>> initializeStateTransitions() {
        Map<State, Set<Event>> transitions = new ConcurrentHashMap<>();
        
        transitions.put(State.CREATED, EnumSet.of(Event.START, Event.CANCEL));
        transitions.put(State.RUNNING, EnumSet.of(Event.PAUSE, Event.COMPLETE, Event.FAIL, Event.CANCEL));
        transitions.put(State.PAUSED, EnumSet.of(Event.RESUME, Event.CANCEL));
        transitions.put(State.COMPLETED, EnumSet.of(Event.RESET));
        transitions.put(State.FAILED, EnumSet.of(Event.RESET, Event.CANCEL));
        transitions.put(State.CANCELLED, EnumSet.of(Event.RESET));
        
        return transitions;
    }
    
    private Map<StateEventPair, State> initializeTargetStates() {
        Map<StateEventPair, State> targets = new ConcurrentHashMap<>();
        
        // From CREATED
        targets.put(new StateEventPair(State.CREATED, Event.START), State.RUNNING);
        targets.put(new StateEventPair(State.CREATED, Event.CANCEL), State.CANCELLED);
        
        // From RUNNING
        targets.put(new StateEventPair(State.RUNNING, Event.PAUSE), State.PAUSED);
        targets.put(new StateEventPair(State.RUNNING, Event.COMPLETE), State.COMPLETED);
        targets.put(new StateEventPair(State.RUNNING, Event.FAIL), State.FAILED);
        targets.put(new StateEventPair(State.RUNNING, Event.CANCEL), State.CANCELLED);
        
        // From PAUSED
        targets.put(new StateEventPair(State.PAUSED, Event.RESUME), State.RUNNING);
        targets.put(new StateEventPair(State.PAUSED, Event.CANCEL), State.CANCELLED);
        
        // From terminal states
        targets.put(new StateEventPair(State.COMPLETED, Event.RESET), State.CREATED);
        targets.put(new StateEventPair(State.FAILED, Event.RESET), State.CREATED);
        targets.put(new StateEventPair(State.FAILED, Event.CANCEL), State.CANCELLED);
        targets.put(new StateEventPair(State.CANCELLED, Event.RESET), State.CREATED);
        
        return targets;
    }
    
    @Override
    public State getCurrentState() {
        return currentState;
    }
    
    @Override
    public Set<Event> getAllowedEvents() {
        return stateTransitions.getOrDefault(currentState, Collections.emptySet());
    }
    
    @Override
    public TransitionResult trigger(Event event) {
        return trigger(event, Collections.emptyMap());
    }
    
    @Override
    public synchronized TransitionResult trigger(Event event, Map<String, Object> context) {
        State fromState = currentState;
        
        if (!isEventAllowed(event)) {
            String message = String.format("Event %s not allowed in state %s", event, currentState);
            logger.warn(message);
            TransitionResult result = new TransitionResult(fromState, fromState, event, false, message);
            history.add(result);
            return result;
        }
        
        State targetState = getTargetState(event);
        if (targetState == null) {
            String message = String.format("No target state defined for event %s in state %s", event, currentState);
            logger.error(message);
            TransitionResult result = new TransitionResult(fromState, fromState, event, false, message);
            history.add(result);
            return result;
        }
        
        // Perform pre-transition validation
        if (!validateTransition(fromState, targetState, event, context)) {
            String message = String.format("Transition validation failed for %s -> %s on event %s", 
                    fromState, targetState, event);
            logger.warn(message);
            TransitionResult result = new TransitionResult(fromState, fromState, event, false, message);
            history.add(result);
            return result;
        }
        
        // Execute transition
        try {
            executeTransition(fromState, targetState, event, context);
            currentState = targetState;
            
            String message = String.format("Successfully transitioned from %s to %s on event %s", 
                    fromState, targetState, event);
            logger.info(message);
            
            TransitionResult result = new TransitionResult(fromState, targetState, event, true, message);
            history.add(result);
            return result;
            
        } catch (Exception e) {
            String message = String.format("Transition execution failed: %s", e.getMessage());
            logger.error(message, e);
            TransitionResult result = new TransitionResult(fromState, fromState, event, false, message);
            history.add(result);
            return result;
        }
    }
    
    @Override
    public boolean isEventAllowed(Event event) {
        Set<Event> allowedEvents = getAllowedEvents();
        return allowedEvents.contains(event);
    }
    
    @Override
    public State getTargetState(Event event) {
        return targetStates.get(new StateEventPair(currentState, event));
    }
    
    @Override
    public void reset() {
        logger.info("Resetting state machine from state: {}", currentState);
        State fromState = currentState;
        currentState = State.CREATED;
        
        TransitionResult result = new TransitionResult(fromState, State.CREATED, Event.RESET, true, 
                "State machine reset to initial state");
        history.add(result);
    }
    
    @Override
    public List<TransitionResult> getHistory() {
        return new ArrayList<>(history);
    }
    
    /**
     * Validate if a transition is allowed based on business rules
     */
    protected boolean validateTransition(State fromState, State toState, Event event, Map<String, Object> context) {
        // Default implementation - can be overridden for custom validation
        return true;
    }
    
    /**
     * Execute transition logic (e.g., side effects, notifications)
     */
    protected void executeTransition(State fromState, State toState, Event event, Map<String, Object> context) {
        // Default implementation - can be overridden for custom transition logic
        logger.debug("Executing transition from {} to {} on event {}", fromState, toState, event);
    }
    
    /**
     * Helper class for state-event pair as map key
     */
    private static class StateEventPair {
        private final State state;
        private final Event event;
        
        public StateEventPair(State state, Event event) {
            this.state = state;
            this.event = event;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StateEventPair that = (StateEventPair) o;
            return state == that.state && event == that.event;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(state, event);
        }
        
        @Override
        public String toString() {
            return String.format("StateEventPair{state=%s, event=%s}", state, event);
        }
    }
}