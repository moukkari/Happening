package fi.tuni.thehappening;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView taskMonitor;
    private LocalDateTime today = LocalDateTime.now();
    private ArrayList<MainTask> testDataBase = new ArrayList<MainTask>(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void addNewTask(View v) {
        MainTask testTask = new MainTask(1, "Testi", today, today.plusDays(7));
        testDataBase.add(testTask);
        int tmpItem = testDataBase.indexOf(testTask);
        Log.d("TAG", "Added: " + testDataBase.get(tmpItem));
        updateTaskMonitor();
    }

    public void updateTaskMonitor() {
        taskMonitor = (TextView) findViewById(R.id.taskMonitor);
        String tmp = "";
        for (MainTask x : testDataBase)
            tmp += x + "\n";
        taskMonitor.setText(tmp);
    }
}
