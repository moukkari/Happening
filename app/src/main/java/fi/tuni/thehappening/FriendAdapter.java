package fi.tuni.thehappening;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom adapter to view your friends and choose them with a checkbox
 * for sharing tasks with
 */

public class FriendAdapter extends ArrayAdapter<Friend> {
    private Context mContext;
    private List<Friend> friendList = new ArrayList<>();
    private OnFriendItemClick callback;

    public FriendAdapter(@NonNull Context context, ArrayList<Friend> friends, OnFriendItemClick listener) {
        super(context, 0, friends);
        mContext = context;
        friendList = friends;
        callback = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.friendadapter, parent,false);

        Friend currentFriend = friendList.get(position);

        TextView friendMailTV = (TextView) listItem.findViewById(R.id.friendMailTV);
        friendMailTV.setText(currentFriend.getMail());

        CheckBox friendCheckBox = (CheckBox) listItem.findViewById(R.id.friendCheckbox);

        friendCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    callback.onCheckBoxClicked(position, true);
                } else {
                    callback.onCheckBoxClicked(position, false);
                }
            }
        });


        return listItem;
    }
}
