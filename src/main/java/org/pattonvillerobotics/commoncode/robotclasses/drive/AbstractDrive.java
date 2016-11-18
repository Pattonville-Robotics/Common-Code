package org.pattonvillerobotics.commoncode.robotclasses.drive;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.pattonvillerobotics.commoncode.enums.Direction;

/**
 * Created by Mitchell on 9/9/2016.
 */
public abstract class AbstractDrive implements Drive {

    public DcMotor leftDriveMotor;
    public DcMotor rightDriveMotor;
    public LinearOpMode linearOpMode;
    public HardwareMap hardwareMap;

    public AbstractDrive(LinearOpMode linearOpMode, HardwareMap hardwareMap) {
        this.leftDriveMotor = hardwareMap.dcMotor.get("left_drive_motor");
        this.rightDriveMotor = hardwareMap.dcMotor.get("right_drive_motor");
        this.linearOpMode = linearOpMode;
        this.hardwareMap = hardwareMap;

        this.leftDriveMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.rightDriveMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        this.leftDriveMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.rightDriveMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        this.rightDriveMotor.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public void moveFreely(double leftPower, double rightPower) {
        leftDriveMotor.setPower(Range.clip(leftPower, -1, 1));
        rightDriveMotor.setPower(Range.clip(rightPower, -1, 1));
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
    }

    @Override
    public void stop() {
        moveFreely(0, 0);
    }

    public void sleep(long milli) throws InterruptedException {
        this.linearOpMode.sleep(milli);
    }

    public Telemetry.Item telemetry(String tag, String message) {
        return this.linearOpMode.telemetry.addData(tag, message);
    }
}
