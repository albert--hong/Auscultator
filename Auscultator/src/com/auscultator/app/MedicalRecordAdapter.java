package com.auscultator.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MedicalRecordAdapter extends SimpleAdapter {
    private Context context; 
    private List<Map<String, Object>> data; 
    private int layoutResource; 
    

	public MedicalRecordAdapter(Context context,
			List<Map<String, Object>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
		this.context = context; 
        this.data = data; 
        this.layoutResource = resource; 
        
	}
	
	class ViewHolder {
	    public TextView name;
	    public TextView gender;
	    public TextView age;
	    public TextView tel;
	    public ImageView user_img;
	}
	
    @Override 
    public View getView(int position, View convertView, ViewGroup parent) {
    	// get the view;
    	LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE); 
        View layout = layoutInflater.inflate(layoutResource, null);
        
        ViewHolder views = new ViewHolder();
        views.name = (TextView) layout.findViewById(R.id.name);
        views.gender = (TextView) layout.findViewById(R.id.gender);
        views.age = (TextView) layout.findViewById(R.id.age);
        views.tel = (TextView) layout.findViewById(R.id.tel);
        views.user_img = (ImageView) layout.findViewById(R.id.user_img);
        
        // get the view's data
    	Map<String, Object> record = this.data.get(position);
    	String str_name = (String) record.get("name");
    	int n_gender = (Integer) record.get("gender");
    	String str_gender = n_gender == 0 ? "女" : "男";
    	int n_age = (Integer) record.get("age");
    	String str_tel = (String) record.get("tel");
    	
    	// set the view content
    	views.name.setText(str_name);
    	views.gender.setText(str_gender);
    	views.age.setText(String.valueOf(n_age));
    	views.tel.setText(str_tel);
    	
    	if (position % 2 == 1) {
    		views.user_img.setImageResource(R.drawable.head_portrait_1);    		
    	}
    	
    	return layout;
    }
	
}
