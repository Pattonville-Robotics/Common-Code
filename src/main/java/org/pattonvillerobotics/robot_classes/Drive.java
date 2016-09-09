package org.pattonvillerobotics.robot_classes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.pattonvillerobotics.enums.Direction;

public class Drive extends AbstractDrive {

    public Drive(HardwareMap hardwareMap, LinearOpMode linearOpMode) {
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

        leftDriveMotor.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
        rightDriveMotor.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

        leftDriveMotor.setTargetPosition(targetPositionLeft);
        rightDriveMotor.setTargetPosition(targetPositionRight);

        move(direction, power);

        while (leftDriveMotor.getCurrentPosition() != targetPositionLeft || rightDriveMotor.getCurrentPosition() != targetPositionRight) {
            move(direction, power);
        }
        stop();
    }

    @Override
    protected void sleep(long milli) {
        try {
            linearOpMode.sleep(milli);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void telemetry(String tag, String message) {
        this.linearOpMode.telemetry.addData(tag, message);
    }
}
