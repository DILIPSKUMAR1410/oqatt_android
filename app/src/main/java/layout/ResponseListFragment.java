package layout;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dk.androidclient.ChooseContactActivity;
import com.dk.androidclient.R;
import com.github.fabtransitionactivity.SheetLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResponseListFragment extends Fragment implements SheetLayout.OnFabAnimationEndListener {

    private static final int REQUEST_CODE = 1;
    @Bind(R.id.bottom_sheet)
    SheetLayout mSheetLayout;
    @Bind(R.id.fab)
    FloatingActionButton mFab;

    public ResponseListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_response_list, container, false);
        ButterKnife.bind(this, view);

        mSheetLayout.setFab(mFab);
        mSheetLayout.setFabAnimationEndListener(this);

        // Inflate the layout for this fragment
        return view;
    }

    @OnClick(R.id.fab)
    void onFabClick() {
        mSheetLayout.expandFab();
    }

    @Override
    public void onFabAnimationEnd() {
        Intent intent = new Intent(getActivity(), ChooseContactActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            mSheetLayout.contractFab();
        }

    }
}