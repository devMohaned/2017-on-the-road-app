package com.ride.travel.Utils;

import android.app.Activity;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by bestway on 09/07/2018.
 */

public class AppUtils {

    public static void hideSoftKeyboard(Activity activity) {
        try{
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }catch (NullPointerException e)
        {
            Log.d(activity.toString(), "No Keyboard Found " + e.toString());
        }
    }




}
