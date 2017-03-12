package laochanlam.app;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.reflect.Array;

/**
 * Created by poyushen on 2017/3/2.
 */

public class JsonArray {
    public JSONArray jArray = new JSONArray();

    public JSONObject jObject1 = new JSONObject();
    public JSONObject jObject2 = new JSONObject();
    public JSONObject jObject3 = new JSONObject();
    public JSONObject jObject4 = new JSONObject();
    public JSONObject jObject5 = new JSONObject();
    public JsonArray()
    {

        try {


            jObject1.put("Id",1);
            jObject1.put("Title","Nagasaki");
            jObject1.put("Latitude",20);
            jObject1.put("Longitude",15);
            jObject1.put("Description","Nagasaki station");

            jObject2.put("Id",2);
            jObject2.put("Title","Hakata");
            jObject2.put("Latitude",32);
            jObject2.put("Longitude",24);
            jObject2.put("Description","Hakata station");

            jObject3.put("Id",3);
            jObject3.put("Title","Kumamoto");
            jObject3.put("Latitude",40);
            jObject3.put("Longitude",30);
            jObject3.put("Description","Kumamoto station");

            jObject4.put("Id",4);
            jObject4.put("Title","Oita");
            jObject4.put("Latitude",80);
            jObject4.put("Longitude",60);
            jObject4.put("Description","Oita station");

            jObject5.put("Id",5);
            jObject5.put("Title","Aso");
            jObject5.put("Latitude",120);
            jObject5.put("Longitude",90);
            jObject5.put("Description","Aso station");

            jArray.put(jObject1);
            jArray.put(jObject2);
            jArray.put(jObject3);
            jArray.put(jObject4);
            jArray.put(jObject5);


        }catch (JSONException e)
        {
            e.printStackTrace();
        }

        Log.i("tag",jArray.toString());
    }
}
