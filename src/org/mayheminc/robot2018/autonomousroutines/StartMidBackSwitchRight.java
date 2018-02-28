package org.mayheminc.robot2018.autonomousroutines;

import org.mayheminc.robot2018.commands.DriveStraightOnHeading;
import org.mayheminc.robot2018.commands.ElevatorArmSetMotorAuto;
import org.mayheminc.robot2018.commands.ElevatorSetPosition;
import org.mayheminc.robot2018.commands.TurretMoveTo;
import org.mayheminc.robot2018.commands.DriveStraightOnHeading.DistanceUnits;
import org.mayheminc.robot2018.subsystems.Elevator;
import org.mayheminc.robot2018.subsystems.Turret;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
public class StartMidBackSwitchRight extends CommandGroup {

    public StartMidBackSwitchRight() {
    	addSequential(new TurretMoveTo(Turret.RIGHT_POSITION));
    	
    	addSequential(new ElevatorSetPosition(Elevator.SWITCH_LOW));
    	
    	// drive backwards
    	addSequential(new DriveStraightOnHeading(-1.0, DistanceUnits.INCHES, 200.0, 250.0));

    	addSequential(new DriveStraightOnHeading(-1.0, DistanceUnits.INCHES, 200.0, 170.0));
    	
       	// spit the cube 
    	addParallel(new ElevatorArmSetMotorAuto(0.4));
    }
}
