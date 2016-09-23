package org.pattonvillerobotics.commoncode.robotclasses;

/**
 * Created by skaggsm on 9/22/16.
 */

public class RobotParameters {
    public static final int TICKS_PER_REVOLUTION = 1440;

    private final double wheelBaseRadius, wheelRadius, driveGearRatio;
    private final boolean gyroEnabled, encodersEnabled;

    private RobotParameters(double wheelBaseRadius, double wheelRadius, double driveGearRatio, boolean gyroEnabled, boolean encodersEnabled) {
        this.wheelBaseRadius = wheelBaseRadius;
        this.wheelRadius = wheelRadius;
        this.driveGearRatio = driveGearRatio;
        this.gyroEnabled = gyroEnabled;
        this.encodersEnabled = encodersEnabled;
    }

    public double getWheelBaseRadius() {
        return wheelBaseRadius;
    }

    public double getWheelRadius() {
        return wheelRadius;
    }

    public double getDriveGearRatio() {
        return driveGearRatio;
    }

    public boolean isGyroEnabled() {
        return gyroEnabled;
    }

    public boolean isEncodersEnabled() {
        return encodersEnabled;
    }

    public class Builder {
        private double wheelBaseRadius;
        private double wheelRadius;
        private double driveGearRatio = 1;
        private boolean gyroEnabled = false;
        private boolean encodersEnabled = false;

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

        public RobotParameters build() {
            if (wheelBaseRadius <= 0)
                throw new IllegalArgumentException("wheelBaseRadius must be > 0");
            if (wheelRadius <= 0)
                throw new IllegalArgumentException("wheelRadius must be > 0");
            return new RobotParameters(wheelBaseRadius, wheelRadius, driveGearRatio, gyroEnabled, encodersEnabled);
        }
    }
}
