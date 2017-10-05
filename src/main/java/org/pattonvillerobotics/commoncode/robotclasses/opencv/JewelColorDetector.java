package org.pattonvillerobotics.commoncode.robotclasses.opencv;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Created by gregbahr on 9/26/17.
 */

public class JewelColorDetector extends ImageProcessor {

    public static final String TAG = JewelColorDetector.class.getSimpleName();

    public JewelColorDetector(HardwareMap hardwareMap) {
        super(hardwareMap);
    }
}
