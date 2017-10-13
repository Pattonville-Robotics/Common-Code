package org.pattonvillerobotics.commoncode.robotclasses.gamepad;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by skaggsm on 10/6/16.
 */

public class ListenableGamepad {

    private final Map<GamepadData.Button, ListenableButton> buttons;

    public ListenableGamepad() {
        this.buttons = new HashMap<>();

        for (GamepadData.Button b : GamepadData.Button.values())
            buttons.put(b, new ListenableButton());
    }

    public void update(GamepadData gamepadData) {
        for (GamepadData.Button b : GamepadData.Button.values())
            buttons.get(b).update(gamepadData.getButtonPressed(b));
    }

    public ListenableButton getButton(GamepadData.Button button) {
        return buttons.get(button);
    }

}
