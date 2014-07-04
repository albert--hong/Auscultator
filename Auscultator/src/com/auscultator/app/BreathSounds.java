package com.auscultator.app;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;


public class BreathSounds extends Activity {
	private static String TAG = "BREATH_SOUNDS";
	
	private SimpleAdapter adapter;
	private ListView listView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breath_sounds);
        
        listView = (ListView)findViewById(R.id.listView);
        
        this.adapter = new SimpleAdapter(this, this.getData(), R.layout.list_audio_item, 
        		new String[]{"name", "time", "img"},
        		new int[]{R.id.name, R.id.time, R.id.img});
        
        listView.setAdapter(adapter);
    }
    
    private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", "患者1");
		map.put("time", "2014.7.2");
		map.put("img", R.drawable.head_portrait);
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("name", "患者2");
		map.put("time", "2014.7.3");
		map.put("img", R.drawable.head_portrait_1);
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("name", "患者3");
		map.put("time", "2014.7.4");
		map.put("img", R.drawable.head_portrait);
		list.add(map);
		
		return list;
	}
}
