package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
        import android.view.LayoutInflater;
        import android.view.View;

        import android.widget.AdapterView;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.Toast;


//add new
public class MainActivity extends AppCompatActivity {

    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;


    List<Product> products;
    DatabaseReference databaseProducts;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        listViewProducts = (ListView) findViewById(R.id.listViewProducts);
        buttonAddProduct = (Button) findViewById(R.id.addButton);
        databaseProducts = FirebaseDatabase.getInstance().getReference("products");

        products = new ArrayList<>();

        //adding an onclicklistener to button
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });




        listViewProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = products.get(i);
                showUpdateDeleteDialog(product.getId(), product.getProductName());
                return true;
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Attach a ValueEventListener to retrieve the products from the database


                // Attach a ValueEventListener to retrieve the products from the database
            databaseProducts.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    products.clear(); // Clear the existing product list

                    for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                        Product product = productSnapshot.getValue(Product.class);
                        products.add(product);
                    }

                    // Create an adapter to display the products in the ListView
                    ProductList adapter = new ProductList(MainActivity.this, products);
                    listViewProducts.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors or failed database read operations
                }
            });
        }




    private void showUpdateDeleteDialog(final String productId, String productName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        final EditText editTextPrice  = (EditText) dialogView.findViewById(R.id.editTextPrice);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdateProduct);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDeleteProduct);

        dialogBuilder.setTitle(productName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));
                if (!TextUtils.isEmpty(name)) {
                    updateProduct(productId, name, price);
                    b.dismiss();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct(productId);
                b.dismiss();
            }
        });
    }

    private void updateProduct(String id, String name, double price) {
        DatabaseReference productRef = databaseProducts.child(id);
        Product updatedProduct = new Product(id, name, price);

        productRef.setValue(updatedProduct).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to update product", Toast.LENGTH_SHORT).show();
                }
            }

        });

    }

    private void deleteProduct(String id) {
        DatabaseReference productRef = databaseProducts.child(id);

        productRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to delete product", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void addProduct() {

        String name = editTextName.getText().toString().trim();
        double price = Double.parseDouble(editTextPrice.getText().toString().trim());

        if (!TextUtils.isEmpty(name)) {
            String id = databaseProducts.push().getKey(); // Generate a unique ID
            Product product = new Product(id, name, price);

            // Add the product to the Firebase database
            databaseProducts.child(id).setValue(product);

            // Clear the input fields
            editTextName.setText("");
            editTextPrice.setText("");

            Toast.makeText(this, "Product added to the database", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please enter a product name", Toast.LENGTH_SHORT).show();
        }
    }



    }
