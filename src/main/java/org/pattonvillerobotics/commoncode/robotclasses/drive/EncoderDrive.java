package org.pattonvillerobotics.commoncode.robotclasses.drive;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.apache.commons.math3.util.FastMath;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.pattonvillerobotics.commoncode.enums.Direction;

public class EncoderDrive extends AbstractComplexDrive {

    public EncoderDrive(HardwareMap hardwareMap, LinearOpMode linearOpMode, RobotParameters robotParameters) {
        super(linearOpMode, hardwareMap, robotParameters);
        if (!robotParameters.areEncodersEnabled())
            throw new IllegalArgumentException("Robot must have encoders enabled to use EncoderDrive! If encoders are present, call encodersEnabled(true) when building.");
    }

    public Telemetry.Item telemetry(String message) {
        return super.telemetry("EncoderDrive", message);
    }

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

        leftDriveMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightDriveMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        leftDriveMotor.setTargetPosition(targetPositionLeft);
        rightDriveMotor.setTargetPosition(targetPositionRight);

        telemetry("Moving " + inches + " inches at power " + power);
        telemetry("LMotorT: " + targetPositionLeft);
        telemetry("RMotorT: " + targetPositionRight);
        telemetry("EncoderDelta: " + deltaPosition);
        Telemetry.Item distance = telemetry("DistanceL: -1 DistanceR: -1");

        move(direction, power);
        while (leftDriveMotor.getCurrentPosition() != targetPositionLeft || rightDriveMotor.getCurrentPosition() != targetPositionRight) {
            Thread.yield();
            if (linearOpMode.isStopRequested())
                break;
            distance.setValue("DistanceL: " + leftDriveMotor.getCurrentPosition() + " DistanceR: " + rightDriveMotor.getCurrentPosition());
            linearOpMode.telemetry.update();
        }
        stop();
    }

    @Override
    public void rotateDegrees(Direction direction, double degrees, double power) {
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

        leftDriveMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightDriveMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        leftDriveMotor.setTargetPosition(targetPositionLeft);
        rightDriveMotor.setTargetPosition(targetPositionRight);

        telemetry("Rotating " + degrees + " degrees at power " + power).setRetained(true);
        telemetry("LMotorT: " + targetPositionLeft).setRetained(true);
        telemetry("RMotorT: " + targetPositionRight).setRetained(true);
        telemetry("EncoderDelta: " + deltaPosition).setRetained(true);
        Telemetry.Item distance = telemetry("DistanceL: DistanceR:");

        move(direction, power);
        while (!reachedTarget(leftDriveMotor.getCurrentPosition(), targetPositionLeft, rightDriveMotor.getCurrentPosition(), targetPositionRight)) {
            Thread.yield();
            if (linearOpMode.isStopRequested())
                break;
            distance.setValue("DistanceL: " + leftDriveMotor.getCurrentPosition() + " DistanceR: " + rightDriveMotor.getCurrentPosition());
            linearOpMode.telemetry.update();
        }
        stop();
    }

    private boolean reachedTarget(int currentPositionLeft, int targetPositionLeft, int currentPositionRight, int targetPositionRight) {
        return FastMath.abs(currentPositionLeft - targetPositionLeft) < 16 && FastMath.abs(currentPositionRight - targetPositionRight) < 16;
    }
}
