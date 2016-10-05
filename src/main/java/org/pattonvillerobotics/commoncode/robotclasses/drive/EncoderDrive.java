package org.pattonvillerobotics.commoncode.robotclasses.drive;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.apache.commons.math3.util.FastMath;
import org.pattonvillerobotics.commoncode.enums.Direction;

public class EncoderDrive extends AbstractComplexDrive {

    public EncoderDrive(HardwareMap hardwareMap, LinearOpMode linearOpMode, RobotParameters robotParameters) {
        super(linearOpMode, hardwareMap, robotParameters);
        if (!robotParameters.areEncodersEnabled())
            throw new IllegalArgumentException("Robot must have encoders enabled to use EncoderDrive! If encoders are present, call encodersEnabled(true) when building.");
    }

    @Override
    public void moveInches(Direction direction, double inches, double power) {
        //Move Specified Inches Using Motor Encoders

        int targetPositionLeft;
        int targetPositionRight;

        int startPositionLeft = leftDriveMotor.getCurrentPosition();
        int startPositionRight = rightDriveMotor.getCurrentPosition();

        switch (direction) {
            case FORWARD: {
                int deltaPosition = (int) FastMath.round(inchesToTicks(inches));
                targetPositionLeft = startPositionLeft + deltaPosition;
                targetPositionRight = startPositionRight + deltaPosition;
                break;
            }
            case BACKWARD: {
                int deltaPosition = (int) FastMath.round(inchesToTicks(inches));
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

        while (leftDriveMotor.getCurrentPosition() != targetPositionLeft || rightDriveMotor.getCurrentPosition() != targetPositionRight) {
            move(direction, power);
            if (linearOpMode.isStopRequested())
                break;
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

        switch (direction) {
            case LEFT: {
                int deltaPosition = (int) FastMath.round(inchesToTicks(inches));
                targetPositionLeft = startPositionLeft - deltaPosition;
                targetPositionRight = startPositionRight + deltaPosition;
                break;
            }
            case RIGHT: {
                int deltaPosition = (int) FastMath.round(inchesToTicks(inches));
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

        while (!reachedTarget(leftDriveMotor.getCurrentPosition(), targetPositionLeft, rightDriveMotor.getCurrentPosition(), targetPositionRight)) {
            move(direction, power);
            if (linearOpMode.isStopRequested())
                break;
        }
        stop();
    }

    private boolean reachedTarget(int currentPositionLeft, int targetPositionLeft, int currentPositionRight, int targetPositionRight) {
        return FastMath.abs(currentPositionLeft - targetPositionLeft) < 16 && FastMath.abs(currentPositionRight - targetPositionRight) < 16;
    }
}
