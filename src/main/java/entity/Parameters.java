package entity;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Table(name = "parameters")

public class Parameters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name= "check_in")
    private int check_in;
    @Column(name= "security_check")
    private int security_check;
    @Column(name= "fasttrack")
    private int fasttrack;
    @Column(name= "border_control")
    private int border_control;
    @Column(name= "EU_boarding")
    private int EU_boarding;
    @Column(name= "non_EU_Boarding")
    private int non_EU_Boarding;
    @OneToOne(mappedBy = "parameters")
    private Result result;

    public Parameters( int check_in, int security_check, int fasttrack, int border_control, int EU_boarding, int non_EU_Boarding) {
        this.check_in = check_in;
        this.security_check = security_check;
        this.fasttrack = fasttrack;
        this.border_control = border_control;
        this.EU_boarding = EU_boarding;
        this.non_EU_Boarding = non_EU_Boarding;
    }

    public Parameters() {

    }

    public Result getResult() {
        return result;
    }
    public void setResult(Result result) {
        this.result = result;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCheck_in() {
        return check_in;
    }

    public void setCheck_in(int check_in) {
        this.check_in = check_in;
    }

    public int getSecurity_check() {
        return security_check;
    }

    public void setSecurity_check(int security_check) {
        this.security_check = security_check;
    }

    public int getFasttrack() {
        return fasttrack;
    }

    public void setFasttrack(int fasttrack) {
        this.fasttrack = fasttrack;
    }

    public int getBorder_control() {
        return border_control;
    }

    public void setBorder_control(int border_control) {
        this.border_control = border_control;
    }

    public int getEU_boarding() {
        return EU_boarding;
    }

    public void setEU_boarding(int EU_boarding) {
        this.EU_boarding = EU_boarding;
    }

    public int getNon_EU_Boarding() {
        return non_EU_Boarding;
    }

    public void setNon_EU_Boarding(int non_EU_Boarding) {
        this.non_EU_Boarding = non_EU_Boarding;
    }
}
