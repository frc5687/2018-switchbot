package org.frc5687.switchbot.robot.subsystems;


import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.frc5687.switchbot.robot.Constants;
import org.frc5687.switchbot.robot.Robot;
import org.frc5687.switchbot.robot.RobotMap;

public class Shifter extends Subsystem {
    private Robot _robot;

    private DoubleSolenoid shifterSolenoid;
    private Compressor compressor;
    private long waitPeriodEndTime = 0;
    private boolean autShiftEnabled = false;

    private Solenoid _red;
    private Solenoid _green;
    private Solenoid _blue;

    public Shifter(Robot robot) {
        _robot = robot;
        shifterSolenoid = new DoubleSolenoid(RobotMap.PCM.SHIFTER_HIGH, RobotMap.PCM.SHIFTER_LOW);
        _red = new Solenoid(RobotMap.PCM.RED_LED);
        _green = new Solenoid(RobotMap.PCM.GREEN_LED);
        _blue = new Solenoid(RobotMap.PCM.BLUE_LED);
    }

    @Override
    protected void initDefaultCommand() {
    }

    public void shift(Gear gear, boolean auto) {
        shifterSolenoid.set(gear.getSolenoidValue());
        waitPeriodEndTime = System.currentTimeMillis() + (auto ? Constants.Shifter.AUTO_WAIT_PERIOD : Constants.Shifter.MANUAL_WAIT_PERIOD);
        if (gear==Gear.HIGH) {
            if (!DriverStation.getInstance().isAutonomous()) {
                _robot.getOI().rumbleRight();
            }
            _green.set(false);
            _red.set(true);
            _blue.set(true);
        }
        if (gear==Gear.LOW) {
            if (!DriverStation.getInstance().isAutonomous()) {
                _robot.getOI().rumbleLeft();
            }
            _green.set(false);
            _red.set(true);
            _blue.set(false);
        }
    }

    public boolean waitPeriodElapsed() {
        return System.currentTimeMillis() > waitPeriodEndTime;
    }

    public Gear getGear() {
        DoubleSolenoid.Value current = shifterSolenoid.get();
        if (current==Gear.HIGH.getSolenoidValue()) {
            return Gear.HIGH;
        } else if (current== Gear.LOW.getSolenoidValue()) {
            return Gear.LOW;
        }
        return Gear.UNKNOWN;
    }

    public boolean isCompressorAtPressure() {
        return compressor.getPressureSwitchValue();
    }

    public boolean isCommpressorEnabled() {
        return compressor.enabled();
    }

    public double getCompressorCurrent() {
        return compressor.getCompressorCurrent();
    }

    public void updateDashboard() {
        SmartDashboard.putString("shifter/Gear", getGear()==Gear.HIGH ? "High" : (getGear() == Gear.LOW ? "Low" : "Unknown"));
    }

    public enum Gear {
        UNKNOWN(DoubleSolenoid.Value.kOff),
        HIGH(DoubleSolenoid.Value.kReverse),
        LOW(DoubleSolenoid.Value.kForward);

        private DoubleSolenoid.Value solenoidValue;

        Gear(DoubleSolenoid.Value solenoidValue) {
            this.solenoidValue = solenoidValue;
        }

        public DoubleSolenoid.Value getSolenoidValue() {
            return solenoidValue;
        }

    }

    public boolean isAutShiftEnabled() {
        return autShiftEnabled;
    }

    public void setAutShiftEnabled(boolean enabled) {
        autShiftEnabled = enabled;
    }

}
