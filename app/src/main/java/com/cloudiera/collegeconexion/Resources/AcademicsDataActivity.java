package com.cloudiera.collegeconexion.Resources;

import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Utils.SectionPagerAdapter;

public class AcademicsDataActivity extends AppCompatActivity {

    private static final String TAG = "AcademicsDataActivity";
    private Context mContext = AcademicsDataActivity.this;

    private  ViewPager viewPager;
    private TextView mBooksLabel,mPDFLabel,mPapersLabel;
    private ImageView mResourseMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acadmic_data);

        viewPager = (ViewPager)findViewById(R.id.subject_view);
        mBooksLabel = (TextView)findViewById(R.id.books_list);
        mPDFLabel = (TextView)findViewById(R.id.pdf_list);
        mPapersLabel = (TextView) findViewById(R.id.exam_papers);
//        mResourseMenu = (ImageView) findViewById(R.id.resources_activity_menu);

        setupTabs();


        mBooksLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });
        mPapersLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });
        mPDFLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });

//        mResourseMenu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                PopupMenu dialog = new PopupMenu(mContext,mResourseMenu);
//                dialog.inflate(R.menu.resources_activity_menu);
//                dialog.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        switch (item.getItemId()){
//                            case R.id.hardCopy :
//                                Dialog myDialog = new Dialog(mContext);
//                                myDialog.setContentView(R.layout.layout_hard_copy_popup);
//                                Button callNow = myDialog.findViewById(R.id.callNOw);
//                                callNow.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "+919460002064"));
//                                        startActivity(intent);
//                                    }
//                                });
//                                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                                myDialog.show();
//                                return true;
//                        }
//                        return false;
//                    }
//                });
//                dialog.show();
//
//            }
//        });

    }

    /**
     * Setup tabs for the Resources activity
     */
    private void setupTabs(){


        SectionPagerAdapter adapter  = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new BooksFragment());
        adapter.addFragment(new PapersFragment());
        adapter.addFragment(new PDFDataFragment());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onPageSelected(int position) {
                changeTabs(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void changeTabs(int position) {

        if(position == 0 ){

            mBooksLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tabselected));
            mBooksLabel.setTextSize(19);

            mPapersLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tab_not_selected));
            mPapersLabel.setTextSize(16);

            mPDFLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tab_not_selected));
            mPDFLabel.setTextSize(16);

        }
        if(position == 1 ){

            mBooksLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tab_not_selected));
            mBooksLabel.setTextSize(16);

            mPapersLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tabselected));
            mPapersLabel.setTextSize(19);

            mPDFLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tab_not_selected));
            mPDFLabel.setTextSize(16);
        }
        if(position == 2 ){

            mBooksLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tab_not_selected));
            mBooksLabel.setTextSize(16);

            mPapersLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tab_not_selected));
            mPapersLabel.setTextSize(16);

            mPDFLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tabselected));
            mPDFLabel.setTextSize(19);
        }

    }


}
