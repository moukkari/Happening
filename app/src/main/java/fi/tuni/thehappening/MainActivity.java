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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    // Firebase stuff here
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private DatabaseReference reference;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    private String user_id;

    private ArrayList<MainTask> LocalDatabase = new ArrayList<MainTask>(0);

    // For test purposes only -->
    // testdatabase data is going to be sent to/replaced by a SQL database
    // the auto-incrementing id number is going to be fetched from the database
    private int tmpIdCounter = 0;
    // Ends here

    private TextView loginStatusTV;
    private Button signinButton, signoutButton, addTaskButton;
    private TaskDialog taskDialog;
    private ListView taskLV;
    private LocalDate dateNow = LocalDate.now();
    private LocalTime timeNow = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        loginStatusTV = (TextView) findViewById(R.id.signedInStatus);
        signinButton = (Button) findViewById(R.id.signinButton);
        signoutButton = (Button) findViewById(R.id.signoutButton);
        addTaskButton = (Button) findViewById(R.id.addTaskButton);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // The following is for database test purposes only -->
        /*
        MainTask newTask = new MainTask(tmpIdCounter++, "title1", "Desc1", dateNow, dateNow.plusDays(7), timeNow);
        MainTask newTask2 = new MainTask(tmpIdCounter++, "title2", "Desc2", dateNow, dateNow.plusDays(7), timeNow);
        MainTask newTask3 = new MainTask(tmpIdCounter++, "title3", "Desc3", dateNow, dateNow.plusDays(7), timeNow);
        MainTask newTask4 = new MainTask(tmpIdCounter++, "title4", "Desc4", dateNow, dateNow.plusDays(7), timeNow);
        testDataBase.add(newTask);
        testDataBase.add(newTask2);
        testDataBase.add(newTask3);
        testDataBase.add(newTask4);
        updateTaskMonitor();
         */

        // Ends here
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    public void signInClicked(View v) {
        Log.d("TAG", "SIGNING IN");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG", "ONACTIVITYRESULT");

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Log.d("TAG", "TASK: " + task);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.d("TAG", "Google sign in failed", e);
                // ...
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }
    public void signOut(View v) {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Log.d("TAG", "DATA: " + user.getEmail() + ", " + user.getUid());
            loginStatusTV.setText("Logged in as " + user.getEmail());

            signinButton.setVisibility(View.GONE);
            signoutButton.setVisibility(View.VISIBLE);
            addTaskButton.setVisibility(View.VISIBLE);

            retrieveDataFromFirebase();

        } else {
            Log.d("TAG","Failed.");
            loginStatusTV.setText("Not logged in.");
            LocalDatabase.clear();

            signinButton.setVisibility(View.VISIBLE);
            signoutButton.setVisibility(View.GONE);
            addTaskButton.setVisibility(View.GONE);
        }
    }
    public void retrieveDataFromFirebase() {
        Log.d("TAG", "id: " + user_id);

        user_id = mAuth.getCurrentUser().getUid();

        reference = FirebaseDatabase.getInstance().getReference().child(user_id);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                LocalDatabase.clear();

                Log.d("TAG", "LOG: " + dataSnapshot.getValue());

                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    LocalDate fireCreationDate = LocalDate.parse(childDataSnapshot.child("creationDate").getValue().toString(), dateFormat);
                    LocalDate fireDueDate = LocalDate.parse(childDataSnapshot.child("dueDate").getValue().toString(), dateFormat);
                    LocalTime fireDueTime = LocalTime.parse(childDataSnapshot.child("dueTime").getValue().toString(), timeFormat);

                    MainTask tmpTask = new MainTask(Integer.parseInt(childDataSnapshot.child("mainId").getValue().toString()),
                            childDataSnapshot.child("title").getValue().toString(),
                            childDataSnapshot.child("desc").getValue().toString(),
                            fireCreationDate,
                            fireDueDate,
                            fireDueTime);

                    LocalDatabase.add(tmpTask);
                    updateTaskMonitor();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    // Creates new task depending on the values set on pop-up dialog
    public void addNewTask(View v) {
        Log.d("TAG", "ADD NEW TASK");

        reference = FirebaseDatabase.getInstance().getReference().child(user_id);

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
                Log.d("TAG", "SET TASK RESULT FINISH");

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate crtn = LocalDate.parse(creation, dtf);
                LocalDate duedt = LocalDate.parse(dueDate, dtf);
                dtf = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime duetm = LocalTime.parse(dueTime, dtf);

                // Sends the new data with new data to the database
                MainTask newTask = new MainTask(id,title, description, crtn, duedt, duetm);
                // testDataBase.add(newTask);

                // Sets the alarm time with given values before sending it setAlarm
                LocalDate alarmDay = duedt.minusDays(alarmD);
                LocalTime alarmTime = duetm.minusHours(alarmH);
                alarmTime = alarmTime.minusMinutes(alarmM);
                setAlarm(alarmDay, alarmTime, newTask);

                // Send to Firebase
                Log.d("TAG", "Sending data to Firebase");
                Toast.makeText(getApplicationContext(), "Data inserted", Toast.LENGTH_SHORT).show();
                FireBaseTask newFireTask = new FireBaseTask(newTask.getMainId(), newTask.getTitle(), newTask.getDesc(),
                        newTask.getCreationDate().toString(),
                        newTask.getDueDate().toString(),
                        newTask.getDueTime().toString());

                reference.child(String.valueOf(tmpIdCounter)).setValue(newFireTask);
                Log.d("TAG", "Sent data to Firebase");


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
                LocalDatabase);
        taskLV.setAdapter(adapter);

        taskLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainTask value = LocalDatabase.get(position);
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
                for(MainTask task : LocalDatabase) {
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
                LocalDatabase.removeIf(obj -> obj.getMainId() == deleteId);

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
