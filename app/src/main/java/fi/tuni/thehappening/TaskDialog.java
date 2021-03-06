package fi.tuni.thehappening;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatDialogFragment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;

public class TaskDialog extends AppCompatDialogFragment implements OnFriendItemClick {
    private ArrayList<Friend> friends;
    private ArrayList<Friend> selectedFriends = new ArrayList<Friend>(0);
    private EditText remindInHours;
    private EditText remindInDays;
    private EditText remindInMinutes;
    private TextView idTV;
    private EditText descET;
    private EditText titleET;
    private TextView creationDate;
    private DatePickerDialog.OnDateSetListener dateListener;
    private TimePickerDialog.OnTimeSetListener timeListener;
    private LocalDate today = LocalDate.now();
    private LocalTime timeNow = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
    private OnNewTaskResult taskResult; // callback
    private int taskId;
    private String task_user_id;
    private String current_user_id;
    private ListView friendsLV;

    // Fetches task's data if it exists (flag is false) and sends them onward to onCreateDialog
    public static TaskDialog newInstance(MainTask taskToEdit, ArrayList<Friend> friends,
                                         boolean flag, String current_user_id) {
        TaskDialog fragment = new TaskDialog();
        int id = taskToEdit.getMainId();
        String title = taskToEdit.getTitle();
        String desc = taskToEdit.getDesc();
        String creation = taskToEdit.getCreationDate().toString();
        String dueDate = taskToEdit.getDueDate().toString();
        String dueTime = taskToEdit.getDueTime().toString();
        Boolean isDone = taskToEdit.getIsDone();

        Bundle bundle = new Bundle();
        bundle.putBoolean("flag", flag);
        bundle.putInt("id", id);
        bundle.putParcelableArrayList("friends", friends);
        bundle.putString("userId", taskToEdit.getSharedBy());
        bundle.putString("currentUser", current_user_id);

        if (!flag) {
            bundle.putString("title", title);
            bundle.putString("desc", desc);
            bundle.putString("creation", creation);
            bundle.putString("dueDate", dueDate);
            bundle.putString("dueTime", dueTime);
            bundle.putBoolean("isDone", isDone);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    // Sets up the pop-up dialog for task editing/creating
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.newtaskdialog, null);

        creationDate = (TextView) view.findViewById(R.id.dateToday);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        creationDate.setText(today.format(formatter));

        friendsLV = (ListView) view.findViewById(R.id.taskFriendMonitor);

        titleET = (EditText) view.findViewById(R.id.title);
        descET = (EditText) view.findViewById(R.id.description);

        final TextView dueDateTV = (TextView) view.findViewById(R.id.dueDateTV);
        final TextView dueTimeTV = (TextView) view.findViewById(R.id.dueTimeTV);
        task_user_id = getArguments().getString("userId");
        current_user_id = getArguments().getString("currentUser");
        final boolean newTaskFlag = getArguments().getBoolean("flag");
        idTV = (TextView) view.findViewById(R.id.idTitle);
        taskId = getArguments().getInt("id");
        final String id = String.valueOf(getArguments().getInt("id"));
        idTV.setText("Task id: " + id);

        friends = getArguments().getParcelableArrayList("friends");
        FriendAdapter friendAdapter = new FriendAdapter(getContext(), friends, this);
        friendsLV.setAdapter(friendAdapter);

        boolean isDone = getArguments().getBoolean("isDone");

        remindInHours = (EditText) view.findViewById(R.id.remindHours);
        remindInDays = (EditText) view.findViewById(R.id.remindDays);
        remindInMinutes = (EditText) view.findViewById(R.id.remindMinutes);

        // Checks if the task is new or editable
        if (!newTaskFlag) {
            String title = getArguments().getString("title");
            String desc = getArguments().getString("desc");
            String creation = getArguments().getString("creation");
            String dueDate = getArguments().getString("dueDate");
            String dueTime = getArguments().getString("dueTime");

            titleET.setText(title);
            descET.setText(desc);
            creationDate.setText(creation);
            dueDateTV.setText(dueDate);
            dueTimeTV.setText(dueTime);

            if (!isDone) {
                Button completeButt = (Button) view.findViewById(R.id.completeTaskButton);
                completeButt.setVisibility(View.VISIBLE);
                completeButt.setOnClickListener(v -> {
                    taskResult.completeTask(id, task_user_id);
                });
            }
            if (current_user_id.equals(task_user_id)) {
                Button deleteButt = (Button) view.findViewById(R.id.delete);
                deleteButt.setVisibility(View.VISIBLE);
                deleteButt.setOnClickListener(v -> taskResult.deleteTask(id, task_user_id));
            }
        } else {
            // The following is because alarm time editing is not yet implemented
            TextView remindTV = (TextView) view.findViewById(R.id.reminderTV);
            remindTV.setVisibility(View.VISIBLE);
            remindInHours.setVisibility(View.VISIBLE);
            remindInDays.setVisibility(View.VISIBLE);
            remindInMinutes.setVisibility(View.VISIBLE);


            dueDateTV.setText(today.plusDays(7).format(formatter));
            dueTimeTV.setText(timeNow.toString());
        }

        // Creates a DatePickerDialog to pick the date when Due date Textview is clicked
        dueDateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        getContext(),
                        android.R.style.Theme_DeviceDefault_Dialog,
                        dateListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        // Sets the date picked to the textview and adds zeros if month or day is under 10
        dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                String syear, smonth, sday;
                if (month < 10) { smonth = "0" + month; } else { smonth = String.valueOf(month); }
                if (dayOfMonth < 10) { sday = "0" + month; } else { sday = String.valueOf(dayOfMonth); }
                String dateSet = year + "-" + smonth + "-" + sday;
                dueDateTV.setText(dateSet);
            }
        };

        // Creates a TimePickerDialog to pick the time when Due time Textview is clicked
        dueTimeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);

                TimePickerDialog timeDialog = new TimePickerDialog(
                        getContext(),
                        android.R.style.Theme_DeviceDefault,
                        timeListener,
                        hour, minute, true);


                timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                timeDialog.show();
            }
        });
        // Sets the time picked to the textview and adds zeros if hour or minute is under 10
        timeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                String shour, sminute;
                if (hour < 10) { shour = "0" + hour; } else { shour = String.valueOf(hour); }
                if (minute < 10) { sminute = "0" + minute; } else { sminute = String.valueOf(minute); }
                String timeSet = shour + ":" + sminute;
                dueTimeTV.setText(timeSet);
            }
        };

        // Sets up the title and the needed buttons for the dialog
        builder.setView(view)
                .setTitle("Task manager")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = String.valueOf(titleET.getText());
                        String description = String.valueOf(descET.getText());
                        String creation = creationDate.getText().toString();
                        String dueDate = dueDateTV.getText().toString();
                        String dueTime = dueTimeTV.getText().toString();

                        // if user doesn't give any value to alarm reminder they're set to 0
                        int ad, ah, am;
                        if (remindInDays.getText().toString().length() > 0) {
                            ad = Integer.parseInt(remindInDays.getText().toString());
                        } else { ad = 0; }
                        if (remindInHours.getText().toString().length() > 0) {
                            ah = Integer.parseInt(remindInHours.getText().toString());
                        } else { ah = 0; }
                        if (remindInMinutes.getText().toString().length() > 0) {
                            am = Integer.parseInt(remindInMinutes.getText().toString());
                        } else { am = 0; }

                        // sends the given data to MainActivity
                        taskResult.edit(taskId, title, description, creation, dueDate, dueTime,
                                    ad, ah, am, selectedFriends, task_user_id);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }

    // The following method and interface able the discussion between MainActivity and TaskDialog
    public void setTaskResult(OnNewTaskResult result){
        taskResult = result;
    }
    public interface OnNewTaskResult{
        void edit(int id, String title, String description,
                  String creation, String dueDt, String dueTm,
                  int alarmDs, int alarmHr, int alarmMin,
                  ArrayList<Friend> selectedFriends, String sharedBy);
        void deleteTask(String id, String sharedBy);
        void completeTask(String id, String sharedBy);
    }


    @Override
    public void onAcceptClick(int position) { }

    @Override
    public void onDeclineClick(int position) { }

    @Override
    public void onCheckBoxClicked(int position, boolean flag) {
        if (flag) {
            Friend tmpFriend = friends.get(position);
            selectedFriends.add(tmpFriend);
        } else {
            Friend tmpFriend = friends.get(position);
            selectedFriends.remove(tmpFriend);
        }
    }


}
