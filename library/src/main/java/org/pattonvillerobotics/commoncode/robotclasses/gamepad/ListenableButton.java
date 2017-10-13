package org.pattonvillerobotics.commoncode.robotclasses.gamepad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by skaggsm on 10/6/16.
 */
public class ListenableButton {

    private final Map<ButtonState, List<ButtonListener>> listeners;
    private ButtonState currentButtonState;

    public ListenableButton() {
        currentButtonState = ButtonState.BEING_RELEASED;
        listeners = new HashMap<>();
        for (ButtonState buttonState : ButtonState.values())
            listeners.put(buttonState, new ArrayList<ButtonListener>());
    }

    public void update(boolean updateValue) {
        switch (currentButtonState) {
            case BEING_PRESSED:
                if (updateValue) {
                    currentButtonState = ButtonState.BEING_PRESSED;
                } else {
                    currentButtonState = ButtonState.JUST_RELEASED;
                }
                break;
            case BEING_RELEASED:
                if (updateValue) {
                    currentButtonState = ButtonState.JUST_PRESSED;
                } else {
                    currentButtonState = ButtonState.BEING_RELEASED;
                }
                break;
            case JUST_PRESSED:
                if (updateValue) {
                    currentButtonState = ButtonState.BEING_PRESSED;
                } else {
                    currentButtonState = ButtonState.JUST_RELEASED;
                }
                break;
            case JUST_RELEASED:
                if (updateValue) {
                    currentButtonState = ButtonState.JUST_PRESSED;
                } else {
                    currentButtonState = ButtonState.BEING_RELEASED;
                }
                break;
        }

        notifyListeners();
    }

    public ListenableButton addListener(ButtonState buttonState, ButtonListener buttonListener) {
        listeners.get(buttonState).add(buttonListener);
        return this;
    }

    public boolean removeListener(ButtonState buttonState, ButtonListener buttonListener) {
        return listeners.get(buttonState).remove(buttonListener);
    }

    private void notifyListeners() {
        for (ButtonListener buttonListener : listeners.get(currentButtonState))
            buttonListener.run();
    }

    public enum ButtonState {
        BEING_PRESSED, BEING_RELEASED, JUST_PRESSED, JUST_RELEASED
    }

    public interface ButtonListener extends Runnable {
    }
}
