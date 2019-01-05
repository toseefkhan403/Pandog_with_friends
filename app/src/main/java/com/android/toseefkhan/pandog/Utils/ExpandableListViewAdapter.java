package com.android.toseefkhan.pandog.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.android.toseefkhan.pandog.R;

import java.util.HashMap;
import java.util.List;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    private List<String> header_questions;
    private HashMap<String,List<String>> answers;
    private Context mContext;

    public ExpandableListViewAdapter(List<String> header_questions, HashMap<String, List<String>> answers, Context mContext) {
        this.header_questions = header_questions;
        this.answers = answers;
        this.mContext = mContext;
    }

    @Override
    public int getGroupCount() {
        return header_questions.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return answers.get(header_questions.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return header_questions.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return answers.get(header_questions.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        String questions =(String) this.getGroup(groupPosition);

        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.parent_layout,null);
        }

        TextView Q = convertView.findViewById(R.id.heading_item);
        Q.setText(questions);


        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        String answers =(String) this.getChild(groupPosition,childPosition);

        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.child_layout,null);
        }

        TextView A = convertView.findViewById(R.id.child_item);
        A.setText(answers);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
