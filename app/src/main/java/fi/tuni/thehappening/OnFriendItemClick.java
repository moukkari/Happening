package fi.tuni.thehappening;

/**
 * An interface for listening Friend objects stuff on FriendsDialog and TaskDialog
 */
public interface OnFriendItemClick {
    void onAcceptClick (int position);
    void onDeclineClick (int position);
    void onCheckBoxClicked (int position, boolean flag);
}
