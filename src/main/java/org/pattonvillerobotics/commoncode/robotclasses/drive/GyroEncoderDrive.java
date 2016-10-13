package org.pattonvillerobotics.commoncode.robotclasses.drive;

import android.util.Log;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.pattonvillerobotics.commoncode.enums.Direction;

public class GyroEncoderDrive extends EncoderDrive {

    private static final double ANGLE_THRESHOLD = 1;

    public ModernRoboticsI2cGyro gyroSensor = null;

    public GyroEncoderDrive(HardwareMap hardwareMap, LinearOpMode linearOpMode, RobotParameters robotParameters) {
        super(hardwareMap, linearOpMode, robotParameters);

        gyroSensor = (ModernRoboticsI2cGyro) hardwareMap.gyroSensor.get("gyro");
        gyroSensor.calibrate();

        while (gyroSensor.isCalibrating()) {
            linearOpMode.sleep(100);
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

        telemetry("Headings", "Current Heading: " + currentHeading + "& Target Heading: " + targetHeading);

        while (currentHeading > targetHeading + ANGLE_THRESHOLD || currentHeading > targetHeading - ANGLE_THRESHOLD) {

            while (currentHeading > targetHeading - ANGLE_THRESHOLD) {
                telemetry("Turning Direction", "Left");

                turn(Direction.LEFT, power);
                currentHeading = gyroSensor.getIntegratedZValue();

                Log.i("GyroHeading", Double.toString(currentHeading));
            }

            while (currentHeading < targetHeading + ANGLE_THRESHOLD) {
                telemetry("Turning Direction", "Right");

                turn(Direction.RIGHT, power);
                currentHeading = gyroSensor.getIntegratedZValue();

                Log.i("GyroHeading", Double.toString(currentHeading));
            }

        }

        telemetry("Drive", "Angle obtained, stopping motors.");
        Log.i("GyroHeading", Double.toString(gyroSensor.getIntegratedZValue()));

        stop();
    }
}
