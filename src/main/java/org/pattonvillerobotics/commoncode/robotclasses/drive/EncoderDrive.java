package org.pattonvillerobotics.commoncode.robotclasses.drive;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.apache.commons.math3.util.FastMath;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.pattonvillerobotics.commoncode.enums.Direction;

public class EncoderDrive extends AbstractComplexDrive {

    public static final int TARGET_REACHED_THRESHOLD = 16;
    private static final String TAG = "EncoderDrive";

    /**
     * sets up Drive object with custom RobotParameters useful for doing calculations with encoders
     *
     * @param hardwareMap     a hardwaremap
     * @param linearOpMode    a linearopmode
     * @param robotParameters a RobotParameters containing robot specific calculations for
     *                        wheel radius and wheel base radius
     */
    public EncoderDrive(HardwareMap hardwareMap, LinearOpMode linearOpMode, RobotParameters robotParameters) {
        super(linearOpMode, hardwareMap, robotParameters);
        if (!robotParameters.areEncodersEnabled())
            throw new IllegalArgumentException("Robot must have encoders enabled to use EncoderDrive! If encoders are present, call encodersEnabled(true) when building.");
    }

    public Telemetry.Item telemetry(String message) {
        return super.telemetry("EncoderDrive", message);
    }

    /**
     * drives a specific number of inches in a given direction
     *
     * @param direction the direction (forward or backward) to drive in
     * @param inches    the number of inches to drive
     * @param power     the power with which to drive
     */
    @Override
    public void moveInches(Direction direction, double inches, double power) {
        //Move Specified Inches Using Motor Encoders

        int targetPositionLeft;
        int targetPositionRight;

        int startPositionLeft = leftDriveMotor.getCurrentPosition();
        int startPositionRight = rightDriveMotor.getCurrentPosition();

        int deltaPosition = (int) FastMath.round(inchesToTicks(inches));

        switch (direction) {
            case FORWARD: {
                targetPositionLeft = startPositionLeft + deltaPosition;
                targetPositionRight = startPositionRight + deltaPosition;
                break;
            }
            case BACKWARD: {
                targetPositionLeft = startPositionLeft - deltaPosition;
                targetPositionRight = startPositionRight - deltaPosition;
                break;
            }
            default:
                throw new IllegalArgumentException("Direction must be Direction.FORWARDS or Direction.BACKWARDS!");
        }

        Log.e(TAG, "Getting motor modes");
        DcMotor.RunMode leftDriveMotorMode = leftDriveMotor.getMode();
        DcMotor.RunMode rightDriveMotorMode = rightDriveMotor.getMode();

        Log.e(TAG, "Setting motor modes");
        if (leftDriveMotorMode != DcMotor.RunMode.RUN_TO_POSITION)
            leftDriveMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        if (rightDriveMotorMode != DcMotor.RunMode.RUN_TO_POSITION)
            rightDriveMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        Log.e(TAG, "Setting motor power high");
        move(Direction.FORWARD, power); // To keep power in [0.0, 1.0]. Encoders control direction

        Log.e(TAG, "Setting target position");
        leftDriveMotor.setTargetPosition(targetPositionLeft);
        rightDriveMotor.setTargetPosition(targetPositionRight);

        telemetry("Moving " + inches + " inches at power " + power);
        telemetry("LMotorT: " + targetPositionLeft);
        telemetry("RMotorT: " + targetPositionRight);
        telemetry("EncoderDelta: " + deltaPosition);
        Telemetry.Item distance = telemetry("DistanceL: N/A DistanceR: N/A");

        while ((!reachedTarget(leftDriveMotor.getCurrentPosition(), targetPositionLeft, rightDriveMotor.getCurrentPosition(), targetPositionRight) || (leftDriveMotor.isBusy() || rightDriveMotor.isBusy())) && !linearOpMode.isStopRequested()) {
            Thread.yield();
            distance.setValue("DistanceL: " + leftDriveMotor.getCurrentPosition() + " DistanceR: " + rightDriveMotor.getCurrentPosition());
            linearOpMode.telemetry.update();
        }
        Log.e(TAG, "Setting motor power low");
        stop();

        Log.e(TAG, "Restoring motor mode");
        if (leftDriveMotorMode != DcMotor.RunMode.RUN_TO_POSITION)
            leftDriveMotor.setMode(leftDriveMotorMode); // Restore the prior mode
        if (rightDriveMotorMode != DcMotor.RunMode.RUN_TO_POSITION)
            rightDriveMotor.setMode(rightDriveMotorMode);

        sleep(500);
    }

