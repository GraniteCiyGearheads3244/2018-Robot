package org.mayheminc.robot2018.autonomousroutines;

import org.mayheminc.robot2018.commands.DriveStraightOnHeading;
import org.mayheminc.robot2018.commands.ElevatorArmSetMotor;
import org.mayheminc.robot2018.commands.ElevatorSetPosition;
import org.mayheminc.robot2018.commands.TurretMoveTo;
import org.mayheminc.robot2018.commands.DriveStraightOnHeading.DistanceUnits;
import org.mayheminc.robot2018.subsystems.Elevator;
import org.mayheminc.robot2018.subsystems.Turret;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
public class StartMidBackScaleLeft extends CommandGroup {

    public StartMidBackScaleLeft() {
    	addSequential(new TurretMoveTo(Turret.RIGHT_REAR));
    	
    	addSequential(new ElevatorSetPosition(Elevator.SWITCH_LOW));
    	
    	// drive backwards
    	addSequential(new DriveStraightOnHeading(-1.0, DistanceUnits.INCHES, 200.0, 110.0));

    	addSequential(new DriveStraightOnHeading(-1.0, DistanceUnits.INCHES, 200.0, 180.0));
    	
    	addSequential(new DriveStraightOnHeading(-0.3, DistanceUnits.INCHES, 50.0, 180.0));

    	// spit the cube 
    	addParallel(new ElevatorArmSetMotor(0.4));
    }
}
