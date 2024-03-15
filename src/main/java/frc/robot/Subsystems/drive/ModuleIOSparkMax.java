
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

package frc.robot.Subsystems.drive;

import static frc.robot.Subsystems.drive.DriveConstants.moduleConstants;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.hardware.CANcoder;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkFlex;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkLowLevel.PeriodicFrame;
import com.revrobotics.CANSparkMax;
import com.revrobotics.REVLibError;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import java.util.OptionalDouble;
import java.util.Queue;

/**
 * Module IO implementation for SparkMax drive motor controller, SparkMax turn motor controller (NEO
 * or NEO 550), and analog absolute encoder connected to the RIO
 *
 * <p>NOTE: This implementation should be used as a starting point and adapted to different hardware
 * configurations (e.g. If using a CANcoder, copy from "ModuleIOTalonFX")
 *
 * <p>To calibrate the absolute encoder offsets, point the modules straight (such that forward
 * motion on the drive motor will propel the robot forward) and copy the reported values from the
 * absolute encoders using AdvantageScope. These values are logged under
 * "/Drive/ModuleX/TurnAbsolutePositionRad"
 */
public class ModuleIOSparkMax implements ModuleIO {
  // Gear ratios for SDS MK4i L3, adjust as necessary
  private static final double DRIVE_GEAR_RATIO = (50.0 / 14.0) * (16.0 / 28.0) * (45.0 / 15.0);
  private static final double TURN_GEAR_RATIO = 150.0 / 7.0;

  private final CANSparkFlex driveSparkMax;
  private final CANSparkMax turnSparkMax;
  private final CANcoder cancoder;
  private final RelativeEncoder driveEncoder;
  private final RelativeEncoder turnRelativeEncoder;
  private final Queue<Double> timestampQueue;
  private final Queue<Double> drivePositionQueue;
  private final Queue<Double> turnPositionQueue;
  private final StatusSignal<Double> turnAbsolutePosition;
  private final boolean isTurnMotorInverted = true;
  private final Rotation2d absoluteEncoderOffset;
  private final PIDController driveController;
  private final PIDController turnController;

  public ModuleIOSparkMax(int index) {
    switch (index) {
      case 0: // FL
        driveSparkMax = new CANSparkFlex(61, MotorType.kBrushless);
        turnSparkMax = new CANSparkMax(62, MotorType.kBrushless);
        cancoder = new CANcoder(10, "The CANivore");
        absoluteEncoderOffset = Rotation2d.fromRotations(0.132); // MUST BE CALIBRATED 0.1455
        break;
      case 1: // FR
        driveSparkMax = new CANSparkFlex(49, MotorType.kBrushless);
        turnSparkMax = new CANSparkMax(50, MotorType.kBrushless);
        cancoder = new CANcoder(11, "The CANivore");
        absoluteEncoderOffset = Rotation2d.fromRotations(-0.453); // MUST BE CALIBRATED -0.453
        break;
      case 2: // BL
        driveSparkMax = new CANSparkFlex(51, MotorType.kBrushless);
        turnSparkMax = new CANSparkMax(52, MotorType.kBrushless);
        cancoder = new CANcoder(12, "The CANivore");
        absoluteEncoderOffset = Rotation2d.fromRotations(-0.305); // MUST BE CALIBRATED -0.288
        break;
      case 3: // BR
        driveSparkMax = new CANSparkFlex(56, MotorType.kBrushless);
        turnSparkMax = new CANSparkMax(55, MotorType.kBrushless);
        cancoder = new CANcoder(13, "The CANivore");
        absoluteEncoderOffset = Rotation2d.fromRotations(-0.112); // MUST BE CALIBRATED -0.112
        break;
      default:
        throw new RuntimeException("Invalid module index");
    }

    driveSparkMax.restoreFactoryDefaults();
    turnSparkMax.restoreFactoryDefaults();

    driveSparkMax.setCANTimeout(250);
    turnSparkMax.setCANTimeout(250);

    driveEncoder = driveSparkMax.getEncoder();
    turnRelativeEncoder = turnSparkMax.getEncoder();

    turnSparkMax.setInverted(isTurnMotorInverted);
    driveSparkMax.setSmartCurrentLimit(40);
    turnSparkMax.setSmartCurrentLimit(30);
    driveSparkMax.enableVoltageCompensation(12.0);
    turnSparkMax.enableVoltageCompensation(12.0);

    driveEncoder.setPosition(0.0);
    driveEncoder.setMeasurementPeriod(10);
    driveEncoder.setAverageDepth(2);

    turnRelativeEncoder.setPosition(0.0);
    turnRelativeEncoder.setMeasurementPeriod(10);
    turnRelativeEncoder.setAverageDepth(2);

    driveSparkMax.setCANTimeout(0);
    turnSparkMax.setCANTimeout(0);

    driveSparkMax.setPeriodicFramePeriod(
        PeriodicFrame.kStatus2, (int) (1000.0 / Module.ODOMETRY_FREQUENCY));
    turnSparkMax.setPeriodicFramePeriod(
        PeriodicFrame.kStatus2, (int) (1000.0 / Module.ODOMETRY_FREQUENCY));
    timestampQueue = SparkMaxOdometryThread.getInstance().makeTimestampQueue();
    drivePositionQueue =
        SparkMaxOdometryThread.getInstance()
            .registerSignal(
                () -> {
                  double value = driveEncoder.getPosition();
                  if (driveSparkMax.getLastError() == REVLibError.kOk) {
                    return OptionalDouble.of(value);
                  } else {
                    return OptionalDouble.empty();
                  }
                });
    turnPositionQueue =
        SparkMaxOdometryThread.getInstance()
            .registerSignal(
                () -> {
                  double value = turnRelativeEncoder.getPosition();
                  if (driveSparkMax.getLastError() == REVLibError.kOk) {
                    return OptionalDouble.of(value);
                  } else {
                    return OptionalDouble.empty();
                  }
                });

    driveSparkMax.burnFlash();
    turnSparkMax.burnFlash();
    cancoder.getConfigurator().apply(new CANcoderConfiguration());
    turnAbsolutePosition = cancoder.getAbsolutePosition();
    BaseStatusSignal.setUpdateFrequencyForAll(50.0, turnAbsolutePosition);

    driveController = new PIDController(moduleConstants.drivekP(), 0.0, moduleConstants.drivekD());
    turnController = new PIDController(moduleConstants.turnkP(), 0.0, moduleConstants.turnkD());
    turnController.enableContinuousInput(-Math.PI, Math.PI);
  }

