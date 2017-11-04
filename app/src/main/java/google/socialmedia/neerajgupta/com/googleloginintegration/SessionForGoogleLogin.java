package google.socialmedia.neerajgupta.com.googleloginintegration;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by USer on 02-11-2017.
 */

public class SessionForGoogleLogin {
    private SharedPreferences pref;

    //Editor reference for Shared preferences
    private SharedPreferences.Editor editor;

    // Context
    private Context _context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREFER_NAME = "AndroidExamplePref_Buss";

    // All Shared Preferences Keys
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_ID = "id";


    // Constructor
    public SessionForGoogleLogin(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //Create login session
    public void createUserLoginSession(boolean isLogin) {
        // Storing login value as TRUE
        editor.putBoolean(IS_USER_LOGIN, isLogin);

        editor.commit();
    }

    // Check for login
    public boolean isUserLoggedIn() {

        return pref.getBoolean(IS_USER_LOGIN, false);
    }


}
