package com.laser.MAVLink;

import java.util.BitSet;

public class MAVLink_Sensors
{
    BitSet bitArray = new BitSet(32);

    public MAVLink_Sensors(){}

    public MAVLink_Sensors(int val) {
        bitArray = new BitSet(17);
        
        setGyro(val);
        setAccelerometer(val);
        setCompass(val);
        setBarometer(val);
        setDifferentialPressure(val);
        setGps(val);
        setOpticalFlow(val);
        setUnused7(val);
        setUnused8(val);
        setUnused9(val);
        setRateControl(val);
        setAttitudeStabilization(val);
        setYawPosition(val);
        setAltitudeControl(val);
        setXYPositionControl(val);
        setMotorControl(val);
        setRcReceiver(val);
    }
    
    public void setGyro(int val) { bitArray.set(0, (val & 1) == 0 ? false : true); }
    public void setAccelerometer(int val) { bitArray.set(1, (val & 2) == 0 ? false : true); }
    public void setCompass(int val) { bitArray.set(2, (val & 4) == 0 ? false : true); }
    public void setBarometer(int val) { bitArray.set(3, (val & 8) == 0 ? false : true); }
    public void setDifferentialPressure(int val) { bitArray.set(4, (val & 16) == 0 ? false : true); }
    public void setGps(int val) { bitArray.set(5, (val & 32) == 0 ? false : true); }
    public void setOpticalFlow(int val) { bitArray.set(6, (val & 64) == 0 ? false : true); }
    public void setUnused7(int val) { bitArray.set(7, (val & 128) == 0 ? false : true); }
    public void setUnused8(int val) { bitArray.set(8, (val & 256) == 0 ? false : true); }
    public void setUnused9(int val) { bitArray.set(9, (val & 512) == 0 ? false : true); }
    public void setRateControl(int val) { bitArray.set(10, (val & 1024) == 0 ? false : true); }
    public void setAttitudeStabilization(int val) { bitArray.set(11, (val & 2048) == 0 ? false : true); }
    public void setYawPosition(int val) { bitArray.set(12, (val & 4096) == 0 ? false : true); }
    public void setAltitudeControl(int val) { bitArray.set(13, (val & 8192) == 0 ? false : true); }
    public void setXYPositionControl(int val) { bitArray.set(14, (val & 16384) == 0 ? false : true); }
    public void setMotorControl(int val) { bitArray.set(15, (val & 32768) == 0 ? false : true); }
    public void setRcReceiver(int val) { bitArray.set(16, (val & 65636) == 0 ? false : true); }
    
    public boolean getGyro() { return bitArray.get(0); }
    public boolean getAccelerometer() { return bitArray.get(1); }
    public boolean getCompass() { return bitArray.get(2); }
    public boolean getBarometer() { return bitArray.get(3); }
    public boolean getDifferentialPressure() { return bitArray.get(4); }
    public boolean getGps() { return bitArray.get(5); }
    public boolean getOpticalFlow() { return bitArray.get(6); }
    public boolean getUnused7() { return bitArray.get(7); }
    public boolean getUnused8() { return bitArray.get(8); }
    public boolean getUnused9() { return bitArray.get(9); }
    public boolean getRateControl() { return bitArray.get(10); }
    public boolean getAttitudeStabilization() { return bitArray.get(11); }
    public boolean getYawPosition() { return bitArray.get(12); }
    public boolean getAltitudeControl() { return bitArray.get(13); }
    public boolean getXYPositionControl() { return bitArray.get(14); }
    public boolean getMotorControl() { return bitArray.get(15); }
    public boolean getRcReceiver() { return bitArray.get(16); }
    
}
