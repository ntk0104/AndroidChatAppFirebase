package com.example.kietnguyen.mychatapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager mviewPager;
//    SectionsPageAdapter is used for returning suitable view when click on tabs
    private SectionsPageAdapter mSectionsPageAdapter;
    // declare tabslayout to switch view
    private TabLayout mTabsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
//        the Toolbar above the tabs bar
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Chat App");
        //Tabs
        mviewPager =  findViewById(R.id.main_tabs_pager);
        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
//        set the view for the viewpaeger by the returned value for SectionsPageAdapter
        mviewPager.setAdapter(mSectionsPageAdapter);
//        the tabs to switch views in MainActivity
        mTabsLayout = findViewById(R.id.main_tabs);
//        link the tabs with associate view
        mTabsLayout.setupWithViewPager(mviewPager);




    }

    // Firebase func
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //If currentUser equal null
        if(currentUser == null){
            // Navigate to Start-activity
            directToStart();
        }
    }

    // Direct to StartActivity
    private void directToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    // Add main_menu.xml to the menu in this activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //set event for menu-item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        //If log out btn is clicked -> sign out user
        if(item.getItemId() == R.id.main_logout_btn){
            FirebaseAuth.getInstance().signOut();
            // then update UI
            directToStart();
        }
        if(item.getItemId() == R.id.main_account_setting_btn){
            Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingIntent);
        }
        if(item.getItemId() == R.id.main_allUser_btn){
            Intent alluserIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(alluserIntent);
        }

        return true;
    }
}
