package com.example.avherramientasappmovil;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.avherramientasappmovil.Common.Common;
import com.example.avherramientasappmovil.Fragments.HomeFragment;
import com.example.avherramientasappmovil.Fragments.ShopingFragment;
import com.example.avherramientasappmovil.Model.User;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class HomeActivity extends AppCompatActivity {
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    BottomSheetDialog bottomSheetDialog;

    CollectionReference userRef;

    AlertDialog dialog;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(HomeActivity.this);


        //Init

        userRef= FirebaseFirestore.getInstance().collection("User");
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        //Comprobar intent, si login = true, habilita el acceso completo

        //si login= false , simplemente deja que el usuario ingrese solo a ver

        if(getIntent() != null){
            boolean isLogin=getIntent().getBooleanExtra(Common.IS_LOGIN, false);
            if(isLogin){
                //Chequear si usuario existe

                dialog.show();

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(final Account account) {
                        if(account != null){
                            DocumentReference currentUser=userRef.document(account.getPhoneNumber().toString());
                            currentUser.get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()){
                                                DocumentSnapshot userSnapShot=task.getResult();
                                                if (!userSnapShot.exists())
                                                {

                                                    showUpdateDialog(account.getPhoneNumber().toString());
                                                }
                                                else{
                                                    //Si el usuario ya está disponible en nuestro sistema.
                                                    Common.currentUser=userSnapShot.toObject(User.class);
                                                    bottomNavigationView.setSelectedItemId(R.id.action_home);
                                                }
                                                if(dialog.isShowing())
                                                    dialog.dismiss();



                                            }
                                        }
                                    });

                        }

                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Toast.makeText(HomeActivity.this, ""+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }
        }


        //View
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment =null;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.action_home)
                    fragment=new HomeFragment();
                else if (menuItem.getItemId()==R.id.action_shopping)
                    fragment=new ShopingFragment();


                return loadFragment(fragment);
            }
        });



    }

    private boolean loadFragment(Fragment fragment) {
        if(fragment!=null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void showUpdateDialog(final String telefono) {



        //inicio dialos
        bottomSheetDialog=new BottomSheetDialog(this);
        bottomSheetDialog.setTitle("Un paso mas!!");
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);
        View sheetView=getLayoutInflater().inflate(R.layout.layout_informacion,null);

        Button btn_ingresarDatos = (Button)sheetView.findViewById(R.id.btn_ingresarDatos);
        final TextInputEditText edt_nombre=(TextInputEditText)sheetView.findViewById(R.id.edt_nombre);
        final TextInputEditText edt_direccion=(TextInputEditText)sheetView.findViewById(R.id.edt_direccion);

        btn_ingresarDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!dialog.isShowing())
                    dialog.show();

                final User user = new User(edt_nombre.getText().toString(),
                        edt_direccion.getText().toString(),
                        telefono);
                userRef.document(telefono)
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                bottomSheetDialog.dismiss();
                                if(dialog.isShowing())
                                    dialog.dismiss();

                                Common.currentUser=user;
                                bottomNavigationView.setSelectedItemId(R.id.action_home);

                                Toast.makeText(HomeActivity.this,"Gracias",Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(dialog.isShowing())
                            dialog.dismiss();
                        bottomSheetDialog.dismiss();
                        Toast.makeText(HomeActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

    }

}
