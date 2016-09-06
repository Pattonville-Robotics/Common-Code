package org.pattonvillerobotics.robot_classes;

import android.util.Log;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.pattonvillerobotics.enums.Direction;

/**
 * Created by developer on 7/30/16.
 */
public class Drive {

    private static final double ANGLE_THRESHOLD = 2;

    public DcMotor left_drive_motor, right_drive_motor = null;
    public ModernRoboticsI2cGyro gyroSensor = null;
    public TouchSensor touchSensor = null;
    public LinearOpMode linearOpMode;
    public HardwareMap hardwareMap;

    public Drive(HardwareMap hardwareMap, LinearOpMode linearOpMode) throws InterruptedException {

        this.linearOpMode = linearOpMode;
        this.hardwareMap = hardwareMap;

        left_drive_motor = hardwareMap.dcMotor.get("left_drive_motor");
        right_drive_motor = hardwareMap.dcMotor.get("right_drive_motor");

        gyroSensor = (ModernRoboticsI2cGyro) hardwareMap.gyroSensor.get("gyro");
        touchSensor = hardwareMap.touchSensor.get("touch_sensor");

        gyroSensor.calibrate();

        while (gyroSensor.isCalibrating()) {
            Thread.sleep(50);
        }

    }

    public void moveFreely(double left_power, double right_power) {
        left_drive_motor.setPower(left_power);
        right_drive_motor.setPower(right_power);
    }

    public void move(Direction direction, double power) {

        double motorPower;

        switch (direction) {
            case FORWARD:
                motorPower = power;
                break;
            case BACKWARD:
                motorPower = -power;
                break;
            default:
                throw new IllegalArgumentException("Direction must be Backward or Forwards");
        }

        moveFreely(motorPower, motorPower);

    }

    public void turn(Direction direction, double power) {

        double left, right;

        switch (direction) {
            case LEFT:
                left = -power;
                right = power;
                break;
            case RIGHT:
                left = power;
                right = -power;
                break;
            default:
                throw new IllegalArgumentException("Direction must be LEFT or RIGHT");
        }

        moveFreely(left, right);
        sleep(50);
        stop();

    }

    public void stop() {
        moveFreely(0, 0);
    }

    public void moveInches(Direction direction, double inches, double power) {
        //Move Specified Inches Using Motor Encoders

        int targetPositionLeft;
        int targetPositionRight;

        int startPositionLeft = left_drive_motor.getCurrentPosition();
        int startPositionRight = right_drive_motor.getCurrentPosition();

        switch (direction) {
            case FORWARD: {
                int deltaPosition = (int) Math.round(inchesToTicks(inches));
                targetPositionLeft = startPositionLeft + deltaPosition;
                targetPositionRight = startPositionRight + deltaPosition;
                break;
            }
            case BACKWARD: {
                int deltaPosition = (int) Math.round(inchesToTicks(inches));
                targetPositionLeft = startPositionLeft - deltaPosition;
                targetPositionRight = startPositionRight - deltaPosition;
                break;
            }
            default:
                throw new IllegalArgumentException("Direction must be FORWARDS or BACKWARDS!");
        }

        left_drive_motor.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
        right_drive_motor.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

        left_drive_motor.setTargetPosition(targetPositionLeft);
        right_drive_motor.setTargetPosition(targetPositionRight);

        move(direction, power);

        while (left_drive_motor.getCurrentPosition() != targetPositionLeft || right_drive_motor.getCurrentPosition() != targetPositionRight) {
            move(direction, power);
        }

        stop();

    }

    public void turnDegrees(Direction direction, double angle, double power) {
        //Turn Specified Degrees Using Gyro Sensor

        double currentHeading = gyroSensor.getIntegratedZValue();
        double targetHeading;

        switch (direction) {
            case LEFT:
                targetHeading = currentHeading - angle;
                break;
            case RIGHT:
                targetHeading = currentHeading + angle;
                break;
            default:
                throw new IllegalArgumentException();

        }

        telemetry("Headings", "Current Heading: " + currentHeading + " Target Heading: " + targetHeading);

        while (currentHeading < targetHeading - ANGLE_THRESHOLD) {
            telemetry("Turning Direction", "Left");

            turn(Direction.LEFT, power);
            currentHeading = gyroSensor.getIntegratedZValue();

            Log.i("GyroHeading", Double.toString(currentHeading));
        }

        while (currentHeading > targetHeading + ANGLE_THRESHOLD) {
            telemetry("Turning Direction", "Right");

            turn(Direction.RIGHT, power);
            currentHeading = gyroSensor.getIntegratedZValue();

            Log.i("GyroHeading", Double.toString(currentHeading));
        }

        telemetry("Drive", "Angle obtained, stopping motors.");
        Log.i("GyroHeading", Double.toString(gyroSensor.getIntegratedZValue()));

        stop();

    }

    public void driveUntilContact(double power) {

        while (!touchSensor.isPressed()) {
            move(Direction.FORWARD, power);
        }

        stop();

    }


    private double inchesToTicks(double inches) {
        return inches;
    }

    private void sleep(long milli) {
        try {
            linearOpMode.sleep(milli);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void telemetry(String tag, String message) {
        this.linearOpMode.telemetry.addData(tag, message);
    }


}
