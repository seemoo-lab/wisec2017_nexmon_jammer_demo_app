package comtud.example.stathis.wifijammer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



/**
 * Created by Stathis on 10-Apr-17.
 */

public class Phase_Fragment extends android.app.Fragment implements CircularSlider.OnSliderMovedListener{
    FragmentListener mCallback;
    Bundle bundle = new Bundle();
    public interface FragmentListener {
        public void onUserAction(Bundle bundle);
    }

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
        Bundle bundle = getArguments();
        if (bundle != null) {
            // ampSet = bundle.getBoolean("ampSet");
        }
    }
    public double angle = 0.0;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         * Inflate the layout for this fragment
         */
        return inflater.inflate(R.layout.phase_fragment, container, false);
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //Restore the fragment's state here
            angle = savedInstanceState.getDouble("angle");
        }

        CircularSlider slider = (CircularSlider) getView().findViewById(R.id.circular);
        slider.setOnSliderMovedListener(this);
        slider.setPosition(angle);


    }
    public void onSliderMoved(double pos){
        angle = pos;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("angle", angle);
    }
}

