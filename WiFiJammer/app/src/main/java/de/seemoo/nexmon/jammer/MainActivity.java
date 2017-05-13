package de.seemoo.nexmon.jammer;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity implements SeekBarFragment.FragmentListener {
    private static final String TAG = "MainActivity";
    public double amps[];
    public double phases[];
    public double freqs[] = new double[128];
    public HashMap<String, Integer> vars;
    public LinkedList<Integer> checkedViews = new LinkedList<>();
    public AlertDialog idftDialog;
    public AlertDialog channelDialog;
    public AlertDialog helpDialog;
    public Menu menu;
    public SeekBarFragment ampFragment;
    public SeekBarFragment phaseFragment;
    public PlotFragment timePlotFragment;
    public PlotFragment freqPlotFragment;
    public TransmitterFragment transmitterFragment;
    public ReceiverFragment receiverFragment;
    public AboutUsFragment aboutUsFragment;
    public boolean startup;
    public boolean jammer_in_background;
    public boolean first_run;


    public void onUserAction(Bundle bundle) {
        if (bundle.containsKey("Amplitudes")) {
            amps = bundle.getDoubleArray("Amplitudes");
        }
        if (bundle.containsKey("Phases")) {
            phases = bundle.getDoubleArray("Phases");
        }
        if (bundle.containsKey("freqs")) {
            freqs = bundle.getDoubleArray("freqs");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        setContentView(R.layout.main);

        if (savedInstanceState != null) {
            vars = (HashMap<String, Integer>) savedInstanceState.getSerializable("vars");
            amps = savedInstanceState.getDoubleArray("AMPS");
            phases = savedInstanceState.getDoubleArray("PHASES");
            freqs = savedInstanceState.getDoubleArray("FREQS");

        } else {
            vars = new HashMap<String, Integer>();
            vars.put("amp_sliders_portrait", 1);
            vars.put("amp_sliders_landscape", 1);
            vars.put("phase_sliders_portrait", 1);
            vars.put("phase_sliders_landscape", 0);
            vars.put("time_plot_portrait", 0);
            vars.put("time_plot_landscape", 0);
            vars.put("frequency_plot_portrait", 0);
            vars.put("frequency_plot_landscape", 0);
            vars.put("jammingPower", 50);
            vars.put("idft size", 128);
            vars.put("Preset", 20);
            vars.put("WiFi Channel", 1);
            vars.put("Bandwidth", 20);
            vars.put("JammerType", 0);
            vars.put("App", 0);
            vars.put("jammerStart", 0);
            amps = new double[Constants.getSlidersCount(vars.get("idft size"))];
            phases = new double[Constants.getSlidersCount(vars.get("idft size"))];
            freqs = new double[Constants.getSlidersCount(vars.get("idft size"))];
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        startup = true;
        jammer_in_background = false;
        first_run = true;
        Log.d("D", "onStart: enter");
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(
                    new NavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(MenuItem menuItem) {
                            menuItem.setChecked(true);
                            switch (menuItem.getTitle().toString()) {
                                case "Transmitter":
                                    vars.put("App", 1);
                                    setTitle("Transmitter");
                                    jammer_in_background = true;
                                    onConfigurationChanged(getResources().getConfiguration());
                                    break;
                                case "Jammer":
                                    vars.put("App", 0);
                                    setTitle("Jammer");
                                    onConfigurationChanged(getResources().getConfiguration());
                                    break;
                                case "Receiver":
                                    vars.put("App", 2);
                                    setTitle("Receiver");
                                    jammer_in_background = true;
                                    onConfigurationChanged(getResources().getConfiguration());
                                    break;
                                case "About":
                                    vars.put("App", 3);
                                    setTitle("About Us");
                                    jammer_in_background = true;
                                    onConfigurationChanged(getResources().getConfiguration());
                                    break;
                            }
                            ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
                            return true;
                        }
                    });
        }
        int jammingPower = vars.get("jammingPower");

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
            args.putDoubleArray("freqs", freqs);
            ampFragment.setArguments(args);
            fragmentTransaction.add(R.id.fragment_container_1, ampFragment, "amp");
        }
        timePlotFragment = (PlotFragment) fragmentManager.findFragmentByTag("timeplot");
        if (timePlotFragment == null) {
            timePlotFragment = new PlotFragment();
            Bundle args = new Bundle();
            args.putDoubleArray("amps", amps);
            args.putDoubleArray("phases", phases);
            args.putDoubleArray("freqs", freqs);
            args.putInt("mode", 0);
            timePlotFragment.setArguments(args);
            fragmentTransaction.add(R.id.fragment_container_3, timePlotFragment, "timeplot");
        }

        freqPlotFragment = (PlotFragment) fragmentManager.findFragmentByTag("freqplot");
        if (freqPlotFragment == null) {
            freqPlotFragment = new PlotFragment();
            Bundle args = new Bundle();
            args.putDoubleArray("amps", amps);
            args.putDoubleArray("phases", phases);
            args.putDoubleArray("freqs", freqs);
            args.putInt("mode", 1);
            freqPlotFragment.setArguments(args);
            fragmentTransaction.add(R.id.fragment_container_4, freqPlotFragment, "freqplot");
        }

        transmitterFragment = (TransmitterFragment) fragmentManager.findFragmentByTag("transmitter");
        if (transmitterFragment == null) {
            transmitterFragment = new TransmitterFragment();
            fragmentTransaction.add(R.id.fragment_container_5, transmitterFragment, "transmitter");
        }

        receiverFragment = (ReceiverFragment) fragmentManager.findFragmentByTag("receiver");
        if (receiverFragment == null) {
            receiverFragment = new ReceiverFragment();
            fragmentTransaction.add(R.id.fragment_container_6, receiverFragment, "receiver");
        }

        aboutUsFragment = (AboutUsFragment) fragmentManager.findFragmentByTag("aboutUs");
        if (aboutUsFragment == null) {
            aboutUsFragment = new AboutUsFragment();
            fragmentTransaction.add(R.id.fragment_container_7, aboutUsFragment, "aboutUs");
        }
        fragmentTransaction.commit();
        Log.d("D", "onStart: exit");
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

        //String orientation = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) ? "landscape" : "portrait";

        /*menu.findItem(R.id.amp_sliders).setChecked(vars.get("amp_sliders_" + orientation) == 1);
        menu.findItem(R.id.phase_sliders).setChecked(vars.get("phase_sliders_" + orientation) == 1);
        menu.findItem(R.id.time_plot).setChecked(vars.get("time_plot_" + orientation) == 1);
        menu.findItem(R.id.frequency_plot).setChecked(vars.get("frequency_plot_" + orientation) == 1);*/


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
                                    switch (value) {
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
                                if (item.getTitle().toString().replace("in", "").contains(vars.get("Preset").toString() + " ")) {
                                    setPresetPilots(vars.get("Preset"));
                                    item.setChecked(true);
                                }
                                break;
                            case "Type":
                                int type = vars.get("JammerType");
                                if (getJammerType(item.getTitle().toString()) == type) {
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

        //Application finished initializing
        if (startup) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            startup = false;
        }

        Configuration config = getResources().getConfiguration();
        onConfigurationChanged(config);

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
            case R.id.help_jammer:
                helpDialog.show();
                return true;
        }

        if (item.getGroupId() == R.id.group_view) {

            int max_size = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) ? 1 : 2;

            if (item.isChecked()) {
                checkedViews.remove(item.getItemId());
                item.setChecked(false);
                findViewById(getFragmentId(item)).setVisibility(View.GONE);

            } else {
                checkedViews.add(item.getItemId());
                item.setChecked(true);
                findViewById(getFragmentId(item)).setVisibility(View.VISIBLE);

                if (checkedViews.size() > max_size) {
                    MenuItem firstItem = menu.findItem(checkedViews.getFirst());
                    //System.out.println(firstItem.getTitle());
                    int fragmentId = getFragmentId(firstItem);
                    System.out.println(fragmentId);
                    findViewById(fragmentId).setVisibility(View.GONE);
                    firstItem.setChecked(false);
                    checkedViews.removeFirst();
                }
            }

            /*
            String orientation;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                orientation = "landescape";
                ViewGroup viewGroup = (ViewGroup) menu.findItem(R.id.group_view);

            }else{
                orientation = "portrait";
            }
            //String orientation = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) ? "landscape" : "portrait";

            switch (item.getItemId()) {
                case R.id.amp_sliders:
                    if (item.isChecked()) {
                        item.setChecked(false);
                        vars.put("amp_sliders_" + orientation, 0);
                        findViewById(R.id.fragment_container_1).setVisibility(View.GONE);
                    } else {
                        item.setChecked(true);
                        vars.put("amp_sliders_" + orientation, 1);
                        findViewById(R.id.fragment_container_1).setVisibility(View.VISIBLE);
                    }
                    return true;

                case R.id.phase_sliders:
                    if (item.isChecked()) {
                        item.setChecked(false);
                        vars.put("phase_sliders_" + orientation, 0);
                        findViewById(R.id.fragment_container_2).setVisibility(View.GONE);
                    } else {
                        item.setChecked(true);
                        vars.put("phase_sliders_" + orientation, 1);
                        findViewById(R.id.fragment_container_2).setVisibility(View.VISIBLE);
                    }
                    return true;

                case R.id.time_plot:
                    if (item.isChecked()) {
                        item.setChecked(false);
                        vars.put("time_plot_" + orientation, 0);
                        findViewById(R.id.fragment_container_3).setVisibility(View.GONE);
                    } else {
                        item.setChecked(true);
                        vars.put("time_plot_" + orientation, 1);
                        findViewById(R.id.fragment_container_3).setVisibility(View.VISIBLE);
                        timePlotFragment.plotSignals(amps, phases, freqs);
                    }
                    return true;

                case R.id.frequency_plot:
                    if (item.isChecked()) {
                        item.setChecked(false);
                        vars.put("frequency_plot_" + orientation, 0);
                        findViewById(R.id.fragment_container_4).setVisibility(View.GONE);
                    } else {
                        item.setChecked(true);
                        vars.put("frequency_plot_" + orientation, 1);
                        findViewById(R.id.fragment_container_4).setVisibility(View.VISIBLE);
                        freqPlotFragment.plotSignals(amps, phases, freqs);
                    }
                    return true;
            }*/
        } else if (!item.isChecked()) {
            switch (item.getGroupId()) {
                case R.id.group_type:
                    item.setChecked(true);
                    int value = getJammerType(item.getTitle().toString());
                    vars.put("JammerType", value);
                    return true;
                case R.id.group_preset:
                    item.setChecked(true);
                    value = Integer.parseInt(item.getTitle().toString().replaceAll("[^0-9]", ""));
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
                    switch (value) {
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
        System.out.println("Frequencies: " + Arrays.toString(freqs));
        Button startBtn = (Button) view;
        switch (vars.get("jammerStart")) {
            case 0: // not started -> now starting
                vars.put("jammerStart", 1);
                startBtn.setText("stop");
                break;
            case 1: // started -> now stopping
                vars.put("jammerStart", 0);
                startBtn.setText("start");
                break;
            default:
                vars.put("jammerStart", 0);

        }
    }

    private void createAlertDialogs() {

        View linear_layout = getLayoutInflater().inflate(R.layout.idft_size_dialog, null, true);
        final SeekBar seekBar = (SeekBar) linear_layout.findViewById(R.id.idftSeekbar);
        final EditText seekBarText = (EditText) linear_layout.findViewById(R.id.idftSeekbarText);
        seekBarText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    int value = Integer.parseInt(((EditText) v).getText().toString());
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
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1, int id) {
                        int newIdftSize = Integer.parseInt(seekBarText.getText().toString());
                        if (newIdftSize != vars.get("idft size")) {
                            if (newIdftSize > Constants.MAX_IDFT_SIZE) newIdftSize = Constants.MAX_IDFT_SIZE;
                            if (newIdftSize < Constants.MIN_IDFT_SIZE) newIdftSize = Constants.MIN_IDFT_SIZE;
                            vars.put("idft size", newIdftSize);
                            amps = new double[Constants.getSlidersCount(newIdftSize)];
                            freqs = new double[Constants.getSlidersCount(newIdftSize)];
                            ampFragment.setData(amps);
                            ampFragment.setFreqs(freqs);
                            ampFragment.setVerticalSeekBars();
                            phases = new double[Constants.getSlidersCount(newIdftSize)];
                            phaseFragment.setData(phases);
                            phaseFragment.setVerticalSeekBars();
                            setPresetPilots(vars.get("Preset"));

                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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
                if (checkedTextView.isChecked()) {
                    vars.put("WiFi Channel", Integer.parseInt(checkedTextView.getText().toString().split(" ", 2)[0]));
                }

            }
        });

        String[] channels = {"1 (2412MHz)", "2 (2417MHz)", "3 (2422MHz)", "4 (2427MHz)", "5 (2432MHz)", "6 (2437MHz)", "7 (2442MHz)", "8 (2447MHz)", "9 (2452MHz)", "10 (2457MHz)", "11 (2462MHz)", "12 (2467MHz)", "13 (2472MHz)", "14 (2484MHz)", "184 (4920MHz)", "186 (4930MHz)", "188 (4940MHz)", "190 (4950MHz)", "192 (4960MHz)", "194 (4970MHz)", "196 (4980MHz)", "198 (4990MHz)", "200 (5000MHz)", "202 (5010MHz)", "204 (5020MHz)", "206 (5030MHz)", "208 (5040MHz)", "210 (5050MHz)", "212 (5060MHz)", "214 (5070MHz)", "216 (5080MHz)", "218 (5090MHz)", "220 (5100MHz)", "222 (5110MHz)", "224 (5120MHz)", "226 (5130MHz)", "228 (5140MHz)", "32 (5160MHz)", "34 (5170MHz)", "36 (5180MHz)", "38 (5190MHz)", "40 (5200MHz)", "42 (5210MHz)", "44 (5220MHz)", "46 (5230MHz)", "48 (5240MHz)", "50 (5250MHz)", "52 (5260MHz)", "54 (5270MHz)", "56 (5280MHz)", "58 (5290MHz)", "60 (5300MHz)", "62 (5310MHz)", "64 (5320MHz)", "66 (5330MHz)", "68 (5340MHz)", "70 (5350MHz)", "72 (5360MHz)", "74 (5370MHz)", "76 (5380MHz)", "78 (5390MHz)", "80 (5400MHz)", "82 (5410MHz)", "84 (5420MHz)", "86 (5430MHz)", "88 (5440MHz)", "90 (5450MHz)", "92 (5460MHz)", "94 (5470MHz)", "96 (5480MHz)", "98 (5490MHz)", "100 (5500MHz)", "102 (5510MHz)", "104 (5520MHz)", "106 (5530MHz)", "108 (5540MHz)", "110 (5550MHz)", "112 (5560MHz)", "114 (5570MHz)", "116 (5580MHz)", "118 (5590MHz)", "120 (5600MHz)", "122 (5610MHz)", "124 (5620MHz)", "126 (5630MHz)", "128 (5640MHz)", "130 (5650MHz)", "132 (5660MHz)", "134 (5670MHz)", "136 (5680MHz)", "138 (5690MHz)", "140 (5700MHz)", "142 (5710MHz)", "144 (5720MHz)", "145 (5725MHz)", "146 (5730MHz)", "147 (5735MHz)", "148 (5740MHz)", "149 (5745MHz)", "150 (5750MHz)", "151 (5755MHz)", "152 (5760MHz)", "153 (5765MHz)", "154 (5770MHz)", "155 (5775MHz)", "156 (5780MHz)", "157 (5785MHz)", "158 (5790MHz)", "159 (5795MHz)", "160 (5800MHz)", "161 (5805MHz)", "162 (5810MHz)", "163 (5815MHz)", "164 (5820MHz)", "165 (5825MHz)", "166 (5830MHz)", "168 (5840MHz)", "170 (5850MHz)", "172 (5860MHz)", "174 (5870MHz)", "176 (5880MHz)", "178 (5890MHz)", "180 (5900MHz)"};
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.list_item, channels);
        listView.setAdapter(adapter);
        listView.setItemChecked(vars.get("WiFi Channel") - 1, true);

        alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(list_layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("CLOSE", null);


        // create alert dialog
        channelDialog = alertDialogBuilder.create();

        list_layout = getLayoutInflater().inflate(R.layout.help_jammer, null, true);

        ImageView imgNexmonLogo = (ImageView) list_layout.findViewById(R.id.imgNexmonLogo);
        ImageView imgSeemooLogo = (ImageView) list_layout.findViewById(R.id.imgSeemooLogo);
        ImageView imgTudLogo = (ImageView) list_layout.findViewById(R.id.imgTudLogo);
        Button btnLicenses = (Button) list_layout.findViewById(R.id.btnLicenses);

        imgSeemooLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://seemoo.tu-darmstadt.de"));
                startActivity(intent);
            }
        });

        imgNexmonLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://nexmon.org"));
                startActivity(intent);
            }
        });

        imgTudLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://www.tu-darmstadt.de"));
                startActivity(intent);
            }
        });

        btnLicenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LicenseDialog licenseDialog = LicenseDialog.newInstance();
                licenseDialog.show(getFragmentManager(), "");
            }
        });

        alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(list_layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("CLOSE", null);


        // create alert dialog
        helpDialog = alertDialogBuilder.create();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Do nothing if app has not initialized
        if (startup) return;


        /**
         * Check the device orientation and act accordingly
         */

        Configuration config = getResources().getConfiguration();

        int app = vars.get("App");
        if (app == 1) {
            //Transmitter
            menu.findItem(R.id.start).setVisible(false);
            menu.findItem(R.id.reset).setVisible(false);
            menu.findItem(R.id.view).setVisible(false);
            menu.findItem(R.id.preset).setVisible(false);
            menu.findItem(R.id.bandwidth).setVisible(false);
            menu.findItem(R.id.channel).setVisible(false);
            menu.findItem(R.id.idft).setVisible(false);
            menu.findItem(R.id.type).setVisible(false);
            menu.findItem(R.id.help_transmitter).setVisible(true);
            menu.findItem(R.id.help_receiver).setVisible(false);
            menu.findItem(R.id.help_jammer).setVisible(false);
            findViewById(R.id.jammingPower).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_1).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_2).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_3).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_4).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_5).setVisibility(View.VISIBLE);
            findViewById(R.id.fragment_container_6).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_7).setVisibility(View.GONE);
        } else if (app == 2) {
            //Receiver
            menu.findItem(R.id.start).setVisible(true);
            menu.findItem(R.id.reset).setVisible(true);
            menu.findItem(R.id.view).setVisible(false);
            menu.findItem(R.id.preset).setVisible(false);
            menu.findItem(R.id.bandwidth).setVisible(false);
            menu.findItem(R.id.channel).setVisible(false);
            menu.findItem(R.id.idft).setVisible(false);
            menu.findItem(R.id.type).setVisible(false);
            menu.findItem(R.id.help_transmitter).setVisible(false);
            menu.findItem(R.id.help_receiver).setVisible(true);
            menu.findItem(R.id.help_jammer).setVisible(false);
            findViewById(R.id.jammingPower).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_1).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_2).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_3).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_4).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_5).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_6).setVisibility(View.VISIBLE);
            findViewById(R.id.fragment_container_7).setVisibility(View.GONE);
        } else if (app == 3) {
            //About
            menu.findItem(R.id.start).setVisible(false);
            menu.findItem(R.id.reset).setVisible(false);
            menu.findItem(R.id.view).setVisible(false);
            menu.findItem(R.id.preset).setVisible(false);
            menu.findItem(R.id.bandwidth).setVisible(false);
            menu.findItem(R.id.channel).setVisible(false);
            menu.findItem(R.id.idft).setVisible(false);
            menu.findItem(R.id.type).setVisible(false);
            menu.findItem(R.id.help_transmitter).setVisible(false);
            menu.findItem(R.id.help_receiver).setVisible(false);
            menu.findItem(R.id.help_jammer).setVisible(false);
            findViewById(R.id.jammingPower).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_1).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_2).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_3).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_4).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_5).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_6).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_7).setVisibility(View.VISIBLE);
        } else {
            //Jammer
            menu.findItem(R.id.start).setVisible(false);
            menu.findItem(R.id.reset).setVisible(false);
            menu.findItem(R.id.view).setVisible(true);
            menu.findItem(R.id.preset).setVisible(true);
            menu.findItem(R.id.bandwidth).setVisible(true);
            menu.findItem(R.id.channel).setVisible(true);
            menu.findItem(R.id.idft).setVisible(true);
            menu.findItem(R.id.type).setVisible(true);
            menu.findItem(R.id.help_transmitter).setVisible(false);
            menu.findItem(R.id.help_receiver).setVisible(false);
            menu.findItem(R.id.help_jammer).setVisible(true);
            findViewById(R.id.jammingPower).setVisibility(View.VISIBLE);

            findViewById(R.id.fragment_container_5).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_6).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_7).setVisibility(View.GONE);


            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                /**
                 * Landscape mode of the device
                 */
                menu.findItem(R.id.view).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.preset).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.bandwidth).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.channel).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.idft).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

                if (first_run) {
                    // First time during initialization
                    findViewById(R.id.fragment_container_1).setVisibility(View.VISIBLE);
                    findViewById(R.id.fragment_container_2).setVisibility(View.GONE);
                    findViewById(R.id.fragment_container_3).setVisibility(View.GONE);
                    findViewById(R.id.fragment_container_4).setVisibility(View.GONE);

                    MenuItem firstItem = menu.getItem(0).getSubMenu().getItem(0);

                    firstItem.setChecked(true);

                    checkedViews.add(firstItem.getItemId());

                } else if (jammer_in_background) {
                    //changing from other app
                    for (Integer viewId : checkedViews) {
                        MenuItem item = menu.getItem(0).getSubMenu().findItem(viewId);
                        int fragmentId = getFragmentId(item);
                        findViewById(fragmentId).setVisibility(View.VISIBLE);
                        jammer_in_background = false;
                    }

                } else {
                    //normal mode
                    MenuItem firstItem = menu.getItem(0).getSubMenu().findItem(checkedViews.getFirst());
                    int fragmentId = getFragmentId(firstItem);
                    findViewById(fragmentId).setVisibility(View.GONE);
                    firstItem.setChecked(false);
                    checkedViews.removeFirst();
                }

                System.out.println("Entering Landscape Mode");
                //orientation = "landscape";
            } else {
                /**
                 * Portrait mode of the device
                 **/
                //menu.findItem(R.id.view).setVisible(false);
                menu.findItem(R.id.view).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                menu.findItem(R.id.preset).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                menu.findItem(R.id.bandwidth).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                menu.findItem(R.id.channel).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                menu.findItem(R.id.idft).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                if (first_run) {
                    // First time during initialization

                    findViewById(R.id.fragment_container_1).setVisibility(View.VISIBLE);
                    findViewById(R.id.fragment_container_2).setVisibility(View.VISIBLE);
                    findViewById(R.id.fragment_container_3).setVisibility(View.GONE);
                    findViewById(R.id.fragment_container_4).setVisibility(View.GONE);

                    MenuItem firstItem = menu.getItem(0).getSubMenu().getItem(0);
                    MenuItem secondItem = menu.getItem(0).getSubMenu().getItem(1);

                    firstItem.setChecked(true);
                    secondItem.setChecked(true);

                    checkedViews.add(firstItem.getItemId());
                    checkedViews.add(secondItem.getItemId());

                } else if (jammer_in_background) {
                    //changing from other app
                    for (Integer viewId : checkedViews) {
                        MenuItem item = menu.getItem(0).getSubMenu().findItem(viewId);
                        int fragmentId = getFragmentId(item);
                        findViewById(fragmentId).setVisibility(View.VISIBLE);
                        jammer_in_background = false;
                    }
                } else {
                    //normal mode

                    int index = getMenuItemIndex(menu.getItem(0).getSubMenu().findItem(checkedViews.getFirst()));
                    MenuItem newItem = menu.getItem(0).getSubMenu().getItem((index + 1) % 4);
                    checkedViews.add(newItem.getItemId());
                    newItem.setChecked(true);
                    findViewById(getFragmentId(newItem)).setVisibility(View.VISIBLE);

                    System.out.println("Entering Portrait Mode");
                    //orientation = "portrait";
                }

            }


            /*findViewById(R.id.fragment_container_1).setVisibility((vars.get("amp_sliders_" + orientation) == 1) ? View.VISIBLE : View.GONE);
            findViewById(R.id.fragment_container_2).setVisibility((vars.get("phase_sliders_" + orientation) == 1) ? View.VISIBLE : View.GONE);
            findViewById(R.id.fragment_container_3).setVisibility((vars.get("time_plot_" + orientation) == 1) ? View.VISIBLE : View.GONE);
            findViewById(R.id.fragment_container_4).setVisibility((vars.get("frequency_plot_" + orientation) == 1) ? View.VISIBLE : View.GONE);
            findViewById(R.id.fragment_container_5).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_6).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_7).setVisibility(View.GONE);

            menu.findItem(R.id.amp_sliders).setChecked(vars.get("amp_sliders_" + orientation) == 1);
            menu.findItem(R.id.phase_sliders).setChecked(vars.get("phase_sliders_" + orientation) == 1);
            menu.findItem(R.id.time_plot).setChecked(vars.get("time_plot_" + orientation) == 1);
            menu.findItem(R.id.frequency_plot).setChecked(vars.get("frequency_plot_" + orientation) == 1);*/

            createAlertDialogs();
        }


        // take care of changed views, when jammer in background and orientation changes
        if (app != 0) {
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {

                MenuItem firstItem = menu.getItem(0).getSubMenu().findItem(checkedViews.getFirst());
                firstItem.setChecked(false);
                checkedViews.removeFirst();

            } else {

                int index = getMenuItemIndex(menu.getItem(0).getSubMenu().findItem(checkedViews.getFirst()));
                MenuItem newItem = menu.getItem(0).getSubMenu().getItem((index + 1) % 4);
                checkedViews.add(newItem.getItemId());
                newItem.setChecked(true);
            }

        }

        first_run = false;

    }

    public void setPresetPilots(int value) {
        switch (value) {
            case 20:
                VerticalSeekBar seekBar = (VerticalSeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_0");
                seekBar.setProgress(100);
                break;
            case 40:
                seekBar = (VerticalSeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_1");
                seekBar.setProgress(100);
                break;
            case 2040:
                seekBar = (VerticalSeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_2");
                seekBar.setProgress(100);
                break;
            case 80:
                seekBar = (VerticalSeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_3");
                seekBar.setProgress(100);
                break;
            case 2080:
                seekBar = (VerticalSeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_4");
                seekBar.setProgress(100);
                break;
            case 4080:
                seekBar = (VerticalSeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_5");
                seekBar.setProgress(100);
                break;
        }
    }

    public int getJammerType(String title) {
        switch (title) {
            case "Simple Reactive Jammer":
                return 0;
            case "Acknowledging Jammer":
                return 1;
            case "Adaptive Power Control Jammer":
                return 2;
            default:
                return -1;
        }
    }

    public int getFragmentId(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.amp_sliders:
                return R.id.fragment_container_1;
            case R.id.phase_sliders:
                return R.id.fragment_container_2;
            case R.id.time_plot:
                return R.id.fragment_container_3;
            case R.id.frequency_plot:
                return R.id.fragment_container_4;
        }
        return 0;
    }

    public int getMenuItemIndex(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.amp_sliders:
                return 0;
            case R.id.phase_sliders:
                return 1;
            case R.id.time_plot:
                return 2;
            case R.id.frequency_plot:
                return 3;
        }
        return -1;
    }


}