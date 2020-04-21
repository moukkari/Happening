package fi.tuni.thehappening;

import android.os.Parcel;
import android.os.Parcelable;

public class Friend implements Parcelable {
    String key;
    String mail;
    public Friend(String key, String mail) {
        this.key = key;
        this.mail = mail;
    }
    public String getKey() {
        return this.key;
    }
    public String getMail() {
        return this.mail;
    }


    // Parcelling part
    public Friend(Parcel in){
        String[] data = new String[2];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.key = data[0];
        this.mail = data[1];
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.key, this.mail});
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Friend createFromParcel(Parcel in) {
            return new Friend(in);
        }

        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };

    public String toString() {
        return this.getMail();
    }
}
