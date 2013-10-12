package com.probojnik.terminal.util;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;
import com.probojnik.terminal.view.MainActivity;

import java.util.ArrayList;

/**
 * @author Stanislav Shamji
 */
public class ButtonAdapter extends BaseAdapter {
    private ArrayList<ButtonEntry> buttonEntry;
    private MainActivity mainActivity;
    private Context ctx;

    public ButtonAdapter(MainActivity mainActivity, Context ctx, ArrayList<ButtonEntry> buttonEntry) {
        this.buttonEntry = buttonEntry;
        this.mainActivity = mainActivity;
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return buttonEntry.size();
    }

    @Override
    public Object getItem(int position) {
        return buttonEntry.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Button button;

        if (convertView == null) {
            button = new Button(mainActivity);
        } else {
            button = (Button) convertView;
        }

        ButtonEntry getButtonEntry = (ButtonEntry) getItem(position);

        button.setText(getButtonEntry.getName());
        button.setTag(getButtonEntry.getTag());
        button.setOnClickListener(onClickListener);  // new ClickListener(mainActivity, ctx)

        return button;
    }

    OnClickListener onClickListener = new OnClickListener(){
        public void onClick(View view) {
            System.out.println("OnClickListener | onClick | getTag = " + view.getTag() + " , getId = " + view.getId());
            if (view.getTag() == "info") {
                mainActivity.infoDialogFragment.show(mainActivity.getFragmentManager(), "infoDialogFragment");
            } else if (view.getTag() == "home") {
                mainActivity.homeClick();
            } else if(view.getTag().toString().contains("{")){    // navigation
                mainActivity.task(mainActivity, String.valueOf(view.getTag())); // groupslist&departament=12&teinalid=99999&parent=0
            }else{
                Toast.makeText(mainActivity, "KEYCODE undefined", Toast.LENGTH_LONG).show();
            }
        }
    };
}