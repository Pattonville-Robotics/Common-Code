package org.pattonvillerobotics.commoncode.robotclasses;

/**
 * Created by skaggsm on 9/23/16.
 */

public interface Drive {

    void telemetry(String tag, String message);

    void sleep(long milli) throws InterruptedException;

    void moveFreely(double left_power, double right_power);

    void stop();
}
