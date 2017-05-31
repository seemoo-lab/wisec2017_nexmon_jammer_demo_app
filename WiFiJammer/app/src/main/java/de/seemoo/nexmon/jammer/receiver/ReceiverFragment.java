package de.seemoo.nexmon.jammer.receiver;

import android.app.Fragment;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import de.seemoo.nexmon.jammer.MainActivity;
import de.seemoo.nexmon.jammer.global.ColorsTuDarmstadt;
import de.seemoo.nexmon.jammer.R;
import de.seemoo.nexmon.jammer.utils.Nexutil;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by Stathis on 05-May-17.
 */


//MAC, IP Address, new graph button
public class ReceiverFragment extends Fragment implements IAxisValueFormatter {

    public HashMap<String, float[]> data = new HashMap<>();
    ViewGroup container;
    AlertDialog helpDialog;
    Menu menu;
    private UDPReceiver udpReceiver;
    private Plotter plotter;
    private HorizontalBarChart mChart;
    private ArrayList<String> hashes = new ArrayList<>();
    private SortedSet<Packet> packetSet = new TreeSet<>();
    private Semaphore semaphore;
    private TextView xAxisLabel;
    private TextView yAxisLabel;
    private TableLayout streamDescriptionTable;
    private View streamDescriptionScrollView;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         * Inflate the layout for this fragment
         */
        this.container = container;
        createAlertDialogs();
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.receiver_fragment, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //installCustomWiFiFirmware();
        xAxisLabel = (TextView) getView().findViewById(R.id.x_axis);
        yAxisLabel = (TextView) getView().findViewById(R.id.y_axis);
        streamDescriptionTable = (TableLayout) getView().findViewById(R.id.streamDescriptionTable);
        streamDescriptionScrollView = getView().findViewById(R.id.streamDescriptionScrollView);
        mChart = (HorizontalBarChart) getView().findViewById(R.id.chart1);
        initializePlot();