    /**
     * turns the robot a certain number of degrees in a given direction
     *
     * @param direction the direction (left or right) to turn in
     * @param degrees   the number of degrees to turn
     * @param speed     the speed at which to turn
     */
    @Override
    public void rotateDegrees(Direction direction, double degrees, double speed) {
        //Move specified degrees using motor encoders

        int targetPositionLeft;
        int targetPositionRight;

        int startPositionLeft = leftDriveMotor.getCurrentPosition();
        int startPositionRight = rightDriveMotor.getCurrentPosition();

        double inches = degreesToInches(degrees);
        int deltaPosition = (int) FastMath.round(inchesToTicks(inches));

        switch (direction) {
            case LEFT: {
                targetPositionLeft = startPositionLeft - deltaPosition;
                targetPositionRight = startPositionRight + deltaPosition;
                break;
            }
            case RIGHT: {
                targetPositionLeft = startPositionLeft + deltaPosition;
                targetPositionRight = startPositionRight - deltaPosition;
                break;
            }
            default:
                throw new IllegalArgumentException("Direction must be Direction.LEFT or Direction.RIGHT!");
        }

        DcMotor.RunMode leftDriveMotorMode = leftDriveMotor.getMode();
        DcMotor.RunMode rightDriveMotorMode = rightDriveMotor.getMode();

        if (leftDriveMotorMode != DcMotor.RunMode.RUN_TO_POSITION)
            leftDriveMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        if (rightDriveMotorMode != DcMotor.RunMode.RUN_TO_POSITION)
            rightDriveMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        leftDriveMotor.setTargetPosition(targetPositionLeft);
        rightDriveMotor.setTargetPosition(targetPositionRight);

        Telemetry.Item[] items = new Telemetry.Item[]{
                telemetry("Rotating " + degrees + " degrees at speed " + speed).setRetained(true),
                telemetry("LMotorT: " + targetPositionLeft).setRetained(true),
                telemetry("RMotorT: " + targetPositionRight).setRetained(true),
                telemetry("EncoderDelta: " + deltaPosition).setRetained(true),
                telemetry("DistanceL: DistanceR:")
        };
        Telemetry.Item distance = items[4];

        move(Direction.FORWARD, speed); // To keep speed in [0.0, 1.0]. Encoders control direction
        while ((!reachedTarget(leftDriveMotor.getCurrentPosition(), targetPositionLeft, rightDriveMotor.getCurrentPosition(), targetPositionRight) || (leftDriveMotor.isBusy() || rightDriveMotor.isBusy())) && !linearOpMode.isStopRequested()) {
            Thread.yield();
            distance.setValue("DistanceL: " + leftDriveMotor.getCurrentPosition() + " DistanceR: " + rightDriveMotor.getCurrentPosition());
            linearOpMode.telemetry.update();
        }
        stop();

        if (leftDriveMotorMode != DcMotor.RunMode.RUN_TO_POSITION)
            leftDriveMotor.setMode(leftDriveMotorMode); // Restore the prior mode
        if (rightDriveMotorMode != DcMotor.RunMode.RUN_TO_POSITION)
            rightDriveMotor.setMode(rightDriveMotorMode);

        for (Telemetry.Item i : items)
            i.setRetained(false);

        sleep(500);
    }

    /**
     * determines whether or not the robot has reached its target encoder position within a
     * certain threshold
     *
     * @param currentPositionLeft  the current position of the left encoder
     * @param targetPositionLeft   the target position of the left encoder
     * @param currentPositionRight the current position of the right encoder
     * @param targetPositionRight  the target position of the right encoder
     * @return
     */
    protected boolean reachedTarget(int currentPositionLeft, int targetPositionLeft, int currentPositionRight, int targetPositionRight) {
        return FastMath.abs(currentPositionLeft - targetPositionLeft) < TARGET_REACHED_THRESHOLD && FastMath.abs(currentPositionRight - targetPositionRight) < TARGET_REACHED_THRESHOLD;
    }
}
