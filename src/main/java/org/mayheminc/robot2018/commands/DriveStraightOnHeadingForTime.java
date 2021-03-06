package org.mayheminc.robot2018.commands;

import org.mayheminc.robot2018.Robot;
import org.mayheminc.robot2018.subsystems.Drive;

import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class DriveStraightOnHeadingForTime extends Command {

	double m_targetPower;
	double m_desiredDisplacement;
	double m_desiredHeading;
	public enum DistanceUnits { ENCODER_TICKS, INCHES };

	public DriveStraightOnHeadingForTime(double arg_targetSpeed, double arg_distance, double heading) {
		this(arg_targetSpeed, DistanceUnits.INCHES, arg_distance, heading);
	}
	/**
	 * 
	 * @param arg_targetPower +/- motor power [-1.0, +1.0]
	 * @param arg_distance Distance in encoder counts
	 */
	public DriveStraightOnHeadingForTime(double arg_targetSpeed, DistanceUnits units, double arg_distance, double heading) {
		
		if (units == DistanceUnits.INCHES) {
			arg_distance = arg_distance / Drive.DISTANCE_PER_PULSE;
		}
		m_targetPower = arg_targetSpeed;
		m_desiredDisplacement = Math.abs(arg_distance);   
		m_desiredHeading = heading;
	}

	// Called just before this Command runs the first time
	protected void initialize() {
		Robot.drive.saveInitialWheelDistance();
		Robot.drive.setDesiredHeading(m_desiredHeading);
		System.out.println("Starting Routine: Drive Straight On Heading");
	}

	// Called repeatedly when this Command is scheduled to run
	protected void execute() {
		Robot.drive.speedRacerDrive(m_targetPower, 0, false);
	}

	// Make this return true when this Command no longer needs to run execute()
	protected boolean isFinished() {
		int displacement = (int)Robot.drive.getWheelDistance();
		
		displacement = Math.abs(displacement);
		System.out.println("displacement" + displacement);
		return (displacement >= m_desiredDisplacement);
	}

	// Called once after isFinished returns true
	protected void end() {
		Robot.drive.stop();    	
	}

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
	protected void interrupted() {
		Robot.drive.stop();
	}
}
