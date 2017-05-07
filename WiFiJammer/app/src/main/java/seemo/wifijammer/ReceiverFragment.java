package seemo.wifijammer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by Stathis on 05-May-17.
 */

public class ReceiverFragment extends Fragment {

    private ProgressDialog progressbox;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         * Inflate the layout for this fragment
         */
        return inflater.inflate(R.layout.receiver_fragment, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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


}

