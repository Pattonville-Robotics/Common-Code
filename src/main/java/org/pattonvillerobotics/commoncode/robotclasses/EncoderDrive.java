package org.pattonvillerobotics.commoncode.robotclasses;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.pattonvillerobotics.commoncode.enums.Direction;

public class EncoderDrive extends AbstractDrive {

    public EncoderDrive(HardwareMap hardwareMap, LinearOpMode linearOpMode) {
        super(linearOpMode, hardwareMap);
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

        leftDriveMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightDriveMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        leftDriveMotor.setTargetPosition(targetPositionLeft);
        rightDriveMotor.setTargetPosition(targetPositionRight);

        move(direction, power);

        while (leftDriveMotor.getCurrentPosition() != targetPositionLeft || rightDriveMotor.getCurrentPosition() != targetPositionRight) {
            move(direction, power);
        }
        stop();
    }
}
