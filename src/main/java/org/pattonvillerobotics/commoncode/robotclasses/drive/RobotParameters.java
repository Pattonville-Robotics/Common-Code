package org.pattonvillerobotics.commoncode.robotclasses.drive;

import org.apache.commons.math3.util.FastMath;

/**
 * Created by skaggsm on 9/22/16.
 */

public class RobotParameters {
    public static final int TICKS_PER_REVOLUTION = 1440;

    /**
     * Provided values
     */
    private final double wheelBaseRadius, wheelRadius, driveGearRatio;
    private final boolean gyroEnabled, encodersEnabled;
    private final int dcMotorMaxSpeed;

    /**
     * Cached computed values, never change since the class is final
     */
    private final double wheelCircumference, wheelBaseCircumference, adjustedTicksPerRevolution;

    private RobotParameters(double wheelBaseRadius, double wheelRadius, double driveGearRatio, boolean gyroEnabled, boolean encodersEnabled, int dcMotorMaxSpeed) {
        this.wheelBaseRadius = wheelBaseRadius;
        this.wheelRadius = wheelRadius;
        this.driveGearRatio = driveGearRatio;
        this.gyroEnabled = gyroEnabled;
        this.encodersEnabled = encodersEnabled;
        this.dcMotorMaxSpeed = dcMotorMaxSpeed;

        this.wheelCircumference = wheelRadius * 2 * FastMath.PI;
        this.wheelBaseCircumference = wheelBaseRadius * 2 * FastMath.PI;
        this.adjustedTicksPerRevolution = TICKS_PER_REVOLUTION / driveGearRatio;
    }

    public double getWheelBaseRadius() {
        return wheelBaseRadius;
    }

    public double getWheelRadius() {
        return wheelRadius;
    }

    public double getAdjustedTicksPerRevolution() {
        return adjustedTicksPerRevolution;
    }

    public double getWheelCircumference() {
        return wheelCircumference;
    }

    public double getWheelBaseCircumference() {
        return wheelBaseCircumference;
    }

    public double getDriveGearRatio() {
        return driveGearRatio;
    }

    public boolean isGyroEnabled() {
        return gyroEnabled;
    }

    public boolean areEncodersEnabled() {
        return encodersEnabled;
    }

    public int getDcMotorMaxSpeed() {
        return dcMotorMaxSpeed;
    }

    public static class Builder {
        private double wheelBaseRadius;
        private double wheelRadius;
        private double driveGearRatio = 1;
        private boolean gyroEnabled = false;
        private boolean encodersEnabled = false;
        private int dcMotorMaxSpeed = TICKS_PER_REVOLUTION * 2;

        public Builder() {
        }

        public Builder wheelBaseRadius(double wheelBaseRadius) {
            this.wheelBaseRadius = wheelBaseRadius;
            return this;
        }

        public Builder wheelRadius(double wheelRadius) {
            this.wheelRadius = wheelRadius;
            return this;
        }

        public Builder driveGearRatio(double driveGearRatio) {
            this.driveGearRatio = driveGearRatio;
            return this;
        }

        public Builder gyroEnabled(boolean gyroEnabled) {
            this.gyroEnabled = gyroEnabled;
            return this;
        }

        public Builder encodersEnabled(boolean encodersEnabled) {
            this.encodersEnabled = encodersEnabled;
            return this;
        }

        public Builder dcMotorMaxSpeed(int dcMotorMaxSpeed) {
            this.dcMotorMaxSpeed = dcMotorMaxSpeed;
            return this;
        }

        public RobotParameters build() {
            if (wheelBaseRadius <= 0)
                throw new IllegalArgumentException("wheelBaseRadius must be > 0");
            if (wheelRadius <= 0)
                throw new IllegalArgumentException("wheelRadius must be > 0");
            if (dcMotorMaxSpeed <= 0 && encodersEnabled)
                throw new IllegalArgumentException("dcMotorMaxSpeed must be > 0 when encoders are used");
            return new RobotParameters(wheelBaseRadius, wheelRadius, driveGearRatio, gyroEnabled, encodersEnabled, dcMotorMaxSpeed);
        }
    }
}
