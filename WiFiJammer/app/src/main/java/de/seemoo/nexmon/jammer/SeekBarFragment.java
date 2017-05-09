package de.seemoo.nexmon.jammer;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by Stathis on 04-Apr-17.
 */

public class SeekBarFragment extends android.app.Fragment {
    public double data[];
    public double freqs[];
    public String name;
    public int color;
    public int bandwidth;
    public LinearLayout container;
    public LayoutInflater inflater;
    public boolean started = false;

    // Interface to pass data to Activity
    Bundle bundle = new Bundle();
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

    public void setData(double[] data) {
        this.data = data;
    }

    public void setFreqs(double[] freqs) {
        this.freqs = freqs;
    }

    public void setBandwidth(int band) {
        this.bandwidth = band;
        updateFrequencies();
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
        if (data == null) {
            if (savedInstanceState != null) {
                //Restore the fragment's state here
                data = savedInstanceState.getDoubleArray("data");
                name = savedInstanceState.getString("name");
                color = savedInstanceState.getInt("color");
                bandwidth = savedInstanceState.getInt("Bandwidth");
                if (name == "Amplitudes") freqs = savedInstanceState.getDoubleArray("data");
            } else {
                name = getArguments().getString("name");
                data = getArguments().getDoubleArray(name);
                color = getArguments().getInt("color");
                bandwidth = getArguments().getInt("Bandwidth");
                if (name == "Amplitudes") freqs = getArguments().getDoubleArray("freqs");
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
        outState.putDoubleArray("data", data);
        outState.putString("name", name);
        outState.putInt("color", color);
        outState.putInt("Bandwidth", bandwidth);
    }

    public void setVerticalSeekBars() {
        final int idft_size = data.length;
        for (int j = 0; j < 512; j++) {
            LinearLayout layout = (LinearLayout) getView().getRootView().findViewWithTag(name + "_layout_" + j);
            if (j < idft_size) layout.setVisibility(View.VISIBLE);
            else layout.setVisibility(View.GONE);
        }
        updateFrequencies();
        setScrollToMiddle();
        if (name.equals("Amplitudes")) passFreqs();
        bundle.putBoolean("setup", true);
        mCallback.onUserAction(bundle);

    }

    public void createSeekBars() {
        int idft_size = 512;

        final DecimalFormat df = new DecimalFormat("#.###");

        Date counter = new Date();
        // System.out.println(counter.getTime());

        for (int i = 0; i < idft_size; i++) {

            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.verticalseekbar, container, false);
            layout.setTag(name + "_layout_" + i);
            //container.addView(layout);


            TextView freq = (TextView) layout.findViewById(R.id.verticalSeekbarFreq);

            freq.setTag(name + "_freq_" + i);

            TextView sliderText = (TextView) layout.findViewById(R.id.verticalSeekbarText);

            freq.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public synchronized boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        HorizontalScrollView scrollView = (HorizontalScrollView) getView().findViewById(R.id.horizontalScrollView);
                        // enable scrolling
                        scrollView.setOnTouchListener(null);
                        return true;
                    }
                    return false;
                }
            });

            sliderText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public synchronized boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        HorizontalScrollView scrollView = (HorizontalScrollView) getView().findViewById(R.id.horizontalScrollView);
                        // enable scrolling
                        scrollView.setOnTouchListener(null);
                        return true;
                    }
                    return false;
                }
            });

            sliderText.setTag(name + "_tag_" + i);


            final VerticalSeekBar verticalSeekBar = (VerticalSeekBar) layout.findViewById(R.id.verticalSeekbar);
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
                    if (name == "Amplitudes") value = progress / 100.0;
                    else value = (progress - 50) / 100.0 * 2 * Math.PI;

                    value = Double.valueOf(df.format(value));
                    sliderText.setText("" + value);
                    data[tag] = value;

                    // send new data to Activity
                    bundle.putDoubleArray(name, data);
                    mCallback.onUserAction(bundle);
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

    public void updateFrequencies() {
        int idft_size = data.length;

        double subcarrier_s = (bandwidth * 2 / (double) idft_size) * 1000;

        final DecimalFormat df = new DecimalFormat("#.###");
        double subcarrier_size = Double.valueOf(df.format(subcarrier_s));

        for (int j = 0; j < idft_size; j++) {
            final LinearLayout layout = (LinearLayout) getView().getRootView().findViewWithTag(name + "_layout_" + j);
            final TextView freq = (TextView) getView().getRootView().findViewWithTag(name + "_freq_" + j);
            final TextView sliderText = (TextView) getView().getRootView().findViewWithTag(name + "_tag_" + j);

            double value = Double.valueOf(df.format(subcarrier_size * (idft_size / 2 - j) * (-1)));

            if (Math.abs(value) <= bandwidth * 500) layout.setBackgroundColor(color);
            else layout.setBackgroundColor(Color.WHITE);

            if (value == 0) value = 0;

            if (name.equals("Amplitudes")) freqs[j] = value * 1000;

            final String t;
            if (Math.abs(value) >= 1000) {
                value = Double.valueOf(df.format(value / 1000));
                t = "MHz";
            } else t = "kHz";

            int scText;
            if (j < idft_size / 2) scText = idft_size / 2 + j;
            else scText = j - idft_size / 2;

            freq.setText("SC " + scText + " at\n" + value + t);

            if (name.equals("Amplitudes")) sliderText.setText("" + data[j]);
            else sliderText.setText("" + Double.valueOf(df.format(data[j])));

            final VerticalSeekBar verticalSeekBar = (VerticalSeekBar) getView().getRootView().findViewWithTag(name + "_seekBar_" + j);

            Handler mHandler = new Handler();
            mHandler.post(new Runnable() {
                public void run() {
                    int tag = Integer.parseInt(((String) verticalSeekBar.getTag()).replaceAll("[^0-9]", ""));
                    double value;
                    if (name.equals("Amplitudes")) value = data[tag] * 100.0;
                    else value = (data[tag] * 2 * Math.PI) / 100 + 50;
                    value = Double.valueOf(df.format(value));
                    verticalSeekBar.setProgress((int) value);
                }
            });
        }
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

        int x = data.length;
        if (x % 2 != 0) x--;
        TextView freq = (TextView) getView().getRootView().findViewById(R.id.main).findViewWithTag(name + "_freq_" + 1);
        //System.out.println(x);
        //System.out.println(freq.getWidth());

        scrollView.scrollTo(freq.getWidth() * x / 2 - (scrollView.getWidth() / 2 - freq.getWidth() / 2), 0);
    }

    public void passFreqs() {
        bundle.putDoubleArray("freqs", freqs);
        mCallback.onUserAction(bundle);
    }

    public interface FragmentListener {
        void onUserAction(Bundle bundle);
    }
}

        /*
        final CountDownLatch latch = new CountDownLatch(2);
        Thread a = new Thread(new WorkerThread_1(this, latch));
        Thread b = new Thread(new WorkerThread_2(this, latch));
        a.start();
        b.start();
        latch.await();  //main thread is waiting on CountDownLatch to finish
        System.out.println("All services are up, Application is starting now");
        */

