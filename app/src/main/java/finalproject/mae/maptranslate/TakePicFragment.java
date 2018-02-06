package finalproject.mae.maptranslate;

import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import finalproject.mae.maptranslate.ImageTranslation.RETCONSTANT;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TakePicFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TakePicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TakePicFragment extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;

    public TakePicFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TakePicFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TakePicFragment newInstance(String param1, String param2) {
        TakePicFragment fragment = new TakePicFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pic_chooser, container, false);
        ImageButton camera = v.findViewById(R.id.camera);
        ImageButton gallery = v.findViewById(R.id.gallery);
        camera.setOnClickListener(this);
        gallery.setOnClickListener(this);
        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onClick(View v){
        if(v.getId() == R.id.camera){
            mListener.FragmentResponse(RETCONSTANT.CAMERA);
        }
        else if(v.getId() == R.id.gallery){
            mListener.FragmentResponse(RETCONSTANT.GALLERY);
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(this);
        transaction.commit();
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void FragmentResponse(int flag);
    }
}
