package com.fontys.practical.Authentication;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.fontys.practical.R;
import com.fontys.practical.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatDialogFragment {
    private View view;

    private LayoutInflater inflater;
    private FirebaseAuth mAuth;
    private AlertDialog.Builder dlgBuilder;

    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordRepeat;
    private EditText etUserName;
    FirebaseDatabase rootRef;
    DatabaseReference usersRef ;
    FirebaseUser currentUser;
    private  FirebaseAuth firebaseAuth;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        dlgBuilder = new AlertDialog.Builder(getActivity());
        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.register_dialog, null);

        rootRef= FirebaseDatabase.getInstance();
        usersRef = rootRef.getReference("UserNames");
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        etEmail = (EditText) view.findViewById(R.id.register_et_email);
        etUserName=(EditText) view.findViewById(R.id.register_et_username);

        etPassword = (EditText) view.findViewById(R.id.register_et_password);
        etPasswordRepeat = (EditText) view.findViewById(R.id.register_et_repeatPassword);

        dlgBuilder.setTitle("Register")
                .setView(view)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        final String regEmail = etEmail.getText().toString();
                        final String regUser = etUserName.getText().toString();
                        String password = etPassword.getText().toString();
                        String repeatedPassword = etPasswordRepeat.getText().toString();
                        final Context contect = getContext();
//                        final LatiLngi latLng1User=new LatiLngi(regUser)
                        final User userName=new User(regUser);
                        if(password.equals(repeatedPassword)) {
                            mAuth.createUserWithEmailAndPassword(regEmail, password)
                                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(regEmail).build();
//                                              usersRef.child(currentUser.getUid()).setValue(userName);
                                                // No user is signed in
//                                                Toast.makeText(this,currentUser.getUid().toString(),Toast.LENGTH_LONG).show();

                                                // lat=Double.parseDouble()
//       usersRef.setValue(latLng1);
//                                                usersRef.child(currentUser.getUid()).setValue(latLng1)
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d("User Registration", "createUserWithEmail:success");
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                //updateUI(user);
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Log.w("User Registration", "createUserWithEmail:failure", task.getException());
                                                Toast.makeText(contect, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                                //updateUI(null);
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(getContext(), "Passwords do not match.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return dlgBuilder.create();
    }

}
