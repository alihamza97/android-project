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

import com.fontys.practical.MainActivity;
import com.fontys.practical.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatDialogFragment {
    private View view;
    private MainActivity mainActivity;

    private LayoutInflater inflater;
    private FirebaseAuth mAuth;
    private AlertDialog.Builder dlgBuilder;

    private EditText etEmail;
    private EditText etPassword;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        dlgBuilder = new AlertDialog.Builder(getActivity());
        inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.login_dialog, null);
        mainActivity = ((MainActivity)getActivity());

        etEmail = (EditText) view.findViewById(R.id.login_et_email);
        etPassword = (EditText) view.findViewById(R.id.login_et_password);

        dlgBuilder.setTitle("Login")
                .setView(view)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        Thread loginThread = new Thread() {
                            @Override
                            public void run() {
                                String email = etEmail.getText().toString();
                                String password = etPassword.getText().toString();
                                final Context context = getContext();

                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Sign in success, update UI with the signed-in user's information
                                                    Log.d("User Login", "createUserWithEmail:success");
                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                    Toast.makeText(context, "Logged In.", Toast.LENGTH_SHORT).show();
                                                    mainActivity.updateUI(user);
                                                } else {
                                                    // If sign in fails, display a message to the user.
                                                    Log.w("User Login", "createUserWithEmail:failure", task.getException());
                                                    Toast.makeText(context, "Login failed.", Toast.LENGTH_SHORT).show();
                                                    //updateUI(null);
                                                }
                                            }
                                        });
                            }
                        };

                        loginThread.start();
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
