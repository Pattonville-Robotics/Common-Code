package org.pattonvillerobotics.commoncode.robotclasses;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.pattonvillerobotics.commoncode.enums.Direction;

/**
 * Created by skaggsm on 9/27/16.
 */

public abstract class AbstractComplexDrive extends AbstractDrive {
    public AbstractComplexDrive(LinearOpMode linearOpMode, HardwareMap hardwareMap, RobotParameters robotParameters) {
        super(linearOpMode, hardwareMap, robotParameters);
    }

    public abstract void moveInches(Direction direction, double inches, double power);

    public abstract void rotateDegrees(Direction direction, double degrees, double power);
}
