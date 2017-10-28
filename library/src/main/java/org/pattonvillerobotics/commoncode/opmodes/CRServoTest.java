package org.pattonvillerobotics.commoncode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;

import org.pattonvillerobotics.commoncode.robotclasses.gamepad.GamepadData;
import org.pattonvillerobotics.commoncode.robotclasses.gamepad.ListenableButton;
import org.pattonvillerobotics.commoncode.robotclasses.gamepad.ListenableGamepad;

/**
 * Created by bahrg on 3/16/17.
 */
@TeleOp(name = "CRServoTest1")
@Disabled
public class CRServoTest extends LinearOpMode {

    private CRServo crServo;
    private ListenableGamepad gamepad;

    @Override
    public void runOpMode() throws InterruptedException {
        crServo = hardwareMap.crservo.get("crservo");
        gamepad = new ListenableGamepad();
        gamepad.getButton(GamepadData.Button.A).addListener(ListenableButton.ButtonState.JUST_PRESSED, () -> crServo.setPower(1));

        gamepad.getButton(GamepadData.Button.B).addListener(ListenableButton.ButtonState.JUST_PRESSED, () -> crServo.setPower(0));

        gamepad.getButton(GamepadData.Button.X).addListener(ListenableButton.ButtonState.JUST_PRESSED, () -> crServo.setPower(-1));
        waitForStart();

        while (opModeIsActive()) {
            gamepad.update(gamepad1);
        }
    }
}
