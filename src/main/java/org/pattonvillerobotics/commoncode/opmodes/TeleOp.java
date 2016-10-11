package org.pattonvillerobotics.commoncode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.pattonvillerobotics.commoncode.robotclasses.drive.SimpleDrive;

/**
 * Created by developer on 10/11/16.
 */

public class TeleOp extends LinearOpMode {

    private SimpleDrive drive;

    @Override
    public void runOpMode() throws InterruptedException{
        initialize();
        waitForStart();

        while (opModeIsActive()){
            doLoop();
        }
    }

    public void initialize(){
        drive = new SimpleDrive(this, hardwareMap);
        gamepad1.left_stick_y = 0;
        gamepad1.right_stick_y = 0;
    }

    public void doLoop(){
        drive.moveFreely(-gamepad1.left_stick_y, -gamepad1.right_stick_y);
    }
}
