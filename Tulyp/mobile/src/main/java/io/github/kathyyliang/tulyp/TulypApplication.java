package io.github.kathyyliang.tulyp;

import android.app.Application;
import com.firebase.client.Firebase;

/**
 * We can use this to share methods and variables across activities.
 * Created by TK on 4/28/16.
 */
public class TulypApplication extends Application {
    public static MyFirebase mFirebase;
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        mFirebase = new MyFirebase();
    }
}
