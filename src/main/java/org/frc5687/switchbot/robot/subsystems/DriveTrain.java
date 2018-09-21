package org.frc5687.switchbot.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.kauailabs.navx.IMUProtocol;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.frc5687.switchbot.robot.Constants;
import org.frc5687.switchbot.robot.Robot;
import org.frc5687.switchbot.robot.RobotMap;
import org.frc5687.switchbot.robot.commands.AllDrive;
import org.frc5687.switchbot.robot.commands.ArcadeDrive;
import org.frc5687.switchbot.robot.commands.TankDrive;
import sun.util.resources.cldr.or.CalendarData_or_IN;

import static org.frc5687.switchbot.robot.utils.Helpers.limit;

/**
 * Created by Ben Bernard on 6/4/2018.
 */
public class DriveTrain extends Subsystem  implements PIDSource {
    // Add the objects for the motor controllers

    // Left side needs one TalonSRX master and two VictorSPX followers
    TalonSRX _leftMaster;
    VictorSPX _leftFollowerA;
    VictorSPX _leftFollowerB;

    // And right side needs one TalonSRX master and two VictorSPX followers
    TalonSRX _rightMaster;
    VictorSPX _rightFollowerA;
    VictorSPX _rightFollowerB;

    private Robot _robot;
    private DriveMode _driveMode = DriveMode.ARCADE;
    public AHRS _imu;

    public DriveTrain(Robot robot) {
        _robot = robot;
        _imu = robot.getIMU();

        // Motor Initialization
        _leftMaster = new TalonSRX(RobotMap.CAN.LEFT_MASTER);
        _leftFollowerA = new VictorSPX(RobotMap.CAN.LEFT_FOLLOWER_A);
        _leftFollowerB = new VictorSPX(RobotMap.CAN.LEFT_FOLLOWER_B);

        _rightMaster = new TalonSRX(RobotMap.CAN.RIGHT_MASTER);
        _rightFollowerA = new VictorSPX(RobotMap.CAN.RIGHT_FOLLOWER_A);
        _rightFollowerB = new VictorSPX(RobotMap.CAN.RIGHT_FOLLOWER_B);
        
        // Setup followers to follow their master
        _leftFollowerA.follow(_leftMaster);
        _leftFollowerB.follow(_leftMaster);
        
        _rightFollowerA.follow(_rightMaster);
        _rightFollowerB.follow(_rightMaster);

        // Setup motors
        _leftMaster.configPeakOutputForward(Constants.DriveTrain.HIGH_POW, 0);
        _leftFollowerA.configPeakOutputForward(Constants.DriveTrain.HIGH_POW, 0);
        _leftFollowerB.configPeakOutputForward(Constants.DriveTrain.HIGH_POW, 0);

        _rightMaster.configPeakOutputForward(Constants.DriveTrain.HIGH_POW, 0);
        _rightFollowerA.configPeakOutputForward(Constants.DriveTrain.HIGH_POW, 0);
        _rightFollowerB.configPeakOutputForward(Constants.DriveTrain.HIGH_POW, 0);

        _leftMaster.configPeakOutputReverse(Constants.DriveTrain.LOW_POW, 0);
        _leftFollowerA.configPeakOutputReverse(Constants.DriveTrain.LOW_POW, 0);
        _leftFollowerB.configPeakOutputReverse(Constants.DriveTrain.LOW_POW, 0);

        _rightMaster.configPeakOutputReverse(Constants.DriveTrain.LOW_POW, 0);
        _rightFollowerA.configPeakOutputReverse(Constants.DriveTrain.LOW_POW, 0);
        _rightFollowerB.configPeakOutputReverse(Constants.DriveTrain.LOW_POW, 0);


        _leftMaster.configNominalOutputForward(0.0, 0);
        _leftFollowerA.configNominalOutputForward(0.0, 0);
        _leftFollowerB.configNominalOutputForward(0.0, 0);
        _rightMaster.configNominalOutputForward(0.0, 0);
        _rightFollowerA.configNominalOutputForward(0.0, 0);
        _rightFollowerB.configNominalOutputForward(0.0, 0);

        _leftMaster.configNominalOutputReverse(0.0, 0);
        _leftFollowerA.configNominalOutputReverse(0.0, 0);
        _leftFollowerB.configNominalOutputReverse(0.0, 0);
        _rightMaster.configNominalOutputReverse(0.0, 0);
        _rightFollowerA.configNominalOutputReverse(0.0, 0);
        _rightFollowerB.configNominalOutputReverse(0.0, 0);

        _leftMaster.setInverted(Constants.DriveTrain.LEFT_MOTORS_INVERTED);
        _leftFollowerA.setInverted(Constants.DriveTrain.LEFT_MOTORS_INVERTED);
        _leftFollowerB.setInverted(Constants.DriveTrain.LEFT_MOTORS_INVERTED);
        _rightMaster.setInverted(Constants.DriveTrain.RIGHT_MOTORS_INVERTED);
        _rightFollowerA.setInverted(Constants.DriveTrain.RIGHT_MOTORS_INVERTED);
        _rightFollowerB.setInverted(Constants.DriveTrain.RIGHT_MOTORS_INVERTED);

        // Configure the encoders
        _leftMaster.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
        _rightMaster.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
        resetDriveEncoders();


    }


