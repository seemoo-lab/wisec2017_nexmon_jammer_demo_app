package de.seemoo.nexmon.jammer;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by Stathis on 05-May-17.
 */


//MAC, IP Address, new graph button
public class ReceiverFragment extends Fragment implements IAxisValueFormatter {

    static Boolean isRootAvailable;
    public HashMap<Integer, int[]> data = new HashMap<>();
    ViewGroup container;
    AlertDialog ipAddressDialog;
    AlertDialog srcPortDialog;
    AlertDialog dstPortDialog;
    int srcPort;
    int dstPort;
    InetAddress ipAddress;
    Menu menu;
    private ProgressDialog progressbox;
    private PlotThread plotThread;
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
        dstPort = 1234;
        try {
            ipAddress = Inet4Address.getByName("192.168.1.2");
        } catch (Exception e) {
            e.printStackTrace();
        }



        progressbox = new ProgressDialog(getActivity());
        progressbox.setTitle("Initialising");
        progressbox.setMessage("Requesting root permissions..");
        progressbox.setIndeterminate(true);
        progressbox.setCancelable(false);
        progressbox.show();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                isRootAvailable = Shell.SU.available();
                Boolean processExists = false;
                String pid = null;
                if (isRootAvailable) {

                    initializePlot();

                    List<String> out = Shell.SH.run("ps | grep tcpdump");
                    if (out.size() == 1) {
                        processExists = true;
                        TcpdumpPacketCapture.stopTcpdumpCapture(getActivity());
                    } else if (out.size() == 0) {
                        if (loadTcpdumpFromAssets() != 0)
                            throw new RuntimeException("Copying tcpdump binary failed.");
                    } else
                        throw new RuntimeException("Searching for running process returned unexpected result.");
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isRootAvailable) {
                            ((TextView) getView().findViewById(R.id.main_tv)).setText("Root permission denied or phone is not rooted!");
                        } else {
                            ((TextView) getView().findViewById(R.id.main_tv)).setText("Receiver Ready");
                        }
                    }
                });
                progressbox.dismiss();
            }
        };
        new Thread(runnable).start();
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
                if (isRootAvailable) {
                    if (item.getTitle().toString().equals("Stop")) {
                        //Using progress dialogue from main. See comment in: TcpdumpPacketCapture.stopTcpdumpCapture
                        progressbox.setMessage("Killing Tcpdump process.");
                        progressbox.show();
                        TcpdumpPacketCapture.stopTcpdumpCapture(getActivity());
                        stopPlotThread();
                        item.setTitle("Start");
                        ((TextView) getView().findViewById(R.id.main_tv)).setText("Packet capture stopped.");
                        progressbox.dismiss();

                    } else {

                        TcpdumpPacketCapture.setIpAddress(ipAddress);
                        TcpdumpPacketCapture.setPort(dstPort);
                        TcpdumpPacketCapture.initialiseCapture(getActivity());
                        startPlotThread();
                        item.setTitle("Stop");
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Root privileges are needed. Please grant root permissions or root your phone.", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.reset:
                if (isRootAvailable) {
                    progressbox.setMessage("Killing Tcpdump process.");
                    progressbox.show();
                    TcpdumpPacketCapture.stopTcpdumpCapture(getActivity());
                    stopPlotThread();
                    TcpdumpPacketCapture.resetData();
                    data = new HashMap<>();
                    ports = new ArrayList<>();
                    initializePlot();
                    menu.findItem(R.id.start).setTitle("Start");
                    ((TextView) getView().findViewById(R.id.main_tv)).setText("Packet capture reseted.");
                    progressbox.dismiss();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Root privileges are needed. Please grant root permissions or root your phone.", Toast.LENGTH_SHORT).show();
                }
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public void createAlertDialogs() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        final View srcPortLayout = getActivity().getLayoutInflater().inflate(R.layout.udpstream_dialog, container, false);

        alertDialogBuilder.setView(srcPortLayout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1, int id) {
                       /* EditText editText = (EditText) srcPortLayout.findViewById(R.id.portText);
                        int port = Integer.parseInt(editText.getText().toString());
                        if (port > 10000 || port < 1) {
                            Toast.makeText(getActivity().getApplicationContext(), "This is not a port number please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            srcPort = port;

                        }
                        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);*/
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        srcPortDialog = alertDialogBuilder.create();

        final View dstPortLayout = getActivity().getLayoutInflater().inflate(R.layout.dst_port_dialog, container, false);

        alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setView(dstPortLayout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1, int id) {
                       /* EditText editText = (EditText) dstPortLayout.findViewById(R.id.portText);
                        int port = Integer.parseInt(editText.getText().toString());
                        if (port > 10000 || port < 1) {
                            Toast.makeText(getActivity().getApplicationContext(), "This is not a port number please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            dstPort = port;

                        }
                        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);*/
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        dstPortDialog = alertDialogBuilder.create();

        final View ipLayout = getActivity().getLayoutInflater().inflate(R.layout.ip_dialog, container, false);

        alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setView(ipLayout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1, int id) {

                        EditText editText = (EditText) ipLayout.findViewById(R.id.ipAddress);
                        final IPAddressValidator ipAddressValidator = new IPAddressValidator();
                        try {
                            String txt = editText.getText().toString();
                            if (ipAddressValidator.validate(txt)) {
                                ipAddress = Inet4Address.getByName(txt);
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "This is not a valid IP address please try again", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        ipAddressDialog = alertDialogBuilder.create();

    }

    private int loadTcpdumpFromAssets() {
        int retval = 0;
        // updating progress message from other thread causes exception.
        // progressbox.setMessage("Setting up data..");
        String rootDataPath = getActivity().getApplicationInfo().dataDir + "/files";
        String filePath = rootDataPath + "/tcpdump";
        File file = new File(filePath);
        AssetManager assetManager = getActivity().getAssets();

        try {
            if (file.exists()) {
                Shell.SH.run("chmod 755 " + filePath);
                return retval;
            }
            new File(rootDataPath).mkdirs();
            retval = copyFileFromAsset(assetManager, "tcpdump", filePath);
            // Mark the binary executable

            Shell.SH.run("chmod 755 " + filePath);
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
        xAxis.setPosition(XAxisPosition.TOP);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);


        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
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
            data.setValueFormatter(new LargeValueFormatter());

            mChart.setData(data);
        }

        mChart.setFitBars(true);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

    public void startPlotThread() {

        plotThread = new PlotThread();
        plotThread.start();
    }

    private void stopPlotThread() {
        if (plotThread != null) plotThread.stopThread();
    }

    private class PlotThread extends Thread {
        public boolean running;

        public void stopThread() {
            running = false;
        }

        @Override
        public void run() {
            running = true;
            while (running) {
                data = TcpdumpPacketCapture.getData();
                if (data.size() > 0) updatePlot();
                try {
                    sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }
}

