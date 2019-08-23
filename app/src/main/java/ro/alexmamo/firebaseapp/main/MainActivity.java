package ro.alexmamo.firebaseapp.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import ro.alexmamo.firebaseapp.R;
import ro.alexmamo.firebaseapp.auth.AuthActivity;

public class MainActivity  extends DaggerAppCompatActivity implements FirebaseAuth.AuthStateListener,
        NavigationView.OnNavigationItemSelectedListener, Observer<FirebaseUser> {
    @Inject GoogleSignInClient googleSignInClient;
    @Inject FirebaseAuth auth;
    @Inject MainViewModel mainViewModel;
    @Inject RequestManager requestManager;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFirebaseUserFromAuthentication();
        initToolBar();
        initNavigationDrawer();
    }

    public String getUidFromIntent() {
        return getIntent().getStringExtra("uid");
    }

    public String getNameFromIntent() {
        return getIntent().getStringExtra("name");
    }

    private void getFirebaseUserFromAuthentication() {
        mainViewModel.getFirebaseUserLiveData().observe(this, this);
    }

    @Override
    public void onChanged(FirebaseUser firebaseUser) {
        View rootView = navigationView.getHeaderView(0);

        ImageView profileUrlImageView = rootView.findViewById(R.id.profile_url_image_view);
        requestManager.load(firebaseUser.getPhotoUrl())
                .apply(RequestOptions.circleCropTransform())
                .apply(new RequestOptions().override(250, 250))
                .into(profileUrlImageView);

        TextView nameTextView = rootView.findViewById(R.id.name_text_view);
        nameTextView.setText(firebaseUser.getDisplayName());

        TextView emailTextView = rootView.findViewById(R.id.email_text_view);
        emailTextView.setText(firebaseUser.getEmail());
    }

    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawerLayout.closeDrawers();

        switch (menuItem.getItemId()) {
            case R.id.nav_profile:
                navController.navigate(R.id.profile_fragment);
                break;

            case R.id.nav_products:
                navController.navigate(R.id.products_fragment);
                break;
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, drawerLayout);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            goToAuthActivity();
        }
    }

    private void goToAuthActivity() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }

    private void signOut() {
        singOutFirebase();
        signOutGoogle();
    }

    private void singOutFirebase() {
        auth.signOut();
    }

    private void signOutGoogle() {
        googleSignInClient.signOut();
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.sign_out_button) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}