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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by Stathis on 05-May-17.
 */


//MAC, IP Address, new graph button
public class ReceiverFragment extends Fragment implements IAxisValueFormatter {

    private static Shell.Interactive rootShell;
    private static boolean isInitialised = false;
    public HashMap<Integer, int[]> data = new HashMap<>();
    ViewGroup container;
    AlertDialog helpDialog;
    Menu menu;
    private UDPReceiver udpReceiver;
    private HorizontalBarChart mChart;
    private ArrayList<Integer> ports = new ArrayList<>();

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
        rootShell = new Shell.Builder().
                useSU().
                setWantSTDERR(false).
                setMinimalLogging(true).
                open(new Shell.OnCommandResultListener() {
                    @Override
                    public void onCommandResult(int commandVal, int exitVal, List<String> out) {
                        //Callback checking successful shell start.
                        if (exitVal == Shell.OnCommandResultListener.SHELL_RUNNING) {
                            isInitialised = true;

                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "Root privileges are needed. Please grant root permissions or try again.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

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

            List<String> out = Shell.SH.run("ifconfig wlan0 down && ifconfig wlan0 up");
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
                        item.setTitle("Start");

                    } else {
                        udpReceiver = new UDPReceiver();
                        udpReceiver.start();
                        item.setTitle("Stop");
                    }
                return true;
            case R.id.reset:
                udpReceiver.shutdown();
                initializePlot();
                menu.findItem(R.id.start).setTitle("Start");
                getView().findViewById(R.id.x_axis).setVisibility(View.GONE);
                getView().findViewById(R.id.y_axis).setVisibility(View.GONE);
                return true;
            case R.id.help_receiver:
                List<String> out = Shell.SH.run("nexutil -g500p");
                Log.d("Shell", out.toString());
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


        ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();

        int i = 0;
        for (HashMap.Entry<Integer, int[]> entry : data.entrySet()) {

            int key = entry.getKey();
            int[] value = entry.getValue();

            float val1 = value[0];
            float val2 = value[0];

            ports.add(i, key);

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
            set.setStackLabels(new String[]{"FCS correct", "FCS incorrect"});

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set);

            BarData data = new BarData(dataSets);
            data.setValueFormatter(new com.github.mikephil.charting.formatter.LargeValueFormatter());

            mChart.setData(data);
        }

        mChart.setFitBars(true);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

        for (int i = 0; i < colors.length; i++) {
            colors[i] = ColorTemplate.MATERIAL_COLORS[i];
        }

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
                int length = p.getLength();
                p.getData();
                int port = p.getPort();

                byte[] timestampbytes = new byte[8];
                timestampbytes[0] = 0;
                timestampbytes[1] = 0;
                timestampbytes[2] = 0;
                timestampbytes[3] = 0;
                timestampbytes[4] = buffer[3];
                timestampbytes[5] = buffer[2];
                timestampbytes[6] = buffer[1];
                timestampbytes[7] = buffer[0];
                long timestamp = java.nio.ByteBuffer.wrap(timestampbytes).getLong();

                byte[] portbytes = new byte[4];
                portbytes[0] = 0;
                portbytes[1] = 0;
                portbytes[2] = buffer[4];
                portbytes[3] = buffer[5];
                //int port = java.nio.ByteBuffer.wrap(portbytes).getInt();

                int fcs_error = buffer[6];

                if (!data.containsKey(port)) {
                    data.put(port, new int[2]);
                }
                Log.d(TAG, String.valueOf(fcs_error));
                data.get(port)[0]++;

                if (data.size() > 0) updatePlot();

                Log.d(TAG, "timestamp: " + timestamp + " port: " + port + " fcs error: " + fcs_error);
                try {
                    sleep(1000);
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

}

