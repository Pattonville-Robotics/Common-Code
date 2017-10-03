package org.pattonvillerobotics.commoncode.robotclasses.drive;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.apache.commons.math3.util.FastMath;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.pattonvillerobotics.commoncode.enums.Direction;

/**
 * Created by greg on 10/2/2017.
 */

public class MecanumEncoderDrive extends QuadEncoderDrive {

    private static final String TAG = "MecanumEncoderDrive";
    private final double COS135 = FastMath.cos(FastMath.toRadians(135));
    private final double SIN135 = -COS135;
    private final double DEG45 = FastMath.PI / 4;

    public MecanumEncoderDrive(HardwareMap hardwareMap, LinearOpMode linearOpMode, RobotParameters robotParameters) {
        super(hardwareMap, linearOpMode, robotParameters);

        if (!super.secondaryLeftDriveMotor.isPresent() || !super.secondaryRightDriveMotor.isPresent()) {
            throw new IllegalArgumentException("Mecanum drive requires all 4 motors to be present!");
        }

    }

    /**
     * can be used to convert joystick values to polar
     *
     * @return coordinate array in the form of [r, theta]
     */
    public static double[] toPolar(double x, double y) {
        return new double[]{FastMath.hypot(x, y), FastMath.atan2(y, x)};
    }

    /**
     * used to drive a mecanum drive train
     *
     * @param angle    direction to go in radians
     * @param speed    speed to go
     * @param rotation rate of rotation
     */
    public void moveFreely(double angle, final double speed, final double rotation) {
        final double xcomponent = COS135 * (FastMath.cos(angle + DEG45));
        final double ycomponent = SIN135 * (FastMath.sin(angle + DEG45));

        super.leftDriveMotor.setPower(speed * ycomponent + rotation);
        super.rightDriveMotor.setPower(speed * xcomponent + rotation);
        this.secondaryLeftDriveMotor.get().setPower(speed * xcomponent - rotation);
        this.secondaryRightDriveMotor.get().setPower(speed * ycomponent - rotation);
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
        int targetPositionLeftRear;
        int targetPositionRightRear;

        Log.e(TAG, "Getting motor modes");
        storeMotorModes();

        resetMotorEncoders();

        int deltaPosition = (int) FastMath.round(inchesToTicks(inches));

        switch (direction) {
            case FORWARD: {
                targetPositionLeft = deltaPosition;
                targetPositionRight = deltaPosition;
                targetPositionLeftRear = deltaPosition;
                targetPositionRightRear = deltaPosition;
                break;
            }
            case BACKWARD: {
                targetPositionLeft = -deltaPosition;
                targetPositionRight = -deltaPosition;
                targetPositionLeftRear = -deltaPosition;
                targetPositionRightRear = -deltaPosition;
                break;
            }
            case LEFT: {
                targetPositionLeft = deltaPosition;
                targetPositionRight = -deltaPosition;
                targetPositionLeftRear = -deltaPosition;
                targetPositionRightRear = deltaPosition;
                break;
            }
            case RIGHT: {
                targetPositionLeft = -deltaPosition;
                targetPositionRight = deltaPosition;
                targetPositionLeftRear = deltaPosition;
                targetPositionRightRear = -deltaPosition;
                break;
            }
            default:
                throw new IllegalArgumentException("Direction must be Direction.FORWARDS, Direction.BACKWARDS, Direction.LEFT, or Direction.RIGHT!");
        }

        Log.e(TAG, "Setting motor modes");
        setMotorsRunToPosition();

        Log.e(TAG, "Setting motor power high");
        move(Direction.FORWARD, power); // To keep power in [0.0, 1.0]. Encoders control direction

        Log.e(TAG, "Setting target position");
        setMotorTargets(targetPositionLeft, targetPositionRight, targetPositionLeftRear, targetPositionRightRear);

        telemetry("Moving " + inches + " inches at power " + power);
        telemetry("LFMotorT: " + targetPositionLeft);
        telemetry("RFMotorT: " + targetPositionRight);
        telemetry("LRMotorT: " + targetPositionLeftRear);
        telemetry("RRMotorT: " + targetPositionRightRear);
        telemetry("EncoderDelta: " + deltaPosition);
        Telemetry.Item distance = telemetry("DistanceL: N/A DistanceR: N/A");
        Telemetry.Item distanceRear = telemetry("DistanceLR: N/A DistanceRR: N/A");

        while (isMovingToPosition() || !motorsReachedTarget(targetPositionLeft, targetPositionRight, targetPositionLeftRear, targetPositionRightRear) && !linearOpMode.isStopRequested() && linearOpMode.opModeIsActive()) {
            Thread.yield();
            distance.setValue("DistanceL: " + leftDriveMotor.getCurrentPosition() + " DistanceR: " + rightDriveMotor.getCurrentPosition());
            distanceRear.setValue("DistanceLR: " + secondaryLeftDriveMotor.get().getCurrentPosition() + " DistanceRR: " + secondaryRightDriveMotor.get().getCurrentPosition());
            linearOpMode.telemetry.update();
        }
        Log.e(TAG, "Setting motor power low");
        stop();

        Log.e(TAG, "Restoring motor mode");
        restoreMotorModes();

        sleep(100);
    }

