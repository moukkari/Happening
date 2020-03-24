/**
 * The Happening
 *
 * An app to make tasks and get reminder notifications for them.
 * User should be able to share tasks with others.
 *
 * MainActivity holds the following methods:
 * addNewTask, updateTaskMonitor, editTask, createNotificationChannel, setAlarm
 *
 * @author – Ilmari Tyrkkö
 * @version – 0.1
 * @since - 2020-03
 */
package fi.tuni.thehappening;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    // For test purposes only -->
    // testdatabase data is going to be sent to/replaced by a SQL database
    // the auto-incrementing id number is going to be fetched from the database
    private ArrayList<MainTask> testDataBase = new ArrayList<MainTask>(0);
    private int tmpIdCounter = 0;
    // Ends here

    private TaskDialog taskDialog;
    private ListView taskLV;
    private LocalDate dateNow = LocalDate.now();
    private LocalTime timeNow = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        // The following is for database test purposes only -->
        MainTask newTask = new MainTask(tmpIdCounter++, "title1", "Desc1", dateNow, dateNow.plusDays(7), timeNow);
        MainTask newTask2 = new MainTask(tmpIdCounter++, "title2", "Desc2", dateNow, dateNow.plusDays(7), timeNow);
        MainTask newTask3 = new MainTask(tmpIdCounter++, "title3", "Desc3", dateNow, dateNow.plusDays(7), timeNow);
        MainTask newTask4 = new MainTask(tmpIdCounter++, "title4", "Desc4", dateNow, dateNow.plusDays(7), timeNow);
        testDataBase.add(newTask);
        testDataBase.add(newTask2);
        testDataBase.add(newTask3);
        testDataBase.add(newTask4);
        updateTaskMonitor();
        // Ends here
    }

    // Creates new task depending on the values set on pop-up dialog
    public void addNewTask(View v) {
        // Make an empty task object which has a default due date a week forward from now on
        MainTask tmpTask = new MainTask(tmpIdCounter++, "", "", dateNow,
                dateNow.plusDays(7), timeNow);
        // Sends data to Taskdialog, flag=false determines that the task already exists
        taskDialog = new TaskDialog().newInstance(tmpTask, true);
        taskDialog.show(getSupportFragmentManager(), "New Task");

        taskDialog.setTaskResult(new TaskDialog.OnNewTaskResult(){
            // TaskDialog sends data here
            public void finish(int id, String title, String description, String creation,
                               String dueDate, String dueTime,
                               int alarmD, int alarmH, int alarmM){
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate crtn = LocalDate.parse(creation, dtf);
                LocalDate duedt = LocalDate.parse(dueDate, dtf);
                dtf = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime duetm = LocalTime.parse(dueTime, dtf);

                // Sends the new data with new data to the database
                MainTask newTask = new MainTask(id,title, description, crtn, duedt, duetm);
                testDataBase.add(newTask);

                // Sets the alarm time with given values before sending it setAlarm
                LocalDate alarmDay = duedt.minusDays(alarmD);
                LocalTime alarmTime = duetm.minusHours(alarmH);
                alarmTime = alarmTime.minusMinutes(alarmM);
                setAlarm(alarmDay, alarmTime, newTask);

                updateTaskMonitor();
            }
            public void deleteTask(String id) { }
        });

        updateTaskMonitor();
    }

    // Updates the list of tasks
    // Clicking a task opens editTask where you can edit the task
    public void updateTaskMonitor() {
        taskLV = (ListView) findViewById(R.id.taskMonitor);
        ArrayAdapter<MainTask> adapter;
        adapter = new ArrayAdapter<MainTask>(
                getApplicationContext(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                testDataBase);
        taskLV.setAdapter(adapter);

        taskLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainTask value = testDataBase.get(position);
                editTask(value);
            }
        });
    }

    // Opens TaskDialog and updates database with given information
    // Alarm time changing not yet implemented
    public void editTask(MainTask taskToEdit) {
        // Sends data to Taskdialog, flag=false determines that the task already exists
        taskDialog = new TaskDialog().newInstance(taskToEdit, false);
        taskDialog.show(getSupportFragmentManager(), "Edit Task");
        taskDialog.setTaskResult(new TaskDialog.OnNewTaskResult(){
            public void finish(int id, String title, String description, String creation,
                               String dueDate, String dueTime,
                               int alarmD, int alarmH, int alarmM){
                for(MainTask task : testDataBase) {
                    if(task.getMainId() == id) {
                        task.setTitle(title);
                        task.setDescription(description);
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate duedate = LocalDate.parse(dueDate, dtf);
                        dtf = DateTimeFormatter.ofPattern("HH:mm");
                        LocalTime duetime = LocalTime.parse(dueTime, dtf);
                        task.setDueDate(duedate);
                        task.setDueTime(duetime);
                    }
                }
                updateTaskMonitor();
            }
            public void deleteTask(String id) {
                Log.d("TAG", "Deleted: " + id);
                final int deleteId = Integer.parseInt(id);
                testDataBase.removeIf(obj -> obj.getMainId() == deleteId);

                taskDialog.dismiss();
                updateTaskMonitor();
            }
        });
    }

    // A notificationChannel builder for screen notifications
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Happening", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Sets alarm (screen notification) for task depending on due date/time
    // Needs the alarm date (YYYY-MM-DD), alarm time(HH:mm) and the task object
    public void setAlarm(LocalDate alarmDate, LocalTime alarmTime, MainTask task) {
        Calendar calendar = Calendar.getInstance();

        int year = alarmDate.getYear();
        int month = alarmDate.getMonthValue();
        int day = alarmDate.getDayOfMonth();

        int hour = alarmTime.getHour();
        int minute = alarmTime.getMinute();

        calendar.set(Calendar.YEAR,year);
        // Month is month -1 (January is 0)
        calendar.set(Calendar.MONTH,month-1);
        calendar.set(Calendar.DAY_OF_MONTH,day);
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,0);

        Intent i = new Intent(getApplicationContext(), NotificationReceiver.class);
        i.setAction(String.valueOf(task.getMainId()));
        i.putExtra("id", String.valueOf(task.getMainId()));
        i.putExtra("title", task.getTitle());
        PendingIntent pI = PendingIntent.getBroadcast(getApplicationContext(),
                task.getMainId(),i,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager aM = (AlarmManager) getSystemService(ALARM_SERVICE);
        aM.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pI);
    }
}
