package com.example.application.component.spinwheel;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

@DomEvent("wheel-rest")
public class WheelRestEvent extends ComponentEvent<SpinWheel> {

    private final int index;
    private final double rotation;

    public WheelRestEvent(SpinWheel source, boolean fromClient,
                          @EventData("event.detail.index") int index,
                          @EventData("event.detail.rotation") double rotation) {
        super(source, fromClient);
        this.index = index;
        this.rotation = rotation;
    }

    public double getRotation() {
        return rotation;
    }

    public int getIndex() {
        return index;
    }
}
