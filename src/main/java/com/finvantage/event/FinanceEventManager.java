package com.finvantage.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event Broker / Subject implementing the GoF Observer Pattern.
 * Broadcasts data modification events to all active Swing panels for real-time synchronization.
 */
public class FinanceEventManager {

    private static final FinanceEventManager INSTANCE = new FinanceEventManager();
    private final List<FinanceEventListener> listeners = new CopyOnWriteArrayList<>();

    private FinanceEventManager() {}

    public static FinanceEventManager getInstance() {
        return INSTANCE;
    }

    public void addListener(FinanceEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(FinanceEventListener listener) {
        listeners.remove(listener);
    }

    public void notifyChange(String eventType, Object payload) {
        for (FinanceEventListener listener : listeners) {
            try {
                listener.onDataChanged(eventType, payload);
            } catch (Exception e) {
                System.err.println("[EventManager] Error dispatching event: " + eventType + " - " + e.getMessage());
            }
        }
    }
}
