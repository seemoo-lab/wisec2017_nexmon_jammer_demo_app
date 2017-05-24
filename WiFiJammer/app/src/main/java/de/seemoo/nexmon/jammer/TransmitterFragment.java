package de.seemoo.nexmon.jammer;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Stathis on 05-May-17.
 */

// Power, Modulation(a/g(OFDM) Rate 6,9,12,18,24,36,48,54 ,b(DSSS) rate: 1, 2, 5,5 11,n(mcs index 0..7)  20/40 Mhz Band, LDPC on/off, ac mcs index 0..9  20/40/80 Band), DestPort

public class TransmitterFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static CustomAdapter adapter;
    private static List<String> modulations = Arrays.asList("802.11a/g", "802.11b", "802.11n", "802.11ac");
    private static List<String> rates802_11_a_g = Arrays.asList("6", "9", "12", "18", "24", "36", "48", "54");
    private static List<String> rates802_11_b = Arrays.asList("1", "2", "5", "11");
    private static List<String> rates802_11_n_mcs_index = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7");
    private static List<String> rates802_11_ac_mcs_index = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    private static List<String> bands802_11_n = Arrays.asList("20", "40");
    private static List<String> bands802_11_ac = Arrays.asList("20", "40", "80");
    ArrayList<UDPStream> udpStreams;
    ListView listView;
    ViewGroup container;
    AlertDialog newUDPStreamDialog;
    AlertDialog helpDialog;
    InetAddress ipAddress;
    int existing_dialog_id = -1;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         * Inflate the layout for this fragment
         */
        this.container = container;
        setHasOptionsMenu(true);
        createNewUDPStreamDialog();
        createAlertDialogs();
        return inflater.inflate(R.layout.transmiter_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        try {

            FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    existing_dialog_id = -1;
                    newUDPStreamDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (newUDPStreamDialog.isShowing()) {
                            }
                            if (udpStreams.size() > 0) {
                                udpStreams.get(udpStreams.size() - 1).alertDialog = newUDPStreamDialog;
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        createNewUDPStreamDialog();
                                    }
                                });
                            }


                        }
                    }).start();


                }
            });

            ipAddress = Inet4Address.getByName("192.168.1.2");

            listView = (ListView) getView().findViewById(R.id.senderList);

            udpStreams = new ArrayList<>();


            adapter = new CustomAdapter(udpStreams, getActivity().getApplicationContext(), this);

            listView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help_transmitter:
                helpDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void createAlertDialogs() {

        View list_layout = getActivity().getLayoutInflater().inflate(R.layout.help_transmitter, null, true);

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

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(list_layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("CLOSE", null);


        // create alert dialog
        helpDialog = alertDialogBuilder.create();

    }

    public void createNewUDPStreamDialog() {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);

        final View linear_layout = getActivity().getLayoutInflater().inflate(R.layout.udpstream_dialog, container, false);

        alertDialogBuilder.setView(linear_layout);

        final SeekBar seekBar = (SeekBar) linear_layout.findViewById(R.id.udpSeekbar);
        final EditText seekBarText = (EditText) linear_layout.findViewById(R.id.udpSeekbarText);

        seekBarText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    int value = Integer.parseInt(((EditText) v).getText().toString());
                    if (value > 100) value = 100;
                    if (value < 0) value = 0;
                    seekBar.setProgress(value);
                    return true;
                }
                return false;
            }
        });

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

        final SeekBar numberSamplesSeekBar = (SeekBar) linear_layout.findViewById(R.id.numbPaSeekbar);
        final EditText numbPaSeekbarText = (EditText) linear_layout.findViewById(R.id.numbPaSeekbarText);

        numbPaSeekbarText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    int value = Integer.parseInt(((EditText) v).getText().toString());
                    if (value < 0) value = 0;
                    numberSamplesSeekBar.setProgress(value);
                    return true;
                }
                return false;
            }
        });

        numberSamplesSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                numbPaSeekbarText.setText(String.valueOf(progress));
            }

        });

        createSpinners(linear_layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1, int id) {

                        EditText editText = (EditText) linear_layout.findViewById(R.id.port);
                        int port = Integer.parseInt(editText.getText().toString());
                        if (port > 65535 || port < 0) {
                            Toast.makeText(getActivity().getApplicationContext(), "This is not a valid port number please try again", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int power = ((SeekBar) linear_layout.findViewById(R.id.udpSeekbar)).getProgress();
                        String modulation = ((Spinner) linear_layout.findViewById(R.id.modulation_spinner)).getSelectedItem().toString();
                        int rate = Integer.valueOf(((Spinner) linear_layout.findViewById(R.id.rate_spinner)).getSelectedItem().toString());
                        int numbSamples = ((SeekBar) linear_layout.findViewById(R.id.numbPaSeekbar)).getProgress();
                        int bandwidth = 0;
                        boolean ldpc = false;
                        try {
                            bandwidth = Integer.valueOf(((Spinner) linear_layout.findViewById(R.id.band_spinner)).getSelectedItem().toString());
                            ldpc = ((Switch) linear_layout.findViewById(R.id.LDPCSwitch)).isChecked();
                        } catch (Exception e) {
                        }

                        if (existing_dialog_id < 0) {

                            UDPStream udpStream = new UDPStream(udpStreams.size(), port, power, modulation, rate, bandwidth, ldpc, numbSamples, getActivity());
                        udpStreams.add(udpStream);
                        } else {
                            UDPStream udpStream = udpStreams.get(existing_dialog_id);
                            udpStream.destPort = port;
                            udpStream.power = power;
                            udpStream.modulation = modulation;
                            udpStream.rate = rate;
                            udpStream.bandwidth = bandwidth;
                            udpStream.ldpc = ldpc;
                            udpStream.numbFrames = numbSamples;
                        }
                        adapter.notifyDataSetChanged();



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
        newUDPStreamDialog = alertDialogBuilder.create();


    }

    public void createSpinners(View linear_layout) {
        // Spinner element
        Spinner mod_spinner = (Spinner) linear_layout.findViewById(R.id.modulation_spinner);
        mod_spinner.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        // Spinner click listener
        mod_spinner.setOnItemSelectedListener(this);


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, modulations);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        mod_spinner.setAdapter(dataAdapter);

        // Spinner element
        Spinner rate_spinner = (Spinner) linear_layout.findViewById(R.id.rate_spinner);
        rate_spinner.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        // Spinner click listener
        rate_spinner.setOnItemSelectedListener(this);


        // Spinner element
        Spinner band_spinner = (Spinner) linear_layout.findViewById(R.id.band_spinner);
        band_spinner.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        // Spinner click listener
        band_spinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
        ((TextView) parent.getChildAt(0)).setTextSize(15);
        String item = parent.getItemAtPosition(position).toString();

        switch (parent.getId()) {
            case R.id.modulation_spinner:
                ArrayAdapter<String> rateDataAdapter;
                ArrayAdapter<String> bandDataAdapter;
                if (id < 2) {
                    view.getRootView().findViewById(R.id.band_text).setVisibility(View.GONE);
                    view.getRootView().findViewById(R.id.band_spinner).setVisibility(View.GONE);
                    view.getRootView().findViewById(R.id.LDPCSwitch).setVisibility(View.GONE);
                    ((TextView) view.getRootView().findViewById(R.id.rate_text)).setText("Data Rate:");

                    if (id == 0) {
                        // Creating adapter for spinner
                        rateDataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, rates802_11_a_g);

                    } else {
                        // Creating adapter for spinner
                        rateDataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, rates802_11_b);

                    }

                } else if (id == 2) {
                    view.getRootView().findViewById(R.id.band_text).setVisibility(View.VISIBLE);
                    view.getRootView().findViewById(R.id.band_spinner).setVisibility(View.VISIBLE);
                    view.getRootView().findViewById(R.id.LDPCSwitch).setVisibility(View.VISIBLE);
                    ((TextView) view.getRootView().findViewById(R.id.rate_text)).setText("MCS Index:");
                    // Creating adapter for spinner
                    rateDataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, rates802_11_n_mcs_index);
                    bandDataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, bands802_11_n);
                    bandDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    ((Spinner) view.getRootView().findViewById(R.id.band_spinner)).setAdapter(bandDataAdapter);
                } else {
                    view.getRootView().findViewById(R.id.band_text).setVisibility(View.VISIBLE);
                    view.getRootView().findViewById(R.id.band_spinner).setVisibility(View.VISIBLE);
                    view.getRootView().findViewById(R.id.LDPCSwitch).setVisibility(View.GONE);
                    ((TextView) view.getRootView().findViewById(R.id.rate_text)).setText("MCS Index:");
                    // Creating adapter for spinner
                    rateDataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, rates802_11_ac_mcs_index);
                    bandDataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, bands802_11_ac);
                    bandDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    ((Spinner) view.getRootView().findViewById(R.id.band_spinner)).setAdapter(bandDataAdapter);

                }
                // Drop down layout style - list view with radio button
                rateDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // attaching data adapter to spinner
                ((Spinner) view.getRootView().findViewById(R.id.rate_spinner)).setAdapter(rateDataAdapter);
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }
}