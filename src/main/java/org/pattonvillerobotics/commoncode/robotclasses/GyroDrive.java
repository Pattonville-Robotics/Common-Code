package org.pattonvillerobotics.commoncode.robotclasses;

import android.util.Log;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.pattonvillerobotics.commoncode.enums.Direction;

public class GyroDrive extends Drive {

    private static final double ANGLE_THRESHOLD = 2;

    public ModernRoboticsI2cGyro gyroSensor = null;

    public GyroDrive(HardwareMap hardwareMap, LinearOpMode linearOpMode) {
        super(hardwareMap, linearOpMode);

        gyroSensor = (ModernRoboticsI2cGyro) hardwareMap.gyroSensor.get("gyro");
        gyroSensor.calibrate();

        while (gyroSensor.isCalibrating()) {
            try {
                linearOpMode.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
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
}