        udpReceiver = new UDPReceiver();
        plotter = new Plotter();
        semaphore = new Semaphore(1, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.start:
                if (item.getTitle().toString().equals("Stop")) {
                    try {
                        Nexutil.getInstance().setIoctl(Nexutil.WLC_SET_MONITOR, 0);
                        Nexutil.getInstance().setIoctl(512, 0); // deactivate filtering for MAC addresses
                        udpReceiver.shutdown();
                        plotter.shutdown();
                        item.setTitle("Start");
                    } catch (Nexutil.FirmwareNotFoundException e) {
                        MainActivity.getInstance().getFirmwareDialog().show();
                    }
                } else {
                    try {
                        Nexutil.getInstance().setIoctl(Nexutil.WLC_SET_MONITOR, 96);
                        Nexutil.getInstance().setIoctl(508); // set NEXMON MAC address
                        Nexutil.getInstance().setIoctl(512, 1); // activate filtering for MAC addresses
                        udpReceiver = new UDPReceiver();
                        udpReceiver.start();
                        plotter = new Plotter();
                        plotter.start();
                        item.setTitle("Stop");
                    } catch (Nexutil.FirmwareNotFoundException e) {
                        MainActivity.getInstance().getFirmwareDialog().show();
                    }
                }
                return true;
            case R.id.reset:
                try {
                    Nexutil.getInstance().setIoctl(Nexutil.WLC_SET_MONITOR, 0);
                    Nexutil.getInstance().setIoctl(512, 0); // deactivate filtering for MAC addresses
                    udpReceiver.shutdown();
                    plotter.shutdown();
                } catch (Nexutil.FirmwareNotFoundException e) {
                    MainActivity.getInstance().getFirmwareDialog().show();
                }

                mChart.clear();
                initializePlot();
                data = new HashMap<>();
                hashes = new ArrayList<>();
                packetSet = new TreeSet<>();

                menu.findItem(R.id.start).setTitle("Start");
                xAxisLabel.setVisibility(View.GONE);
                yAxisLabel.setVisibility(View.GONE);
                streamDescriptionScrollView.setVisibility(View.GONE);
                return true;
            case R.id.help_receiver:
                try {
                    String ret = Nexutil.getInstance().getIoctl(500);
                    Log.d("Shell", ret);
                } catch (Nexutil.FirmwareNotFoundException e) {
                    MainActivity.getInstance().getFirmwareDialog().show();
                }
                helpDialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createAlertDialogs() {

        View list_layout = getActivity().getLayoutInflater().inflate(R.layout.help_receiver, null, true);

        WebView wvHelp = (WebView) list_layout.findViewById(R.id.wvHelp);
        wvHelp.loadUrl("file:///android_asset/html/help_receiver.html");

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

    private void initializePlot() {

        mChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(40);

        mChart.setNoDataText("No packets were received yet");

        mChart.setAutoScaleMinMaxEnabled(false);

        mChart.setKeepPositionOnRotation(true);

        mChart.setPinchZoom(true);

        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);

        mChart.setDrawValueAboveBar(false);

        mChart.setHighlightFullBarEnabled(false);
        mChart.setHighlightPerTapEnabled(false);
        mChart.setHighlightPerDragEnabled(false);

        mChart.setHardwareAccelerationEnabled(true);


        // change the position of the y-labels
        YAxis leftAxis = mChart.getAxisRight();
        leftAxis.setDrawGridLines(false);
        // this replaces setStartAtZero(true)
        //leftAxis.setValueFormatter(new com.github.mikephil.charting.formatter.LargeValueFormatter());
        mChart.getAxisLeft().setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(this);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);


        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);


    }

    private void updatePlot() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();

                streamDescriptionTable.removeAllViews();
                final TableRow tableHeader = (TableRow) getActivity().getLayoutInflater().inflate(R.layout.receiver_plot_table_header, null);
                //Add row to the table
                streamDescriptionTable.addView(tableHeader);
                int i = 0;
                for (HashMap.Entry<String, float[]> entry : data.entrySet()) {

                    String key = entry.getKey();

                    String[] params = key.split("-");

                    final TableRow tableRow = (TableRow) getActivity().getLayoutInflater().inflate(R.layout.receiver_plot_table_row, null);
                    TextView tv;

                    //Filling in cells
                    tv = (TextView) tableRow.findViewById(R.id.nameValue);
                    tv.setText("Stream " + i);

                    tv = (TextView) tableRow.findViewById(R.id.portValue);
                    tv.setText(params[0]);

                    tv = (TextView) tableRow.findViewById(R.id.encodingValue);
                    tv.setText(params[1]);

                    tv = (TextView) tableRow.findViewById(R.id.bandwidthValue);
                    tv.setText(params[2]);

                    tv = (TextView) tableRow.findViewById(R.id.rateValue);
                    tv.setText(params[3]);

                    tv = (TextView) tableRow.findViewById(R.id.ldpcValue);
                    tv.setText(params[4]);

                    //Add row to the table
                    streamDescriptionTable.addView(tableRow);

                    float[] value = entry.getValue();

                    float val1 = value[1];
                    float val2 = value[0];

                    yVals.add(new BarEntry(i, new float[]{val1, val2}));
                    i++;
                }

                BarDataSet set;

                if (mChart.getData() != null &&
                        mChart.getData().getDataSetCount() > 0) {
                    set = (BarDataSet) mChart.getData().getDataSetByIndex(0);
                    set.setValues(yVals);
                    mChart.getData().notifyDataChanged();
                    mChart.notifyDataSetChanged();
                } else {
                    set = new BarDataSet(yVals, "");
                    set.setDrawIcons(false);
                    set.setColors(getColors());
                    set.setStackLabels(new String[]{"FCS incorrect", "FCS correct"});

                    ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
                    dataSets.add(set);

                    BarData data = new BarData(dataSets);
                    //data.setValueFormatter(new com.github.mikephil.charting.formatter.LargeValueFormatter());

                    mChart.setData(data);
                }

                mChart.setFitBars(true);

                xAxisLabel.setVisibility(View.VISIBLE);
                yAxisLabel.setVisibility(View.VISIBLE);
                streamDescriptionScrollView.setVisibility(View.VISIBLE);
                mChart.invalidate();
            }
        });

    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)

        return "Stream " + (int) value;
    }

    private int[] getColors() {

        int stacksize = 2;

        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];

        colors[0] = ColorsTuDarmstadt.COLOR_1B;
        colors[1] = ColorsTuDarmstadt.COLOR_6B;

        return colors;
    }

    private final class UDPReceiver extends Thread {

        public static final int RECV_BUFFER_SIZE = 1000;
        private static final String TAG = "UDPReceiverThread";
        private final char[] hexArray = "0123456789ABCDEF".toCharArray();
        private boolean mContinueRunning = true;
        private DatagramSocket mSocket = null;

        public String bytesToHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }

        public void run() {
            Log.d(TAG, "Thread run");
            mContinueRunning = true;

            try {
                mSocket = new DatagramSocket(5500);
            } catch (SocketException e) {
                // TODO: Handle address already in use.
                Log.d(TAG, "Error opening the UDP socket.");
                e.printStackTrace();
                return;
            }

            byte[] buffer = new byte[RECV_BUFFER_SIZE];
            DatagramPacket p = new DatagramPacket(buffer, buffer.length);

            while (mContinueRunning) {
                try {
                    mSocket.receive(p);


                    // TODO: Check source address of packet and/or validate it with other means
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!mContinueRunning) {
                    Log.d(TAG, "Stop thread activity...");
                    break;
                }

                Packet packet = new Packet(buffer);
                //Log.d(TAG, "timestamp: " + packet.timestamp_mac + " port: " + packet.port + " fcs_error error: " + packet.fcs_error + " length: " + packet.length);

                try {

                    semaphore.acquire();

                    packetSet.add(packet);

                    if (!data.containsKey(packet.hash)) {
                        hashes.add(packet.hash);
                        data.put(packet.hash, new float[2]);
                    }

                    semaphore.release();

                } catch (Exception e) {
                    e.printStackTrace();
                }




            }

            Log.d(TAG, "Thread afterrun");
        }

        public void shutdown() {
            mContinueRunning = false;
            mSocket.close();
        }
    }


    private final class Plotter extends Thread {


        private static final String TAG = "PlotterThread";

        private boolean mContinueRunning = true;


        public void run() {
            Log.d(TAG, "Plotter Thread run");
            mContinueRunning = true;


            while (mContinueRunning) {

                try {

                    long windows_size = 1L;
                    int sum = 0;
                    int sum_length_fcs_1 = 0;
                    int sum_length_fcs_0 = 0;
                    int count_removes = 0;
                    long current_time = System.nanoTime();
                    long time = current_time - windows_size * 1000000000L;

                    //Log.d(TAG, "acquiring Semaphore");
                    semaphore.acquire();

                    for (Iterator<Packet> i = packetSet.iterator(); i.hasNext(); ) {
                        Packet pa = i.next();
                        if (pa.timestamp_android < time) {
                            i.remove();
                            count_removes++;
                        }
                    }

                    if (hashes.size() > 0) {
                        for (String hash : hashes) {

                            for (Iterator<Packet> i = packetSet.iterator(); i.hasNext(); ) {
                                Packet pa = i.next();
                                if (hash.equals(pa.hash)) {
                                    sum++;
                                    if (pa.fcs_error) {
                                        sum_length_fcs_1 += pa.length;
                                    } else {
                                        sum_length_fcs_0 += pa.length;
                                    }
                                }
                            }

                            float throughput_fcs_0 = sum_length_fcs_0 / windows_size * 8 / 1e6f;
                            float throughput_fcs_1 = sum_length_fcs_1 / windows_size * 8 / 1e6f;


                            data.get(hash)[0] = throughput_fcs_0;
                            data.get(hash)[1] = throughput_fcs_1;

                            if (data.size() > 0) updatePlot();
                            //Log.d(TAG, "Plotting!!!");


                        }
                    }
                    semaphore.release();
                    //Log.d(TAG, "releasing Semaphore");
                    sleep(1000);


                    if (!mContinueRunning) {
                        Log.d(TAG, "Stop thread activity...");
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "Plotter afterrun");
        }

        public void shutdown() {
            mContinueRunning = false;
        }
    }
    /*
    struct jamming_receiver_header {
    uint32 timestamp;
    uint16 port;
    bool fcs_error;
    uint16 length;
    uint8 encoding;
    uint8 bandwidth;
    uint16 rate;
    bool ldpc;
    }
     */

    public class Packet implements Comparable<Packet> {

        long timestamp_mac;
        long timestamp_android;
        int port;
        boolean fcs_error;
        int length;
        byte encoding;
        byte bandwidth;
        int rate;
        boolean ldpc;
        String hash;


        public Packet(byte buffer[]) {
            ByteBuffer buf = ByteBuffer.wrap(buffer);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            this.timestamp_mac = (long) buf.getInt() & 0xffffffffL;
            this.timestamp_android = System.nanoTime();
            this.port = (int) buf.getShort() & 0xffff;
            this.fcs_error = ((int) buf.get() & 0xf) == 1;
            this.length = (int) buf.getShort() & 0xffff;
            this.encoding = buf.get();
            this.bandwidth = buf.get();
            this.rate = (int) buf.getShort() & 0xffff;
            this.ldpc = ((int) buf.get() & 0xf) == 1;
            this.hash = port + "-" + encoding + "-" + bandwidth + "-" + rate + "-" + ldpc;

        }

        public int compareTo(Packet other) {
            return Long.compare(this.timestamp_android, other.timestamp_android);
        }


    }

}

