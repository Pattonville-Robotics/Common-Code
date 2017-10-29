package org.pattonvillerobotics.commoncode.robotclasses.gamepad

import com.qualcomm.robotcore.hardware.Gamepad
import org.pattonvillerobotics.commoncode.robotclasses.gamepad.GamepadData.Button
import java.util.*

/**
 * This class allows users to subscribe to certain buttons' states in an event-driven fashion.
 *
 * @author Mitchell Skaggs
 * @since 3.4.0
 */
class ListenableGamepad {

    private val buttons: MutableMap<Button, ListenableButton> = HashMap()

    /**
     * Constructs a new ListenableGamepad and populates it with [ListenableButton] instances
     */
    init {
        for (b in Button.values())
            buttons.put(b, ListenableButton())
    }

    /**
     * Calls all button listeners based on the new gamepad values.
     *
     * @param gamepadData the gamepad values
     */
    fun update(gamepadData: GamepadData) {
        for (b in Button.values())
            buttons[b]!!.update(gamepadData.getButtonPressed(b))
    }


    /**
     * Calls all button listeners based on the new gamepad values.
     *
     * @param gamepad the gamepad from which to extract immutable values
     */
    fun update(gamepad: Gamepad) {
        this.update(GamepadData(gamepad))
    }

    /**
     * Gets an instance of a button from the button's corresponding enum
     *
     * @param button the button enum to find
     * @return a button matching the enum
     */
    fun getButton(button: Button): ListenableButton {
        return buttons[button]!!
    }

}
