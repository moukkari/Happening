package fi.tuni.thehappening;

public interface OnFriendItemClick {
    void onAcceptClick (int position);
    void onDeclineClick (int position);
    void onCheckBoxClicked (int position, boolean flag);
}
