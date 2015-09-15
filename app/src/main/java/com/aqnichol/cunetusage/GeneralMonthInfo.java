package com.aqnichol.cunetusage;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * GeneralMonthInfo stores general information about data usage on a given month.
 */
public class GeneralMonthInfo implements Parcelable {

    public long totalUsageMB;
    public long freeUsageMB;
    public long billableUsageMB;
    public String billingRate;
    public String totalCharge;

    public GeneralMonthInfo() {
    }

    public GeneralMonthInfo(Parcel p) {
        totalUsageMB = p.readLong();
        freeUsageMB = p.readLong();
        billableUsageMB = p.readLong();
        billingRate = p.readString();
        totalCharge = p.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(totalUsageMB);
        dest.writeLong(freeUsageMB);
        dest.writeLong(billableUsageMB);
        dest.writeString(billingRate);
        dest.writeString(totalCharge);
    }

    public static final Parcelable.Creator<GeneralMonthInfo> CREATOR
            = new Parcelable.Creator<GeneralMonthInfo>() {
        public GeneralMonthInfo createFromParcel(Parcel in) {
            return new GeneralMonthInfo(in);
        }

        public GeneralMonthInfo[] newArray(int size) {
            return new GeneralMonthInfo[size];
        }
    };

}
