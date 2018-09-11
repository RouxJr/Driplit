package com.example.s215131746.driplit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import Adapters.TipListAdapter;
import viewmodels.PersonModel;
import viewmodels.TipModel;

/**
 * Created by s216127904 on 2018/04/30.
 */

public class TipTrickClass extends Fragment {
    DBAccess business;
    EditText txtTip;
    GeneralMethods m;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.tip_trick, container, false);
        final ListView postedItems = rootView.findViewById(R.id.lvPostedTips);
        m = new GeneralMethods(getContext());
        business = new DBAccess();
        ArrayList<TipModel> tips;
        final String [] fuck = m.Read(this.getString(R.string.person_file_name),",");
        if(fuck[PersonModel.ISAMDIN].equals("true"))
            tips =  business.GetAdminTips();
        else
            tips = business.GetTips();

        final TipListAdapter la = new TipListAdapter(getContext(),tips);
        txtTip = rootView.findViewById(R.id.txtTipTrick);
        final ImageButton btnPost = rootView.findViewById(R.id.imgbtnPost);
        txtTip.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
             }
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 if(s.length() != 0)
                     btnPost.setClickable(true);
                 else
                     btnPost.setClickable(false);
             }

             @Override
             public void afterTextChanged(Editable s) {
             }
         });
        postedItems.setAdapter(la);

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tipd = txtTip.getText().toString();
                if(tipd.length()<10){
                    Toast.makeText(rootView.getContext(),"Tip not descriptive enough",Toast.LENGTH_LONG).show();
                }else if(!tipd.equals("")){
                    Toast.makeText(rootView.getContext(),"your tip will wait for approval",Toast.LENGTH_LONG).show();
                    TipModel tip = new TipModel();
                    tip.PersonID =Integer.parseInt(
                            m.Read(rootView.getContext().getString(R.string.person_file_name)
                                    ,",")[PersonModel.ID]);
                    tip.TipDescription = tipd;
                    business.MobAddTip(tip);
                    ArrayList<TipModel> tips =  business.GetTips();
                    TipListAdapter la = new TipListAdapter(getContext(),tips);
                    postedItems.setAdapter(la);
                    txtTip.setText("");
                }
            }
        });
        ListView lvItemList = rootView.findViewById(R.id.lvPostedTips);

        lvItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //after an item has been clicked the the following line will either make the bottom controllers visible or invisible
                if(fuck[PersonModel.ISAMDIN].equals("true")) {
                    la.approveTip(view,i);
                }
            }
        });
        return rootView;
    }

}