    @Override
    protected void initDefaultCommand() {
        setDefaultCommand(new AllDrive(this, _robot.getOI()));
    }

    public void setPower(double leftSpeed, double rightSpeed) {
        setPower(leftSpeed, rightSpeed, false);
    }
    public void setPower(double leftSpeed, double rightSpeed, boolean override) {
        try {
            _leftMaster.set(ControlMode.PercentOutput, leftSpeed);
            _rightMaster.set(ControlMode.PercentOutput, rightSpeed);
        } catch (Exception e) {
            DriverStation.reportError("DriveTrain.setPower exception: " + e.toString(), false);
        }
        SmartDashboard.putNumber("DriveTrain/PowerRight", rightSpeed);
        SmartDashboard.putNumber("DriveTrain/PowerLeft", leftSpeed);
    }


    public void resetDriveEncoders() {
        try {
            _leftMaster.setSelectedSensorPosition(0,0,0);
            _rightMaster.setSelectedSensorPosition(0, 0, 0);
        } catch (Exception e) {
            DriverStation.reportError("DriveTrain.resetDriveEncoders exception. I suppose this is really bad. : " + e.toString(), false);
        }
    }


    public void arcadeDrive(double speed, double rotation) {
        SmartDashboard.putNumber("DriveTrain/Speed", speed);
        SmartDashboard.putNumber("DriveTrain/Rotation", rotation);

        speed = limit(speed);

        rotation = limit(rotation);

        // Square the inputs (while preserving the sign) to increase fine control
        // while permitting full power.
        speed = Math.copySign(speed * speed, speed);
        rotation = Math.copySign(rotation * rotation, rotation);

        double leftMotorOutput;
        double rightMotorOutput;

        double maxInput = Math.copySign(Math.max(Math.abs(speed), Math.abs(rotation)), speed);

        if (speed >= 0.0) {
            // First quadrant, else second quadrant
            if (rotation >= 0.0) {
                leftMotorOutput = maxInput;
                rightMotorOutput = speed - rotation;
            } else {
                leftMotorOutput = speed + rotation;
                rightMotorOutput = maxInput;
            }
        } else {
            // Third quadrant, else fourth quadrant
            if (rotation >= 0.0) {
                leftMotorOutput = speed + rotation;
                rightMotorOutput = maxInput;
            } else {
                leftMotorOutput = maxInput;
                rightMotorOutput = speed - rotation;
            }
        }

        setPower(leftMotorOutput, rightMotorOutput);
    }

    public void tankDrive(double leftSpeed, double rightSpeed, boolean overrideCaps) {
        SmartDashboard.putNumber("DriveTrain/LeftSpeed", leftSpeed);
        SmartDashboard.putNumber("DriveTrain/RightSpeed", rightSpeed);
        double leftMotorOutput = leftSpeed;
        double rightMotorOutput = rightSpeed;



        setPower(leftMotorOutput, rightMotorOutput);
    }

    public float getYaw() {
        return _imu.getYaw();
    }

    /**
     * Get the number of ticks since the last reset
     * @return
     */
    public long getLeftTicks() {
        return _leftMaster.getSelectedSensorPosition(0);
    }
    public long getRightTicks() {
        return _rightMaster.getSelectedSensorPosition(0);
    }

    /**
     * The left distance in Inches since the last reset.
     * @return
     */
    public double getLeftDistance() {
        return getLeftTicks() * Constants.Encoders.INCHES_PER_PULSE;
    }
    public double getRightDistance() {
        return getRightTicks() * Constants.Encoders.INCHES_PER_PULSE;
    }


    /**
     * @return average of leftDistance and rightDistance
     */
    public double getDistance() {
        if (Math.abs(getRightTicks())<10) {
            return getLeftDistance();
        }
        if (Math.abs(getLeftTicks())<10) {
            return getRightDistance();
        }
        return (getLeftDistance() + getRightDistance()) / 2;
    }




