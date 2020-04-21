package fi.tuni.thehappening;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends ArrayAdapter<Friend> {
    private Context mContext;
    private List<Friend> requestList = new ArrayList<>();
    private OnFriendItemClick callBack;

    public RequestAdapter(@NonNull Context context, @SuppressLint("SupportAnnotationUsage") @LayoutRes ArrayList<Friend> list, OnFriendItemClick listener) {
        super(context, 0, list);
        mContext = context;
        requestList = list;
        callBack = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.requestsadapter, parent,false);

        Friend currentRequest = requestList.get(position);

        TextView adapterIdTV = (TextView) listItem.findViewById(R.id.friendsAdapterKeyTV);
        adapterIdTV.setText(String.valueOf(currentRequest.getKey()));
        TextView adapterTitleTV = (TextView) listItem.findViewById(R.id.friendsAdapterMailTV);
        adapterTitleTV.setText(currentRequest.getMail());

        Button acceptButton = (Button) listItem.findViewById(R.id.friendsAdapterAccept);
        Button declineButton = (Button) listItem.findViewById(R.id.friendsAdapterDecline);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "Accept clicked");
                callBack.onAcceptClick(position);
            }
        });
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "Decline clicked");
                callBack.onDeclineClick(position);
            }
        });

        return listItem;
    }
}
