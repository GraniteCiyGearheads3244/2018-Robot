package org.mayheminc.robot2018.commands;

import org.mayheminc.robot2018.Robot;

import edu.wpi.first.wpilibj.command.InstantCommand;

/**
 *
 */
public class PivotZeroEncoder extends InstantCommand {

    public PivotZeroEncoder() {
        super();
        // Use requires() here to declare subsystem dependencies
        // eg. requires(chassis);
    	setRunWhenDisabled(true);

    }

    // Called once when the command executes
    protected void initialize() {
    	Robot.pivot.zeroPivot();
    }

}