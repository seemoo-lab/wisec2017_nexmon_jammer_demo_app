package de.seemoo.nexmon.jammer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by Stathis on 03-May-17.
 */

public class PlotFragment extends android.app.Fragment {
    public int mode;
    public double[] amps;
    public double[] phases;
    public double[] freqs;
    public Double[] times;
    public ArrayList<double[]> data = new ArrayList<>();
    private XYPlot timePlot;
    private XYPlot freqPlot;
    private XYSeries series1;
    private XYSeries series2;
    private XYSeries series3;
    private LineAndPointFormatter seriesFormat1;
    private LineAndPointFormatter seriesFormat2;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //Restore the fragment's state here
            amps = savedInstanceState.getDoubleArray("amps");
            phases = savedInstanceState.getDoubleArray("phases");
            freqs = savedInstanceState.getDoubleArray("freqs");
            mode = savedInstanceState.getInt("mode");
        } else {
            amps = getArguments().getDoubleArray("amps");
            phases = getArguments().getDoubleArray("phases");
            freqs = getArguments().getDoubleArray("freqs");
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



        timePlot = (XYPlot) getActivity().findViewById(R.id.timePlot);
        freqPlot = (XYPlot) getActivity().findViewById(R.id.freqPlot);

        seriesFormat1 = new LineAndPointFormatter(getActivity(), R.xml.line_point_formatter1);
        seriesFormat2 = new LineAndPointFormatter(getActivity(), R.xml.line_point_formatter2);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void constructFFTPlotData() {

        int size = amps.length;
        Double values[] = new Double[size];

        Log.d("D", "size: " + size);

        for (int i = 0; i < size; i++) {
            //values[i] = (size / 2 - i) * (-1);
            values[i] = freqs[i];
        }
        System.out.println(Arrays.toString(values));

        Double mags[] = new Double[size];

        for (int i = 0; i < size; i++) {
            mags[i] = amps[i];
        }

        List<? extends Number> xVals = Arrays.asList(values);
        List<? extends Number> yVals = Arrays.asList(mags);

        freqPlot.removeSeries(series3);
        series3 = new SimpleXYSeries(xVals, yVals, "FFT");
        freqPlot.addSeries(series3, seriesFormat1);
    }

    public void plotSignals(double[] amps_new, double[] phases_new, double[] freqs_new) {
        this.amps = amps_new;
        this.phases = phases_new;
        this.freqs = freqs_new;

        if (mode == 0) {
            // Time Plot

            // TODO update idft_size according to setting
            int idft_size = 128;

            // TODO update bandwidth according to settings
            double bandwidth = 20e6;
            double fs = bandwidth * Constants.OVERSAMPLING_RATE;

            Double timeI[] = new Double[idft_size];
            Double timeQ[] = new Double[idft_size];
            times = new Double[idft_size];

            for (int n = 0; n < idft_size; n++) {
                timeI[n] = 0.0;
                timeQ[n] = 0.0;
            }

            for (int k = 0; k < amps.length; k++) {
                for (int n = 0; n < idft_size; n++) {
                    times[n] = n / fs;
                    timeI[n] -= amps[k] * Math.sin(2*Math.PI*freqs[k]*times[n] + phases[k]);
                    timeQ[n] += amps[k] * Math.cos(2*Math.PI*freqs[k]*times[n] + phases[k]);
                }
            }

            List<? extends Number> xVals = Arrays.asList(times);
            List<? extends Number> yValsReal = Arrays.asList(timeI);
            List<? extends Number> yValsImag = Arrays.asList(timeQ);

            timePlot.removeSeries(series1);
            timePlot.removeSeries(series2);
            series1 = new SimpleXYSeries(xVals, yValsReal, "Real");
            series2 = new SimpleXYSeries(xVals, yValsImag, "Imaginary");
            timePlot.addSeries(series1, seriesFormat1);
            timePlot.addSeries(series2, seriesFormat2);
        } else {
            // Frequency Plot
            constructFFTPlotData();
        }

    }
}
