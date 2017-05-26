package de.seemoo.nexmon.jammer;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

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

import de.seemoo.nexmon.jammer.utils.Nexutil;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by Stathis on 05-May-17.
 */


//MAC, IP Address, new graph button
public class ReceiverFragment extends Fragment implements IAxisValueFormatter {

    private static boolean isInitialised = false;
    public HashMap<Integer, float[]> data = new HashMap<>();
    ViewGroup container;
    AlertDialog helpDialog;
    Menu menu;
    private UDPReceiver udpReceiver;
    private Plotter plotter;
    private HorizontalBarChart mChart;
    private ArrayList<Integer> ports = new ArrayList<>();
    private SortedSet<Packet> packetSet = new TreeSet<>();
    private Semaphore semaphore;

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
        initializePlot();
        //installCustomWiFiFirmware();
        udpReceiver = new UDPReceiver();
        plotter = new Plotter();
        semaphore = new Semaphore(1, true);
        new Nexutil(getActivity());
        isInitialised = Nexutil.isInitialized();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    private int installCustomWiFiFirmware() {
        int retval = 0;
        // updating progress message from other thread causes exception.
        // progressbox.setMessage("Setting up data..");
        String rootDataPath = "/vendor/firmware/";
        String filePath = "/vendor/firmware/fw_bcmdhd.bin";
        File file = new File(filePath);
        AssetManager assetManager = getActivity().getAssets();

        try {
            if (file.exists()) {
                //return retval;
            }
            new File(rootDataPath).mkdirs();
            retval = copyFileFromAsset(assetManager, "fw_bcmdhd.bin", filePath);

            List<String> out = Shell.SU.run("ifconfig wlan0 down && ifconfig wlan0 up");
            Log.d("Shell", out.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            retval = -1;
        }
        return retval;
    }

    private int copyFileFromAsset(AssetManager assetManager, String sourcePath, String destPath) {
        byte[] buff = new byte[1024];
        int len;
        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(sourcePath);
            new File(destPath).createNewFile();
            out = new FileOutputStream(destPath);
            // write file
            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
        return 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.start:
                if (item.getTitle().toString().equals("Stop")) {
                    udpReceiver.shutdown();
                    plotter.shutdown();
                    item.setTitle("Start");

                } else {
                    udpReceiver = new UDPReceiver();
                    udpReceiver.start();
                    plotter = new Plotter();
                    plotter.start();
                    item.setTitle("Stop");
                }
                return true;
            case R.id.reset:
                udpReceiver.shutdown();
                plotter.shutdown();
                initializePlot();
                menu.findItem(R.id.start).setTitle("Start");
                getView().findViewById(R.id.x_axis).setVisibility(View.GONE);
                getView().findViewById(R.id.y_axis).setVisibility(View.GONE);
                return true;
            case R.id.help_receiver:
                String ret = Nexutil.getIoctl(500);

                Log.d("Shell", ret);
                //helpDialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createAlertDialogs() {

        View list_layout = getActivity().getLayoutInflater().inflate(R.layout.help_receiver, null, true);

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

    private void initializePlot() {
        mChart = (HorizontalBarChart) getView().findViewById(R.id.chart1);

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

        // change the position of the y-labels
        YAxis leftAxis = mChart.getAxisRight();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        //leftAxis.setValueFormatter(new com.github.mikephil.charting.formatter.LargeValueFormatter());
        mChart.getAxisLeft().setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(this);
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);


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

                int i = 0;
                for (HashMap.Entry<Integer, float[]> entry : data.entrySet()) {

                    int key = entry.getKey();
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


                getView().findViewById(R.id.x_axis).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.y_axis).setVisibility(View.VISIBLE);
                mChart.invalidate();
            }
        });

    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)

        return ports.get((int) value).toString();
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
                Log.d(TAG, "timestamp: " + packet.timestamp_mac + " port: " + packet.port + " fcs_error error: " + packet.fcs_error + " length: " + packet.length);

                try {

                    semaphore.acquire();

                    packetSet.add(packet);

                    if (!data.containsKey(packet.port)) {
                        ports.add(packet.port);
                        data.put(packet.port, new float[2]);
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


                    long windows_size = 10L;
                    int sum = 0;
                    int sum_length_fcs_1 = 0;
                    int sum_length_fcs_0 = 0;
                    int count_removes = 0;
                    long current_time = System.nanoTime();
                    long time = current_time - windows_size * 1000000000L;

                    Log.d(TAG, "acquiring Semaphore");
                    semaphore.acquire();

                    for (Iterator<Packet> i = packetSet.iterator(); i.hasNext(); ) {
                        Packet pa = i.next();
                        if (pa.timestamp_android < time) {
                            i.remove();
                            count_removes++;
                        }
                    }

                    if (ports.size() > 0) {
                        for (int port : ports) {

                            for (Iterator<Packet> i = packetSet.iterator(); i.hasNext(); ) {
                                Packet pa = i.next();
                                if (port == pa.port) {
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


                            data.get(port)[0] = throughput_fcs_0;
                            data.get(port)[1] = throughput_fcs_1;

                            if (data.size() > 0) updatePlot();
                            Log.d(TAG, "Plotting!!!");


                        }
                    }
                    semaphore.release();
                    Log.d(TAG, "releasing Semaphore");
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
        int hash;


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

        }

        public int compareTo(Packet other) {
            return Long.compare(this.timestamp_android, other.timestamp_android);
        }


    }

}

