package de.seemoo.nexmon.jammer.jammer;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import de.seemoo.nexmon.jammer.R;
import de.seemoo.nexmon.jammer.global.Constants;
import de.seemoo.nexmon.jammer.global.Variables;

import static java.lang.Math.ceil;

/**
 * Created by Stathis on 04-Apr-17.
 */

public class SeekBarFragment extends android.app.Fragment {

    public String name;
    public int color;
    public LinearLayout container;
    public LayoutInflater inflater;
    public boolean started = false;

    // Interface to pass data to Activity

    FragmentListener mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (FragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         * Inflate the layout for this fragment
         */
        return inflater.inflate(R.layout.seekbar_fragment, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (name == null) {
            if (savedInstanceState != null) {
                //Restore the fragment's state here
                name = savedInstanceState.getString("name");
                color = savedInstanceState.getInt("color");
            } else {
                name = getArguments().getString("name");
                color = getArguments().getInt("color");
            }
        }


        ((TextView) getView().findViewById(R.id.fragment_header)).setText(name);
        (getView().findViewById(R.id.fragment_header)).setBackgroundColor(color);

        inflater = LayoutInflater.from(getActivity());
        container = (LinearLayout) getView().findViewById(R.id.container);
        container.setBackgroundColor(color);

        setConcurrentScrolling(name);
        createSeekBars();
        setVerticalSeekBars();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", name);
        outState.putInt("color", color);
    }

    public void setVerticalSeekBars() {
        final int sliders_count = Variables.amps.length;
        for (int j = 0; j < Constants.MAX_SLIDERS_COUNT; j++) {
            LinearLayout layout = (LinearLayout) getView().getRootView().findViewWithTag(name + "_layout_" + j);
            if (j < sliders_count) layout.setVisibility(View.VISIBLE);
            else layout.setVisibility(View.GONE);
        }
        updateFrequencies();
        setScrollToMiddle();


    }

    public void createSeekBars() {
        for (int i = 0; i < Constants.MAX_SLIDERS_COUNT; i++) {

            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.verticalseekbar, container, false);
            layout.setTag(name + "_layout_" + i);

            TextView freq = (TextView) layout.findViewById(R.id.verticalSeekbarFreq);

            freq.setTag(name + "_freq_" + i);

            TextView sliderText = (TextView) layout.findViewById(R.id.verticalSeekbarText);


            sliderText.setTag(name + "_tag_" + i);


            final SeekBar verticalSeekBar = (SeekBar) layout.findViewById(R.id.verticalSeekbar);
            verticalSeekBar.setTag(name + "_seekBar_" + i);

            verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public synchronized void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    int tag = Integer.parseInt(((String) seekBar.getTag()).replaceAll("[^0-9]", ""));

                    TextView sliderText = (TextView) getView().findViewWithTag(name + "_tag_" + tag);
                    double value;
                    if (name == "Amplitudes") {
                        value = progress / 100.0;
                        value = round(value, 3);
                        Variables.amps[tag] = value;
                        sliderText.setText("" + value);
                    } else {
                        value = (progress - 50) / 100.0 * 2 * Math.PI;
                        value = round(value, 3);
                        Variables.phases[tag] = value;
                        value = value / Math.PI * 180;
                        value = round(value, 2);
                        sliderText.setText(value + "°");

                    }
                    // notify Activity
                    if (fromUser) mCallback.onUserAction();
                }
            });

            container.addView(layout);
        }
    }

    public void setConcurrentScrolling(String name) {
        HorizontalScrollView scrollView = (HorizontalScrollView) getView().findViewById(R.id.horizontalScrollView);
        scrollView.setTag(name);

        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (v.getTag().toString().equals("Amplitudes")) {
                    HorizontalScrollView sv = (HorizontalScrollView) getView().getRootView().findViewWithTag("Phases");
                    sv.scrollTo(scrollX, scrollY);
                } else {
                    HorizontalScrollView sv = (HorizontalScrollView) getView().getRootView().findViewWithTag("Amplitudes");
                    sv.scrollTo(scrollX, scrollY);
                }

            }
        });

    }

    public double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return ceil(value * factor) / factor;
    }

    public void updateFrequencies() {

        int slidersCount = Variables.amps.length;

        double subcarrierSpacing = round((Variables.bandwidth * Constants.OVERSAMPLING_RATE / (double) Variables.idft_size) * 1000, 3);

        for (int j = 0; j < slidersCount; j++) {
            final LinearLayout layout = (LinearLayout) getView().getRootView().findViewWithTag(name + "_layout_" + j);
            final TextView freq = (TextView) getView().getRootView().findViewWithTag(name + "_freq_" + j);
            final TextView sliderText = (TextView) getView().getRootView().findViewWithTag(name + "_tag_" + j);

            double subcarrierFrequency = round(subcarrierSpacing * (slidersCount / 2 - j) * (-1), 4);

            if (Math.abs(subcarrierFrequency) <= Variables.bandwidth * 500)
                layout.setBackgroundColor(color);
            else layout.setBackgroundColor(Color.WHITE);

            // hide minus from zero
            if (subcarrierFrequency == 0) subcarrierFrequency = 0;

            if (name.equals("Amplitudes")) Variables.freqs[j] = subcarrierFrequency * 1000;

            final String freqUnit;
            if (Math.abs(subcarrierFrequency) >= 1000) {

                subcarrierFrequency = round(subcarrierFrequency / 1000, 3);

                freqUnit = "MHz";
            } else freqUnit = "kHz";

            int subcarrierNumber = j - slidersCount / 2;

            freq.setText("SC " + subcarrierNumber + " at\n" + subcarrierFrequency + freqUnit);

            if (name.equals("Amplitudes")) {
                sliderText.setText("" + Variables.amps[j]);
            } else {
                double phase_in_degrees = Variables.phases[j] / Math.PI * 180;
                sliderText.setText(round(phase_in_degrees, 2) + "°");
            }

            final SeekBar verticalSeekBar = (SeekBar) getView().getRootView().findViewWithTag(name + "_seekBar_" + j);

            Handler mHandler = new Handler();
            mHandler.post(new Runnable() {
                public void run() {
                    int tag = Integer.parseInt(((String) verticalSeekBar.getTag()).replaceAll("[^0-9]", ""));
                    double value;
                    if (name.equals("Amplitudes")) {
                        value = Variables.amps[tag] * 100.0;
                    } else {
                        value = round((Variables.phases[tag] * 2 * Math.PI) / 100 + 50, 3);
                    }
                    verticalSeekBar.setProgress((int) value);
                }
            });
        }
        //update plots only once
        //if (name.equals("Amplitudes")) mCallback.onUserAction();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!started) {
            setScrollToMiddle();
            started = true;
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void setScrollToMiddle() {
        HorizontalScrollView scrollView = (HorizontalScrollView) getView().getRootView().findViewById(R.id.horizontalScrollView);

        int slidersCount = Variables.amps.length;
        if (slidersCount % 2 != 0) slidersCount--;
        TextView freq = (TextView) getView().getRootView().findViewById(R.id.main).findViewWithTag(name + "_freq_" + 1);
        //System.out.println(x);
        //System.out.println(freq.getWidth());

        scrollView.scrollTo(freq.getWidth() * slidersCount / 2 - (scrollView.getWidth() / 2 - freq.getWidth() / 2), 0);
    }


    public interface FragmentListener {
        void onUserAction();
    }
}
