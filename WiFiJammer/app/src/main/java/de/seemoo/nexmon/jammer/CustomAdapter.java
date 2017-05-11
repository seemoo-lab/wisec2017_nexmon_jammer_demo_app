package de.seemoo.nexmon.jammer;


import android.content.Context;
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

/**
 * Created by Stathis on 05-May-17.
 */


public class CustomAdapter extends ArrayAdapter<UDPStream> implements View.OnClickListener {

    Context mContext;
    TextView txtId;
    TextView txtPort;
    TextView txtPower;
    TextView txtModulation;
    TextView txtRate;
    TextView txtBand;
    TextView txtLDPC;
    ImageView delete;
    ImageView run_pause;
    View parentView;
    TransmitterFragment fragment;
    private ArrayList<UDPStream> dataSet;


    public CustomAdapter(ArrayList<UDPStream> data, Context context, TransmitterFragment frag) {
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
                 * TODO Stop UDP Stream
                 */
                udpStream.running = false;
                dataSet.remove(udpStream);
                notifyDataSetChanged();
                break;
            case R.id.item_run_stop:
                run_pause = (ImageView) parentView.findViewWithTag(position);
                if (udpStream.running) {
                    /**
                     * TODO Stop UDP Stream
                     */
                    udpStream.running = false;
                    Log.i("TRANSMITTER", "Stopping " + txtId + " at port " + txtPort);
                    run_pause.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    /**
                     * TODO Start UDP Stream
                     */
                    udpStream.running = true;
                    Log.i("TRANSMITTER", "Starting " + txtId + " at port " + txtPort);
                    run_pause.setImageResource(android.R.drawable.ic_media_pause);
                }
                break;
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
        txtModulation.setText(udpStream.modulation);
        switch (udpStream.modulation) {
            case "802.11a/g":
                convertView.findViewById(R.id.band_text).setVisibility(View.GONE);
                convertView.findViewById(R.id.bandValue).setVisibility(View.GONE);
                convertView.findViewById(R.id.bandUnit).setVisibility(View.GONE);
                convertView.findViewById(R.id.ldpcText).setVisibility(View.GONE);
                convertView.findViewById(R.id.ldpcValue).setVisibility(View.GONE);
                break;
            case "802.11b":
                convertView.findViewById(R.id.band_text).setVisibility(View.GONE);
                convertView.findViewById(R.id.bandValue).setVisibility(View.GONE);
                convertView.findViewById(R.id.bandUnit).setVisibility(View.GONE);
                convertView.findViewById(R.id.ldpcText).setVisibility(View.GONE);
                convertView.findViewById(R.id.ldpcValue).setVisibility(View.GONE);
                break;
            case "802.11n":
                ((TextView) convertView.findViewById(R.id.rate_text)).setText("MCS");
                ((TextView) convertView.findViewById(R.id.rateUnit)).setText("index");
                break;
            case "802.11ac":
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


