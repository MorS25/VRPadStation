package com.laser.ui.fragments;

import com.laser.VrPadStation.R;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class SettingsListFragment extends ListFragment {

    /**
     * Interfaccia di Callback per comunicare con l'activity che contiene il Fragment.
     */
    public static interface OnFlyListFragmentItemClick {
            public void onClick(int item);
    }
   
    /**
     * Riferimento all'activity di Callback.
     */
    private OnFlyListFragmentItemClick mActivityAttached;
   
    @Override
    public void onAttach(Activity activity) {
            super.onAttach(activity);
           
            if(activity instanceof OnFlyListFragmentItemClick) {
                    // L'activity che contiene il fragment è compatibile con l'interfaccia di Callback, mi memorizzo il riferimento.
                    mActivityAttached = (OnFlyListFragmentItemClick)activity;
            }
            else {
                    // L'activity non è compatibile, creo un riferimento fittizzio.
                    mActivityAttached = new OnFlyListFragmentItemClick() {
                            public void onClick(int item) {}
                    };
            }
    }
   
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
           
            setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.SettingsList)));
    }
   
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            // Richiamo il metodo di callback
            mActivityAttached.onClick(position);
    }
	
}
