package com.daksh.kuro.eatit.Classes;

import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daksh.kuro.eatit.Common.Common;
import com.daksh.kuro.eatit.Database.Database;
import com.daksh.kuro.eatit.Model.Order;
import com.daksh.kuro.eatit.Model.Request;
import com.daksh.kuro.eatit.R;
import com.daksh.kuro.eatit.ViewHolder.CartAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.widget.FButton;

public class CartActivity extends AppCompatActivity {

    @BindView(R.id.list_cart)
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    @BindView(R.id.total)
    TextView txtTotalPrice;
    @BindView(R.id.btnPlaceOrder)
    FButton btnPlace;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        ButterKnife.bind(this);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //init
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialogue();
            }
        });
        
        loadListFood();
    }

    private void showAlertDialogue() {
        AlertDialog.Builder alertDialogue = new AlertDialog.Builder(CartActivity.this);
        alertDialogue.setTitle("One More Step!");
        alertDialogue.setMessage("Enter Your Address:");

        final EditText editAddress = new EditText(CartActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editAddress.setLayoutParams(lp);
        alertDialogue.setView(editAddress);
        alertDialogue.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialogue.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        editAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        cart
                );

                requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);

                new Database(getBaseContext()).cleanCart();
                Toast.makeText(CartActivity.this,"Thanl You,Oder Placed",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        alertDialogue.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialogue.show();
    }

    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart,this);
        recyclerView.setAdapter(adapter);

        int total=0;
        for (Order order:cart)
        {
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
            Locale locale = new Locale("en","US");
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);

            txtTotalPrice.setText(numberFormat.format(total));
        }
    }
}
