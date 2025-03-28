package com.example.application.views;

import com.example.application.component.lottie.LottieComponent;
import com.example.application.component.spinwheel.SpinWheel;
import com.example.application.service.ComponentNameService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.firitin.components.messagelist.MarkdownMessage;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@Menu(order = 0, title = "Component Name Generator View", icon = LineAwesomeIconUrl.SPINNER_SOLID)
@Route("")
@RouteAlias("component-name-generator")
public class ComponentNameGeneratorView extends SplitLayout {

    private static final Logger LOGGER = Logger.getLogger(ComponentNameGeneratorView.class.getName());
    private static final int MAX_SUGGESTIONS = 8;
    private static final int DEFAULT_SUGGESTIONS = 5;

    private final ComponentNameService componentNameService;

    // UI Components
    private final TextArea codeTextArea;
    private final NumberField amountOfSuggestionField;
    private final Button generateButton;
    private final VerticalLayout aiResultList;
    private final Notification successfulNotification;

    // Temporary UI components that change during interaction
    private Button openSpinWheelButton;

    public ComponentNameGeneratorView(ComponentNameService componentNameService) {
        this.componentNameService = componentNameService;

        // Initialize UI components
        this.codeTextArea = createCodeTextArea();
        this.amountOfSuggestionField = createNumberField();
        this.generateButton = createGenerateButton();
        this.aiResultList = createResultList();
        this.successfulNotification = new Notification();

        // Set up layouts
        HorizontalLayout buttonLayout = createButtonLayout();
        VerticalLayout codeLayout = createCodeLayout(buttonLayout);
        Scroller resultScroller = createResultScroller();

        // Add components to split layout
        addToPrimary(codeLayout);
        addToSecondary(resultScroller);

        setOrientation(Orientation.HORIZONTAL);
        setSplitterPosition(50);
        setSizeFull();
    }

    private TextArea createCodeTextArea() {
        var textArea = new TextArea("Paste your code here:");
        textArea.setValueChangeMode(ValueChangeMode.EAGER);
        textArea.setSizeFull();
        textArea.addValueChangeListener(event ->
                generateButton.setEnabled(!event.getValue().isEmpty())
        );
        return textArea;
    }

    private NumberField createNumberField() {
        var field = new NumberField();
        field.setHelperText("Suggestions");
        field.setStepButtonsVisible(true);
        field.setMin(1);
        field.setMax(MAX_SUGGESTIONS);
        field.setValue((double) DEFAULT_SUGGESTIONS);
        field.setWidth(94, Unit.PIXELS);

        field.addValueChangeListener(this::handleNumberFieldValueChange);
        return field;
    }

    private void handleNumberFieldValueChange(NumberField.ValueChangeEvent<Double> event) {
        var isOverLimit = event.getValue() >= MAX_SUGGESTIONS;
        amountOfSuggestionField.setInvalid(isOverLimit);
        generateButton.setEnabled(!isOverLimit);

        if (isOverLimit) {
            showSuggestionLimitNotification();
        } else {
            successfulNotification.close();
        }
    }

    private void showSuggestionLimitNotification() {
        successfulNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        successfulNotification.setText("If you want more suggestions, please make sure there is a suggestion.");
        successfulNotification.setPosition(Notification.Position.MIDDLE);
        successfulNotification.open();
    }

    private Button createGenerateButton() {
        var button = new Button("Generate");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.setEnabled(false);

        button.addClickListener(e -> generateComponentNames());
        return button;
    }

    private void generateComponentNames() {
        var code = codeTextArea.getValue();
        var amountOfNames = amountOfSuggestionField.getValue();

        var componentNames = componentNameService.getComponentNames(code, amountOfNames.intValue());
        LOGGER.info(componentNames);
        var componentNameList = convertComponentNames(componentNames);
        LOGGER.info(componentNameList.toString());

        if (componentNameList != null && !componentNameList.isEmpty()) {
            displayComponentNames(componentNameList);
        }
    }

