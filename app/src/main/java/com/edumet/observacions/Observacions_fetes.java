package com.edumet.observacions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Observacions_fetes extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v=inflater.inflate(R.layout.observacions_fetes, container, false);
        //setContentView(R.layout.dynamically_create_view_element);

        final LinearLayout lm = (LinearLayout) v.findViewById(R.id.linearMain);

        // create the layout params that will be used to define how your
        // button will be displayed
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //Create four
        for(int j=0;j<=6;j++)
        {
            LinearLayout ll = new LinearLayout(getContext());
            ll.setOrientation(LinearLayout.HORIZONTAL);

final ImageButton btn =new ImageButton(getContext());
            btn.setId(j+1);
            //btn.setText("Add To Cart");
            btn.setLayoutParams(params);

            final int index = j;
            // Set click listener for button
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.i("TAG", "index :" + index);
                    Toast.makeText(getContext(),
                            "Clicked Button Index :" + index,Toast.LENGTH_LONG).show();

                }
            });
            //Add button to LinearLayout
            ll.addView(btn);

            // Create TextView
            TextView product = new TextView(getContext());
            product.setText(" Product"+j+"    ");
            ll.addView(product);

            // Create TextView
            TextView price = new TextView(getContext());
            price.setText("  $"+j+"     ");
            ll.addView(price);


            lm.addView(ll);
        }
        return v;
    }
}