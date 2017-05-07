package seemo.wifijammer;

/**
 * Created by Stathis on 07-May-17.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.TextView;
import java.util.List;
import eu.chainfire.libsuperuser.Shell;

public class TcpdumpPacketCapture {

    private static Activity activity;
    private static Shell.Interactive rootTcpdumpShell;
    private static ProgressDialog progressBox;
    private static boolean isInitialised = false;

    public static void initialiseCapture(Activity _activity) {
        activity = _activity;
        progressBox = new ProgressDialog(activity);
        progressBox.setTitle("Initialising Capture");
        progressBox.setMessage("Please wait while packet capture is initialised...");
        progressBox.setIndeterminate(true);
        progressBox.setCancelable(false);
        progressBox.show();
        if (rootTcpdumpShell != null) {
            if(!isInitialised)
                throw new RuntimeException("rootTcpdump shell: not null, initialized:false");
            startTcpdumpCapture();
            progressBox.dismiss();
        }
        else {
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
                            }
                            else {
                                progressBox.setMessage("There was an error starting root shell. Please grant root permissions or try again.");
                            }
                        }
                    });
        }
    }

    private static void startTcpdumpCapture() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    List<String> out = Shell.SH.run("ps | grep tcpdump");
                    if(out.size() > 0){
                        //One process already running. Don't start another.
                        ((TextView)activity.findViewById(R.id.main_tv)).setText("Tcpdump "+out.size()+" process already running at pid: " + (out.get(0).split("\\s+"))[1] );
                        return;
                    }
                    rootTcpdumpShell.addCommand("cd "+ activity.getApplicationInfo().dataDir + "/files/");

                    rootTcpdumpShell.addCommand("./tcpdump -vvv -nn udp dst port 53", 0, new Shell.OnCommandLineListener() {
                        @Override
                        public void onCommandResult(int commandVal, int exitVal) {
                            if (exitVal < 0) {
                                if(progressBox.isShowing()) {
                                    progressBox.setMessage("Error returned by shell command...");
                                }
                            }
                        }
                        @Override
                        public void onLine(String line) {
                            System.out.println(line);
                            appendOutput(line);
                        }
                    });
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }
        }).start();
        ((TextView)activity.findViewById(R.id.main_tv)).setText("Packet capture started..");
    }

    public static int stopTcpdumpCapture(Activity _activity){
        int retVal = 0;
        try{
            List<String> out = Shell.SH.run("ps | grep tcpdump");
            for(String x : out) {
                String[] temp = x.split("\\s+");
                Integer pid =  Integer.valueOf(temp[1]);
                List<String> exitOutput =  Shell.SU.run("kill -9 " + pid.toString());
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
            //retVal = -1;
            throw ex;
        }
        return retVal;
    }

    private static void appendOutput(final String line) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder out = (new StringBuilder()).
                        append(line).
                        append((char)10);
                ((TextView)activity.findViewById(R.id.main_tv)).append(out.toString());
            }
        });

    }
}