  @Override
  public void updateInputs(ModuleIOInputs inputs) {
    BaseStatusSignal.refreshAll(turnAbsolutePosition);
    inputs.drivePositionRad =
        Units.rotationsToRadians(driveEncoder.getPosition()) / DRIVE_GEAR_RATIO;
    inputs.driveVelocityRadPerSec =
        Units.rotationsPerMinuteToRadiansPerSecond(driveEncoder.getVelocity()) / DRIVE_GEAR_RATIO;
    inputs.driveAppliedVolts = driveSparkMax.getAppliedOutput() * driveSparkMax.getBusVoltage();
    inputs.driveCurrentAmps = new double[] {driveSparkMax.getOutputCurrent()};

    inputs.turnAbsolutePosition =
        Rotation2d.fromRotations(turnAbsolutePosition.getValueAsDouble())
            .minus(absoluteEncoderOffset);
    inputs.turnPosition =
        Rotation2d.fromRotations(turnRelativeEncoder.getPosition() / TURN_GEAR_RATIO);
    inputs.turnVelocityRadPerSec =
        Units.rotationsPerMinuteToRadiansPerSecond(turnRelativeEncoder.getVelocity())
            / TURN_GEAR_RATIO;
    inputs.turnAppliedVolts = turnSparkMax.getAppliedOutput() * turnSparkMax.getBusVoltage();
    inputs.turnCurrentAmps = new double[] {turnSparkMax.getOutputCurrent()};

    inputs.odometryTimestamps =
        timestampQueue.stream().mapToDouble((Double value) -> value).toArray();
    inputs.odometryDrivePositionsRad =
        drivePositionQueue.stream()
            .mapToDouble((Double value) -> Units.rotationsToRadians(value) / DRIVE_GEAR_RATIO)
            .toArray();
    inputs.odometryTurnPositions =
        turnPositionQueue.stream()
            .map((Double value) -> Rotation2d.fromRotations(value / TURN_GEAR_RATIO))
            .toArray(Rotation2d[]::new);

    timestampQueue.clear();
    drivePositionQueue.clear();
    turnPositionQueue.clear();
  }

  @Override
  public void setDriveVoltage(double volts) {
    driveSparkMax.setVoltage(volts);
  }

  @Override
  public void setTurnVoltage(double volts) {
    turnSparkMax.setVoltage(volts);
  }

  @Override
  public void setDriveBrakeMode(boolean enable) {
    driveSparkMax.setIdleMode(enable ? IdleMode.kBrake : IdleMode.kCoast);
  }

  @Override
  public void setTurnBrakeMode(boolean enable) {
    turnSparkMax.setIdleMode(enable ? IdleMode.kBrake : IdleMode.kCoast);
  }

  @Override
  public void setDrivePID(double kP, double kI, double kD) {
    driveController.setPID(kP, kI, kD);
  }

  @Override
  public void setTurnPID(double kP, double kI, double kD) {
    turnController.setPID(kP, kI, kD);
  }
}
