package com.example.sayem;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Sayem Mahmud on 6/11/20.
 */

public class App extends Application {

    public String loggedInUserPhone;

    private static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public String getLoggedInUserPhone(){
        return loggedInUserPhone;
    }

    public void setLoggedInUserPhone(String phone){
        this.loggedInUserPhone  = phone;
    }

    public void logOut(){
        SharedPreferences sharedPref = getSharedPreferences("USER_PREFERENCE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("LOGGED_IN_USER_PHONE", null);
        editor.apply();
    }
}
