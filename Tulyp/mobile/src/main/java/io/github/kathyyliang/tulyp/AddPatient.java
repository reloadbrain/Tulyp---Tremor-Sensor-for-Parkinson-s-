package io.github.kathyyliang.tulyp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class AddPatient extends AppCompatActivity {
    User user = TulypApplication.mUser;
    MyFirebase myfirebase = TulypApplication.mFirebase;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add Patient");
        setSupportActionBar(toolbar);
    }

    public void addPatientView(View view) {
        EditText emailField = (EditText) findViewById(R.id.editText);
        if (emailField != null && emailField.getText() != null && emailField.getText().length() > 0) {
            addPatient(emailField.getText().toString());
        }
    }

    /**
     * THIS IS ASYNC
     * @param email
     */
    public void addPatient(String email) {
        Query queryRef = myfirebase.getFirebaseRef().child("Users").limitToFirst(2).orderByChild("email").equalTo(email);
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getValue();
                String patID;
                if (map == null) {
                    patID = null;
                } else {
                    String[] ids = map.keySet().toArray(new String[1]);
                    patID = ids[0];
                }
                if (patID == null || patID.equals("")) {
                    new AlertDialog.Builder(context)
                            .setMessage("No patient with that email found.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    Log.d("AddPatient", "No patient found for input email");
                    return;
                }
                List<String> patIds = user.getPatientIDs();
                boolean hasPatient = false;
                for (String id : patIds) {
                    if (id.equals(patID)) {
                        hasPatient = true;
                        break;
                    }
                }

                if (!hasPatient) {
                    user.addPatientID(patID);
                    Log.d("AddPatient", "Successfully found patient's ID" + patID);
                    myfirebase.setNewUserInfo(user);
                    CharSequence text = "Patient Added!";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration); //not sure if this way of passing in context is correct.
                    toast.show();
                }

                Intent intent = new Intent(getBaseContext(), DoctorView.class);
                startActivity(intent);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("Firebase", "Failed to retrieve User data\n" + firebaseError);
            }
        });
    }
}
