package com.github.xuejike.android_udp_multicast.activity;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.github.xuejike.android_udp_multicast.UdpHelper;
import com.github.xuejike.android_udp_multicast.UdpReceiveListener;
import com.github.xuejike.android_udp_multicast.Exception.WifiDisableException;
import com.xuejike.android_udp_multicast.R;
import com.xuejike.android_udp_multicast.R.id;
import com.xuejike.android_udp_multicast.R.layout;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.sax.TextElementListener;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener, UdpReceiveListener {

	private TextView logView;
	private UdpHelper udpHelper;
	private EditText edit;
	private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		logView=(TextView) findViewById(R.id.textView1);
		Button send=(Button) findViewById(R.id.btnSend);
		send.setOnClickListener(this);
		
		edit=(EditText) findViewById(R.id.editText1);
		
		udpHelper=new UdpHelper((WifiManager) getSystemService(WIFI_SERVICE), 8001);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					udpHelper.startListen(MainActivity.this);
				} catch (WifiDisableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void print(final String msg){
		logView.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				StringBuilder sb=new StringBuilder( logView.getText().toString());
				sb.insert(0,getDate()+":"+msg+"\n");
				logView.setText(sb.toString());
			}
		});
	}
	public String getDate(){
		return sdf.format(new Date());
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		udpHelper.sendMulticast(edit.getText().toString());
		
	}

	@Override
	public void receive(InetAddress address, String msg) {
		// TODO Auto-generated method stub
		print(address.toString()+":"+msg);
	}
	
	

}
