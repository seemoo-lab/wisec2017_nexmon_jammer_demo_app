package de.seemoo.nexmon.jammer;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
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
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.seemoo.nexmon.jammer.aboutus.AboutUsFragment;
import de.seemoo.nexmon.jammer.global.Constants;
import de.seemoo.nexmon.jammer.global.Variables;
import de.seemoo.nexmon.jammer.jammer.PlotFragment;
import de.seemoo.nexmon.jammer.jammer.SeekBarFragment;
import de.seemoo.nexmon.jammer.receiver.ReceiverFragment;
import de.seemoo.nexmon.jammer.transmitter.TransmitterFragment;
import de.seemoo.nexmon.jammer.utils.Assets;
import de.seemoo.nexmon.jammer.utils.LEDControl;
import de.seemoo.nexmon.jammer.utils.Nexutil;
import eu.chainfire.libsuperuser.Shell;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;


public class MainActivity extends AppCompatActivity implements SeekBarFragment.FragmentListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";

    public LinkedList<Integer> checkedViews = new LinkedList<>();
    public AlertDialog idftDialog;
    public AlertDialog channelDialog;
    public AlertDialog helpDialog;
    public AlertDialog optionsDialog;
    public AlertDialog firmwareDialog;
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
    public int oldOrientation;

    public static MainActivity instance;

    public HashSet<Integer> presets = new HashSet<>();

    /**
     * Enables/Disables all child views in a view group.
     *
     * @param viewGroup the view group
     * @param enabled   <code>true</code> to enable, <code>false</code> to disable
     *                  the views.
     */
    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) view, enabled);
            }
        }
    }

    public void onUserAction() {

        if (!startup) {
            timePlotFragment.plotSignals();
            freqPlotFragment.plotSignals();
        }

    }

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        setContentView(R.layout.main);

        try {
            Nexutil nexutil = Nexutil.getInstance();
            nexutil.setFirmwareInstalled(true);
            String versionString = nexutil.getStringIoctl(Nexutil.NEX_GET_VERSION_STRING, 1000);
            if (!versionString.contains("nexmon_jammer_ver")) {
                nexutil.setFirmwareInstalled(false);
            }
        } catch (Nexutil.FirmwareNotFoundException e) {
            // Should never get here
        }

        if (savedInstanceState != null) {


        } else {

            Variables.jammingPower = 50;
            Variables.channel = 1;
            Variables.jammerType = Variables.JammingType.SIMPLE_REACTIVE_JAMMER;
            Variables.app = 0;
            Variables.jammerStart = 0;
            Variables.idft_size = 128;
            Variables.bandwidth = 20;
            double[] amps = new double[Constants.getSlidersCount(Variables.idft_size)];
            double[] phases = new double[Constants.getSlidersCount(Variables.idft_size)];
            double[] freqs = new double[Constants.getSlidersCount(Variables.idft_size)];

            new Variables(amps, phases, freqs, 128, 20);
        }

        startup = true;
        jammer_in_background = false;
        first_run = true;
        oldOrientation = -1;
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
                                    if (Variables.app != 1) {
                                        Variables.app = 1;
                                        setTitle("Transmitter");
                                        jammer_in_background = true;
                                        oldOrientation = getResources().getConfiguration().orientation;
                                        onConfigurationChanged(getResources().getConfiguration());
                                    }

                                    break;
                                case "Jammer":
                                    if (Variables.app != 0) {
                                        Variables.app = 0;
                                        setTitle("Jammer");
                                        onConfigurationChanged(getResources().getConfiguration());
                                    }
                                    break;
                                case "Receiver":
                                    if (Variables.app != 2) {
                                        Variables.app = 2;
                                        setTitle("Receiver");
                                        jammer_in_background = true;
                                        oldOrientation = getResources().getConfiguration().orientation;
                                        onConfigurationChanged(getResources().getConfiguration());
                                    }
                                    break;
                                case "About":
                                    if (Variables.app != 3) {
                                        Variables.app = 3;
                                        setTitle("About Us");
                                        jammer_in_background = true;
                                        oldOrientation = getResources().getConfiguration().orientation;
                                        onConfigurationChanged(getResources().getConfiguration());
                                    }
                                    break;
                            }
                            ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
                            return true;
                        }
                    });
        }


        createAlertDialogs();

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);
        final TextView seekBarText = (TextView) findViewById(R.id.seekbarText);
        seekBarText.setText(Variables.jammingPower + "%");
        seekBar.setProgress(Variables.jammingPower);
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
                Variables.jammingPower = progress;
            }

        });

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        phaseFragment = (SeekBarFragment) fragmentManager.findFragmentByTag("ph");
        if (phaseFragment == null) {
            phaseFragment = new SeekBarFragment();
            Bundle args = new Bundle();
            args.putInt("color", Color.LTGRAY);
            args.putString("name", "Phases");
            phaseFragment.setArguments(args);
            fragmentTransaction.add(R.id.fragment_container_2, phaseFragment, "ph");
        }
        ampFragment = (SeekBarFragment) fragmentManager.findFragmentByTag("amp");
        if (ampFragment == null) {
            ampFragment = new SeekBarFragment();
            Bundle args = new Bundle();
            args.putInt("color", Color.GRAY);
            args.putString("name", "Amplitudes");
            ampFragment.setArguments(args);
            fragmentTransaction.add(R.id.fragment_container_1, ampFragment, "amp");
        }
        timePlotFragment = (PlotFragment) fragmentManager.findFragmentByTag("timeplot");
        if (timePlotFragment == null) {
            timePlotFragment = new PlotFragment();
            Bundle args = new Bundle();
            args.putInt("mode", 0);
            timePlotFragment.setArguments(args);
            fragmentTransaction.add(R.id.fragment_container_3, timePlotFragment, "timeplot");
        }

        freqPlotFragment = (PlotFragment) fragmentManager.findFragmentByTag("freqplot");
        if (freqPlotFragment == null) {
            freqPlotFragment = new PlotFragment();
            Bundle args = new Bundle();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);

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
                                int value = Variables.bandwidth;
                                if (item.getTitle().toString().contains(String.valueOf(value))) {
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
            case R.id.options:
                optionsDialog.show();
                return true;
            case R.id.idft:
                idftDialog.show();
                return true;
            case R.id.channel:
                channelDialog.show();
                return true;
            case R.id.help_jammer:
                helpDialog.show();
                return true;
            case R.id.pre_reset:
                //Reset Presets when changing Bandwidth
                Variables.amps = new double[Variables.freqs.length];
                Variables.phases = new double[Variables.freqs.length];
                ampFragment.updateFrequencies();
                phaseFragment.updateFrequencies();
                presets = new HashSet<>();
                if (!startup) {
                    timePlotFragment.plotSignals();
                    freqPlotFragment.plotSignals();
                }
                menu.findItem(R.id.pre_20).setChecked(false);
                menu.findItem(R.id.pre_40).setChecked(false);
                menu.findItem(R.id.pre_20in40).setChecked(false);
                menu.findItem(R.id.pre_80).setChecked(false);
                menu.findItem(R.id.pre_20in80).setChecked(false);
                menu.findItem(R.id.pre_40in80).setChecked(false);

                return true;
            case R.id.pre_all:
                //Reset Presets when changing Bandwidth
                Variables.amps = new double[Variables.freqs.length];
                Variables.phases = new double[Variables.freqs.length];

                double subcarrierSpacing = ampFragment.round((Variables.bandwidth * Constants.OVERSAMPLING_RATE / (double) Variables.idft_size) * 1000, 3);
                for (int i = 0; i < Variables.amps.length; i++) {
                    double subcarrierFrequency = ampFragment.round(subcarrierSpacing * (Variables.amps.length / 2 - i) * (-1), 4);
                    if (Math.abs(subcarrierFrequency) <= Variables.bandwidth * 500) Variables.amps[i] = 1.0;
                }
                ampFragment.updateFrequencies();
                phaseFragment.updateFrequencies();
                presets = new HashSet<>();
                if (!startup) {
                    timePlotFragment.plotSignals();
                    freqPlotFragment.plotSignals();
                }

                menu.findItem(R.id.pre_20).setChecked(false);
                menu.findItem(R.id.pre_40).setChecked(false);
                menu.findItem(R.id.pre_20in40).setChecked(false);
                menu.findItem(R.id.pre_80).setChecked(false);
                menu.findItem(R.id.pre_20in80).setChecked(false);
                menu.findItem(R.id.pre_40in80).setChecked(false);


                return true;
        }

        if (item.getGroupId() == R.id.group_view) {

            int max_size = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) ? 1 : 2;

            if (item.isChecked()) {
                checkedViews.removeFirstOccurrence(item.getItemId());
                item.setChecked(false);
                findViewById(getFragmentId(item)).setVisibility(View.GONE);

            } else {
                checkedViews.add(item.getItemId());
                item.setChecked(true);
                findViewById(getFragmentId(item)).setVisibility(View.VISIBLE);

                if (checkedViews.size() > max_size) {
                    MenuItem firstItem = menu.findItem(checkedViews.getFirst());
                    int fragmentId = getFragmentId(firstItem);
                    findViewById(fragmentId).setVisibility(View.GONE);
                    firstItem.setChecked(false);
                    checkedViews.removeFirst();
                }
            }

        } else if (item.getGroupId() == R.id.group_preset) {
            int value = Integer.parseInt(item.getTitle().toString().replaceAll("[^0-9]", ""));
            if (item.isChecked()) {
                item.setChecked(false);
                setPresetPilots(value, 0);
                presets.remove(value);
            } else {
                item.setChecked(true);
                boolean changed = false;
                int newIdftSize = Variables.idft_size;
                switch (Variables.bandwidth) {
                    case 20:
                        if (Variables.idft_size != 128) {
                            changed = true;
                            newIdftSize = 128;
                        }
                        break;
                    case 40:
                        if (Variables.idft_size != 256) {
                            changed = true;
                            newIdftSize = 256;
                        }
                        break;
                    case 80:
                        if (Variables.idft_size != 512) {
                            changed = true;
                            newIdftSize = 512;
                        }
                }
                if (changed) {
                    Variables.idft_size = newIdftSize;
                    Variables.amps = new double[Constants.getSlidersCount(newIdftSize)];
                    Variables.freqs = new double[Constants.getSlidersCount(newIdftSize)];
                    Variables.phases = new double[Constants.getSlidersCount(newIdftSize)];
                    ampFragment.setVerticalSeekBars();
                    phaseFragment.setVerticalSeekBars();

                    for (Integer preset : presets) {
                        setPresetPilots(preset, 100);
                    }
                    idftDialog = createIdftDialog();
                }

                presets.add(value);
                setPresetPilots(value, 100);
            }
            if (!startup) {
                timePlotFragment.plotSignals();
                freqPlotFragment.plotSignals();
            }
            return true;

        } else if (!item.isChecked() && item.getGroupId() == R.id.group_bandwidth) {

            item.setChecked(true);
            int value = Integer.parseInt(item.getTitle().toString().replaceAll("[^0-9]", ""));
            Variables.bandwidth = value;
            ampFragment.updateFrequencies();
            phaseFragment.updateFrequencies();

            //Reset Presets when changing Bandwidth
            for (Integer preset : presets) {
                setPresetPilots(preset, 0);
            }
            presets = new HashSet<>();

            switch (value) {
                case 20:
                    menu.findItem(R.id.pre_20).setVisible(true).setChecked(false);
                    menu.findItem(R.id.pre_40).setVisible(false).setChecked(false);
                    menu.findItem(R.id.pre_20in40).setVisible(false).setChecked(false);
                    menu.findItem(R.id.pre_80).setVisible(false).setChecked(false);
                    menu.findItem(R.id.pre_20in80).setVisible(false).setChecked(false);
                    menu.findItem(R.id.pre_40in80).setVisible(false).setChecked(false);
                    break;
                case 40:
                    menu.findItem(R.id.pre_20).setVisible(false).setChecked(false);
                    menu.findItem(R.id.pre_40).setVisible(true).setChecked(false);
                    menu.findItem(R.id.pre_20in40).setVisible(true).setChecked(false);
                    menu.findItem(R.id.pre_80).setVisible(false).setChecked(false);
                    menu.findItem(R.id.pre_20in80).setVisible(false).setChecked(false);
                    menu.findItem(R.id.pre_40in80).setVisible(false).setChecked(false);
                    break;
                case 80:
                    menu.findItem(R.id.pre_20).setVisible(false).setChecked(false);
                    menu.findItem(R.id.pre_40).setVisible(false).setChecked(false);
                    menu.findItem(R.id.pre_20in40).setVisible(false).setChecked(false);
                    menu.findItem(R.id.pre_80).setVisible(true).setChecked(false);
                    menu.findItem(R.id.pre_20in80).setVisible(true).setChecked(false);
                    menu.findItem(R.id.pre_40in80).setVisible(true).setChecked(false);
                    break;
            }
            if (!startup) {
                timePlotFragment.plotSignals();
                freqPlotFragment.plotSignals();
            }
            return true;


        }
        return super.onOptionsItemSelected(item);

    }

    public void onButtonClick(View view) {
        //Start Jamming
        System.out.println("Jamming started with following parameters");
        System.out.println("Amplitudes: " + Arrays.toString(Variables.amps));
        System.out.println("Phases: " + Arrays.toString(Variables.phases));
        System.out.println("Frequencies: " + Arrays.toString(Variables.freqs));
        Button startBtn = (Button) view;
        switch (Variables.jammerStart) {
            case 0: // not started -> now starting
                try {
                    Toast.makeText(getApplicationContext(), "Configuring and starting jammer, please wait ...", Toast.LENGTH_SHORT).show();
                    Nexutil.getInstance().setIoctl(514, Variables.getBytes());
                    Variables.jammerStart = 1;

                    startBtn.setText("stop");

                    //Disable Interface
                    enableDisableViewGroup((ViewGroup) findViewById(R.id.frames), false);
                    enableDisableViewGroup((ViewGroup) findViewById(R.id.my_toolbar), false);
                    enableDisableViewGroup((ViewGroup) findViewById(R.id.nav_view), false);
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(final Void ... params) {
                            LEDControl.setBrightnessRGB(rgb("#ff0000"));
                            LEDControl.setOnOffMsRGB(1000, 1000);
                            LEDControl.activateLED();
                            return null;
                        }
                    }.execute();
                } catch (Nexutil.FirmwareNotFoundException e) {
                    getFirmwareDialog().show();
                }
                break;
            case 1: // started -> now stopping
                Variables.jammerStart = 0;
                startBtn.setText("start");
                //Enable Interface
                enableDisableViewGroup((ViewGroup) findViewById(R.id.frames), true);
                enableDisableViewGroup((ViewGroup) findViewById(R.id.my_toolbar), true);
                enableDisableViewGroup((ViewGroup) findViewById(R.id.nav_view), true);
                if (Nexutil.getInstance().isFirmwareInstalled()) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(final Void... params) {
                            LEDControl.deactivateLED();
                            return null;
                        }
                    }.execute();
                }
                break;
            default:
                Variables.jammerStart = 0;

        }
    }

    private AlertDialog createIdftDialog() {
        View linear_layout = getLayoutInflater().inflate(R.layout.idft_size_dialog, null, true);
        final SeekBar seekBar = (SeekBar) linear_layout.findViewById(R.id.idftSeekbar);
        seekBar.setProgress(Variables.idft_size);
        final EditText seekBarText = (EditText) linear_layout.findViewById(R.id.idftSeekbarText);
        seekBarText.setText(String.valueOf(Variables.idft_size));
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

        int value = Variables.idft_size;
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

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        alertDialogBuilder.setView(linear_layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1, int id) {

                        int newIdftSize = Integer.parseInt(seekBarText.getText().toString());

                        if (newIdftSize != Variables.idft_size) {
                            if (newIdftSize > Constants.MAX_IDFT_SIZE)
                                newIdftSize = Constants.MAX_IDFT_SIZE;
                            if (newIdftSize < Constants.MIN_IDFT_SIZE)
                                newIdftSize = Constants.MIN_IDFT_SIZE;
                            Variables.idft_size = newIdftSize;
                            Variables.amps = new double[Constants.getSlidersCount(newIdftSize)];
                            Variables.freqs = new double[Constants.getSlidersCount(newIdftSize)];
                            Variables.phases = new double[Constants.getSlidersCount(newIdftSize)];
                            ampFragment.setVerticalSeekBars();
                            phaseFragment.setVerticalSeekBars();

                            for (Integer preset : presets) {
                                setPresetPilots(preset, 100);
                            }
                            if (!startup) {
                                timePlotFragment.plotSignals();
                                freqPlotFragment.plotSignals();
                            }


                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        seekBarText.setText(String.valueOf(Variables.idft_size));
                        dialog.cancel();
                    }
                });

        // create alert dialog
        return alertDialogBuilder.create();
    }

    private AlertDialog createChannelListDialog() {
        View list_layout = getLayoutInflater().inflate(R.layout.channels_list, null, true);
        ListView listView = (ListView) list_layout.findViewById(R.id.channels);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = (CheckedTextView) view;
                if (checkedTextView.isChecked()) {
                    Variables.channel = Integer.parseInt(checkedTextView.getText().toString().split(" ", 2)[0]);
                }

            }
        });

        String[] channels = {"1 (2412MHz)", "2 (2417MHz)", "3 (2422MHz)", "4 (2427MHz)", "5 (2432MHz)", "6 (2437MHz)", "7 (2442MHz)", "8 (2447MHz)", "9 (2452MHz)", "10 (2457MHz)", "11 (2462MHz)", "12 (2467MHz)", "13 (2472MHz)", "14 (2484MHz)", "184 (4920MHz)", "186 (4930MHz)", "188 (4940MHz)", "190 (4950MHz)", "192 (4960MHz)", "194 (4970MHz)", "196 (4980MHz)", "198 (4990MHz)", "200 (5000MHz)", "202 (5010MHz)", "204 (5020MHz)", "206 (5030MHz)", "208 (5040MHz)", "210 (5050MHz)", "212 (5060MHz)", "214 (5070MHz)", "216 (5080MHz)", "218 (5090MHz)", "220 (5100MHz)", "222 (5110MHz)", "224 (5120MHz)", "226 (5130MHz)", "228 (5140MHz)", "32 (5160MHz)", "34 (5170MHz)", "36 (5180MHz)", "38 (5190MHz)", "40 (5200MHz)", "42 (5210MHz)", "44 (5220MHz)", "46 (5230MHz)", "48 (5240MHz)", "50 (5250MHz)", "52 (5260MHz)", "54 (5270MHz)", "56 (5280MHz)", "58 (5290MHz)", "60 (5300MHz)", "62 (5310MHz)", "64 (5320MHz)", "66 (5330MHz)", "68 (5340MHz)", "70 (5350MHz)", "72 (5360MHz)", "74 (5370MHz)", "76 (5380MHz)", "78 (5390MHz)", "80 (5400MHz)", "82 (5410MHz)", "84 (5420MHz)", "86 (5430MHz)", "88 (5440MHz)", "90 (5450MHz)", "92 (5460MHz)", "94 (5470MHz)", "96 (5480MHz)", "98 (5490MHz)", "100 (5500MHz)", "102 (5510MHz)", "104 (5520MHz)", "106 (5530MHz)", "108 (5540MHz)", "110 (5550MHz)", "112 (5560MHz)", "114 (5570MHz)", "116 (5580MHz)", "118 (5590MHz)", "120 (5600MHz)", "122 (5610MHz)", "124 (5620MHz)", "126 (5630MHz)", "128 (5640MHz)", "130 (5650MHz)", "132 (5660MHz)", "134 (5670MHz)", "136 (5680MHz)", "138 (5690MHz)", "140 (5700MHz)", "142 (5710MHz)", "144 (5720MHz)", "145 (5725MHz)", "146 (5730MHz)", "147 (5735MHz)", "148 (5740MHz)", "149 (5745MHz)", "150 (5750MHz)", "151 (5755MHz)", "152 (5760MHz)", "153 (5765MHz)", "154 (5770MHz)", "155 (5775MHz)", "156 (5780MHz)", "157 (5785MHz)", "158 (5790MHz)", "159 (5795MHz)", "160 (5800MHz)", "161 (5805MHz)", "162 (5810MHz)", "163 (5815MHz)", "164 (5820MHz)", "165 (5825MHz)", "166 (5830MHz)", "168 (5840MHz)", "170 (5850MHz)", "172 (5860MHz)", "174 (5870MHz)", "176 (5880MHz)", "178 (5890MHz)", "180 (5900MHz)"};
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.list_item, channels);
        listView.setAdapter(adapter);
        listView.setItemChecked(Variables.channel - 1, true);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(list_layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("CLOSE", null);


        // create alert dialog
        return alertDialogBuilder.create();
    }

    private AlertDialog createHelpDialog() {
        View list_layout = getLayoutInflater().inflate(R.layout.help_jammer, null, true);

        WebView wvHelp = (WebView) list_layout.findViewById(R.id.wvHelp);
        wvHelp.loadUrl("file:///android_asset/html/help_jammer.html");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(list_layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("CLOSE", null);


        // create alert dialog
        return alertDialogBuilder.create();
    }

    private AlertDialog createFirmwareDialog() {
        View list_layout = getLayoutInflater().inflate(R.layout.firmware_dialog, null, true);

        final Button btnBackupOriginalFirmware = (Button) list_layout.findViewById(R.id.btnBackupOriginalFirmware);
        final Button btnInstallJammingFirmware = (Button) list_layout.findViewById(R.id.btnInstallJammingFirmware);

        final File file = new File("/sdcard/fw_bcmdhd.orig.bin");
        if (file.exists()) {
            btnBackupOriginalFirmware.setEnabled(false);
            btnInstallJammingFirmware.setEnabled(true);
        } else {
            btnBackupOriginalFirmware.setEnabled(true);
            btnInstallJammingFirmware.setEnabled(false);
        }

        btnBackupOriginalFirmware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "creating firmware backup", Toast.LENGTH_SHORT).show();

                Shell.SU.run("cp /vendor/firmware/fw_bcmdhd.bin /sdcard/fw_bcmdhd.orig.bin");

                if (file.exists()) {
                    btnBackupOriginalFirmware.setEnabled(false);
                    btnInstallJammingFirmware.setEnabled(true);
                }
            }
        });

        btnInstallJammingFirmware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "installing jamming firmware", Toast.LENGTH_SHORT).show();

                AssetManager assetManager = getAssets();
                Assets.copyFileFromAsset(assetManager, "fw_bcmdhd.bin", "/sdcard/fw_bcmdhd.jammer.bin");
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(list_layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("CLOSE", null);

        // create alert dialog
        return alertDialogBuilder.create();
    }

    private AlertDialog createOptionsDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        final LinearLayout linear_layout_options = (LinearLayout) getLayoutInflater().inflate(R.layout.options_dialog, null, false);

        alertDialogBuilder.setView(linear_layout_options);

        // Spinner element
        Spinner type_spinner = (Spinner) linear_layout_options.findViewById(R.id.type_spinner);
        type_spinner.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        // Spinner click listener
        type_spinner.setOnItemSelectedListener(this);

        // Creating adapter for spinner
        List<String> types = Arrays.asList(Variables.JammingType.SIMPLE_REACTIVE_JAMMER.toString(), Variables.JammingType.ACKNOWLEDGING_JAMMER.toString(), Variables.JammingType.ADAPTIVE_POWER_CONTROL_JAMMER.toString());
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, types);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        type_spinner.setAdapter(dataAdapter);

        Variables.jammingSignalRepetitions = 200;
        Variables.jammingPort = 3333;
        Variables.jammerType = Variables.JammingType.SIMPLE_REACTIVE_JAMMER;

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1, int id) {

                        EditText editText = (EditText) linear_layout_options.findViewById(R.id.jammingSignalRepetitionsValue);
                        Variables.jammingSignalRepetitions = Integer.parseInt(editText.getText().toString());
                        Variables.jammerType = Variables.JammingType.getJammingTypeFromString(((Spinner) linear_layout_options.findViewById(R.id.type_spinner)).getSelectedItem().toString());
                        EditText portText = (EditText) linear_layout_options.findViewById(R.id.portValue);
                        int port = Integer.parseInt(portText.getText().toString());
                        if (port > 65535 || port < 0) {
                            Toast.makeText(getApplicationContext(), "This is not a valid port number please try again", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Variables.jammingPort = port;
                        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        // create alert dialog
        return alertDialogBuilder.create();
    }

    private void createAlertDialogs() {
        idftDialog = createIdftDialog();
        channelDialog = createChannelListDialog();
        helpDialog = createHelpDialog();
        optionsDialog = createOptionsDialog();
        firmwareDialog = createFirmwareDialog();
    }

    public AlertDialog getFirmwareDialog() {
        return firmwareDialog;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
        ((TextView) parent.getChildAt(0)).setTextSize(15);

    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Do nothing if app has not initialized
        if (startup) return;

        /**
         * Check the device orientation and act accordingly
         */

        int app = Variables.app;
        if (app == 1) {
            //Transmitter
            menu.findItem(R.id.start).setVisible(false);
            menu.findItem(R.id.reset).setVisible(false);
            menu.findItem(R.id.view).setVisible(false);
            menu.findItem(R.id.preset).setVisible(false);
            menu.findItem(R.id.bandwidth).setVisible(true);
            menu.findItem(R.id.channel).setVisible(true);
            menu.findItem(R.id.idft).setVisible(false);
            menu.findItem(R.id.options).setVisible(false);
            menu.findItem(R.id.help_transmitter).setVisible(true);
            menu.findItem(R.id.help_receiver).setVisible(false);
            menu.findItem(R.id.help_jammer).setVisible(false);
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                menu.findItem(R.id.bandwidth).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                menu.findItem(R.id.help_transmitter).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            } else {
                menu.findItem(R.id.bandwidth).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                menu.findItem(R.id.help_transmitter).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
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
            menu.findItem(R.id.bandwidth).setVisible(true);
            menu.findItem(R.id.channel).setVisible(true);
            menu.findItem(R.id.idft).setVisible(false);
            menu.findItem(R.id.options).setVisible(false);
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
            menu.findItem(R.id.options).setVisible(false);
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
            menu.findItem(R.id.options).setVisible(true);
            menu.findItem(R.id.help_transmitter).setVisible(false);
            menu.findItem(R.id.help_receiver).setVisible(false);
            menu.findItem(R.id.help_jammer).setVisible(true);
            findViewById(R.id.jammingPower).setVisibility(View.VISIBLE);

            findViewById(R.id.fragment_container_5).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_6).setVisibility(View.GONE);
            findViewById(R.id.fragment_container_7).setVisibility(View.GONE);


            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                /**
                 * Landscape mode of the device
                 */
                menu.findItem(R.id.view).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.preset).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.bandwidth).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.findItem(R.id.channel).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
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

            } else {
                /**
                 * Portrait mode of the device
                 **/

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
                }
                System.out.println("Entering Portrait Mode");

            }
        }

        // take care of changed views, when jammer in background and orientation changes
        if (app != 0 && newConfig.orientation != oldOrientation) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

                MenuItem firstItem = menu.getItem(0).getSubMenu().findItem(checkedViews.getFirst());
                firstItem.setChecked(false);
                checkedViews.removeFirst();

            } else {

                int index = getMenuItemIndex(menu.getItem(0).getSubMenu().findItem(checkedViews.getFirst()));
                MenuItem newItem = menu.getItem(0).getSubMenu().getItem((index + 1) % 4);
                checkedViews.add(newItem.getItemId());
                newItem.setChecked(true);
            }
            oldOrientation = newConfig.orientation;

        }

        first_run = false;

    }

    public void setPresetPilots(int value, int power) {

        int maxSize = Variables.amps.length;
        SeekBar seekBar;
        switch (value) {
            case 20:
                int sc_minus_7 = Variables.amps.length / 2 - 7;
                int sc_plus_7 = Variables.amps.length / 2 + 7;
                int sc_minus_21 = Variables.amps.length / 2 - 21;
                int sc_plus_21 = Variables.amps.length / 2 + 21;

                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_7);
                if (seekBar != null && sc_minus_7 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_plus_7);
                if (seekBar != null && sc_plus_7 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_21);
                if (seekBar != null && sc_minus_21 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_plus_21);
                if (seekBar != null && sc_plus_21 < maxSize) {
                    seekBar.setProgress(power);
                }
                break;
            case 40:
                int sc_minus_11 = Variables.amps.length / 2 - 11;
                int sc_plus_11 = Variables.amps.length / 2 + 11;
                int sc_minus_25 = Variables.amps.length / 2 - 25;
                int sc_plus_25 = Variables.amps.length / 2 + 25;
                int sc_minus_53 = Variables.amps.length / 2 - 53;
                int sc_plus_53 = Variables.amps.length / 2 + 53;

                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_11);
                if (seekBar != null && sc_minus_11 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_plus_11);
                if (seekBar != null && sc_plus_11 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_25);
                if (seekBar != null && sc_minus_25 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_plus_25);
                if (seekBar != null && sc_plus_25 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_53);
                if (seekBar != null && sc_minus_53 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_plus_53);
                if (seekBar != null && sc_plus_53 < maxSize) {
                    seekBar.setProgress(power);
                }

                break;
            case 2040:
                sc_minus_7 = Variables.amps.length / 2 - 7;
                sc_plus_7 = Variables.amps.length / 2 + 7;
                sc_minus_21 = Variables.amps.length / 2 - 21;
                sc_plus_21 = Variables.amps.length / 2 + 21;
                int sc_minus_39 = Variables.amps.length / 2 - 39;
                sc_minus_25 = Variables.amps.length / 2 - 25;
                sc_minus_53 = Variables.amps.length / 2 - 53;
                sc_minus_11 = Variables.amps.length / 2 - 11;


                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_7);
                if (seekBar != null && sc_minus_7 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_plus_7);
                if (seekBar != null && sc_plus_7 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_21);
                if (seekBar != null && sc_minus_21 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_plus_21);
                if (seekBar != null && sc_plus_21 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_39);
                if (seekBar != null && sc_minus_39 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_25);
                if (seekBar != null && sc_minus_25 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_53);
                if (seekBar != null && sc_minus_53 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_11);
                if (seekBar != null && sc_minus_11 < maxSize) {
                    seekBar.setProgress(power);
                }

                break;
            case 80:
                sc_minus_11 = Variables.amps.length / 2 - 11;
                sc_plus_11 = Variables.amps.length / 2 + 11;
                sc_minus_39 = Variables.amps.length / 2 - 39;
                int sc_plus_39 = Variables.amps.length / 2 + 39;
                int sc_minus_75 = Variables.amps.length / 2 - 75;
                int sc_plus_75 = Variables.amps.length / 2 + 75;
                int sc_minus_103 = Variables.amps.length / 2 - 103;
                int sc_plus_103 = Variables.amps.length / 2 + 103;
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_11);
                if (seekBar != null && sc_minus_11 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_plus_11);
                if (seekBar != null && sc_plus_11 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_39);
                if (seekBar != null && sc_minus_39 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_plus_39);
                if (seekBar != null && sc_plus_39 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_75);
                if (seekBar != null && sc_minus_75 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_plus_75);
                if (seekBar != null && sc_plus_75 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_103);
                if (seekBar != null && sc_minus_103 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_plus_103);
                if (seekBar != null && sc_plus_103 < maxSize) {
                    seekBar.setProgress(power);
                }
                break;
            case 2080:
                sc_minus_75 = Variables.amps.length / 2 - 75;
                int sc_minus_98 = Variables.amps.length / 2 - 98;
                sc_minus_103 = Variables.amps.length / 2 - 103;
                int sc_minus_117 = Variables.amps.length / 2 - 117;
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_75);
                if (seekBar != null && sc_minus_75 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_98);
                if (seekBar != null && sc_minus_98 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_103);
                if (seekBar != null && sc_minus_103 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_117);
                if (seekBar != null && sc_minus_117 < maxSize) {
                    seekBar.setProgress(power);
                }
                break;
            case 4080:
                sc_minus_11 = Variables.amps.length / 2 - 11;
                sc_minus_39 = Variables.amps.length / 2 - 39;
                sc_minus_53 = Variables.amps.length / 2 - 53;
                sc_minus_75 = Variables.amps.length / 2 - 75;
                int sc_minus_89 = Variables.amps.length / 2 - 89;
                sc_minus_117 = Variables.amps.length / 2 - 117;
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_11);
                if (seekBar != null && sc_minus_11 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_39);
                if (seekBar != null && sc_minus_39 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_53);
                if (seekBar != null && sc_minus_53 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_75);
                if (seekBar != null && sc_minus_75 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_89);
                if (seekBar != null && sc_minus_89 < maxSize) {
                    seekBar.setProgress(power);
                }
                seekBar = (SeekBar) findViewById(R.id.main).findViewWithTag("Amplitudes_seekBar_" + sc_minus_117);
                if (seekBar != null && sc_minus_117 < maxSize) {
                    seekBar.setProgress(power);
                }
                break;
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