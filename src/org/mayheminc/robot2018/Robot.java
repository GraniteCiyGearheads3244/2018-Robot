
package org.mayheminc.robot2018;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.networktables.*;

import org.mayheminc.robot2018.commands.RunAutonomous;
import org.mayheminc.robot2018.subsystems.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot { //FRCWaitsForIterativeRobot
	static NetworkTable table;

	public static final boolean DEBUG = true;
	public static final boolean PRACTICE_BOT = false;
	
	// create commands to be invoked for autonomous and teleop
	private Command autonomousCommand;
	private Runtime runtime = Runtime.getRuntime();

	// create the robot subsystems
	public static final Compressor compressor = new Compressor();
	public static final Drive drive = new Drive();
	public static final PowerDistributionPanel pdp = new PowerDistributionPanel();
	public static Claw claw = new Claw();
	public static Lifter lifter = new Lifter();
	public static Launcher launcher = new Launcher();
	public static Arm arm = new Arm();
	// allocate the "virtual" subsystems; wait to construct these until
	// robotInit()
	public static Autonomous autonomous;
	public static OI oi;

	// autonomous start time monitoring
	private static long autonomousStartTime;
	private static boolean printAutoElapsedTime = false;

	// timer for the match, will rumble when twenty seconds are left
	public static final Timer rumbleTimer = new Timer();
	public static final Timer brownoutTimer = new Timer();

	public static final double BROWNOUT_VOLTAGE_LOWER_THRESHOLD = 10.0;
	public static final double BROWNOUT_VOLTAGE_UPPER_THRESHOLD = 11.0;
	public static final double BROWNOUT_REST_PERIOD = 3;
	public static boolean brownoutMode = false;

	public static final boolean UPDATE_AUTO_SETUP_FIELDS = true;
	public static final boolean DONT_UPDATE_AUTO_SETUP_FIELDS = false;

	public Robot() {

	}

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	public void robotInit() {

		table = NetworkTable.getTable("datatable");

		System.out.println("robotInit");
		SmartDashboard.putString("Auto Prog", "Initializing...");

		System.out.println("About to DriverStation.reportError for 'Initializing...'");
		DriverStation.reportError("Initializing... ", false);
		System.out.println("Done with DriverStation.reportError for 'Initializing...'");

		DriverStation.reportError("About to construct Autonomous Subsystem... \n", false);
		autonomous = new Autonomous();
		DriverStation.reportError("Done constructing Autonomous Subsystem.\n", false);

		DriverStation.reportError("About to construct OI... \n", false);
		oi = new OI();
		DriverStation.reportError("Done constructing OI.\n", false);

		// initialize the subsystems that need it
		drive.init();

		// instantiate the command used for the autonomous period
		autonomousCommand = new RunAutonomous();
		DriverStation.reportError("Constructed auto command.\n", false);
		SmartDashboard.putString("Auto Prog", "Done.");
		Autonomous.updateSmartDashboard();
	}

	/**
	 * This function is called when the disabled button is hit. You can use it
	 * to reset subsystems before shutting down.
	 */
	public void disabledInit() {
		if (printAutoElapsedTime) {
			double autonomousTimeElapsed = (double) (System.currentTimeMillis() - autonomousStartTime) / 1000.0;
			DriverStation.reportError("Autonomous Time Elapsed: " + autonomousTimeElapsed + "\n", false);
			printAutoElapsedTime = false;
		}
	}

	public void disabledPeriodic() {
		// update sensors that need periodic update
		updateImgResults();	
		
		Scheduler.getInstance().run();

		// update Smart Dashboard, including fields for setting up autonomous operation
		updateSmartDashboard(UPDATE_AUTO_SETUP_FIELDS);
		Robot.drive.updateHistory();
	}

	public void autonomousInit() {
		//force low gear
		drive.setShifter(drive.LOW_GEAR);
		// turn off the compressor
		compressor.stop();

		drive.zeroHeadingGyro();
		
		// schedule the autonomous command (example)
		if (autonomousCommand != null) {
			autonomousCommand.start();
		}
		autonomousStartTime = System.currentTimeMillis();
		printAutoElapsedTime = true;

        launcher.treatAutonomousStartAsCalibrated();
        
		DriverStation.reportError(
				"AutonomousTimeRemaining from autonomousInit = " + Robot.autonomousTimeRemaining() + "\n", false);
	}

	public static double autonomousTimeRemaining() {
		double autonomousTimeElapsed = (double) (System.currentTimeMillis() - autonomousStartTime) / 1000.0;
		return (15.0 - autonomousTimeElapsed);
	}

	/**
	 * This function is called periodically during autonomous
	 */
	public void autonomousPeriodic() {
		// update sensors that need periodic update
		updateImgResults();	
		
		Scheduler.getInstance().run();

		// update subsystems that need periodic update
		arm.periodic();
		launcher.periodic();
		
		updateSmartDashboard(DONT_UPDATE_AUTO_SETUP_FIELDS);
		Robot.drive.updateHistory();
	}

	public void teleopInit() {
		// turn off the compressor
		compressor.start();

		// This makes sure that the autonomous stops running when
		// teleop starts running. If you want the autonomous to
		// continue until interrupted by another command, remove
		// this line or comment it out.
		if (autonomousCommand != null) {
			autonomousCommand.cancel();
		}

		DriverStation.reportError("Entering Teleop.\n", false);
		rumbleTimer.reset();
		rumbleTimer.start();

		drive.zeroHeadingGyro();
	}

	/**
	 * This function is called periodically during operator control
	 */
	boolean teleopOnce = false;

	public void teleopPeriodic() {

		if (!teleopOnce) {
			DriverStation.reportError("Teleop Periodic is running!", false);
		}
		teleopOnce = true;
		
		// update sensors that need periodic update
		updateImgResults();	
		
		Scheduler.getInstance().run();

		if (!oi.autoInTeleop()) {
			if (drive.isSpeedRacerDrive()) {
				drive.speedRacerDrive(oi.driveThrottle(), oi.steeringX(), oi.quickTurn());
			} else {
				drive.tankDrive(oi.tankDriveLeft(), oi.tankDriveRight());
			}
		} else {
//			DriverStation.reportError("Running Command: " + drive.getCurrentCommand(), false);
		}

		// update subsystems that need periodic update
		arm.periodic();
		launcher.periodic();

		updateSmartDashboard(DONT_UPDATE_AUTO_SETUP_FIELDS);

		Robot.drive.updateAutoShift();
		Robot.drive.updateHistory();
	}

	public void updateGRIP() {
		double[] defaultValue = new double[0];
		double[] areas = table.getNumberArray("area", defaultValue);
		for (double area : areas) {
			SmartDashboard.putNumber("grip_area", area);
			DriverStation.reportError("GRIP Contour Report" + area, false);
		}
	}

	public static boolean getBrownoutMode() {
		return brownoutMode;
	}

	/**
	 * This function is called periodically during test mode
	 */
	public void testPeriodic() {
		LiveWindow.run();
	}

	private long SMART_DASHBOARD_UPDATE_INTERVAL = 250;
	private long nextSmartDashboardUpdate = System.currentTimeMillis();

	public void updateSmartDashboard(boolean updateAutoFields) {
		try {
			if (System.currentTimeMillis() > nextSmartDashboardUpdate) {
				// display free memory for the JVM
				double freeMemoryInKB = runtime.freeMemory() / 1024;
				SmartDashboard.putNumber("Free Memory", freeMemoryInKB);

				SmartDashboard.putNumber("Battery Voltage", pdp.getVoltage());
				SmartDashboard.putBoolean("FRC Comm Checked In", oi.IsCheckedInWithFieldManagement());

				// display mode information
				// SmartDashboard.putBoolean("Is Teleop",
				// DriverStation.getInstance().isOperatorControl());
				// SmartDashboard.putBoolean("Is Autonomous",
				// DriverStation.getInstance().isAutonomous());
				// SmartDashboard.putBoolean("Is Enabled",
				// DriverStation.getInstance().isEnabled());

				// display interesting OI information
				// SmartDashboard.putNumber("DriveX", oi.driveX());
				// SmartDashboard.putNumber("DriveY", oi.driveY());
				// SmartDashboard.putNumber("DriveRotation",
				// oi.driveRotation());

				drive.updateSmartDashboard();
				arm.updateSmartDashboard();
				launcher.updateSmartDashboard();

				if (updateAutoFields) {
					Autonomous.updateSmartDashboard();
				}
				SmartDashboard.putNumber("Frame Number", getFrameNum());
				SmartDashboard.putNumber("Center of Target", getCenterX());

				nextSmartDashboardUpdate += SMART_DASHBOARD_UPDATE_INTERVAL;
			}
		} catch (Exception e) {
			return;
		}
	}

	public static boolean getBrownoutLowerThreshold() {
		return (pdp.getVoltage() < BROWNOUT_VOLTAGE_LOWER_THRESHOLD);
	}

	public static boolean getBrownoutUpperThreshold() {
		return (pdp.getVoltage() > BROWNOUT_VOLTAGE_UPPER_THRESHOLD);

	}
	
	/* 
	 * Functions for camera image processing via network tables
	 */
	
	// NOTE:  By convention, data in the "ImgResults" array is as follows:
	//       imgResults[0] is frameNumber
	//       imgResults[1] is centerX
	//    (1000.0 is a "magic number" meaning no target found by image processing)
	//    (1001.0 is a "magic number" meaning no info in network tables
	
	private static final double[] DEFAULT_IMG_RESULTS = {0.0, 1001.0};
	private static double[] latestImgResults = {0.0, 1001.0};
	private static int latestFrameNum = 0;
	private static int latestCenterX = 1001;
	private static double latestImageHeading = 0.0;
	
	private static void updateImgResults() {
	  latestImgResults = table.getNumberArray("ImgResults", DEFAULT_IMG_RESULTS);
	  // check to see if these are new results
	  if ( (int) latestImgResults[0] != latestFrameNum) {
		  latestFrameNum = (int) latestImgResults[0];
		  latestCenterX = (int) latestImgResults[1];
		  latestImageHeading = drive.getHeadingForCapturedImage();
	  }
	}
	
	public static int getFrameNum() {
		return latestFrameNum;	
	}
	
	public static int getCenterX() {
		return latestCenterX;
	}
	
	public static double getImageHeading() {
		return latestImageHeading;
	}

	public static boolean isTargetVisible() {
		return (getCenterX() < 1000);
	}

}
