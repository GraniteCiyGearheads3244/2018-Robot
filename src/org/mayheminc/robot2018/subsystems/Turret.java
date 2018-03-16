package org.mayheminc.robot2018.subsystems;

import org.mayheminc.robot2018.Robot;
import org.mayheminc.robot2018.RobotMap;
import org.mayheminc.util.MayhemTalonSRX;
import org.mayheminc.util.PidTunerObject;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 */
public class Turret extends Subsystem implements PidTunerObject {

	// Turret Positions:
	//   Driven by a VersaPlanetary with BAG motor on a 63:1 gearbox and a VP Encoder (4096 cpr) on the output shaft.
	//   Experiment on 28 Feb 2018 shows approx 17000 counts per full 360-degree rotation.
	//   Desired range of rotation is -225 degrees to +225 degrees, which is approx -10000 counts to +10000 counts
	//   180 degrees is approximately 8500 counts.
	
	//   Encoder is set up so that + rotation is clockwise (right) when "forward" motor power is applied.

	public static final int FRONT_POSITION = 0;
	public static final int ZERO_POSITION = FRONT_POSITION;
	public static final int RIGHT_POSITION = 4250;
	public static final int LEFT_POSITION = -RIGHT_POSITION;

	public static final int RIGHT_ANGLED_BACK_POSITION = 8000;
	public static final int LEFT_ANGLED_BACK_POSITION = -RIGHT_ANGLED_BACK_POSITION;
	
	public static final int RIGHT_REAR = 8500;
	public static final int LEFT_REAR = -RIGHT_REAR;
	
	public static final int RIGHT_SAFETY_LIMIT = 10000;
	public static final int LEFT_SAFETY_LIMIT = -RIGHT_SAFETY_LIMIT;

	public static final int POSITION_TOLERANCE = 250; // 250 units is "close enough" to be at a position

	MayhemTalonSRX m_turretMotor = new MayhemTalonSRX(RobotMap.TURRET_TALON);
	boolean m_manualmode = true;
	int m_desiredPosition = 0;
	boolean m_fieldOriented = false;
	
    public void initDefaultCommand() { }
    
    public Turret()
    {
    	super();
    	
    	// initialize the PID controller
    	m_turretMotor.config_kP(0,  0.4,  0);
    	m_turretMotor.config_kI(0,  0.0,  0);
    	m_turretMotor.config_kD(0,  0.0,  0);
    	m_turretMotor.config_kF(0,  0.0, 0);
    	
    	m_turretMotor.selectProfileSlot(0,  0);
    	
    	m_turretMotor.setNeutralMode(NeutralMode.Coast);
    	m_turretMotor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
    	
		m_turretMotor.setInverted(true);
		m_turretMotor.setSensorPhase(false);
		
		m_turretMotor.configClosedloopRamp(0.25, 0);
		m_turretMotor.configOpenloopRamp(0.25,  0);
		
		m_turretMotor.setSelectedSensorPosition(m_turretMotor.getSelectedSensorPosition(0), 0, 0);
		m_turretMotor.configMotionAcceleration(1000,  0);
		
		m_turretMotor.configNominalOutputForward(0.0,  0);
		m_turretMotor.configNominalOutputReverse(0.0, 0);
		m_turretMotor.configPeakOutputForward(0.5,  0);
		m_turretMotor.configPeakOutputReverse(-0.5,  0); 
		
		m_turretMotor.configForwardSoftLimitThreshold(RIGHT_SAFETY_LIMIT, 0);
		m_turretMotor.configForwardSoftLimitEnable(true,  0);
		m_turretMotor.configReverseSoftLimitThreshold(LEFT_SAFETY_LIMIT, 0);
		m_turretMotor.configReverseSoftLimitEnable(true,  0);
		
    	m_turretMotor.enableControl();		
    }
    
    public boolean isAtPosition()
    {
    	return ( Math.abs(getCurrentPosition() - m_desiredPosition) < 250);
    }
    
    public int getCurrentPosition()
    {
    	return m_turretMotor.getSelectedSensorPosition(0);
    }
    
    public void setDesiredPosition(int position)
    {
    	System.out.println("Turret: setPosition" + position);
    	m_desiredPosition = position;
    	m_manualmode = false;
    }
    
    /**
     * Where ever the turret is, hold that position.
     */
    public void holdCurrentPosition()
    {
    	// "hold" current position is the same as a setPosition of the current position
    	setDesiredPosition(m_turretMotor.getSelectedSensorPosition(0));
    }
    
    public void zeroEncoder()
    {
    	m_turretMotor.setEncPosition(ZERO_POSITION);
    }
    
    public void periodic()
    {
    	double manualPowerRequested = Robot.oi.getTurretPower();
    	
    	// if the joystick is being commanded...
    	if (Math.abs(manualPowerRequested) > 0.01)
    	{
    		System.out.println("Turret: periodic: Power: " + manualPowerRequested);
    		m_manualmode = true;
    	}
    	
    	if ( m_manualmode )
    	{
    		m_turretMotor.set(ControlMode.PercentOutput, manualPowerRequested);
    	}
    	else if( m_fieldOriented )
    	{
    		// need to add checks for 0 to 360 degrees or -180 to 180
    		int robotHeading = (int)(Robot.drive.getHeading() + 360*4) % 360; // 0 to 360 // 360*4 is to make sure this is positive.
    		int desiredTurretHeading = m_desiredPosition * 90 / RIGHT_POSITION; // -180 to +180.  use 90 degrees is RIGHT_POSITION for calculation
    		int turretHeading = desiredTurretHeading - robotHeading; // -540 to 180
    		turretHeading += 720.0; // 180 to 900
    		turretHeading %= 360; // 0 to 360
    		// convert to -180 to 180
    		if( turretHeading > 180 )
    		{
    			// 181 ==> -179
    			// 270 ==?> -90
    			// 359 ==> -1
    			turretHeading = -(360-turretHeading); // -180 to 180
    		}
    		int turrentEncoder = (int) (turretHeading * RIGHT_POSITION / 90.0);
    		m_turretMotor.set(ControlMode.Position, turrentEncoder);
    	}
		else // PID mode is set in setPosition()
    	{
			m_turretMotor.set(ControlMode.Position, m_desiredPosition);
    	}
    }
    
    public void updateSmartDashboard()
    {
    	SmartDashboard.putNumber("Turret Pos", m_turretMotor.getPosition());
    	SmartDashboard.putNumber("Turret Power", m_turretMotor.getMotorOutputPercent());
    	SmartDashboard.putBoolean("Turret Manual Mode",  m_manualmode);
    }
    
    /////////////////////////////////////////////////////////
    // Implement PidTunerObject
    /////////////////////////////////////////////////////////
    
	public double getP()
	{
		return m_turretMotor.getP();
	}
	public double getI()
	{
		return m_turretMotor.getI();
	}
	public double getD()
	{
		return m_turretMotor.getD();
	}
	public double getF()
	{
		return m_turretMotor.getF();
	}
	public void setP(double d)
	{
		m_turretMotor.config_kP(0,  d,  0);
	}
	public void setI(double d)
	{
		m_turretMotor.config_kI(0,  d,  0);
	}
	public void setD(double d)
	{
		m_turretMotor.config_kD(0,  d,  0);
	}
	public void setF(double d)
	{
		m_turretMotor.config_kF(0,  d,  0);
	}
}

