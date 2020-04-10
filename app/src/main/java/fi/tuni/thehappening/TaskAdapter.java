package fi.tuni.thehappening;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends ArrayAdapter<MainTask> {
    private Context mContext;
    private List<MainTask> taskList = new ArrayList<>();

    public TaskAdapter(@NonNull Context context, @SuppressLint("SupportAnnotationUsage") @LayoutRes ArrayList<MainTask> list) {
        super(context, 0, list);
        mContext = context;
        taskList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.taskadapter, parent,false);

        MainTask currentTask = taskList.get(position);

        TextView adapterIdTV = (TextView) listItem.findViewById(R.id.adapterIdTV);
        adapterIdTV.setText(String.valueOf(currentTask.getMainId()));

        TextView adapterTitleTV = (TextView) listItem.findViewById(R.id.adapterTitleTV);
        adapterTitleTV.setText(currentTask.getTitle());

        TextView adapterDueDateTV = (TextView) listItem.findViewById(R.id.adapterDueDate);
        adapterDueDateTV.setText(currentTask.getDueDate().toString() + "\n" +
                currentTask.getDueTime().toString());

        return listItem;
    }
}
