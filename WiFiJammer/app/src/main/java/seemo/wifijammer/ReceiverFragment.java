package seemo.wifijammer;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.widget.SeekBar;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;


import eu.chainfire.libsuperuser.Shell;

/**
 * Created by Stathis on 05-May-17.
 */

public class ReceiverFragment extends Fragment {

    private ProgressDialog progressbox;
    ViewGroup container;
    AlertDialog ipAddressDialog;
    AlertDialog srcPortDialog;
    AlertDialog dstPortDialog;
    int srcPort;
    int dstPort;
    InetAddress ipAddress;

    private HorizontalBarChart mChart;

    private TextView tvX, tvY;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         * Inflate the layout for this fragment
         */
        setHasOptionsMenu(true);
        this.container = container;
        createAlertDialogs();

        return inflater.inflate(R.layout.receiver_fragment, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        drawPlot();
        dstPort = 1234;
        try{
            ipAddress = Inet4Address.getByName("192.168.1.2");
        }catch (Exception e){e.printStackTrace();}

        final Button bt = (Button) getView().findViewById(R.id.button_packet_capture);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button bt = (Button) v;
                bt.setEnabled(false);
                if((int)bt.getTag() == 1){
                    //Using progress dialogue from main. See comment in: TcpdumpPacketCapture.stopTcpdumpCapture
                    progressbox.setMessage("Killing Tcpdump process.");
                    progressbox.show();
                    TcpdumpPacketCapture.stopTcpdumpCapture(getActivity());
                    bt.setText("Start Capture");
                    bt.setTag(0);
                    ((TextView) getView().findViewById(R.id.main_tv)).setText("Packet capture stopped.");
                    progressbox.dismiss();
                }
                else if ((int)bt.getTag() == 0){

                    TcpdumpPacketCapture.setIpAddress(ipAddress);
                    TcpdumpPacketCapture.setPort(dstPort);
                    TcpdumpPacketCapture.initialiseCapture(getActivity());
                    bt.setText("Stop  Capture");
                    bt.setTag(1);
                }
                bt.setEnabled(true);
            }
        });


        progressbox = new ProgressDialog(getActivity());
        progressbox.setTitle("Initialising");
        progressbox.setMessage("Requesting root permissions..");
        progressbox.setIndeterminate(true);
        progressbox.setCancelable(false);
        progressbox.show();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final Boolean isRootAvailable = Shell.SU.available();
                Boolean processExists = false;
                String pid = null;
                if(isRootAvailable) {
                    List<String> out = Shell.SH.run("ps | grep tcpdump");
                    if(out.size() == 1) {
                        processExists = true;
                        pid = (out.get(0).split("\\s+"))[1];
                    }
                    else if(out.size() == 0) {
                        if (loadTcpdumpFromAssets() != 0)
                            throw new RuntimeException("Copying tcpdump binary failed.");
                    }
                    else
                        throw new RuntimeException("Searching for running process returned unexpected result.");
                }

                final Boolean processExistsFinal = processExists;
                final String pidFinal = pid;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isRootAvailable) {
                            ((TextView) getView().findViewById(R.id.main_tv)).setText("Root permission denied or phone is not rooted!");
                            ( getView().findViewById(R.id.button_packet_capture)).setEnabled(false);
                        }
                        else {
                            if(processExistsFinal){
                                ((TextView) getView().findViewById(R.id.main_tv)).setText("Tcpdump already running at pid: " + pidFinal );
                                bt.setText("Stop  Capture");
                                bt.setTag(1);
                            }
                            else {
                                ((TextView) getView().findViewById(R.id.main_tv)).setText("Initialization Successful.");
                                bt.setTag(0);
                            }
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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.ip_address:
                ipAddressDialog.show();
                return true;
            case R.id.dstPort:
                dstPortDialog.show();
                return true;
            case R.id.srcPort:
                srcPortDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void createAlertDialogs(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        final View srcPortLayout = getActivity().getLayoutInflater().inflate(R.layout.src_port_dialog, container, false);

        alertDialogBuilder.setView(srcPortLayout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1,int id) {
                        EditText editText = (EditText) srcPortLayout.findViewById(R.id.portText);
                        int port = Integer.parseInt(editText.getText().toString());
                        if (port>10000 || port <1){
                            Toast.makeText(getActivity().getApplicationContext(), "This is not a port number please try again", Toast.LENGTH_SHORT).show();
                        }else{
                            srcPort = port;

                        }
                        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
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
                .setPositiveButton("Save",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1,int id) {
                        EditText editText = (EditText) dstPortLayout.findViewById(R.id.portText);
                        int port = Integer.parseInt(editText.getText().toString());
                        if (port>10000 || port <1){
                            Toast.makeText(getActivity().getApplicationContext(), "This is not a port number please try again", Toast.LENGTH_SHORT).show();
                        }else{
                            dstPort = port;

                        }
                        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
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
                .setPositiveButton("Save",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1,int id) {

                        EditText editText = (EditText) ipLayout.findViewById(R.id.ipAddress);
                        final IPAddressValidator ipAddressValidator = new IPAddressValidator();
                        try{
                            String txt = editText.getText().toString();
                            if (ipAddressValidator.validate(txt)) {
                                ipAddress = Inet4Address.getByName(txt);
                            }
                            else {
                                Toast.makeText(getActivity().getApplicationContext(), "This is not a valid IP address please try again", Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){e.printStackTrace();}
                        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        ipAddressDialog = alertDialogBuilder.create();

    }

    private int loadTcpdumpFromAssets(){
        int retval = 0;
        // updating progress message from other thread causes exception.
        // progressbox.setMessage("Setting up data..");
        String rootDataPath = getActivity().getApplicationInfo().dataDir + "/files";
        String filePath = rootDataPath + "/tcpdump";
        File file = new File(filePath);
        AssetManager assetManager = getActivity().getAssets();

        try{
            if (file.exists()) {
                Shell.SH.run("chmod 755 " + filePath);
                return retval;
            }
            new File(rootDataPath).mkdirs();
            retval = copyFileFromAsset(assetManager, "tcpdump", filePath);
            // Mark the binary executable

            Shell.SH.run("chmod 755 " + filePath);
        }
        catch(Exception ex)
        {
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
            while((len = in.read(buff)) != -1){
                out.write(buff, 0, len);
            }
            in.close();
            out.flush();
            out.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return -1;
        }
        return 0;
    }

    private void drawPlot(){

        mChart = (HorizontalBarChart) getView().findViewById(R.id.chart1);

        mChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(40);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);

        mChart.setDrawValueAboveBar(false);
        mChart.setHighlightFullBarEnabled(false);

        // change the position of the y-labels
        YAxis leftAxis = mChart.getAxisLeft();
        //leftAxis.setValueFormatter(new MyAxisValueFormatter());
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        mChart.getAxisRight().setEnabled(false);

        XAxis xLabels = mChart.getXAxis();
        xLabels.setPosition(XAxisPosition.TOP);

        // mChart.setDrawXLabels(false);
        // mChart.setDrawYLabels(false);

        // setting data


        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);

        // mChart.setDrawLegend(false);

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < 10; i++) {
            ;
            float val1 = (float) Math.random()  / 3;
            float val2 = (float) Math.random()  / 3;
            float val3 = (float) Math.random() / 3;

            yVals1.add(new BarEntry(
                    i,
                    new float[]{val1, val2, val3},
                    getResources().getDrawable(R.drawable.ic_menu)));
        }

        BarDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "Statistics Vienna 2014");
            set1.setDrawIcons(false);
            set1.setColors(getColors());
            set1.setStackLabels(new String[]{"Births", "Divorces", "Marriages"});

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            //data.setValueFormatter(new MyValueFormatter());
            data.setValueTextColor(Color.WHITE);

            mChart.setData(data);
        }

        mChart.setFitBars(true);
        mChart.invalidate();
    }

    private int[] getColors() {

        int stacksize = 3;

        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];

        for (int i = 0; i < colors.length; i++) {
            colors[i] = ColorTemplate.MATERIAL_COLORS[i];
        }

        return colors;
    }
}

