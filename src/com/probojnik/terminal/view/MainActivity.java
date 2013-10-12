package com.probojnik.terminal.view;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.probojnik.terminal.R;
import com.probojnik.terminal.TerminalApplication;
import com.probojnik.terminal.util.ButtonAdapter;
import com.probojnik.terminal.util.ButtonEntry;

import java.util.ArrayList;

/**
 * @author Stanislav Shamji
 */
public class MainActivity extends FillLayoutActivity {

    private Context ctx;
    DrawerLayout drawer_layout;

    ArrayList<ButtonEntry> menuArrayListButtonEntry = new ArrayList<ButtonEntry>();

    ListView drawer;

    ButtonAdapter menuButtonAdapter;
    public DialogFragment infoDialogFragment;


    final String[] name = {"Главная","Информация","Кнопка_3","Кнопка_4","Кнопка_5","6","7","8","9","10","11","12","13","14","15","16","17","18","19"};
    final String[] tag = {"home","info","btn3","btn4","btn5","6","7","8","9","10","11","12","13","14","15","16","17","18","19"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);

        mainActivity = this;
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        listView = (ListView) findViewById(R.id.listView);
        drawer = (ListView) findViewById(R.id.drawer);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);


        mainArrayListButtonEntry = new ArrayList<ButtonEntry>();
        mainButtonAdapter = new ButtonAdapter(mainActivity, ctx, mainArrayListButtonEntry);
        listView.setAdapter(mainButtonAdapter);

        fillData();
        menuButtonAdapter = new ButtonAdapter(mainActivity, ctx, menuArrayListButtonEntry);
        drawer.setAdapter(menuButtonAdapter);

        System.out.println("MainActivity | onCreate | ");

        infoDialogFragment = new InfoDialogFragment();


        homeClick(); // AsyncTask
    }

    void fillData() {
        for (int i = 0; i < name.length; i++) {
            menuArrayListButtonEntry.add(new ButtonEntry(name[i], tag[i]));
        }
    }

    private void findViewItems() {

    }



    @Override
    protected void onPause() {
        super.onPause();
        setSession();
        ((TerminalApplication) getApplication()).clear();
    }
    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Home | onResume");
        getSession();
    }

    public void homeClick(){
        task(mainActivity, "terminalservices"); // terminalservices&terminalid=99999
    }

    boolean menuClick(){
        if(drawer_layout.isDrawerOpen(drawer))
            drawer_layout.closeDrawer(drawer);
        else
            drawer_layout.openDrawer(drawer);

        return true;
    }

    boolean upDownClick(boolean direction){
        int currentPosition = listView.getFirstVisiblePosition();
        System.out.println("direction = " + direction + ", currentPosition = " + currentPosition + ", listView.getCount() = " + listView.getCount());
        if(direction){ // UP
            if (currentPosition == 0)
                return false;
            listView.setSelection(currentPosition - 1);
        }else{ // DOWN
            if (currentPosition == listView.getCount() - 1)
                return false;
            listView.setSelection(currentPosition + 1);
        }
        listView.clearFocus();

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { // Called when a key was pressed down and not handled by any of the views inside of the activity.
//        super.onKeyDown(keyCode, event);

        int keyCode1 = event.getKeyCode();
        System.out.println("keyCode = " + keyCode + ", KeyEvent = " + event + ", keyCode1 = " + keyCode1 );
        switch(keyCode1) {
            case KeyEvent.KEYCODE_MENU:
                menuClick();
                break;
            case KeyEvent.KEYCODE_HOME:
                break;
            case KeyEvent.KEYCODE_BACK:
                homeClick();
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                upDownClick(true);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                upDownClick(false);
                break;
        }
        return true;
    }

}