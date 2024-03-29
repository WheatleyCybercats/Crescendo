package frc.robot.Commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Robot;
import frc.robot.RobotProperties;
import frc.robot.Subsystems.Flywheel;
import frc.robot.Subsystems.Indexer;


public class ShootAmpCommand extends Command {
    private final Flywheel flywheel = Robot.flywheel;
    private final Indexer indexer = Robot.indexer;

    public ShootAmpCommand() {
        // each subsystem used by the command must be passed into the
        // addRequirements() method (which takes a vararg of Subsystem)
        addRequirements(this.flywheel, this.indexer);
    }

    @Override
    public void initialize() {
        flywheel.resetPID();

    }

    @Override
    public void execute() {
        flywheel.setTopFlywheelMotorVolt(0.8);
        flywheel.setBotFlywheelMotorVolt(-3.1);
        if(flywheel.topflywheelAtAmpSpeed() && flywheel.botflywheelAtAmpSpeed()){
            indexer.setSpeed(RobotProperties.IndexerProperties.shootingSpeed);
        }
        else{
            indexer.stop();
        }
        Robot.blinkin.rainbow_rainbow();
    }

    @Override
    public boolean isFinished() {
        // TODO: Make this return true when this Command no longer needs to run execute()
        return false;
    }

    @Override
    public void end(boolean interrupted) {
        flywheel.stop();
        indexer.stop();
        flywheel.resetPID();
        Robot.blinkin.set(0);
    }
}
