package org.pattonvillerobotics.commoncode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.apache.commons.math3.util.FastMath;

import static org.pattonvillerobotics.commoncode.robotclasses.drive.EncoderDrive.TARGET_REACHED_THRESHOLD;

/**
 * Created by skaggsw on 9/7/17.
 */
@Autonomous(name="EncoderTest")
public class EncoderTest extends LinearOpMode {

    public DcMotor testMotor;

    @Override
    public void runOpMode() throws InterruptedException {

        testMotor = hardwareMap.dcMotor.get("test_motor");

        testMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        testMotor.setTargetPosition(360);

        testMotor.setPower(0.5);

        while (!reachedTarget(testMotor.getCurrentPosition(), 360) && !this.isStopRequested() && this.opModeIsActive()) {
        }

        testMotor.setPower(0);

    }

    protected boolean reachedTarget(int currentPositionLeft, int targetPositionLeft) {
        return FastMath.abs(currentPositionLeft - targetPositionLeft) < TARGET_REACHED_THRESHOLD;
    }

}
