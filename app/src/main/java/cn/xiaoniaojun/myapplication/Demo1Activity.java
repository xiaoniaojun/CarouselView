package cn.xiaoniaojun.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class Demo1Activity extends AppCompatActivity {

    private ViewGroup mListViewsContainer;
//    private ListView list1;
//    private ListView list2;
//    private ListView list3;

    private CarouselView mCarouselView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeView();
    }

    private void initializeView() {

        mListViewsContainer = (ViewGroup) findViewById(R.id.lay_list_container);
        mCarouselView = (CarouselView) findViewById(R.id.carousel_view);
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCarouselView.smoothScrollBy(100,0);
            }
        });

//        list1 = (ListView) findViewById(R.id.list_1);
//        list2 = (ListView) findViewById(R.id.list_2);
//        list3 = (ListView) findViewById(R.id.list_3);


//
//
//        initListView(list1);
//        initListView(list2);
//        initListView(list3);


    }

    @Override
    protected void onStart() {
        super.onStart();
        final List<Bitmap> carouselBitmapList = new ArrayList<>();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.carousel_1);
        carouselBitmapList.add(bitmap);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.carousel_2);
        carouselBitmapList.add(bitmap);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.carousel_3);
        carouselBitmapList.add(bitmap);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.carousel_4);
        carouselBitmapList.add(bitmap);
        mCarouselView.post(new Runnable() {
            @Override
            public void run() {
                mCarouselView.setCarouselImages(carouselBitmapList);
            }
        });
    }

    private void initListView(ListView list) {
        final int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        ViewGroup.LayoutParams layoutParams = list.getLayoutParams();
        layoutParams.width = screenWidth / 3 * 2;
        Log.v("Demo1Activity", String.valueOf(screenWidth / 3 * 2));
        layoutParams.height = screenHeight;


        ArrayList<String> datas = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            datas.add("name" + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, datas);
        list.setAdapter(adapter);
    }
}
