package org.pattonvillerobotics.commoncode.robotclasses;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.pattonvillerobotics.commoncode.enums.Direction;

/**
 * Created by skaggsm on 9/27/16.
 */

public abstract class AbstractComplexDrive extends AbstractDrive {

    protected RobotParameters robotParameters;

    public AbstractComplexDrive(LinearOpMode linearOpMode, HardwareMap hardwareMap, RobotParameters robotParameters) {
        super(linearOpMode, hardwareMap);
        this.robotParameters = robotParameters;
    }

    /**
     * NOT REVERSIBLE WITH <code>degreesToInches</code>!
     *
     * @param inches the number of inches to be covered by a single wheel
     * @return the number of encoder ticks to achieve that
     */
    public double inchesToTicks(double inches) {
        return RobotParameters.TICKS_PER_REVOLUTION * inches / robotParameters.getWheelCircumference();
    }

    /**
     * NOT REVERSIBLE WITH <code>inchesToTicks</code>!
     *
     * @param degrees the number of degrees to turn the robot
     * @return the number of inches each wheel has to travel
     */
    public double degreesToInches(double degrees) {
        return robotParameters.getWheelBaseCircumference() * degrees / 360;
    }

    public abstract void moveInches(Direction direction, double inches, double power);

    public abstract void rotateDegrees(Direction direction, double degrees, double power);
}
