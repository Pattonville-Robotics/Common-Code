package com.qualcomm.ftcrobotcontroller.robot_classes;

import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * Created by developer on 8/6/16.
 */
public class ToggleableGamePad extends Gamepad {

    private Gamepad gamepad1;
    public boolean buttonPressed, systemActive;

    public ToggleableGamePad(Gamepad gamepad){
        gamepad1 = gamepad;
    }

    public void toggleButton(boolean button,  Runnable inActive, Runnable active){

        if(button){
            if(!buttonPressed){
                if(!systemActive){
                    active.run();
                    systemActive = true;
                }
                else{
                    inActive.run();
                    systemActive = false;
                }
            }
            buttonPressed = true;
        }else{
           buttonPressed = false;
        }

    }

}
