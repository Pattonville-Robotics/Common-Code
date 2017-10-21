package org.pattonvillerobotics.commoncode.robotclasses.gamepad;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.HashMap;
import java.util.Map;

/**
 * This class allows users to subscribe to certain buttons' states in an event-driven fashion.
 *
 * @author Mitchell Skaggs
 * @since 3.4.0
 */
public class ListenableGamepad {

    private final Map<GamepadData.Button, ListenableButton> buttons;

    /**
     * Constructs a new ListenableGamepad and populates it with {@link ListenableButton} instances
     */
    public ListenableGamepad() {
        this.buttons = new HashMap<>();

        for (GamepadData.Button b : GamepadData.Button.values())
            buttons.put(b, new ListenableButton());
    }

    /**
     * Calls all button listeners based on the new gamepad values.
     *
     * @param gamepadData the gamepad values
     */
    public void update(GamepadData gamepadData) {
        for (GamepadData.Button b : GamepadData.Button.values())
            buttons.get(b).update(gamepadData.getButtonPressed(b));
    }


    /**
     * Calls all button listeners based on the new gamepad values.
     *
     * @param gamepad the gamepad from which to extract immutable values
     */
    public void update(Gamepad gamepad) {
        this.update(new GamepadData(gamepad));
    }

    /**
     * Gets an instance of a button from the button's corresponding enum
     *
     * @param button the button enum to find
     * @return a button matching the enum
     */
    public ListenableButton getButton(GamepadData.Button button) {
        return buttons.get(button);
    }

}
