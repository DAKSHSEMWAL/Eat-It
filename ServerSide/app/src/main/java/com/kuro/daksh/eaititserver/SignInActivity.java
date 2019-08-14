package com.kuro.daksh.eaititserver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kuro.daksh.eaititserver.Common.Common;
import com.kuro.daksh.eaititserver.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

public class SignInActivity extends AppCompatActivity {

    EditText editPhone,editPassword;
    Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editPassword = (MaterialEditText)findViewById(R.id.editPassword);
        editPhone    = (MaterialEditText)findViewById(R.id.editPhone);

        btnSignIn    = (Button)findViewById(R.id.btnSignIn1);

        //Init Firebase

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ProgressDialog mDialog = new ProgressDialog(SignInActivity.this);
                mDialog.setMessage("Please Wait ....");
                mDialog.show();

                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.child(editPhone.getText().toString()).exists()) {
                            mDialog.dismiss();
                            //Get User Information
                            User user = dataSnapshot.child(editPhone.getText().toString()).getValue(User.class);
                                user.setPhone(editPhone.getText().toString());
                            if (user.getPassword().equals(editPassword.getText().toString())) {
                                Intent homeIntent = new Intent(SignInActivity.this, HomeActivity.class);
                                Common.currentUser=user;
                                startActivity(homeIntent);
                                finish();
                            } else {
                                Toast.makeText(SignInActivity.this, "Wrong Password!!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            mDialog.dismiss();
                            Toast.makeText(SignInActivity.this, "User Does Not Exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}
