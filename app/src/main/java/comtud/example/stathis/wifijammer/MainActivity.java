package comtud.example.stathis.wifijammer;


import android.content.DialogInterface;

import android.graphics.Color;
import android.graphics.Point;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements SeekBarFragment.FragmentListener {
    private static final String TAG = "MainActivity";
    public double amps[];
    public double phases[];
    public HashMap<String, Integer> vars;

    public AlertDialog idftDialog;
    public AlertDialog channelDialog;
    public Menu menu;
    public SeekBarFragment ampFragment;
    public SeekBarFragment phaseFragment;


    public void onUserAction(Bundle bundle) {
        if (bundle.containsKey("Amplitudes")) {
            amps = bundle.getDoubleArray("Amplitudes");
        }
        if (bundle.containsKey("Phases")) {
            phases = bundle.getDoubleArray("Phases");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        ImageView loadingScreen = (ImageView) findViewById(R.id.loadingscreen);
        loadingScreen.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            vars = (HashMap<String, Integer>) savedInstanceState.getSerializable("vars");
            amps = savedInstanceState.getDoubleArray("AMPS");
            phases = savedInstanceState.getDoubleArray("PHASES");

        } else {
            vars = new HashMap<String, Integer>();
            vars.put("amp_phase", R.id.amp);
            vars.put("jammingPower", 50);
            vars.put("idft size", 128);
            vars.put("Preset", 20);
            vars.put("WiFi Channel", 1);
            vars.put("Bandwidth", 20);
            amps = new double[vars.get("idft size")];
            phases = new double[vars.get("idft size")];
        }


    }

    @Override
    public void onStart(){
        super.onStart();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(
                    new NavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(MenuItem menuItem) {
                            menuItem.setChecked(true);
                            ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
                            return true;
                        }
                    });
        }

        int jammingPower = vars.get("jammingPower");
        //int amp_phase = vars.get("amp_phase");

        createAlertDialogs();

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);
        final TextView seekBarText = (TextView) findViewById(R.id.seekbarText);
        seekBarText.setText(jammingPower + "%");
        seekBar.setProgress(jammingPower);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarText.setText(progress + "%");
                vars.put("jammingPower", progress);
            }

        });

        //Configuration config = getResources().getConfiguration();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        phaseFragment = (SeekBarFragment) fragmentManager.findFragmentByTag("ph");
        if (phaseFragment == null) {
            phaseFragment = new SeekBarFragment();
            Bundle args = new Bundle();
            args.putInt("color", Color.LTGRAY);
            args.putInt("Bandwidth", vars.get("Bandwidth"));
            args.putString("name", "Phases");
            args.putDoubleArray("Phases", phases);
            phaseFragment.setArguments(args);
            fragmentTransaction.add(R.id.fragment_container_2, phaseFragment, "ph");
        }
        ampFragment = (SeekBarFragment) fragmentManager.findFragmentByTag("amp");
        if (ampFragment == null) {
            ampFragment = new SeekBarFragment();
            Bundle args = new Bundle();
            args.putInt("color", Color.GRAY);
            args.putInt("Bandwidth", vars.get("Bandwidth"));
            args.putString("name", "Amplitudes");
            args.putDoubleArray("Amplitudes", amps);
            ampFragment.setArguments(args);
            fragmentTransaction.add(R.id.fragment_container_1, ampFragment, "amp");
        }

        PlotFragment plotFragment= (PlotFragment) fragmentManager.findFragmentByTag("plot");
        if (plotFragment == null){
            plotFragment = new PlotFragment();
            fragmentTransaction.add(R.id.fragment_container_3, plotFragment, "plot");
        }

        fragmentTransaction.commit();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("vars", vars);
        savedInstanceState.putDoubleArray("AMPS", amps);
        savedInstanceState.putDoubleArray("PHASES", phases);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        Configuration config = getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            menu.findItem(R.id.amp_phase).setVisible(true);
            menu.findItem(R.id.amp_phase).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.preset).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.bandwidth).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.channel).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.idft).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            onOptionsItemSelected(menu.findItem(R.id.amp));
        } else {
            findViewById(R.id.fragment_container_1).setVisibility(View.VISIBLE);
            findViewById(R.id.fragment_container_2).setVisibility(View.VISIBLE);
            menu.findItem(R.id.amp_phase).setVisible(false);
        }

        // initialize activity variables
        int i = 1;
        while (true) {
            try {
                Menu submenu = menu.getItem(i).getSubMenu();
                String title = menu.getItem(i).getTitle().toString();
                int j = 0;
                while (submenu != null) {
                    try {
                        MenuItem item = submenu.getItem(j);
                        switch (title) {
                            case "Bandwidth":
                                if (item.getTitle().toString().contains(vars.get("Bandwidth").toString())) {
                                    int value = vars.get("Bandwidth");
                                    switch (value){
                                        case 20:
                                            menu.findItem(R.id.pre_20).setVisible(true);
                                            menu.findItem(R.id.pre_40).setVisible(false);
                                            menu.findItem(R.id.pre_20in40).setVisible(false);
                                            menu.findItem(R.id.pre_80).setVisible(false);
                                            menu.findItem(R.id.pre_20in80).setVisible(false);
                                            menu.findItem(R.id.pre_40in80).setVisible(false);
                                            break;
                                        case 40:
                                            menu.findItem(R.id.pre_20).setVisible(false);
                                            menu.findItem(R.id.pre_40).setVisible(true);
                                            menu.findItem(R.id.pre_20in40).setVisible(true);
                                            menu.findItem(R.id.pre_80).setVisible(false);
                                            menu.findItem(R.id.pre_20in80).setVisible(false);
                                            menu.findItem(R.id.pre_40in80).setVisible(false);
                                            break;
                                        case 80:
                                            menu.findItem(R.id.pre_20).setVisible(false);
                                            menu.findItem(R.id.pre_40).setVisible(false);
                                            menu.findItem(R.id.pre_20in40).setVisible(false);
                                            menu.findItem(R.id.pre_80).setVisible(true);
                                            menu.findItem(R.id.pre_20in80).setVisible(true);
                                            menu.findItem(R.id.pre_40in80).setVisible(true);
                                            break;
                                    }
                                    item.setChecked(true);
                                }
                                break;
                            case "Preset":
                                if (item.getTitle().toString().replace("in","").contains(vars.get("Preset").toString()+ " ")) {
                                    setPresetPilots(vars.get("Preset"));
                                    item.setChecked(true);
                                }
                                break;

                        }
                        j++;
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    }
                }
                i++;
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START);
            return true;
        }
        switch (item.getItemId()) {
            case R.id.idft:
                idftDialog.show();
                return true;
            case R.id.channel:
                channelDialog.show();
                return true;
        }
        if (!item.isChecked()) {
            switch (item.getGroupId()) {
                case R.id.group_amp_phase:
                    switch (item.getItemId()) {
                        case R.id.amp:
                            Log.d(TAG, "Changing to Amp");
                            findViewById(R.id.fragment_container_1).setVisibility(View.VISIBLE);
                            findViewById(R.id.fragment_container_2).setVisibility(View.GONE);
                            findViewById(R.id.fragment_container_3).setVisibility(View.GONE);
                            item.setChecked(true);
                            vars.put("amp_phase", R.id.amp);
                            return true;
                        case R.id.phase:
                            Log.d(TAG, "Changing to Ph");
                            findViewById(R.id.fragment_container_1).setVisibility(View.GONE);
                            findViewById(R.id.fragment_container_2).setVisibility(View.VISIBLE);
                            findViewById(R.id.fragment_container_3).setVisibility(View.GONE);
                            item.setChecked(true);
                            vars.put("amp_phase", R.id.phase);
                            return true;
                        case R.id.plot:
                            Log.d(TAG, "Changing to Plot");
                            findViewById(R.id.fragment_container_1).setVisibility(View.GONE);
                            findViewById(R.id.fragment_container_2).setVisibility(View.GONE);
                            findViewById(R.id.fragment_container_3).setVisibility(View.VISIBLE);
                            item.setChecked(true);
                            vars.put("amp_phase", R.id.plot);
                            return true;
                    }

                case R.id.group_preset:
                    item.setChecked(true);
                    int value = Integer.parseInt(item.getTitle().toString().replaceAll("[^0-9]", ""));
                    vars.put("Preset", value);
                    setPresetPilots(value);
                    return true;
                case R.id.group_bandwidth:
                    item.setChecked(true);
                    value = Integer.parseInt(item.getTitle().toString().replaceAll("[^0-9]", ""));
                    vars.put("Bandwidth", value);
                    ampFragment.setBandwidth(value);
                    phaseFragment.setBandwidth(value);
                    vars.put("Preset", value);
                    switch (value){
                        case 20:
                            menu.findItem(R.id.pre_20).setVisible(true).setChecked(true);
                            menu.findItem(R.id.pre_40).setVisible(false);
                            menu.findItem(R.id.pre_20in40).setVisible(false);
                            menu.findItem(R.id.pre_80).setVisible(false);
                            menu.findItem(R.id.pre_20in80).setVisible(false);
                            menu.findItem(R.id.pre_40in80).setVisible(false);
                            setPresetPilots(20);
                            break;
                        case 40:
                            menu.findItem(R.id.pre_20).setVisible(false);
                            menu.findItem(R.id.pre_40).setVisible(true).setChecked(true);
                            menu.findItem(R.id.pre_20in40).setVisible(true);
                            menu.findItem(R.id.pre_80).setVisible(false);
                            menu.findItem(R.id.pre_20in80).setVisible(false);
                            menu.findItem(R.id.pre_40in80).setVisible(false);
                            setPresetPilots(40);
                            break;
                        case 80:
                            menu.findItem(R.id.pre_20).setVisible(false);
                            menu.findItem(R.id.pre_40).setVisible(false);
                            menu.findItem(R.id.pre_20in40).setVisible(false);
                            menu.findItem(R.id.pre_80).setVisible(true).setChecked(true);
                            menu.findItem(R.id.pre_20in80).setVisible(true);
                            menu.findItem(R.id.pre_40in80).setVisible(true);
                            setPresetPilots(80);
                            break;
                    }
                    return true;

            }
        }
        return super.onOptionsItemSelected(item);

    }

    public void onButtonClick(View view) {
        //Start Jamming
        System.out.println("Jamming started with following parameters");
        System.out.println(vars.toString());
        System.out.println("Amplitudes: " + Arrays.toString(amps));
        System.out.println("Phases: " + Arrays.toString(phases));

    }

    private void createAlertDialogs() {

        View linear_layout = getLayoutInflater().inflate(R.layout.idft_size_dialog, null, true);
        final SeekBar seekBar = (SeekBar) linear_layout.findViewById(R.id.idftSeekbar);
        final EditText seekBarText = (EditText) linear_layout.findViewById(R.id.idftSeekbarText);
        seekBarText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    int value = Integer.parseInt(((EditText)v).getText().toString());
                    if (value > 512) value = 512;
                    if (value < 1) value = 1;
                    seekBar.setProgress(value);
                    return true;
                }
                return false;
            }
        });

        int value = vars.get("idft size");
        seekBarText.setText(String.valueOf(value));
        seekBar.setProgress(value);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarText.setText(String.valueOf(progress));
            }

        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(linear_layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1,int id) {
                        int value = Integer.parseInt(seekBarText.getText().toString());
                        if (value != vars.get("idft size")){
                            if (value > 512) value = 512;
                            if (value < 1) value = 1;
                            vars.put("idft size", value);
                            amps = new double[value];
                            ampFragment.setData(amps);
                            ampFragment.setVerticalSeekBars();
                            phases = new double[value];
                            phaseFragment.setData(phases);
                            phaseFragment.setVerticalSeekBars();

                        }

                    }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        seekBarText.setText(String.valueOf(vars.get("idft size")));
                        dialog.cancel();
                    }
                });

        // create alert dialog
        idftDialog = alertDialogBuilder.create();

        View list_layout = getLayoutInflater().inflate(R.layout.channels_list, null, true);
        ListView listView = (ListView) list_layout.findViewById(R.id.channels);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = (CheckedTextView) view;
                if (checkedTextView.isChecked()){
                    vars.put("WiFi Channel", Integer.parseInt(checkedTextView.getText().toString().split(" ", 2)[0]));
                }

            }
        });

        String[] channels = {"1 (2412MHz)","2 (2417MHz)","3 (2422MHz)","4 (2427MHz)","5 (2432MHz)","6 (2437MHz)","7 (2442MHz)","8 (2447MHz)","9 (2452MHz)","10 (2457MHz)","11 (2462MHz)","12 (2467MHz)","13 (2472MHz)","14 (2484MHz)","184 (4920MHz)","186 (4930MHz)","188 (4940MHz)","190 (4950MHz)","192 (4960MHz)","194 (4970MHz)","196 (4980MHz)","198 (4990MHz)","200 (5000MHz)","202 (5010MHz)","204 (5020MHz)","206 (5030MHz)","208 (5040MHz)","210 (5050MHz)","212 (5060MHz)","214 (5070MHz)","216 (5080MHz)","218 (5090MHz)","220 (5100MHz)","222 (5110MHz)","224 (5120MHz)","226 (5130MHz)","228 (5140MHz)","32 (5160MHz)","34 (5170MHz)","36 (5180MHz)","38 (5190MHz)","40 (5200MHz)","42 (5210MHz)","44 (5220MHz)","46 (5230MHz)"," 48(5240MHz)","50 (5250MHz)","52 (5260MHz)","54 (5270MHz)","56 (5280MHz)","58 (5290MHz)","60 (5300MHz)","62 (5310MHz)","64 (5320MHz)","66 (5330MHz)","68 (5340MHz)","70 (5350MHz)","72 (5360MHz)","74 (5370MHz)","76 (5380MHz)","78 (5390MHz)","80 (5400MHz)","82 (5410MHz)","84 (5420MHz)","86 (5430MHz)","88 (5440MHz)","90 (5450MHz)","92 (5460MHz)","94 (5470MHz)","96 (5480MHz)","98 (5490MHz)","100 (5500MHz)","102 (5510MHz)","104 (5520MHz)","106 (5530MHz)","108 (5540MHz)","110 (5550MHz)","112 (5560MHz)","114 (5570MHz)","116 (5580MHz)","118 (5590MHz)","120 (5600MHz)","122 (5610MHz)","124 (5620MHz)","126 (5630MHz)","128 (5640MHz)","130 (5650MHz)","132 (5660MHz)","134 (5670MHz)","136 (5680MHz)","138 (5690MHz)","140 (5700MHz)","142 (5710MHz)","144 (5720MHz)","145 (5725MHz)","146 (5730MHz)","147 (5735MHz)","148 (5740MHz)","149 (5745MHz)","150 (5750MHz)","151 (5755MHz)","152 (5760MHz)","153 (5765MHz)","154 (5770MHz)","155 (5775MHz)","156 (5780MHz)","157 (5785MHz)","158 (5790MHz)","159 (5795MHz)","160 (5800MHz)","161 (5805MHz)","162 (5810MHz)","163 (5815MHz)","164 (5820MHz)","165 (5825MHz)","166 (5830MHz)","168 (5840MHz)","170 (5850MHz)","172 (5860MHz)","174 (5870MHz)","176 (5880MHz)","178 (5890MHz)","180 (5900MHz)"};
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.list_item, channels);
        listView.setAdapter(adapter);
        listView.setItemChecked(vars.get("WiFi Channel")-1, true);

        alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(list_layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("CLOSE",null);


        // create alert dialog
        channelDialog = alertDialogBuilder.create();


    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        /**
         * Check the device orientation and act accordingly
         */
        Configuration config = getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            /**
             * Landscape mode of the device
             */
            menu.findItem(R.id.amp_phase).setVisible(true);
            menu.findItem(R.id.amp_phase).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.preset).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.bandwidth).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.channel).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.idft).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            System.out.println("Entering Landscape Mode");
            switch(vars.get("amp_phase")){
                case R.id.amp:
                    findViewById(R.id.fragment_container_1).setVisibility(View.VISIBLE);
                    findViewById(R.id.fragment_container_2).setVisibility(View.GONE);
                    findViewById(R.id.fragment_container_3).setVisibility(View.GONE);
                    break;
                case R.id.phase:
                    findViewById(R.id.fragment_container_1).setVisibility(View.GONE);
                    findViewById(R.id.fragment_container_2).setVisibility(View.VISIBLE);
                    findViewById(R.id.fragment_container_3).setVisibility(View.GONE);
                    break;
                case R.id.plot:
                    findViewById(R.id.fragment_container_1).setVisibility(View.GONE);
                    findViewById(R.id.fragment_container_2).setVisibility(View.GONE);
                    findViewById(R.id.fragment_container_3).setVisibility(View.VISIBLE);
            }

        } else {
            /**
             * Portrait mode of the device
             **/
            menu.findItem(R.id.amp_phase).setVisible(false);
            menu.findItem(R.id.amp_phase).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.findItem(R.id.preset).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.findItem(R.id.bandwidth).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.findItem(R.id.channel).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.findItem(R.id.idft).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            System.out.println("Entering Portrait Mode");
            findViewById(R.id.fragment_container_1).setVisibility(View.VISIBLE);
            findViewById(R.id.fragment_container_2).setVisibility(View.VISIBLE);
            //findViewById(R.id.fragment_container_3).setVisibility(View.VISIBLE);
        }
        invalidateOptionsMenu();
        createAlertDialogs();


    }

    public void setPresetPilots(int value){
        switch (value){
            case 20:
                VerticalSeekBar seekBar = (VerticalSeekBar)  findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_0");
                seekBar.setProgress(100);
                break;
            case 40:
                seekBar = (VerticalSeekBar)  findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_1");
                seekBar.setProgress(100);
                break;
            case 2040:
                seekBar = (VerticalSeekBar)  findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_2");
                seekBar.setProgress(100);
                break;
            case 80:
                seekBar = (VerticalSeekBar)  findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_3");
                seekBar.setProgress(100);
                break;
            case 2080:
                seekBar = (VerticalSeekBar)  findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_4");
                seekBar.setProgress(100);
                break;
            case 4080:
                seekBar = (VerticalSeekBar)  findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_5");
                seekBar.setProgress(100);
                break;
        }
    }


}