    @Override
    public void rotateDegrees(Direction direction, double degrees, double speed) {
        //Move specified degrees using motor encoders

        int targetPositionLeft;
        int targetPositionRight;
        int targetPositionLeftRear;
        int targetPositionRightRear;

        Log.e(TAG, "Getting motor modes");
        storeMotorModes();

        resetMotorEncoders();

        double inches = degreesToInches(degrees);
        int deltaPosition = (int) FastMath.round(inchesToTicks(inches));

        switch (direction) {
            case LEFT: {
                targetPositionLeft = -deltaPosition;
                targetPositionRight = deltaPosition;
                targetPositionLeftRear = -deltaPosition;
                targetPositionRightRear = deltaPosition;
                break;
            }
            case RIGHT: {
                targetPositionLeft = deltaPosition;
                targetPositionRight = -deltaPosition;
                targetPositionLeftRear = deltaPosition;
                targetPositionRightRear = -deltaPosition;
                break;
            }
            default:
                throw new IllegalArgumentException("Direction must be Direction.LEFT or Direction.RIGHT!");
        }

        Log.e(TAG, "Setting motor modes");
        setMotorsRunToPosition();

        setMotorTargets(targetPositionLeft, targetPositionRight);

        Telemetry.Item[] items = new Telemetry.Item[]{
                telemetry("Rotating " + degrees + " degrees at speed " + speed).setRetained(true),
                telemetry("LFMotorT: " + targetPositionLeft).setRetained(true),
                telemetry("RFMotorT: " + targetPositionRight).setRetained(true),
                telemetry("LRMotorT: " + targetPositionLeftRear).setRetained(true),
                telemetry("RRMotorT: " + targetPositionRightRear).setRetained(true),
                telemetry("EncoderDelta: " + deltaPosition).setRetained(true),
                telemetry("DistanceLF: DistanceRF:").setRetained(true),
                telemetry("DistanceLR: DistanceRR:").setRetained(true)
        };
        Telemetry.Item distance = items[6];
        Telemetry.Item distanceRear = items[7];

        move(Direction.FORWARD, speed); // To keep speed in [0.0, 1.0]. Encoders control direction
        while (isMovingToPosition() && !motorsReachedTarget(targetPositionLeft, targetPositionRight, targetPositionLeftRear, targetPositionRightRear) && !linearOpMode.isStopRequested() && linearOpMode.opModeIsActive()) {
            Thread.yield();
            distance.setValue("DistanceLF: " + leftDriveMotor.getCurrentPosition() + " DistanceRF: " + rightDriveMotor.getCurrentPosition());
            distanceRear.setValue("DistanceLR: " + secondaryLeftDriveMotor.get().getCurrentPosition() + " DistanceRR: " + secondaryRightDriveMotor.get().getCurrentPosition());
            linearOpMode.telemetry.update();
        }
        stop();

        Log.e(TAG, "Restoring motor mode");
        restoreMotorModes();

        for (Telemetry.Item i : items)
            i.setRetained(false);

        sleep(100);
    }

    @Override
    public void move(Direction direction, double power) {
        double angle;

        switch (direction) {
            case FORWARD:
                angle = FastMath.toRadians(90);
                break;
            case BACKWARD:
                angle = FastMath.toRadians(270);
                break;
            case LEFT:
                angle = FastMath.toRadians(180);
                break;
            case RIGHT:
                angle = 0;
                break;
            default:
                throw new IllegalArgumentException("Direction must be FORWARD, BACKWARD, LEFT, or RIGHT");
        }
        moveFreely(angle, power, 0);
    }

    @Override
    public void turn(Direction direction, double power) {
        double rotation;

        switch (direction) {
            case LEFT:
                rotation = -power;
                break;
            case RIGHT:
                rotation = power;
                break;
            default:
                throw new IllegalArgumentException("Direction must be LEFT or RIGHT");
        }
        moveFreely(0, 0, rotation);
    }

    protected void setMotorTargets(int targetPositionLeft, int targetPositionRight, int targetPositionLeftRear, int targetPositionRightRear) {
        leftDriveMotor.setTargetPosition(targetPositionLeft);
        rightDriveMotor.setTargetPosition(targetPositionRight);
        secondaryLeftDriveMotor.get().setTargetPosition(targetPositionLeftRear);
        secondaryRightDriveMotor.get().setTargetPosition(targetPositionRightRear);
    }

    protected boolean motorsReachedTarget(int targetPositionLeft, int targetPositionRight, int targetPositionLeftRear, int targetPositionRightRear) {
        return reachedTarget(leftDriveMotor.getCurrentPosition(), targetPositionLeft, rightDriveMotor.getCurrentPosition(), targetPositionRight) &&
                reachedTarget(secondaryLeftDriveMotor.get().getCurrentPosition(), targetPositionLeftRear, secondaryRightDriveMotor.get().getCurrentPosition(), targetPositionRightRear);
    }
}
