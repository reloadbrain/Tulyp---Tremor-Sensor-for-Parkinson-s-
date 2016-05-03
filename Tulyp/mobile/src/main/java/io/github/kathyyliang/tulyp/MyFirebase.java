package io.github.kathyyliang.tulyp;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles all authentication and calls to this app's Firebase backend
 * Created by TK on 4/28/16.
 */
public class MyFirebase {
    private Firebase mfirebase;
    private String uid;

    public MyFirebase() {
        mfirebase = new Firebase("https://tulyp.firebaseio.com");
    }

    public void createUser(final String email, final String password) {
        mfirebase.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                Log.d("Firebase", "Create User successful!");
                TulypApplication.mUser = new User(email);
                Log.d("firebase", "done with setting user");
                login(email, password);
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                // there was an error
                Log.d("Firebase", "Create User unsuccessful. >:(\n" + firebaseError);
            }
        });
    }

    public Firebase getFirebaseRef() {
        return mfirebase;
    }

    /**
     * Using email and password, authenticate to Firebase. If login is successful, fetch user data.
     *
     * @param email:    String email from user input.
     * @param password: String password from user input.
     */
    public void login(String email, String password) {
        //todo this Firebase instance will have to be moved to a global application context class so other activities can use it.
        mfirebase.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d("Firebase", "Login successful!");
                uid = authData.getUid();
                mfirebase.child("Users").child(uid).keepSynced(true); //keep local data synced even when offline.
//                System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
//                CharSequence text = "Login Successful!";
//                int duration = Toast.LENGTH_SHORT;
//                Toast toast = Toast.makeText(context, text, duration); //not sure if this way of passing in context is correct.
//                toast.show();
            }

            @Override
            public void onAuthenticationError(FirebaseError error) {
                // there was an error
                switch (error.getCode()) {
                    case FirebaseError.USER_DOES_NOT_EXIST:
                        // todo handle a non existing user. Show alert dialog? This is up to Frontend
//                        new AlertDialog.Builder()
//                                .setMessage("Woops! An account under this email does not exist.\nCheck your email address or sign up to Tulyp.")
//                                .setIcon(android.R.drawable.ic_dialog_alert)
//                                .show();
//                        break;
                    case FirebaseError.INVALID_PASSWORD:
                        // handle an invalid password
                        CharSequence text = "Incorrect password";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(TulypApplication.getAppContext(), text, duration);
                        toast.show();
                        break;
                    default:
                        // handle other errors
                        break;
                }
            }
        });
    }

    /**
     * Monitors for change in authentication.
     */
    public void authListener() {
        mfirebase.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    // user is logged in
                    fetchUserData();
                } else {
                    // user is not logged in
                    //TODO: kick off user and go back to sign in activity
                }
            }
        });
    }

    public boolean isAuthenticated() {
        AuthData authData = mfirebase.getAuth();
        if (authData != null) {
            // user authenticated
            return true;
        } else {
            // no user authenticated
            return false;
        }
    }

    /**
     * @return true if successfully unauthenticated from Firebase
     */
    public boolean logout() {
        uid = null;
        if (isAuthenticated()) {
            mfirebase.unauth();
            return !isAuthenticated();
        }
        return true;
    }

    public String getUID() {
        return uid;
    }

    public void setUID(String uid) {
        this.uid = uid;
    }

    /**
     * Stores user data to Firebase.
     * Warning: This will overwrite and potentially delete data!
     * @param user: a User class with user information
     */
    public void setNewUserInfo(User user) {
        if (user == null) {
            Log.d("Firebase", "Trying to set a null user");
        }
        if (uid != null) {
            Firebase userRef = mfirebase.child("Users").child(uid);
            userRef.setValue(user);
        }
    }

    /**
     * Adds info to current authenticated User's database.
     *
     * @param info a Map with each entry containing information title as key and user specific information as value.
     *             You can put multiple entries in the map.
     *             ex. "name" as key; "McDonald" as value.
     */
    public void addUserInfo(HashMap<String, Object> info) {
        Firebase userRef = mfirebase.child("Users").child(uid);
        userRef.updateChildren(info);
    }

    /**
     * fetches the currently authenticated user's profile data.
     */
    public void fetchUserData() {
        mfirebase.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                TulypApplication.mUser = snapshot.getValue(User.class);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("Firebase", "Failed to retrieve User data\n" + firebaseError);
            }
        });
    }


}