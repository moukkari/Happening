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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Firebase stuff here
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    // A variable to set the database address
    private DatabaseReference reference;
    // Firebase authentication
    private FirebaseAuth mAuth;

    // Variables to get current users id and mail address
    private String user_id;
    private String user_mail;

    // Local arrays to keep user's data in
    private ArrayList<Friend> REQUESTS = new ArrayList<Friend>(0);
    private ArrayList<Friend> FRIENDS = new ArrayList<Friend>(0);
    private ArrayList<MainTask> LOCALDATABASE = new ArrayList<MainTask>(0);
    private ArrayList<MainTask> SHAREDTASKS = new ArrayList<MainTask>(0);
    private ArrayList<MainTask> COMPLETEDTASKS = new ArrayList<MainTask>(0);

    // Current users database's tasks biggest id number
    // It's set again every time a new task is created
    private int BIGGEST_ID = 0;

    // UI Stuff
    private TextView loginStatusTV, infoTV;
    private Button signoutButton, addTaskButton, friendsButton;
    private SignInButton signinButton;
    private TaskDialog taskDialog;
    private FriendsDialog friendsDialog;
    private ListView taskLV;
    private ListView sharedTaskLV;
    private ListView completedTaskLV;

    // Provides time info when creating a new task
    private LocalDate dateNow = LocalDate.now();
    private LocalTime timeNow = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);

    // Sets the layout and sets GoogleAuthentication Options
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        sharedTaskLV = (ListView) findViewById(R.id.sharedTaskMonitor);
        View sharedHeader = View.inflate(this, R.layout.sharedtasksheader, null);
        sharedTaskLV.addHeaderView(sharedHeader);

        completedTaskLV = (ListView) findViewById(R.id.completedTaskMonitor);
        View completedHeader = View.inflate(this, R.layout.completedtasksheader, null);
        completedTaskLV.addHeaderView(completedHeader);

        taskLV = (ListView) findViewById(R.id.taskMonitor);
        View header = View.inflate(this, R.layout.taskheader, null);
        taskLV.addHeaderView(header);

        loginStatusTV = (TextView) findViewById(R.id.signedInStatus);
        infoTV = (TextView) findViewById(R.id.infoTextView);
        signinButton = (SignInButton) findViewById(R.id.signinButton);
        // SignInButton's listener can't be registered via xml
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInClicked(v);
            }
        });
        signoutButton = (Button) findViewById(R.id.signoutButton);
        addTaskButton = (Button) findViewById(R.id.addTaskButton);
        friendsButton = (Button) findViewById(R.id.friendsButton);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    // Sets the UI according to Firebase user status
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    // Starts the sign in process to with Google authentication
    public void signInClicked(View v) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Listens for google authentication result and starts firebase sign in if successful
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Signs in to Firebase database
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    // Signs out of Google and Firebase
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

    // Updates the UI if user has signed in or not
    // If user has signed in, this calls retrieveDataFromFirebase()
    // and databaseSetUp() to get users data from the database
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            loginStatusTV.setText("Logged in as " + user.getEmail());
            signinButton.setVisibility(View.GONE);
            signoutButton.setVisibility(View.VISIBLE);
            addTaskButton.setVisibility(View.VISIBLE);
            friendsButton.setVisibility(View.VISIBLE);
            sharedTaskLV.setVisibility(View.VISIBLE);
            taskLV.setVisibility(View.VISIBLE);
            completedTaskLV.setVisibility(View.VISIBLE);
            retrieveDataFromFirebase();
            databaseSetUp();
        } else {
            loginStatusTV.setText("Not logged in.");
            LOCALDATABASE.clear();
            SHAREDTASKS.clear();
            COMPLETEDTASKS.clear();
            signinButton.setVisibility(View.VISIBLE);
            signoutButton.setVisibility(View.GONE);
            addTaskButton.setVisibility(View.GONE);
            friendsButton.setVisibility(View.GONE);
            sharedTaskLV.setVisibility(View.GONE);
            taskLV.setVisibility(View.GONE);
            completedTaskLV.setVisibility(View.GONE);
        }
    }
    // First fetches tasks that are shared with user and after that fetches users own tasks
    public void retrieveDataFromFirebase() {
        user_id = mAuth.getCurrentUser().getUid();
        user_mail = mAuth.getCurrentUser().getEmail();

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        reference = FirebaseDatabase.getInstance().getReference().child(user_id).child("shared");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SHAREDTASKS.clear();

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot childOfChildShot : childDataSnapshot.getChildren()) {
                        reference = FirebaseDatabase.getInstance().getReference()
                                .child(childDataSnapshot.getKey()).child("database")
                                .child(childOfChildShot.getKey());

                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot childShot) {
                                if (childShot.getValue() != null) {

                                    LocalDate fireCreationDate = LocalDate.parse(childShot.child("creationDate").getValue().toString(), dateFormat);
                                    LocalDate fireDueDate = LocalDate.parse(childShot.child("dueDate").getValue().toString(), dateFormat);
                                    LocalTime fireDueTime = LocalTime.parse(childShot.child("dueTime").getValue().toString(), timeFormat);

                                    MainTask tmpTask = new MainTask(Integer.parseInt(childShot.child("mainId").getValue().toString()),
                                            childShot.child("title").getValue().toString(),
                                            childShot.child("desc").getValue().toString(),
                                            fireCreationDate,
                                            fireDueDate,
                                            fireDueTime,
                                            childShot.child("sharedBy").getValue().toString(),
                                            childShot.child("isDone").getValue(Boolean.class));
                                    for (MainTask task : SHAREDTASKS) {
                                        if (task.getMainId() == tmpTask.getMainId() && task.getSharedBy().equals(tmpTask.getSharedBy())) {
                                            SHAREDTASKS.remove(task);
                                        }
                                    }
                                    if (!tmpTask.getIsDone()) {
                                        SHAREDTASKS.add(tmpTask);
                                    } else {
                                        for (MainTask task : COMPLETEDTASKS) {
                                            if (task.getMainId() == tmpTask.getMainId() && task.getSharedBy().equals(tmpTask.getSharedBy())) {
                                                COMPLETEDTASKS.remove(task);
                                            }
                                        }
                                        COMPLETEDTASKS.add(tmpTask);
                                    }

                                    updateTaskMonitor();
                                } else {
                                    for (MainTask tmpTask : SHAREDTASKS) {
                                        if (tmpTask.getMainId() == Integer.parseInt(childShot.getKey())) {
                                            SHAREDTASKS.remove(tmpTask);
                                        }
                                    }
                                    updateTaskMonitor();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        reference = FirebaseDatabase.getInstance().getReference().child(user_id).child("database");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LOCALDATABASE.clear();

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                    // Get the biggest id to avoid same id numbers
                    if (Integer.parseInt(childDataSnapshot.getKey()) >= BIGGEST_ID) {
                        BIGGEST_ID = Integer.parseInt(childDataSnapshot.getKey()) + 1;
                    }

                    LocalDate fireCreationDate = LocalDate.parse(childDataSnapshot.child("creationDate").getValue().toString(), dateFormat);
                    LocalDate fireDueDate = LocalDate.parse(childDataSnapshot.child("dueDate").getValue().toString(), dateFormat);
                    LocalTime fireDueTime = LocalTime.parse(childDataSnapshot.child("dueTime").getValue().toString(), timeFormat);

                    MainTask tmpTask = new MainTask(Integer.parseInt(childDataSnapshot.child("mainId").getValue().toString()),
                            childDataSnapshot.child("title").getValue().toString(),
                            childDataSnapshot.child("desc").getValue().toString(),
                            fireCreationDate,
                            fireDueDate,
                            fireDueTime,
                            childDataSnapshot.child("sharedBy").getValue().toString(),
                            childDataSnapshot.child("isDone").getValue(Boolean.class));

                    if (!tmpTask.getIsDone()) {
                        LOCALDATABASE.add(tmpTask);
                    } else {
                        for (MainTask task : COMPLETEDTASKS) {
                            if (task.getMainId() == tmpTask.getMainId() && task.getSharedBy().equals(tmpTask.getSharedBy())) {
                                COMPLETEDTASKS.remove(task);
                            }
                        }
                        COMPLETEDTASKS.add(tmpTask);
                    }

                    updateTaskMonitor();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    // Adds current user to (Firebase) database and fetches user's friends and friend requests
    public void databaseSetUp() {
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        reference.child(user_id).setValue(user_mail);

        reference = FirebaseDatabase.getInstance().getReference().child(user_id).child("friends");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FRIENDS.clear();
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    FRIENDS.add(new Friend(childSnap.getKey(), childSnap.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        reference = FirebaseDatabase.getInstance().getReference().child(user_id).child("requests");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String requests = "";
                REQUESTS.clear();
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    requests += "New friend request from " + childSnap.getValue() + "\n";
                    REQUESTS.add(new Friend(childSnap.getKey(), childSnap.getValue().toString()));
                }
                infoTV.setText(requests);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // Starts a dialog where you can send friend requests and see your current friends
    // when Friends button is clicked
    public void showFriends(View v) {
        friendsDialog = new FriendsDialog().newInstance(REQUESTS, FRIENDS, user_id, user_mail);
        friendsDialog.show(getSupportFragmentManager(), "Edit friends");

        friendsDialog.setFriends(new FriendsDialog.OnNewFriends() {
            @Override
            public void setNewFriends(ArrayList<Friend> requestArray, ArrayList<Friend> newFriends) {
                FRIENDS = newFriends;
                // reference = FirebaseDatabase.getInstance().getReference().child(user_id).child("friends");
                for (Friend friend : FRIENDS) {
                    reference = FirebaseDatabase.getInstance().getReference().child(friend.getKey()).child("friends");
                    reference.child(user_id).setValue(user_mail);

                    reference = FirebaseDatabase.getInstance().getReference().child(user_id).child("friends");
                    for (Friend newFriend : FRIENDS) {
                        reference.child(newFriend.getKey()).setValue(newFriend.getMail());
                    }
                }

                REQUESTS = requestArray;
                reference = FirebaseDatabase.getInstance().getReference().child(user_id).child("requests");
                reference.removeValue();
                for (Friend req : REQUESTS) {
                    reference.child(req.getKey()).setValue(req.getMail());
                }

            }
        });
    }

    // Creates new task depending on the values set on pop-up dialog
    public void addNewTask(View v) {
        // Make an empty task object which has a default due date a week forward from now on
        MainTask tmpTask = new MainTask(BIGGEST_ID, "", "", dateNow,
                dateNow.plusDays(7), timeNow, user_id, false);
        // Sends data to Taskdialog, flag=false determines that the task already exists
        taskDialog = new TaskDialog().newInstance(tmpTask, FRIENDS, true, user_id);
        taskDialog.show(getSupportFragmentManager(), "New Task");

        taskDialog.setTaskResult(new TaskDialog.OnNewTaskResult(){
            // TaskDialog sends data here
            public void edit(int id, String title, String description, String creation,
                             String dueDate, String dueTime,
                             int alarmD, int alarmH, int alarmM,
                             ArrayList<Friend> selectedFriends, String sharedBy){
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate crtn = LocalDate.parse(creation, dtf);
                LocalDate duedt = LocalDate.parse(dueDate, dtf);
                dtf = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime duetm = LocalTime.parse(dueTime, dtf);

                // Sends the new data to the database
                MainTask newTask = new MainTask(id,title, description, crtn, duedt, duetm,
                        user_id, false);
                // testDataBase.add(newTask);

                // Sets the alarm time with given values before sending it setAlarm
                LocalDate alarmDay = duedt.minusDays(alarmD);
                LocalTime alarmTime = duetm.minusHours(alarmH);
                alarmTime = alarmTime.minusMinutes(alarmM);
                setAlarm(alarmDay, alarmTime, newTask);

                // Send to Firebase
                Toast.makeText(getApplicationContext(), "Data inserted", Toast.LENGTH_SHORT).show();
                FireBaseTask newFireTask = new FireBaseTask(newTask.getMainId(), newTask.getTitle(), newTask.getDesc(),
                        newTask.getCreationDate().toString(),
                        newTask.getDueDate().toString(),
                        newTask.getDueTime().toString(), newTask.getSharedBy(), newTask.getIsDone());

                reference = FirebaseDatabase.getInstance().getReference().child(user_id).child("database");
                reference.child(String.valueOf(BIGGEST_ID)).setValue(newFireTask);

                for (Friend friend : selectedFriends) {
                    reference = FirebaseDatabase.getInstance().getReference().child(friend.getKey()).child("shared");
                    reference.child(user_id).child(String.valueOf(BIGGEST_ID)).setValue(BIGGEST_ID);
                }

                BIGGEST_ID++;
                updateTaskMonitor();
            }

            @Override
            public void deleteTask(String id, String sharedBy) { }

            @Override
            public void completeTask(String id, String sharedBy) { }
        });

        updateTaskMonitor();
    }

    // Updates the lists of tasks
    // Clicking a task opens editTask where you can edit the task
    public void updateTaskMonitor() {
        TaskAdapter completedAdapter = new TaskAdapter(this, COMPLETEDTASKS);
        completedTaskLV.setAdapter(completedAdapter);
        completedTaskLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // -1 because of the header
                MainTask value = COMPLETEDTASKS.get(position - 1);
                editTask(value, true);
            }
        });

        TaskAdapter sharedAdapter = new TaskAdapter(this, SHAREDTASKS);
        sharedTaskLV.setAdapter(sharedAdapter);
        sharedTaskLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // -1 because of the header
                MainTask value = SHAREDTASKS.get(position - 1);
                editTask(value, true);
            }
        });

        TaskAdapter adapter = new TaskAdapter(this, LOCALDATABASE);
        taskLV.setAdapter(adapter);
        taskLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // -1 because of the header
                MainTask value = LOCALDATABASE.get(position - 1);
                editTask(value, false);
            }
        });
    }

    // Opens TaskDialog and updates database with given information
    // Alarm time changing not yet implemented
    public void editTask(MainTask taskToEdit, boolean sharedTask) {
        // Sends data to Taskdialog, flag=false determines that the task already exists
        taskDialog = new TaskDialog().newInstance(taskToEdit, FRIENDS, false, user_id);
        taskDialog.show(getSupportFragmentManager(), "Edit Task");
        taskDialog.setTaskResult(new TaskDialog.OnNewTaskResult(){
            public void edit(int id, String title, String description, String creation,
                             String dueDate, String dueTime,
                             int alarmD, int alarmH, int alarmM,
                             ArrayList<Friend> selectedFriends, String sharedBy){

                ArrayList<MainTask> selectedDataBase;
                if (!sharedTask) {
                    reference = FirebaseDatabase.getInstance().getReference()
                            .child(user_id).child("database");
                    selectedDataBase = LOCALDATABASE;
                } else {
                    reference = FirebaseDatabase.getInstance().getReference()
                            .child(taskToEdit.getSharedBy()).child("database");
                    selectedDataBase = SHAREDTASKS;
                }

                for(MainTask task : selectedDataBase) {
                    if(task.getMainId() == id && task.getSharedBy().equals(sharedBy)) {
                        task.setTitle(title);
                        task.setDescription(description);
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate duedate = LocalDate.parse(dueDate, dtf);
                        dtf = DateTimeFormatter.ofPattern("HH:mm");
                        LocalTime duetime = LocalTime.parse(dueTime, dtf);
                        task.setDueDate(duedate);
                        task.setDueTime(duetime);

                        FireBaseTask newEditedFireTask = new FireBaseTask(id, title, description,
                                creation, dueDate, dueTime, task.getSharedBy(), task.getIsDone());
                        reference.child(String.valueOf(id)).setValue(newEditedFireTask);
                    }
                }
                updateTaskMonitor();
            }
            public void deleteTask(String id, String sharedBy) {
                final int deleteId = Integer.parseInt(id);
                if (sharedBy.equals(user_id)) {
                    LOCALDATABASE.removeIf(obj -> obj.getMainId() == deleteId);

                    reference = FirebaseDatabase.getInstance().getReference().child(user_id)
                            .child("database");
                    reference.child(id).removeValue();
                } else {
                    SHAREDTASKS.removeIf(obj -> obj.getMainId() == deleteId);
                    reference = FirebaseDatabase.getInstance().getReference().child(user_id)
                            .child("shared").child(sharedBy);

                    reference.child(id).removeValue();

                    reference = FirebaseDatabase.getInstance().getReference().child(sharedBy)
                            .child("database");
                    reference.child(id).removeValue();
                }
                taskDialog.dismiss();
                updateTaskMonitor();
            }
            @Override
            public void completeTask(String id, String sharedBy) {
                reference = FirebaseDatabase.getInstance().getReference().child(sharedBy)
                        .child("database").child(id).child("isDone");
                reference.setValue(true);
                taskDialog.dismiss();
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