/*
class WorkerThread_1 implements Runnable {
    SeekBarFragment seekBarFragment;
    public String name;
    public double[] data;
    CountDownLatch latch;

    public WorkerThread_1(SeekBarFragment seekBarFragment, CountDownLatch latch){
        this.seekBarFragment = seekBarFragment;
        name = seekBarFragment.name;
        data = this.seekBarFragment.data;
        this.latch=latch;
    }

    public void run() {
        Looper.prepare();

        final int idft_size = seekBarFragment.data.length;
        double subcarrier_s = (seekBarFragment.bandwidth*2/(double) idft_size) *1000;
        try {
            final DecimalFormat df = new DecimalFormat("#.###");
            final double subcarrier_size = Double.valueOf(df.format(subcarrier_s));

            for (int j = 0; j<idft_size; j++){
                final int k = j;
                seekBarFragment.getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        LinearLayout layout = (LinearLayout) seekBarFragment.getView().getRootView().findViewWithTag(name +"_layout_" + k);
                        layout.setVisibility(View.VISIBLE);

                        layout.setBackgroundColor(seekBarFragment.color);
                        final TextView freq = (TextView) seekBarFragment.getView().getRootView().findViewWithTag(name +"_freq_" + k);
                        final TextView sliderText = (TextView) seekBarFragment.getView().getRootView().findViewWithTag(name +"_tag_" + k);

                        double value = Double.valueOf(df.format(subcarrier_size * (idft_size/2 - k)*(-1)));

                        if (Math.abs(value) <= seekBarFragment.bandwidth*500) layout.setBackgroundColor(seekBarFragment.color);
                        else layout.setBackgroundColor(Color.WHITE);

                        if (value==0) value=0;

                        if (name == "Amplitudes") seekBarFragment.freqs[k] = value*1000;

                        final String t;
                        if(Math.abs(value)>=1000) {
                            value = Double.valueOf(df.format(value / 1000));
                            t = "MHz";
                        } else t = "kHz";

                        int scText;
                        if (k<idft_size/2) scText= idft_size/2 + k;
                        else scText=k-idft_size/2;

                        freq.setText("SC "+scText + " at\n"+value +t);
                        if (name=="Amplitudes") sliderText.setText(""+data[k]);
                        else  sliderText.setText(""+ Double.valueOf(df.format(data[k])));

                        final VerticalSeekBar verticalSeekBar = (VerticalSeekBar) seekBarFragment.getView().getRootView().findViewWithTag(name +"_seekBar_" + k);

                        Handler mHandler = new Handler();
                        mHandler.post(new Runnable() {
                            public void run () {
                                int tag = Integer.parseInt(((String) verticalSeekBar.getTag()).replaceAll("[^0-9]", ""));
                                double value;
                                if (name=="Amplitudes") value = data[tag] *100.0;
                                else  value = (data[tag]*2*Math.PI)/100 +50;
                                value = Double.valueOf(df.format(value));
                                verticalSeekBar.setProgress((int) value);
                            }
                        });

                    }
                });
            }

            latch.countDown();
        }
        catch (NumberFormatException e){
            e.printStackTrace();
        }


    }
}

class WorkerThread_2 implements Runnable {
    SeekBarFragment seekBarFragment;
    public String name;
    public double[] data;
    CountDownLatch latch;

    public WorkerThread_2(SeekBarFragment seekBarFragment, CountDownLatch latch){
        this.seekBarFragment = seekBarFragment;
        name = seekBarFragment.name;
        data = this.seekBarFragment.data;
        this.latch=latch;
    }

    public void run() {
        for (int i = data.length; i<512; i++){
            final int  m = i;
            seekBarFragment.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    LinearLayout layout = (LinearLayout) seekBarFragment.getView().getRootView().findViewWithTag(name + "_layout_" + m);
                    layout.setVisibility(View.GONE);
                }});
        }
        latch.countDown();
    }

}
*/