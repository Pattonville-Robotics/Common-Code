package org.pattonvillerobotics.commoncode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;

import org.pattonvillerobotics.commoncode.enums.AllianceColor;
import org.pattonvillerobotics.commoncode.robot_classes.BeaconColorSensor;

/**
 * Created by developer on 9/10/16.
 */
public class BeaconColorSensorTest extends LinearOpMode {

    BeaconColorSensor beaconColorSensor;
    ColorSensor colorSensor;

    @Override
    public void runOpMode() throws InterruptedException {

        initialize();

        waitForStart();

        while(opModeIsActive()){

            beaconColorSensor.determineColor(AllianceColor.BLUE, new Runnable() {
                @Override
                public void run() {
                    telemetry.addData("RESULT", "Found Blue");
                }
            }, new Runnable() {
                @Override
                public void run() {
                    telemetry.addData("RESULT", "Found Red");
                }
            }, new Runnable() {
                @Override
                public void run() {
                    telemetry.addData("RESULT", "Found Nothing");
                }
            });

        }

    }

    public void initialize(){
        colorSensor = hardwareMap.colorSensor.get("color_sensor");
        beaconColorSensor = new BeaconColorSensor(colorSensor, false);
    }

}
