package com.example.sayem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class UserDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    static final float END_SCALE =0.7f;
   // Button btnLogOut;
    WebView webview;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView menuIcon;
    ImageView News;
    ImageView Hospital;
    ImageView Affected;
    ImageView Location;
    TextView disaster;
    TextView vaccineupdate;
    SearchView searchview;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String TAG = "UserDashboard";

    LinearLayout contentView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_dashboard);
       // btnLogOut= findViewById(R.id.button2);
        drawerLayout=findViewById(R.id.drawer_layout);
        navigationView=findViewById(R.id.navigation_view);
        menuIcon=findViewById(R.id.menu_icon);
        contentView=findViewById(R.id.content);
        News=findViewById(R.id.news);
        Hospital=findViewById(R.id.hospital);
        Affected=findViewById(R.id.affected);
        Location=findViewById(R.id.location);
        searchview=findViewById(R.id.searchview);
        webview=findViewById(R.id.webview);
        disaster=findViewById(R.id.disaster);
        vaccineupdate=findViewById(R.id.vaccineupdate);
        navigationDrawer();

        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                webview.loadUrl("https://www.google.com/search?q="+searchview.getQuery());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        Location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intToMap= new Intent (UserDashboard.this, MapsActivity.class);
                startActivity(intToMap);
            }
        });

        News.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            gotoUrl("https://www.who.int/bangladesh/emergencies/coronavirus-disease-(covid-19)-update");
            }
        });
       Hospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUrl("https://tbsnews.net/coronavirus-chronicle/covid-19-bangladesh/names-labs-contact-numbers-coronavirus-tests-67078");
            }
        });
        Affected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUrl("https://www.worldometers.info/coronavirus/");
            }
        });
        disaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUrl("https://www.google.com/search?q=disaster+of+corona&client=opera&hs=r5U&source=lnms&tbm=nws&sa=X&ved=2ahUKEwjWzJiq8ITuAhXmqksFHV-6A0cQ_AUoA3oECBAQBQ&biw=1326&bih=627");
            }
        });
        vaccineupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUrl("https://www.google.com/search?q=corona+vaccine+update+worldwide&client=opera&hs=Y2U&source=lnms&tbm=nws&sa=X&ved=2ahUKEwiLoLrI74TuAhWTXSsKHbGLCHwQ_AUoAXoECAUQAw&biw=1326&bih=627");
            }
        });


        View actionView = navigationView.getMenu().findItem(R.id.nav_covid_status).getActionView();
        final SwitchCompat drawerSwitch = actionView.findViewById(R.id.sw_covid_status);

        String phone = FirestoreUtil.getInstance().getCurrentUser().getPhoneNumber();

        if (phone != null && !phone.isEmpty()){
            final DocumentReference docRef = FirestoreUtil.getInstance()
                    .getDocumentRef().collection("users").document(phone);
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Map<String, Object> map = snapshot.getData();
                        if (map != null){
                            Boolean status =(Boolean) map.get("status");
                            if (status != null)
                                drawerSwitch.setChecked(status);
                        }
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                }
            });
        }

        drawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendConditionStatus(isChecked);
            }
        });
    }

    private void sendConditionStatus(boolean status){

        //check if user is logged in
        if (FirestoreUtil.getInstance().getCurrentUser() == null){
            return;
        }

        // user sends one's current condition of COVID situation to the db
        String phone = FirestoreUtil.getInstance().getCurrentUser().getPhoneNumber();
        if (phone != null && !phone.isEmpty()){
            FirestoreUtil.getInstance().getDocumentRef().collection("users")
                    .document(phone).update("status", status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener(){
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
        }
    }

    private void gotoUrl(String s) {
        Uri uri = Uri.parse(s);
        startActivity(new Intent(Intent.ACTION_VIEW,uri));
    }

    //Navigation Drawer Function
    private void navigationDrawer() {
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawerLayout.isDrawerVisible(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        animateNavigationDrawer();
    }

    private void animateNavigationDrawer() {
        //Add any color or remove it to use the default one!
        //To make it transparent use Color.Transparent in side setScrimColor();
        //drawerLayout.setScrimColor(Color.TRANSPARENT);
        drawerLayout.setScrimColor((getResources().getColor(R.color.card4)));
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                // Scale the View based on current slide offset
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);

                // Translate the View, accounting for the scaled width
                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                contentView.setTranslationX(xTranslation);
            }
        }
        );

    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }else super.onBackPressed();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId() ) {
            case R.id.nav_home:
                break;
            case R.id.nav_logout:
                   FirebaseAuth.getInstance().signOut();
                   App.getInstance().logOut();
                   Intent intToMain = new Intent(UserDashboard.this, SignUpActivity.class);
                   intToMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   startActivity(intToMain);
                break;
            case R.id.nav_about_app:
                Intent i = new Intent(UserDashboard.this, AboutApp.class);
                startActivity(i);
                break;
            case R.id.nav_about_me:
                Intent e = new Intent(UserDashboard.this, AboutMe.class);
                startActivity(e);
                break;
        }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

        }

}

