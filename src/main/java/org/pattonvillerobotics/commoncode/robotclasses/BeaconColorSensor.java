package org.pattonvillerobotics.commoncode.robotclasses;

import com.qualcomm.robotcore.hardware.ColorSensor;

import org.pattonvillerobotics.commoncode.enums.AllianceColor;
import org.pattonvillerobotics.commoncode.enums.ColorSensorColor;

/**
 * Created by Josh Zahner on 9/10/16.
 */
public class BeaconColorSensor {

    private ColorSensor colorSensor;

    public BeaconColorSensor(ColorSensor colorSensor){

        this.colorSensor = colorSensor;
        this.colorSensor.enableLed(false);

    }

    public void determineColor(AllianceColor allianceColor, Runnable ifPositiveID, Runnable ifNegativeID, Runnable ifNeither){

        ColorSensorColor dominantColor = dominantColor();

        switch(allianceColor){
            case BLUE:

                if(dominantColor == ColorSensorColor.BLUE){
                    ifPositiveID.run();
                }else if(dominantColor == ColorSensorColor.RED){
                    ifNegativeID.run();
                }else{
                    ifNeither.run();
                }

                break;

            case RED:

                if(dominantColor == ColorSensorColor.RED){
                    ifPositiveID.run();
                }else if(dominantColor == ColorSensorColor.BLUE){
                    ifNegativeID.run();
                }else{
                    ifNeither.run();
                }

                break;

        }

    }

    public int red(){
        return colorSensor.red();
    }

    public int green(){
        return colorSensor.green();
    }

    public int blue(){
        return colorSensor.blue();
    }

    public int alpha(){
        return colorSensor.alpha();
    }

    public ColorSensorColor dominantColor(){

        if(blue() > red() && blue() > green()){
            return ColorSensorColor.BLUE;

        }else if(red() > blue() && red() > green()){
            return ColorSensorColor.RED;

        }else if(green() > blue() && green() > red()){
            return ColorSensorColor.GREEN;

        }else{
            return null;

        }

    }





}
