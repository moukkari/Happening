package fi.tuni.thehappening;

/**
 * A class to make original task objects
 * Differs from FireBaseTask so that it uses LocalDate and LocalTime
 * instead of POJO objects
 */

import java.time.LocalDate;
import java.time.LocalTime;

public class MainTask {
    private int mainId;
    private String title;
    private String description;
    private LocalDate creationDate;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private String sharedBy;
    private boolean isDone;

    public MainTask(int id, String ttl, String dscrptn,
                    LocalDate crtnDt, LocalDate dDt,
                    LocalTime dTime, String sharedBy, boolean isDone) {
        this.mainId = id;
        this.title = ttl;
        this.description = dscrptn;
        this.creationDate = crtnDt;
        this.dueDate = dDt;
        this.dueTime = dTime;
        this.sharedBy = sharedBy;
        this.isDone = isDone;
    }
    public int getMainId() {
        return this.mainId;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String x) {
        this.title = x;
    }
    public String getDesc() {
        return this.description;
    }
    public void setDescription(String x) {
        this.description = x;
    }
    public LocalDate getCreationDate() {
        return this.creationDate;
    }
    public LocalDate getDueDate() {
        return this.dueDate;
    }
    public void setDueDate(LocalDate x) {
        this.dueDate = x;
    }
    public LocalTime getDueTime() {
        return this.dueTime;
    }
    public void setDueTime(LocalTime x) {
        this.dueTime = x;
    }
    public String getSharedBy() { return this.sharedBy; };
    public void changeState() {
        this.isDone = !this.isDone;
    }
    public boolean getIsDone() {
        return this.isDone;
    }
    public String toString() {
        return this.mainId + " - " + this.title;
    }
}