    private void displayComponentNames(List<String> componentNameList) {
        aiResultList.removeAll();

        MarkdownMessage message = new MarkdownMessage("Name Finding Assistant:");
        message.appendMarkdown("Here are some suggestions for component names: ");
        componentNameList.forEach(componentName -> {
            message.appendMarkdown("\n- ");
            message.appendMarkdown(componentName);
        });

        aiResultList.add(message);

        openSpinWheelButton = new Button("can't make a choice? Click here!",
                event -> openSpinWheel(componentNameList));
        openSpinWheelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);

        aiResultList.add(openSpinWheelButton);
        aiResultList.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, openSpinWheelButton);
    }

    private void openSpinWheel(List<String> componentNameList) {
        var spinWheel = createSpinWheel(componentNameList);
        var spinButton = createSpinButton(spinWheel);

        aiResultList.replace(openSpinWheelButton, spinButton);
        aiResultList.add(spinWheel);
    }

    private SpinWheel createSpinWheel(List<String> componentNameList) {
        SpinWheel spinWheel = new SpinWheel();
        spinWheel.setWidth(100, Unit.PERCENTAGE);
        spinWheel.setHeight(100, Unit.PERCENTAGE);
        spinWheel.setItems(componentNameList);

        spinWheel.addWheelRestListener(event -> {
            showWinnerNotification(componentNameList, event.getIndex());
            triggerConfettiAnimation();
        });

        return spinWheel;
    }

    private void showWinnerNotification(List<String> componentNameList, int index) {
        var winnerNotification = new Notification();
        winnerNotification.addThemeVariants(
                NotificationVariant.LUMO_SUCCESS,
                NotificationVariant.LUMO_CONTRAST
        );
        winnerNotification.setPosition(Notification.Position.MIDDLE);

        var winnerText = new H1("The winner is: " + componentNameList.get(index));
        winnerText.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST);

        winnerNotification.add(winnerText);
        winnerNotification.setDuration(5000);
        winnerNotification.open();
    }

    private void triggerConfettiAnimation() {
        var confettiComponent = new LottieComponent("/confetti.lottie", true, false, null, null);
        UI.getCurrent().add(confettiComponent);
        confettiComponent.makeFullOverlay();
    }

    private Button createSpinButton(SpinWheel spinWheel) {
        var spinButton = new Button("Spin Wheel");
        spinButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        spinButton.addClickListener(e -> spinWheel.randomSpin());
        return spinButton;
    }

    private VerticalLayout createResultList() {
        var layout = new VerticalLayout();
        layout.setSizeFull();
        return layout;
    }

    private Scroller createResultScroller() {
        var scroller = new Scroller(aiResultList);
        scroller.setSizeFull();
        return scroller;
    }

    private HorizontalLayout createButtonLayout() {
        var buttonLayout = new HorizontalLayout(generateButton, amountOfSuggestionField);
        buttonLayout.setFlexGrow(1, generateButton);
        buttonLayout.setWidthFull();
        return buttonLayout;
    }

    private VerticalLayout createCodeLayout(HorizontalLayout buttonLayout) {
        var codeLayout = new VerticalLayout(codeTextArea, buttonLayout);
        codeLayout.setSizeFull();
        return codeLayout;
    }

    private List<String> convertComponentNames(String componentNames) {
        try {
            var objectMapper = new ObjectMapper();

            //sanitize inout string
            var sanitizedString = componentNames
                    .replaceAll("json", "")
                    .replaceAll("```", "");

            // Parse JSON to List<String>
            List<String> componentNameList = objectMapper.readValue(
                    sanitizedString, new TypeReference<>() {});

            // Optional logging for debugging
            logComponentNames(componentNameList);

            return componentNameList;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error parsing JSON: " + e.getMessage(), e);
            return null;
        }
    }

    private void logComponentNames(List<String> componentNameList) {
        if (componentNameList != null && !componentNameList.isEmpty()) {
            LOGGER.info("Component Names:");
            componentNameList.forEach(LOGGER::info);

            LOGGER.info("First component name: " + componentNameList.get(0));
            LOGGER.info("Total number of components: " + componentNameList.size());
        }
    }
}