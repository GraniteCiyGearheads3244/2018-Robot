package org.mayheminc.robot2018.autonomousroutines;

import org.mayheminc.robot2018.commands.DriveStraightOnHeading;
import org.mayheminc.robot2018.commands.ElevatorArmSetMotorAuto;
import org.mayheminc.robot2018.commands.ElevatorSetPosition;
import org.mayheminc.robot2018.commands.TurretMoveTo;
import org.mayheminc.robot2018.commands.Wait;
import org.mayheminc.robot2018.commands.DriveStraightOnHeading.DistanceUnits;
import org.mayheminc.robot2018.subsystems.Elevator;
import org.mayheminc.robot2018.subsystems.Turret;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
public class StartRightBackSwitchLeft extends CommandGroup {

	public StartRightBackSwitchLeft() {
		// start the turret to shift to the rear
		addSequential(new TurretMoveTo(Turret.RIGHT_POSITION));
		addSequential(new ElevatorSetPosition(Elevator.SWITCH_HEIGHT));

		// drive backwards
		addSequential(new DriveStraightOnHeading(-1.0, DistanceUnits.INCHES, 150.0, 180.0));

		// make the turn slower than full speed
		addSequential(new DriveStraightOnHeading(-0.5, DistanceUnits.INCHES, 75.0, 90.0));
		// drive down the alley.
		addSequential(new DriveStraightOnHeading(-1.0, DistanceUnits.INCHES, 200.0, 90.0));

		// drive to the switch
		addSequential(new DriveStraightOnHeading(0.3, DistanceUnits.INCHES, 50.0, 180.0));

		// wait for the robot to stop swaying
		addSequential(new Wait(0.5));

		// spit the cube 
		addParallel(new ElevatorArmSetMotorAuto(0.4));
	}
}