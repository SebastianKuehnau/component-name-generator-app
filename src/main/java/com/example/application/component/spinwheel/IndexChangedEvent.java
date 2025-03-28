package com.example.application.component.spinwheel;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

@DomEvent("index-changed")
public class IndexChangedEvent extends ComponentEvent<SpinWheel> {

    private final int index;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     *                   side, <code>false</code> otherwise
     */
    public IndexChangedEvent(SpinWheel source, boolean fromClient, @EventData("event.detail.index") int index) {
        super(source, fromClient);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
