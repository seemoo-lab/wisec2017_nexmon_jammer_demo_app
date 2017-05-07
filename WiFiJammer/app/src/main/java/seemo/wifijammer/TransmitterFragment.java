package seemo.wifijammer;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by Stathis on 05-May-17.
 */



public class TransmitterFragment extends Fragment {
    ArrayList<UDPStream> udpStreams;
    ListView listView;

    private static CustomAdapter adapter;
    ViewGroup container;
    AlertDialog newUDPStreamDialog;
    AlertDialog ipAddressDialog;
    AlertDialog srcPortDialog;
    AlertDialog dstPortDialog;
    int srcPort = 1234;
    int dstPort = 5678;

    InetAddress ipAddress;
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         * Inflate the layout for this fragment
         */
        this.container = container;
        setHasOptionsMenu(true);
        createNewUDPStreamDialog();
        createAlertDialogs();
        return inflater.inflate(R.layout.transmiter_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try{

            FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    newUDPStreamDialog.show();

                }
            });

            ipAddress = Inet4Address.getByName("192.168.1.2");

            listView = (ListView) getView().findViewById(R.id.senderList);

            udpStreams= new ArrayList<>();

            udpStreams.add(new UDPStream(srcPort, ipAddress ,dstPort,getActivity()));

            adapter= new CustomAdapter(udpStreams,getActivity().getApplicationContext());

            listView.setAdapter(adapter);

        }catch (Exception e){
            e.printStackTrace();
        }



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

    public void createNewUDPStreamDialog(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        final View linear_layout = getActivity().getLayoutInflater().inflate(R.layout.src_port_dialog, container, false);

        alertDialogBuilder.setView(linear_layout);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog1,int id) {
                        EditText editText = (EditText) linear_layout.findViewById(R.id.portText);
                        int port = Integer.parseInt(editText.getText().toString());
                        if (port>10000 || port <1){
                            Toast.makeText(getActivity().getApplicationContext(), "This is not a port number please try again", Toast.LENGTH_SHORT).show();
                        }else{
                            UDPStream udpStream = new UDPStream(srcPort, ipAddress ,port, getActivity());
                            udpStreams.add(udpStream);
                            adapter.notifyDataSetChanged();
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
        newUDPStreamDialog = alertDialogBuilder.create();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
