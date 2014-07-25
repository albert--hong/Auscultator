package com.auscultator.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HongYan on 2014/6/29.
 */
public class DataAdapter {

	public final static int BREATH_SOUNDS = 0x01;
	public final static int HEART_SOUNDS = 0x02;

	private static DataAdapter mInstance;
	
	private DataAdapter() {
		
	}
	public static DataAdapter getInstance() {
		if (mInstance == null) {
			mInstance = new DataAdapter();
		}
		return mInstance;
	}
    /**
     *
     * @return
     */
    public int initialize() {
        return 0;
    }

    public int create_db() {
        return 0;
    }

    private boolean if_db_exist() {
        return false;
    }

    public int save(Map attributes) {
    	return 0;
    }
	public List<HashMap<String, Object>> get_media_records() {
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>() {
		};
		return list;
	}
}
