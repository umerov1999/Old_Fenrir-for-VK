package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

public class FileItem implements Parcelable {

    public static final Creator<FileItem> CREATOR = new Creator<FileItem>() {
        @Override
        public FileItem createFromParcel(Parcel in) {
            return new FileItem(in);
        }

        @Override
        public FileItem[] newArray(int size) {
            return new FileItem[size];
        }
    };
    public boolean directory;
    public String file;
    public String details;
    public String path;
    public int icon;
    public long Modification;
    public boolean canRead;

    public FileItem(boolean directory, String file, String details, int icon, long Modification, String path, boolean canRead) {
        this.directory = directory;
        this.file = file;
        this.details = details;
        this.path = path;
        this.icon = icon;
        this.canRead = canRead;
        this.Modification = Modification;
    }

    protected FileItem(Parcel in) {
        directory = in.readByte() != 0;
        file = in.readString();
        details = in.readString();
        path = in.readString();
        icon = in.readInt();
        Modification = in.readLong();
        canRead = in.readByte() != 0;
    }

    @NotNull
    @Override
    public String toString() {
        return file;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (directory ? 1 : 0));
        dest.writeString(file);
        dest.writeString(details);
        dest.writeString(path);
        dest.writeInt(icon);
        dest.writeLong(Modification);
        dest.writeByte((byte) (canRead ? 1 : 0));
    }
}
