package laochanlam.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Information extends AppCompatActivity {

    private TextView fbTime,igTime,lineTime,othersTime;
    private TextView fbDis,igDis,lineDis,othersDis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        fbTime=(TextView)findViewById(R.id.fbtime);
        igTime=(TextView)findViewById(R.id.igtime);
        lineTime=(TextView)findViewById(R.id.linetime);
        othersTime=(TextView)findViewById(R.id.otherstime);

        fbDis=(TextView)findViewById(R.id.fbdis);
        igDis=(TextView)findViewById(R.id.igdis);
        lineDis=(TextView)findViewById(R.id.linedis);
        othersDis=(TextView)findViewById(R.id.othersdis);


    }
}