    /*
    public void curvaturerive(double speed, double rotation, boolean isQuickTurn) {

        speed = limit(speed);

        rotation = limit(rotation);

        double angularPower;
        boolean overPower;

        if (isQuickTurn) {
            if (Math.abs(speed) < m_quickStopThreshold) {
                m_quickStopAccumulator = (1 - m_quickStopAlpha) * m_quickStopAccumulator
                        + m_quickStopAlpha * limit(zRotation) * 2;
            }
            overPower = true;
            angularPower = zRotation;
        } else {
            overPower = false;
            angularPower = Math.abs(xSpeed) * zRotation - m_quickStopAccumulator;

            if (m_quickStopAccumulator > 1) {
                m_quickStopAccumulator -= 1;
            } else if (m_quickStopAccumulator < -1) {
                m_quickStopAccumulator += 1;
            } else {
                m_quickStopAccumulator = 0.0;
            }
        }

        double leftMotorOutput = xSpeed + angularPower;
        double rightMotorOutput = xSpeed - angularPower;

        // If rotation is overpowered, reduce both outputs to within acceptable range
        if (overPower) {
            if (leftMotorOutput > 1.0) {
                rightMotorOutput -= leftMotorOutput - 1.0;
                leftMotorOutput = 1.0;
            } else if (rightMotorOutput > 1.0) {
                leftMotorOutput -= rightMotorOutput - 1.0;
                rightMotorOutput = 1.0;
            } else if (leftMotorOutput < -1.0) {
                rightMotorOutput -= leftMotorOutput + 1.0;
                leftMotorOutput = -1.0;
            } else if (rightMotorOutput < -1.0) {
                leftMotorOutput -= rightMotorOutput + 1.0;
                rightMotorOutput = -1.0;
            }
        }

        // Normalize the wheel speeds
        double maxMagnitude = Math.max(Math.abs(leftMotorOutput), Math.abs(rightMotorOutput));
        if (maxMagnitude > 1.0) {
            leftMotorOutput /= maxMagnitude;
            rightMotorOutput /= maxMagnitude;
        }

        m_leftMotor.set(leftMotorOutput * m_maxOutput);
        m_rightMotor.set(-rightMotorOutput * m_maxOutput);

        m_safetyHelper.feed();
    }
*/

    public void setCurrentLimiting(int amps) {
        _leftMaster.configContinuousCurrentLimit(amps, 0);
        _leftMaster.configPeakCurrentLimit(0, 0);
        _rightMaster.configContinuousCurrentLimit(amps, 0);
        _rightMaster.configPeakCurrentLimit(0, 0);
    }


    public double getLeftSpeed() {
        return _leftMaster.getMotorOutputPercent() / Constants.DriveTrain.HIGH_POW;
    }

    public double getRightSpeed() {
        return _rightMaster.getMotorOutputPercent() / Constants.DriveTrain.HIGH_POW;
    }

    public double getLeftRate() {
        return _leftMaster.getSelectedSensorVelocity(0);
    }

    public double getRightRate() {
        return _rightMaster.getSelectedSensorVelocity(0);
    }

    public DriveMode getDriveMode() { return _driveMode; }

    @Override
    public double pidGet() {
        return getDistance();
    }

    @Override
    public PIDSourceType getPIDSourceType() {
        return PIDSourceType.kDisplacement;
    }

    @Override
    public void setPIDSourceType(PIDSourceType pidSource) {
    }

    public void setDriveMode(DriveMode driveMode) { _driveMode = driveMode; }

    public void enableBrakeMode() {
        try {
            _leftMaster.setNeutralMode(NeutralMode.Brake);
            _leftFollowerA.setNeutralMode(NeutralMode.Brake);
            _leftFollowerB.setNeutralMode(NeutralMode.Brake);

            _rightMaster.setNeutralMode(NeutralMode.Brake);
            _rightFollowerA.setNeutralMode(NeutralMode.Brake);
            _rightFollowerB.setNeutralMode(NeutralMode.Brake);

        } catch (Exception e) {
            DriverStation.reportError("DriveTrain.enableBrakeMode exception: " + e.toString(), false);
        }
        SmartDashboard.putString("DriveTrain/neutralMode", "Brake");
    }

    public void enableCoastMode() {
        try {
            _leftMaster.setNeutralMode(NeutralMode.Coast);
            _leftFollowerA.setNeutralMode(NeutralMode.Coast);
            _leftFollowerB.setNeutralMode(NeutralMode.Coast);
            _rightMaster.setNeutralMode(NeutralMode.Coast);
            _rightFollowerA.setNeutralMode(NeutralMode.Coast);
        } catch (Exception e) {
            DriverStation.reportError("DriveTrain.enableCoastMode exception: " + e.toString(), false);
        }
        SmartDashboard.putString("DriveTrain/neutralMode", "Coast");
    }



    public enum DriveMode {
        TANK(0),
        ARCADE(1),
        ARC(2);

        private int _value;

        DriveMode(int value) {
            this._value = value;
        }

        public int getValue() {
            return _value;
        }

    }

    public void updateDashboard() {
        SmartDashboard.putNumber("Drivetrain/LeftDistance", getLeftDistance());
        SmartDashboard.putNumber("Drivetrain/RIghtDistance", getRightDistance());
        SmartDashboard.putNumber("Drivetrain/LeftRate", getLeftRate());
        SmartDashboard.putNumber("Drivetrain/RightRate", getRightRate());
        SmartDashboard.putNumber("Drivetrain/LeftSpeed", getLeftSpeed());
        SmartDashboard.putNumber("Drivetrain/RightSpeed", getRightSpeed());
    }

}
