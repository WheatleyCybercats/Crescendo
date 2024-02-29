package frc.robot.Subsystems;


import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.revrobotics.CANSparkFlex;
import com.revrobotics.CANSparkLowLevel;
import com.revrobotics.CANSparkMax;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotProperties;

public class LeadScrew extends SubsystemBase {

    TalonFX leadScrewMotor = new TalonFX(RobotProperties.LEAD_SCREW_MOTOR_ID, "The CANivore");
    double positionInRotations = leadScrewMotor.getPosition().getValueAsDouble();

    public LeadScrew() {
        leadScrewMotor.setNeutralMode(NeutralModeValue.Brake);
        leadScrewMotor.setPosition(0); //sets motor position to 0 whenever robot is initiated
    }
    public void moveShooterUp(double speed){
        leadScrewMotor.set(-speed);
    }
    public void moveShooterDown(double speed){
        leadScrewMotor.set(-speed);
    }
    public boolean atPosition(double position){
        if (positionInRotations >= position){ //10k is a random number, but the motor should stop whenever it is at or past a specific position
            return true;
        }
        return false;
    }
    public double getPosition(){
        return leadScrewMotor.getPosition().getValueAsDouble();
    }

    public void moveToPosition(double position){ //if starting at top = 0, moving the shooter down goes negative
        if (leadScrewMotor.getPosition().getValueAsDouble() > position){ //less negative: current position is higher than desired position
            moveShooterDown(Math.sqrt(Math.abs(leadScrewMotor.getPosition().getValueAsDouble() - position))/1.5);
        } else if (leadScrewMotor.getPosition().getValueAsDouble() < position){ //current position is lower than desired
            moveShooterUp(-Math.sqrt(Math.abs(leadScrewMotor.getPosition().getValueAsDouble() - position))/4.5);
        } else {
            stop();
        }
    }

    public void stop() {
        leadScrewMotor.stopMotor();
    }


}