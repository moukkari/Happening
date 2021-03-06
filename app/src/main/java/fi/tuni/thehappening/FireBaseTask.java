package fi.tuni.thehappening;
/**
 * A class to make task objects for sending to Firebase database
 * Differs from MainTask so that it uses POJO Objects that Firebase understands
 */
public class FireBaseTask {
    private int mainId;
    private String title;
    private String description;
    private String creationDate;
    private String dueDate;
    private String dueTime;
    private String sharedBy;
    private boolean isDone;

    public FireBaseTask(int id, String ttl, String dscrptn,
                    String crtnDt, String dDt, String dTime, String sharedBy, boolean isDone) {
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
    public String getSharedBy() { return this.sharedBy; };
    public void changeState() {
        this.isDone = !this.isDone;
    }
    public boolean getIsDone() {
        return this.isDone;
    }
    public String toString() {
        return this.getMainId() + " - " + this.getTitle() + " - " + this.getIsDone();
    }
}
