package com.kuro.daksh.eaititserver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.widget.FButton;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btnSignIn)
    FButton btnSignIn;
    TextView txtSlogan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signIn = new Intent(MainActivity.this,SignInActivity.class);
                startActivity(signIn);
            }
        });


    }
}
