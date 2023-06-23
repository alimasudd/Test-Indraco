package id.alimasudd.testindraco.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class SessionManager {

    SharedPreferences pref;

    Editor editor;

    Context _context;

    int PRIVATE_MODE = 0;

    public static final String KEY_IMAGE = "image";

    public static final String KEY_LATITUDE = "latitude";

    public static final String KEY_LONGITUDE = "longitude";

    private static final String PREF_NAME = "indraco";


    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public String getImage() {

        return pref.getString(KEY_IMAGE, null);
    }

    public void setImage(String image) {
        editor.putString(KEY_IMAGE, image);
        editor.commit();
    }


    public String getLatitude() {

        return pref.getString(KEY_LATITUDE, null);
    }

    public void setLatitude(String latitude) {
        editor.putString(KEY_LATITUDE, latitude);
        editor.commit();
    }

    public String getLongitude() {

        return pref.getString(KEY_LONGITUDE, null);
    }

    public void setLongitude(String longitude) {
        editor.putString(KEY_LONGITUDE, longitude);
        editor.commit();
    }
}