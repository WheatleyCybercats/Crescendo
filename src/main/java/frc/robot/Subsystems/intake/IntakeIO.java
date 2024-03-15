// Copyright 2021-2024 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package frc.robot.Subsystems.intake;

import org.littletonrobotics.junction.AutoLog;

public interface IntakeIO {
  @AutoLog
  public static class IntakeIOInputs {
    // public double positionRad = 0.0;
    // public double velocityRadPerSec = 0.0;
    // public double appliedVolts = 0.0;
    public double[] currentAmps = new double[] {};
  }

  /** Updates the set of loggable inputs. */
  public default void updateInputs(IntakeIOInputs inputs) {}

  /** Run open loop at the specified voltage. */
  public default void setVoltage(double volts) {}

  /** Run closed loop at the specified velocity. */
  // public default void setVelocity(double velocityRadPerSec, double ffVolts) {}

  public default void setSpeed(double speed) {}

  /** Stop in open loop. */
  public default void stop() {}
}
