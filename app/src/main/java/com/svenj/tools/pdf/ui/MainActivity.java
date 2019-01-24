package com.svenj.tools.pdf.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.svenj.tools.pdf.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mBottomNavigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        initFragment(item.getItemId());
                        return true;
                    case R.id.navigation_document:
                        initFragment(item.getItemId());
                        return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 42);

        mBottomNavigation = findViewById(R.id.navigation);
        mBottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mBottomNavigation.setSelectedItemId(R.id.navigation_home);
    }

    private Fragment mCurrentFragment;
    private void initFragment(int selectedId) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment targetFragment = null;
        switch (selectedId) {
            case R.id.navigation_home:
                targetFragment = fm.findFragmentByTag(RecentFragment.class.getSimpleName());
                if (targetFragment == null) {
                    targetFragment = RecentFragment.newInstance();
                }
                break;
            case R.id.navigation_document:
                targetFragment = fm.findFragmentByTag(DocumentFragment.class.getSimpleName());
                if (targetFragment == null) {
                    targetFragment = DocumentFragment.newInstance();
                }
                break;
        }

        if (!targetFragment.isAdded()) {
            if (mCurrentFragment != null) {
                ft.hide(mCurrentFragment);
            }

            ft.add(R.id.fragment_content, targetFragment, targetFragment.getClass().getSimpleName());
        } else {
            ft.hide(mCurrentFragment).show(targetFragment);
        }
        mCurrentFragment = targetFragment;
        ft.commit();
    }
}
