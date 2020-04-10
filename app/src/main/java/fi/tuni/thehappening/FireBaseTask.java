package fi.tuni.thehappening;
/**
 * A class to make task objects
 * These objects are meant to be sent to a SQL? database
 *
 * To be added:
 * - Alarm dates
 */
public class FireBaseTask {
    private int mainId;
    private String title;
    private String description;
    private String creationDate;
    private String dueDate;
    private String dueTime;

    public FireBaseTask(int id, String ttl, String dscrptn,
                    String crtnDt, String dDt, String dTime) {
        this.mainId = id;
        this.title = ttl;
        this.description = dscrptn;
        this.creationDate = crtnDt;
        this.dueDate = dDt;
        this.dueTime = dTime;
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
    public String getCreationDate() {
        return this.creationDate;
    }
    public String getDueDate() {
        return this.dueDate;
    }
    public void setDueDate(String x) {
        this.dueDate = x;
    }
    public String getDueTime() {
        return this.dueTime;
    }
    public void setDueTime(String x) {
        this.dueTime = x;
    }
    public String toString() {
        return this.mainId + " - " + this.title;
    }
}
