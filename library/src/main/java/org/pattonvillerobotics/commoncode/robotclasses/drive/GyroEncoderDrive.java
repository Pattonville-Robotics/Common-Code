package org.pattonvillerobotics.commoncode.robotclasses.drive;

import android.util.Log;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.apache.commons.math3.util.FastMath;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.pattonvillerobotics.commoncode.enums.Direction;

public class GyroEncoderDrive extends EncoderDrive {

    private static final double ANGLE_THRESHOLD = 1;

    private BNO055IMU imu = null;
    private Orientation angles;
    public GyroEncoderDrive(HardwareMap hardwareMap, LinearOpMode linearOpMode, RobotParameters robotParameters) {
        super(hardwareMap, linearOpMode, robotParameters);

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json";
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

    }

    @Override
    public Telemetry.Item telemetry(String message) {
        return super.telemetry("GyroEncoderDrive", message);
    }

    @Override
    public void rotateDegrees(Direction direction, double degrees, double speed) {
        this.gyroTurnDegrees(direction, degrees, speed);
    }

    private void gyroTurnDegrees(Direction direction, double angle, double speed) {
        //Turn Specified Degrees Using Gyro Sensor

        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        double currentHeading = angles.firstAngle;
        double targetHeading;
        Direction currentDirection = direction;
        int numOvershoots = 0;

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

        Telemetry.Item headingsTelemetryItem = telemetry("Headings", "Current Heading: " + currentHeading + "& Target Heading: " + targetHeading);

        while (FastMath.abs(currentHeading - targetHeading) > .01) {
            Direction newDirection = FastMath.signum(currentHeading - targetHeading) > 0 ? Direction.LEFT : Direction.RIGHT;
            if (newDirection != currentDirection)
                numOvershoots++;
            turn(currentDirection, speed / (numOvershoots + 1));

            angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            currentHeading = angles.firstAngle;
            headingsTelemetryItem.setValue("Headings", "Current Heading: " + currentHeading + "& Target Heading: " + targetHeading);

            if (numOvershoots > 5)
                break;
        }

        telemetry("Drive", "Angle obtained, stopping motors.");
        Log.i("GyroHeading", Double.toString(currentHeading));

        stop();
    }
}
