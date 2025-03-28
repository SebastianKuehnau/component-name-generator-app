package com.example.application.component.spinwheel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

import java.util.List;

@NpmPackage(value = "spin-wheel", version = "5.0.2")
@JsModule("./component/spin-wheel/spin-wheel-connector.js")
@Tag("spin-wheel")
public class SpinWheel extends Component implements HasSize {
    final static ObjectMapper objectMapper = new ObjectMapper();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Props(@JsonProperty("items") List<Item> items) {
        public record Item(@JsonProperty("label") String label) {

        }

        @Override
        public String toString() {
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setItems(String... items) {
        //convert items array to list of items
        setItems(List.of(items));
    }

    public void setItems(List<String> items) {
        var props = new Props(items.stream().map(Props.Item::new).toList());
        getElement().removeAllChildren();
        getElement().executeJs("""
            window.wheel(this, %s);
        """.formatted(props));
    }

    public void randomSpin() {
        getElement().executeJs("""
            window.randomSpin();
        """);
    }

    public Registration addWheelRestListener(ComponentEventListener<WheelRestEvent> listener) {
        return addListener(WheelRestEvent.class, listener);
    }
}
