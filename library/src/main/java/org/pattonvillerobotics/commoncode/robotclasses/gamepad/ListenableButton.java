package org.pattonvillerobotics.commoncode.robotclasses.gamepad;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.List;


/**
 * This class allows users to subscribe to an individual button's state in an event-driven fashion.
 *
 * @author Mitchell Skaggs
 * @since 3.4.0
 */
public class ListenableButton {

    private final Multimap<ButtonState, ButtonListener> listeners;
    private ButtonState currentButtonState;


    /**
     * Constructs a new ListenableButton that is currently released.
     */
    public ListenableButton() {
        currentButtonState = ButtonState.BEING_RELEASED;
        listeners = ArrayListMultimap.create();
    }

    /**
     * Updates the current value of the button.
     *
     * @param updateValue true if pressed, false otherwise
     */
    void update(boolean updateValue) {
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

    /**
     * Add a listener to a given state.
     *
     * @param buttonState    the state to listen for
     * @param buttonListener the listener
     * @return itself, for chaining calls
     */
    public ListenableButton addListener(ButtonState buttonState, ButtonListener buttonListener) {
        listeners.put(buttonState, buttonListener);
        return this;
    }

    /**
     * Remove a listener from a given state.
     *
     * @param buttonState    the state to listen for
     * @param buttonListener the listener
     * @return see {@link List#remove(Object)}
     */
    public boolean removeListener(ButtonState buttonState, ButtonListener buttonListener) {
        return listeners.remove(buttonState, buttonListener);
    }

    private void notifyListeners() {
        for (ButtonListener buttonListener : listeners.get(currentButtonState))
            buttonListener.run();
    }

    /**
     * The possible states for a button to be in.
     */
    public enum ButtonState {
        BEING_PRESSED, BEING_RELEASED, JUST_PRESSED, JUST_RELEASED
    }

    /**
     * A listener for a certain button state.
     */
    public interface ButtonListener extends Runnable {
        @Override
        void run();
    }
}
