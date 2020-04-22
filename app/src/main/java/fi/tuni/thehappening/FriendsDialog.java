package fi.tuni.thehappening;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A dialog for sending and accepting friend requests
 * You can see your current friends also
 */

public class FriendsDialog extends AppCompatDialogFragment implements OnFriendItemClick {
    private ListView requestLV;
    private ListView friendsLV;
    private ArrayList<Friend> requestList = new ArrayList<Friend>(0);
    private ArrayList<Friend> friendList = new ArrayList<Friend>(0);
    private OnNewFriends requestsResult;
    private DatabaseReference reference;
    private String user_id;
    private String user_mail;


    public static FriendsDialog newInstance(ArrayList requests, ArrayList friends, String user_id, String user_mail) {
        FriendsDialog fragment = new FriendsDialog();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("requests", requests);
        bundle.putParcelableArrayList("friends", friends);
        bundle.putString("user_id", user_id);
        bundle.putString("user_mail", user_mail);

        fragment.setArguments(bundle);

        return fragment;
    }
                                         @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.friendsdialog, null);

        requestLV = (ListView) view.findViewById(R.id.requestMonitor);
        friendsLV = (ListView) view.findViewById(R.id.friendsMonitor);

        requestList = getArguments().getParcelableArrayList("requests");
        RequestAdapter requestAdapter = new RequestAdapter(getContext(), requestList, this);
        requestLV.setAdapter(requestAdapter);

        friendList = getArguments().getParcelableArrayList("friends");
        ArrayAdapter<Friend> friendAdapter = new ArrayAdapter<Friend>(getContext(), R.layout.support_simple_spinner_dropdown_item, friendList);
        friendsLV.setAdapter(friendAdapter);

        user_id = getArguments().getString("user_id");
        user_mail = getArguments().getString("user_mail");


        EditText friendET = (EditText) view.findViewById(R.id.friendRequest);
        Button requestButton = (Button) view.findViewById(R.id.requestButton);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String friendMail = friendET.getText().toString();
                reference = FirebaseDatabase.getInstance().getReference().child("users");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                            if (childSnap.getValue().toString().equals(friendMail)) {
                                DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference()
                                        .child(childSnap.getKey()).child("requests");
                                friendRef.child(user_id).setValue(user_mail);
                                Toast.makeText(getContext(), "Sent friend request", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "No match", Toast.LENGTH_SHORT).show();

                                // Asks user if s/he wants to send an email invitation to use the app
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("User not found.");
                                builder.setMessage("Want to send an email invitation?");
                                builder.setIcon(R.drawable.common_google_signin_btn_icon_light_focused);
                                builder.setPositiveButton("Yes", (dialog, id) -> {
                                    dialog.dismiss();
                                    Intent mailIntent = new Intent(Intent.ACTION_VIEW);
                                    Uri data = Uri.parse("mailto:?subject="
                                            + "Join me in The Happening"
                                            + "&body=" + "Download The Happening from Google Play"
                                            + "&to=" + friendMail);
                                    mailIntent.setData(data);
                                    startActivity(Intent.createChooser(mailIntent, "Send mail..."));

                                });
                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


        builder.setView(view)
            .setTitle("Friends manager")
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestsResult.setNewFriends(requestList, friendList);
                }
            });

        return builder.create();
    }

    @Override
    public void onAcceptClick(int position) {
        Friend friendObj = requestList.get(position);
        friendList.add(friendObj);

        requestList.remove(friendObj);
        RequestAdapter requestAdapter = new RequestAdapter(getContext(), requestList, this);
        requestLV.setAdapter(requestAdapter);
    }

    @Override
    public void onDeclineClick(int position) {
        requestList.remove(position);
        RequestAdapter requestAdapter = new RequestAdapter(getContext(), requestList, this);
        requestLV.setAdapter(requestAdapter);
    }

    // Provides discussion between MainActivity and FriendsDialog
    @Override
    public void onCheckBoxClicked(int position, boolean flag) { }
    public void setFriends(OnNewFriends result){
        requestsResult = result;
    }
    public interface OnNewFriends{
        void setNewFriends(ArrayList<Friend> requestArray, ArrayList<Friend> friendArray);
    }
}
