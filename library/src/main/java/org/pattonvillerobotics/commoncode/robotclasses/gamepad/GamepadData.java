package org.pattonvillerobotics.commoncode.robotclasses.gamepad;

import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * Created by skaggsm on 11/24/15.
 * This class stores all data pertaining to a gamepad at the given time.
 * This class is IMMUTABLE
 */
public class GamepadData {

    public final float left_stick_x;
    public final float left_stick_y;
    public final float right_stick_x;
    public final float right_stick_y;
    public final boolean dpad_up;
    public final boolean dpad_down;
    public final boolean dpad_left;
    public final boolean dpad_right;
    public final boolean a;
    public final boolean b;
    public final boolean x;
    public final boolean y;
    public final boolean left_bumper;
    public final boolean right_bumper;
    public final boolean left_stick_button;
    public final boolean right_stick_button;
    public final float left_trigger;
    public final float right_trigger;

    public GamepadData(Gamepad gamepad) {
        this.left_stick_x = gamepad.left_stick_x;
        this.left_stick_y = gamepad.left_stick_y;
        this.right_stick_x = gamepad.right_stick_x;
        this.right_stick_y = gamepad.right_stick_y;

        this.dpad_up = gamepad.dpad_up;
        this.dpad_down = gamepad.dpad_down;
        this.dpad_left = gamepad.dpad_left;
        this.dpad_right = gamepad.dpad_right;

        this.a = gamepad.a;
        this.b = gamepad.b;
        this.x = gamepad.x;
        this.y = gamepad.y;

        this.left_bumper = gamepad.left_bumper;
        this.right_bumper = gamepad.right_bumper;
        this.left_stick_button = gamepad.left_stick_button;
        this.right_stick_button = gamepad.right_stick_button;

        this.left_trigger = gamepad.left_trigger;
        this.right_trigger = gamepad.right_trigger;
    }

    public boolean getButtonPressed(Button button) {
        switch (button) {
            case A:
                return this.a;
            case B:
                return this.b;
            case X:
                return this.x;
            case Y:
                return this.y;
            case LEFT_BUMPER:
                return this.left_bumper;
            case RIGHT_BUMPER:
                return this.right_bumper;
            case DPAD_UP:
                return this.dpad_up;
            case DPAD_DOWN:
                return this.dpad_down;
            case DPAD_LEFT:
                return this.dpad_left;
            case DPAD_RIGHT:
                return this.dpad_right;
            case STICK_BUTTON_LEFT:
                return this.left_stick_button;
            case STICK_BUTTON_RIGHT:
                return this.right_stick_button;
            default:
                throw new IllegalStateException("The button \"" + button + "\" does not exist!");
        }
    }

    public enum Button {
        A, B, X, Y, LEFT_BUMPER, RIGHT_BUMPER, DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, STICK_BUTTON_LEFT, STICK_BUTTON_RIGHT
    }
}
