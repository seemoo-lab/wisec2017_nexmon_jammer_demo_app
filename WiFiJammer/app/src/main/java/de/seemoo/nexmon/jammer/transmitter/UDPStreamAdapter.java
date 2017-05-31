package de.seemoo.nexmon.jammer.transmitter;


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
import android.widget.Toast;

import java.util.ArrayList;

import de.seemoo.nexmon.jammer.R;
import de.seemoo.nexmon.jammer.utils.LEDControl;
import de.seemoo.nexmon.jammer.utils.Nexutil;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

/**
 * Created by Stathis on 05-May-17.
 */


public class UDPStreamAdapter extends ArrayAdapter<UDPStream> implements View.OnClickListener {

    private static int currentlyActiveCounter = 0;
    Context mContext;
    TransmitterFragment fragment;
    private ArrayList<UDPStream> dataSet;

    public UDPStreamAdapter(ArrayList<UDPStream> data, Context context, TransmitterFragment frag) {
        super(context, R.layout.transmiter_list_item, data);
        this.dataSet = data;
        this.mContext = context;
        this.fragment = frag;
    }

    @Override
    public void onClick(View v) {
        ViewHolder viewHolder = (ViewHolder) ((View) v.getParent()).getTag();
        int position = (Integer) v.getTag();
        UDPStream udpStream = getItem(position);

        switch (v.getId()) {
            case R.id.item_delete:
                try {
                    Nexutil.getInstance().setIoctl(511, udpStream.id);
                    currentlyActiveCounter += udpStream.running ? -1 : 0;
                    udpStream.running = false;
                } catch (Nexutil.FirmwareNotFoundException e) {
                    Toast.makeText(getContext(), "You need to install the jamming firmware first", Toast.LENGTH_SHORT).show();
                }
                dataSet.remove(position);
                fragment.usedIDs.remove(udpStream.id);
                fragment.unusedIDs.add(udpStream.id);
                notifyDataSetChanged();
                break;
            case R.id.item_run_stop:
                if (udpStream.running) {
                    try {
                        Nexutil.getInstance().setIoctl(511, udpStream.id);
                        udpStream.running = false;
                        Log.i("TRANSMITTER", "stopping: " + udpStream.toString());
                        viewHolder.run_pause.setImageResource(android.R.drawable.ic_media_play);
                        currentlyActiveCounter--;
                    } catch (Nexutil.FirmwareNotFoundException e) {
                        Toast.makeText(getContext(), "You need to install the jamming firmware first", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Nexutil.getInstance().setIoctl(510, udpStream.getBytes());
                        udpStream.running = true;
                        Log.i("TRANSMITTER", "starting: " + udpStream.toString());
                        viewHolder.run_pause.setImageResource(android.R.drawable.ic_media_pause);
                        currentlyActiveCounter++;
                    } catch (Nexutil.FirmwareNotFoundException e) {
                        Toast.makeText(getContext(), "You need to install the jamming firmware first", Toast.LENGTH_SHORT).show();
                    }
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
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.transmiter_list_item, parent, false);
            viewHolder.parentView = parent;
            viewHolder.txtId = (TextView) convertView.findViewById(R.id.streamId);
            viewHolder.txtPort = (TextView) convertView.findViewById(R.id.portValue);
            viewHolder.txtPower = (TextView) convertView.findViewById(R.id.powerValue);
            viewHolder.txtModulation = (TextView) convertView.findViewById(R.id.modulation_value);
            viewHolder.txtRate = (TextView) convertView.findViewById(R.id.rateValue);
            viewHolder.txtBand = (TextView) convertView.findViewById(R.id.bandValue);
            viewHolder.txtLDPC = (TextView) convertView.findViewById(R.id.ldpcValue);
            viewHolder.delete = (ImageView) convertView.findViewById(R.id.item_delete);
            viewHolder.run_pause = (ImageView) convertView.findViewById(R.id.item_run_stop);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.txtId.setText(String.valueOf(udpStream.id));
        viewHolder.txtPort.setText(String.valueOf(udpStream.destPort));
        viewHolder.txtPower.setText(String.valueOf(udpStream.power));
        viewHolder.txtModulation.setText(udpStream.modulation.toString());
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

        viewHolder.txtRate.setText(String.valueOf(udpStream.rate));
        viewHolder.txtBand.setText(String.valueOf(udpStream.bandwidth));
        if (udpStream.ldpc) viewHolder.txtLDPC.setText("ON");
        else viewHolder.txtLDPC.setText("OFF");

        viewHolder.delete.setOnClickListener(this);
        viewHolder.delete.setTag(position);
        viewHolder.run_pause.setOnClickListener(this);
        viewHolder.run_pause.setTag(position);

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

    // View lookup cache
    private static class ViewHolder {
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
    }

}


