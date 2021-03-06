package de.seemoo.nexmon.jammer.jammer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import de.seemoo.nexmon.jammer.R;
import de.seemoo.nexmon.jammer.global.Constants;
import de.seemoo.nexmon.jammer.global.Variables;

import static java.lang.Math.ceil;


/**
 * Created by Stathis on 03-May-17.
 */

public class PlotFragment extends android.app.Fragment {
    public int mode;
    public float[] times;
    public ArrayList<double[]> data = new ArrayList<>();
    public float[] timeI;
    public float[] timeQ;
    public float[] freqs;
    public float[] freqSamps;


    private LineChart mChart;
    private ILineDataSet set_real;
    private ILineDataSet set_imag;
    private ILineDataSet set_freq;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //Restore the fragment's state here
            mode = savedInstanceState.getInt("mode");
        } else {

            mode = getArguments().getInt("mode");
        }
        /**
         * Inflate the layout for this fragment
         */
        if (mode == 0) return inflater.inflate(R.layout.time_plot_fragment, container, false);
        else return inflater.inflate(R.layout.freq_plot_fragment, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mode == 0) createTimePlot();
        else createFreqPlot();

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    public void constructIQSamples() {

        double bandwidth = Variables.bandwidth * 1e6;

        double fs = bandwidth * Constants.OVERSAMPLING_RATE;

        Variables.samplingRate = fs;

        int idft_size = Variables.idft_size;

        timeI = new float[idft_size];
        timeQ = new float[idft_size];
        times = new float[idft_size];

        for (int n = 0; n < idft_size; n++) {
            timeI[n] = 0.0f;
            timeQ[n] = 0.0f;
        }

        for (int k = 0; k < Variables.amps.length; k++) {
            for (int n = 0; n < idft_size; n++) {
                times[n] = (float) (n / fs);
                timeI[n] -= Variables.amps[k] * Math.sin(2 * Math.PI * Variables.freqs[k] * times[n] + Variables.phases[k]);
                timeQ[n] += Variables.amps[k] * Math.cos(2 * Math.PI * Variables.freqs[k] * times[n] + Variables.phases[k]);
            }
        }

    }

    public void plotSignals() {

        if (mode == 0) {
            // Time Plot
            updateTimePlot();
        } else {
            // Frequency Plot
            updateFreqPlot();
        }

    }

    public void createTimePlot() {

        TextView title = (TextView) getView().findViewById(R.id.plot_title);
        title.setText("Time Domain Plot");

        RelativeLayout relativeLayout = (RelativeLayout) getView().findViewById(R.id.time_plot_rel);
        relativeLayout.setBackgroundColor(Color.LTGRAY);
        TextView range = (TextView) getView().findViewById(R.id.plot_range);
        range.setBackgroundColor(Color.LTGRAY);


        mChart = (LineChart) getView().findViewById(R.id.chart1);


        // disable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);


        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setEnabled(true);
        l.setForm(Legend.LegendForm.LINE);


        XAxis xl = mChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(true);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setValueFormatter(new SmallValueFormatter("s"));


        YAxis leftAxis = mChart.getAxisLeft();

        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


        updateTimePlot();
    }

    public void createFreqPlot() {
        TextView title = (TextView) getView().findViewById(R.id.plot_title);
        title.setText("Frequency Domain Plot");
        title.setBackgroundColor(Color.LTGRAY);
        TextView range = (TextView) getView().findViewById(R.id.plot_range);
        range.setBackgroundColor(Color.LTGRAY);

        mChart = (LineChart) getView().findViewById(R.id.chart1);


        // disable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();

        // add empty data
        mChart.setData(data);


        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setEnabled(true);
        l.setForm(Legend.LegendForm.LINE);


        XAxis xl = mChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(true);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setValueFormatter(new LargeValueFormatter("Hz"));


        YAxis leftAxis = mChart.getAxisLeft();

        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


        updateFreqPlot();
    }

    private double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return ceil(value * factor) / factor;
    }

    public void updateTimePlot() {

        mChart.clearValues();

        LineData data = mChart.getData();

        set_real = createSet(ColorTemplate.rgb("#005AA9"), "I");
        set_imag = createSet(ColorTemplate.rgb("#E6001A"), "Q");
        data.addDataSet(set_real);
        data.addDataSet(set_imag);

        constructIQSamples();

        double maxSignalSquare = 0;
        double sumSignalSquare = 0;

        for (int i = 0; i < times.length; i++) {
            data.addEntry(new Entry(times[i], timeI[i]), 0);
            data.addEntry(new Entry(times[i], timeQ[i]), 1);
            double signalSquare = Math.pow(timeI[i], 2) + Math.pow(timeQ[i], 2);
            sumSignalSquare += signalSquare;
            if (signalSquare > maxSignalSquare) maxSignalSquare = signalSquare;
        }

        double paprdb = 10 * Math.log10(maxSignalSquare/(sumSignalSquare/times.length));
        TextView plot_papr_tv = (TextView) getView().findViewById(R.id.plot_papr);
        plot_papr_tv.setText("PAPR: " + round(paprdb,3) + " dB");


        data.notifyDataChanged();

        // let the chart know it's data has changed
        mChart.notifyDataSetChanged();
        mChart.fitScreen();
    }

    public void constructFreqSamples() {
        int num_samps = Constants.getSlidersCount(Variables.idft_size) * Constants.FREQ_PLOT_OVERSAMPLING_RATE;
        //int num_samps = Variables.amps.length * 3;

        freqSamps = new float[num_samps];
        freqs = new float[num_samps];
        float freqSpacing = (float) ((Variables.freqs[1] - Variables.freqs[0])) / Constants.FREQ_PLOT_OVERSAMPLING_RATE;

        for (int n = 0; n < num_samps; n++) {
            freqs[n] = (n - num_samps / 2) * freqSpacing;

            if (((n - Constants.FREQ_PLOT_OVERSAMPLING_RATE/2) % Constants.FREQ_PLOT_OVERSAMPLING_RATE) == 0) {
                float value = (float) (10 * Math.log10(Math.pow(Variables.amps[(n - Constants.FREQ_PLOT_OVERSAMPLING_RATE / 2) / Constants.FREQ_PLOT_OVERSAMPLING_RATE],2)));

                if (!Float.isInfinite(value)) {
                    // a = (sin(2*pi.*(-4:4)/10)./(2*pi.*(-4:4)/10)).^2;
                    // a(5) = 1;
                    // a = 10*log10(a);
                    freqSamps[n - 4] = value - 12.620423487820865f;
                    freqSamps[n - 3] = value - 5.941895950655292f;
                    freqSamps[n - 2] = value - 2.420070769541667f;
                    freqSamps[n - 1] = value - 0.579223661261618f;
                    freqSamps[n] = value;
                    freqSamps[n + 1] = value - 0.579223661261618f;
                    freqSamps[n + 2] = value - 2.420070769541667f;
                    freqSamps[n + 3] = value - 5.941895950655292f;
                    freqSamps[n + 4] = value - 12.620423487820865f;
                } else {
                    freqSamps[n] = -60;
                }
            } else if (freqSamps[n] == 0) {
                freqSamps[n] = -60;
            }
        }

    }

    public void updateFreqPlot() {

        mChart.clearValues();

        LineData data = mChart.getData();

        set_freq = createSet(ColorTemplate.rgb("#005AA9"), "Frequencies");
        data.addDataSet(set_freq);

        constructFreqSamples();

        for (int i = 0; i < freqs.length; i++) {
        //for (int i = 0; i < Variables.amps.length; i++) {
            data.addEntry(new Entry(freqs[i], freqSamps[i]), 0);
            //data.addEntry(new Entry((float) Variables.freqs[i], (float) Variables.amps[i]), 0);
        }

        data.notifyDataChanged();

        // let the chart know it's data has changed
        mChart.notifyDataSetChanged();
        mChart.fitScreen();

    }

    private LineDataSet createSet(int color, String legend) {
        LineDataSet set = new LineDataSet(null, legend);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawFilled(false);
        set.setDrawCircles(false);
        set.setColor(color);
        set.setHighlightEnabled(false);
        set.setDrawVerticalHighlightIndicator(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        set.setLineWidth(2f);
        return set;
    }


}
