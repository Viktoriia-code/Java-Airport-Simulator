package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "results")

public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name= "servedPassenger")
    private int servedPassenger;
    @Column(name= "averageQueueLenght")
    private double averageQueueLength;
    @Column(name= "simulationTime")
    private double simulationTime;
    @Column(name= "averageServiceTime")
    private double averageServiceTime;
    @Column(name= "longestQueue")
    private String longestQueue;

    public Result(int servedPassenger, double averageQueueLength, double simulationTime, double averageServiceTime, String longestQueue) {
        super();
        this.servedPassenger = servedPassenger;
        this.averageQueueLength = averageQueueLength;
        this.simulationTime = simulationTime;
        this.averageServiceTime = averageServiceTime;
        this.longestQueue = longestQueue;
    }
    public Result() {

    }
    public int getServedPassenger() {
        return servedPassenger;
    }
    public double getAverageQueueLength() {
        return averageQueueLength;
    }
    public double getSimulationTime() {
        return simulationTime;
    }
    public double getAverageServiceTime() {
        return averageServiceTime;
    }
    public String getLongestQueue() {
        return longestQueue;
    }
    public void setServedPassenger(int servedPassenger) {
        this.servedPassenger = servedPassenger;
    }
    public void setAverageQueueLength(double averageQueueLength) {
        this.averageQueueLength = averageQueueLength;
    }
    public void setSimulationTime(double simulationTime) {
        this.simulationTime = simulationTime;
    }
    public void setAverageServiceTime(double averageServiceTime) {
        this.averageServiceTime = averageServiceTime;
    }
    public void setLongestQueue(String longestQueue) {
        this.longestQueue = longestQueue;
    }
}
