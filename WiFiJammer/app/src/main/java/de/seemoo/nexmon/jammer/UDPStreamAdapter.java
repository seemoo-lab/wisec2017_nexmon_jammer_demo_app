package de.seemoo.nexmon.jammer;


import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import de.seemoo.nexmon.jammer.utils.LEDControl;
import de.seemoo.nexmon.jammer.utils.Nexutil;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

/**
 * Created by Stathis on 05-May-17.
 */


public class UDPStreamAdapter extends ArrayAdapter<UDPStream> implements View.OnClickListener {

    Context mContext;
    TextView txtId;
    TextView txtPort;
    TextView txtPower;
    TextView txtNumbSamples;
    TextView txtModulation;
    TextView txtRate;
    TextView txtBand;
    TextView txtLDPC;
    ImageView delete;
    ImageView run_pause;
    View parentView;
    TransmitterFragment fragment;
    private ArrayList<UDPStream> dataSet;
    private static int currentlyActiveCounter = 0;


    public UDPStreamAdapter(ArrayList<UDPStream> data, Context context, TransmitterFragment frag) {
        super(context, R.layout.transmiter_list_item, data);
        this.dataSet = data;
        this.mContext = context;
        this.fragment = frag;
    }


    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        UDPStream udpStream = getItem(position);


        switch (v.getId()) {
            case R.id.item_delete:
                /**
                 * Delete and Stop UDP Stream
                 */
                currentlyActiveCounter += udpStream.running ? -1 : 0;
                udpStream.running = false;
                dataSet.remove(udpStream);
                Nexutil.setIoctl(511, udpStream.id);
                fragment.usedIDs.remove(udpStream.id);
                fragment.unusedIDs.add(udpStream.id);
                notifyDataSetChanged();
                break;
            case R.id.item_run_stop:
                run_pause = (ImageView) parentView.findViewWithTag(position);
                if (udpStream.running) {
                    /**
                     * Stop UDP Stream
                     */
                    udpStream.running = false;
                    Log.i("TRANSMITTER", "stopping: " + udpStream.toString());
                    Nexutil.setIoctl(511, udpStream.id);
                    run_pause.setImageResource(android.R.drawable.ic_media_play);
                    currentlyActiveCounter--;
                } else {
                    /**
                     * Start UDP Stream
                     */
                    udpStream.running = true;
                    Log.i("TRANSMITTER", "starting: " + udpStream.toString());
                    Nexutil.setIoctl(510, udpStream.getBytes());
                    run_pause.setImageResource(android.R.drawable.ic_media_pause);
                    currentlyActiveCounter++;
                }
                break;
        }

        if (currentlyActiveCounter == 0) {
            LEDControl.deactivateLED();
        } else if (currentlyActiveCounter == 1) {
            LEDControl.setBrightnessRGB(rgb("#007f7f"));
            LEDControl.setOnOffMsRGB(1000, 1000);
            LEDControl.activateLED();
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final UDPStream udpStream = getItem(position);
        parentView = parent;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.transmiter_list_item, parent, false);

        txtId = (TextView) convertView.findViewById(R.id.streamId);
        txtPort = (TextView) convertView.findViewById(R.id.portValue);
        // Add new TextView in list item if needed
        //txtNumbSamples = (TextView) convertView.findViewById(R.id.numbPaSeekbarText);
        txtPower = (TextView) convertView.findViewById(R.id.powerValue);
        txtModulation = (TextView) convertView.findViewById(R.id.modulation_value);
        txtRate = (TextView) convertView.findViewById(R.id.rateValue);
        txtBand = (TextView) convertView.findViewById(R.id.bandValue);
        txtLDPC = (TextView) convertView.findViewById(R.id.ldpcValue);
        delete = (ImageView) convertView.findViewById(R.id.item_delete);
        run_pause = (ImageView) convertView.findViewById(R.id.item_run_stop);


        txtId.setText(String.valueOf(udpStream.id));
        txtPort.setText(String.valueOf(udpStream.destPort));
        txtPower.setText(String.valueOf(udpStream.power));
        //txtNumbSamples.setText(String.valueOf(udpStream.numbFrames));
        txtModulation.setText(udpStream.modulation.toString());
        switch (udpStream.modulation) {
            case IEEE80211ag:
                convertView.findViewById(R.id.band_text).setVisibility(View.GONE);
                convertView.findViewById(R.id.bandValue).setVisibility(View.GONE);
                convertView.findViewById(R.id.bandUnit).setVisibility(View.GONE);
                convertView.findViewById(R.id.ldpcText).setVisibility(View.GONE);
                convertView.findViewById(R.id.ldpcValue).setVisibility(View.GONE);
                break;
            case IEEE80211b:
                convertView.findViewById(R.id.band_text).setVisibility(View.GONE);
                convertView.findViewById(R.id.bandValue).setVisibility(View.GONE);
                convertView.findViewById(R.id.bandUnit).setVisibility(View.GONE);
                convertView.findViewById(R.id.ldpcText).setVisibility(View.GONE);
                convertView.findViewById(R.id.ldpcValue).setVisibility(View.GONE);
                break;
            case IEEE80211n:
                ((TextView) convertView.findViewById(R.id.rate_text)).setText("MCS");
                ((TextView) convertView.findViewById(R.id.rateUnit)).setText("index");
                break;
            case IEEE80211ac:
                convertView.findViewById(R.id.ldpcText).setVisibility(View.GONE);
                convertView.findViewById(R.id.ldpcValue).setVisibility(View.GONE);
                ((TextView) convertView.findViewById(R.id.rate_text)).setText("MCS");
                ((TextView) convertView.findViewById(R.id.rateUnit)).setText("index");
                break;
        }

        txtRate.setText(String.valueOf(udpStream.rate));
        txtBand.setText(String.valueOf(udpStream.bandwidth));
        if (udpStream.ldpc) txtLDPC.setText("ON");
        else txtLDPC.setText("OFF");

        delete.setOnClickListener(this);
        delete.setTag(position);
        run_pause.setOnClickListener(this);
        run_pause.setTag(position);


        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.transListItem);
        relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fragment.existing_dialog_id = udpStream.id;
                udpStream.alertDialog.show();
                return true;
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }

}


