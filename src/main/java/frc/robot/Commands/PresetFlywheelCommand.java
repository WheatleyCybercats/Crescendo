package frc.robot.Commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.Subsystems.flywheel.Flywheel;
import frc.robot.Subsystems.indexer.Indexer;

public class PresetFlywheelCommand extends Command {
  private final Flywheel flywheel;
  private final Indexer indexer;
  private final Constants.PresetFlywheel presetFlywheel;

  public PresetFlywheelCommand(
      Indexer indexer, Flywheel flywheel, Constants.PresetFlywheel presetFlywheel) {
    this.indexer = indexer;
    this.flywheel = flywheel;
    this.presetFlywheel = presetFlywheel;
    // each subsystem used by the command must be passed into the
    // addRequirements() method (which takes a vararg of Subsystem)
    addRequirements(this.flywheel);
  }

  @Override
  public void execute() {
    flywheel.simpleVoltTop(presetFlywheel.getVoltTop());
    flywheel.simpleVoltBot(presetFlywheel.getVoltBot());
    if (presetFlywheel.equals(Constants.PresetFlywheel.AMP)) {
      if (flywheel.atSpeedTop(250) && flywheel.atSpeedBot(-300)) {
        indexer.setSpeed(-0.17);
      }
    } else if (presetFlywheel.equals(Constants.PresetFlywheel.SPEAKER)) {
      if (flywheel.atSpeedTop(4100) && flywheel.atSpeedBot(-4100)) {
        indexer.setSpeed(-0.17);
      }
    }
  }

  @Override
  public boolean isFinished() {
    // TODO: Make this return true when this Command no longer needs to run execute()
    return false;
  }

  @Override
  public void end(boolean interrupted) {
    indexer.stop();
    flywheel.stop();
  }
}