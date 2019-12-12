package com.cloudiera.collegeconexion.Utils;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.cloudiera.collegeconexion.Friends.FriendsActivity;
import com.cloudiera.collegeconexion.Home.HomeActivity;
import com.cloudiera.collegeconexion.NoticeBox.NoticeBoxActivity;
import com.cloudiera.collegeconexion.Profile.ProfileActivity;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Talks.TalksActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

/**
 * Created by HP on 16-Nov-17.
 */

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigation(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setupBottomNavigation: setting up BottomNavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
    }

    public static void enableNavigation(final Context context, final BottomNavigationViewEx view) {


        view.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        int selectedItemId = getSelectedItem(view);

                        if (item.getItemId() != selectedItemId) {

                            switch (item.getItemId()) {
                                case R.id.home:
                                    Intent intent = new Intent(context, HomeActivity.class); // ACTIVITY_NUM = 0
                                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    context.startActivity(intent);
                                    break;
                                case R.id.noticeBox :
                                    Intent intent5 = new Intent(context, NoticeBoxActivity.class); // ACTIVITY_NUM = 1
                                    intent5.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent5.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent5.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent5.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    context.startActivity(intent5);
                                    break;
                                case R.id.friends:
                                    Intent intent3 = new Intent(context, FriendsActivity.class); // ACTIVITY_NUM = 2
                                    intent3.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent3.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    context.startActivity(intent3);
                                    break;
                                case R.id.talks:
                                    Intent intent4 = new Intent(context, TalksActivity.class); // ACTIVITY_NUM = 3
                                    intent4.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent4.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent4.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    context.startActivity(intent4);
                                    break;
//                                case R.id.academics:
//                                    Intent intent1 = new Intent(context, ResourceActivity.class); // ACTIVITY_NUM = 3
//                                    context.startActivity(intent1);
//                                    break;
                                case R.id.profile:
                                    Intent intent2 = new Intent(context, ProfileActivity.class); // ACTIVITY_NUM = 4
                                    intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    context.startActivity(intent2);
                                    break;
                            }
                        }
                        return false;
                    }
                });

    }

    /**
     * Used to get the id of the selected item in bottom naviagation view
     *
     * @param bottomNavigationView
     * @return selected item id
     */
    private static int getSelectedItem(BottomNavigationViewEx bottomNavigationView) {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.isChecked()) {
                return menuItem.getItemId();
            }
        }
        return 0;
    }
}
