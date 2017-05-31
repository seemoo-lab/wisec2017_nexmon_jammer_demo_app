package de.seemoo.nexmon.jammer.aboutus;

/**
 * Created by stathis on 5/11/17.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import de.seemoo.nexmon.jammer.R;


public class LicenseDialog extends DialogFragment {

    public LicenseDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LicenseDialog.
     */
    // TODO: Rename and change types and number of parameters
    public static LicenseDialog newInstance() {
        LicenseDialog fragment = new LicenseDialog();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Set all the title, button etc. for the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
        builder.setTitle("Licences");

        //Get LayoutInflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //Inflate the layout but ALSO store the returned view to allow us to call findViewById
        View view = inflater.inflate(R.layout.fragment_license_dialog, null);


        //Finally, give the custom view to the AlertDialog builder
        builder.setView(view);

        builder.setCancelable(false)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        return builder.create();
    }


}