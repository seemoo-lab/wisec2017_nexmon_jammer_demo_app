package de.seemoo.nexmon.jammer;

/**
 * Created by Stathis on 07-May-17.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class TcpdumpPacketCapture {

    private static Activity activity;
    private static Shell.Interactive rootTcpdumpShell;
    private static ProgressDialog progressBox;
    private static boolean isInitialised = false;
    private static String port;
    private static String ipAddress;

    private static HashMap<Integer, int[]> data = new HashMap<>();

    public static HashMap<Integer, int[]> getData() {
        return data;
    }

    public static void setPort(int new_port) {
        port = String.valueOf(new_port);
    }

    public static void setIpAddress(InetAddress ipA) {
        ipAddress = ipA.toString();
    }

    public static void initialiseCapture(Activity _activity) {
        activity = _activity;
        progressBox = new ProgressDialog(activity);
        progressBox.setTitle("Initialising Capture");
        progressBox.setMessage("Please wait while packet capture is initialised...");
        progressBox.setIndeterminate(true);
        progressBox.setCancelable(false);
        progressBox.show();
        if (rootTcpdumpShell != null) {
            if (!isInitialised)
                throw new RuntimeException("rootTcpdump shell: not null, initialized:false");
            startTcpdumpCapture();
            progressBox.dismiss();
        } else {
            rootTcpdumpShell = new Shell.Builder().
                    useSU().
                    setWantSTDERR(false).
                    setMinimalLogging(true).
                    open(new Shell.OnCommandResultListener() {
                        @Override
                        public void onCommandResult(int commandVal, int exitVal, List<String> out) {
                            //Callback checking successful shell start.
                            if (exitVal == Shell.OnCommandResultListener.SHELL_RUNNING) {
                                isInitialised = true;
                                progressBox.setMessage("Starting packet capture..");
                                startTcpdumpCapture();
                                progressBox.dismiss();
                            } else {
                                progressBox.dismiss();
                                Toast.makeText(activity.getApplicationContext(), "Root privileges are needed. Please grant root permissions or try again.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }
    }

    private static void startTcpdumpCapture() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> out = Shell.SH.run("ps | grep tcpdump");
                    if (out.size() > 0) {
                        //One process already running. Don't start another.
                        ((TextView) activity.findViewById(R.id.main_tv)).setText("Tcpdump " + out.size() + " process already running at pid: " + (out.get(0).split("\\s+"))[1]);
                        return;
                    }
                    rootTcpdumpShell.addCommand("cd " + activity.getApplicationInfo().dataDir + "/files/");

                    rootTcpdumpShell.addCommand("./tcpdump -vvv -n src host 130.83.113.225"/* src host " + ipAddress + " dst port "+ port*/, 0, new Shell.OnCommandLineListener() {
                        @Override
                        public void onCommandResult(int commandVal, int exitVal) {
                            if (exitVal < 0) {
                                if (progressBox.isShowing()) {
                                    progressBox.setMessage("Error returned by shell command...");
                                }
                            }
                        }

                        @Override
                        public void onLine(String line) {
                            System.out.println(line);

                            if (line.contains(">")) {
                                int port = extractPort(line);
                                if (port > 0) {

                                    if (!data.containsKey(port)) {
                                        data.put(port, new int[2]);
                                    }

                                    if (line.contains("[udp sum ok]")) {

                                        data.get(port)[0]++;

                                    } else {

                                        data.get(port)[1]++;
                                    }
                                }
                            }


                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }
        }).start();
        ((TextView) activity.findViewById(R.id.main_tv)).setText("Packet capture started..");
    }

    public static int stopTcpdumpCapture(Activity _activity) {
        int retVal = 0;
        try {
            List<String> out = Shell.SH.run("ps | grep tcpdump");
            for (String x : out) {
                String[] temp = x.split("\\s+");
                Integer pid = Integer.valueOf(temp[1]);
                List<String> exitOutput = Shell.SU.run("kill -9 " + pid.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            //retVal = -1;
            throw ex;
        }
        return retVal;
    }

    private static int extractPort(String line) {
        int port = -1;
        int index2 = line.indexOf(": [");
        System.out.println(index2);
        int index1 = line.lastIndexOf(".");
        System.out.println(index1);
        line = line.substring(index1 + 1, index2);
        port = Integer.parseInt(line);

        if (port > 10000 || port < 1) {

            throw new RuntimeException("Could not extract port from tcpdump line");

        } else {


        }

        return port;
    }

    public static void resetData() {
        data = new HashMap<>();
    }

}
