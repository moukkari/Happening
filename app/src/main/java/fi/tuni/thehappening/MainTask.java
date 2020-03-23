package fi.tuni.thehappening;

import java.time.LocalDateTime;

public class MainTask {
    private int mainId;
    private String description;
    private LocalDateTime creationDate;
    private LocalDateTime dueDate;

    public MainTask(int id, String dscrptn, LocalDateTime crtnDt, LocalDateTime dDt) {
        this.mainId = id;
        this.description = dscrptn;
        this.creationDate = crtnDt;
        this.dueDate = dDt;
    }
}
