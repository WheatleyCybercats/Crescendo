package frc.robot.Commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.Preset;
import frc.robot.Subsystems.leadscrew.Leadscrew;

public class PresetCommand extends Command {
  private final Leadscrew leadscrew;
  private final double position;

  public PresetCommand(Leadscrew leadscrew, Preset preset) {
    this.leadscrew = leadscrew;
    position = preset.getAngle();
    // each subsystem used by the command must be passed into the
    // addRequirements() method (which takes a vararg of Subsystem)
    addRequirements(this.leadscrew);
  }

  @Override
  public void initialize() {}

  @Override
  public void execute() {
    if (position == Preset.AMP.getAngle()) {
      leadscrew.runSetpoint(position);
    }
  }

  @Override
  public boolean isFinished() {
    return leadscrew.atPosition(position);
  }

  @Override
  public void end(boolean interrupted) {}
}
