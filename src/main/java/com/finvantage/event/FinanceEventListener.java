package com.finvantage.event;

/**
 * Observer interface for reacting to domain model and transaction changes in the UI.
 * Demonstrates the GoF Observer Pattern.
 */
@FunctionalInterface
public interface FinanceEventListener {
    void onDataChanged(String eventType, Object payload);
}
