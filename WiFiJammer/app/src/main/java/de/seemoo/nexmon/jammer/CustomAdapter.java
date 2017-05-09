package de.seemoo.nexmon.jammer;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Stathis on 05-May-17.
 */


public class CustomAdapter extends ArrayAdapter<UDPStream> implements View.OnClickListener {

    Context mContext;
    TextView txtName;
    TextView txtPort;
    ImageView delete;
    ImageView run_pause;
    View parentView;
    private ArrayList<UDPStream> dataSet;


    public CustomAdapter(ArrayList<UDPStream> data, Context context) {
        super(context, R.layout.transmiter_list_item, data);
        this.dataSet = data;
        this.mContext = context;

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
                    System.out.println("Thread stopped");
                    run_pause.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    /**
                     * TODO Start UDP Stream
                     */
                    udpStream.running = true;
                    run_pause.setImageResource(android.R.drawable.ic_media_pause);
                }
                break;
            case R.id.name:
                System.out.println("hi");
                Snackbar.make(v, "UDP Stream state: " + udpStream.running, Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
            default:
                System.out.println("hi");
                Snackbar.make(v, "UDP Stream state: " + udpStream.running, Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        UDPStream udpStream = getItem(position);
        parentView = parent;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.transmiter_list_item, parent, false);

        txtName = (TextView) convertView.findViewById(R.id.name);
        txtPort = (TextView) convertView.findViewById(R.id.port);
        delete = (ImageView) convertView.findViewById(R.id.item_delete);
        run_pause = (ImageView) convertView.findViewById(R.id.item_run_stop);


        txtName.setText("UDP Stream");
        txtPort.setText(String.valueOf(udpStream.port));
        delete.setOnClickListener(this);
        delete.setTag(position);
        run_pause.setOnClickListener(this);
        run_pause.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }
}