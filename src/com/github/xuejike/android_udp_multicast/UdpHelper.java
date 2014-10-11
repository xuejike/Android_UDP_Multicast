package com.github.xuejike.android_udp_multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import com.github.xuejike.android_udp_multicast.Exception.SendStateListener;
import com.github.xuejike.android_udp_multicast.Exception.WifiDisableException;

import android.net.wifi.WifiManager;
import android.util.Log;

public class UdpHelper {
	private static final String TAG="UdpHelper";

	private  WifiManager.MulticastLock lock;
	private WifiManager wifiManager;
	private int port;
	private int packetLength=1024;
	private Charset charset=Charset.forName("UTF-8");
	private boolean listening;
	private SendStateListener sendListener;
	private InetAddress multicastAddress;
	
	
	public UdpHelper(WifiManager manager,int port) {
		this.lock = manager.createMulticastLock("UDPwifi");
		this.wifiManager=manager;
		this.port=port;
		try {
			this.multicastAddress=InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void startListen(final UdpReceiveListener listener) throws WifiDisableException {
		check();
		listening=true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				DatagramSocket datagramSocket;
				try {
					datagramSocket = new DatagramSocket(port);
					datagramSocket.setBroadcast(true);
					byte[] data=new byte[packetLength];
					while(listening){
						DatagramPacket datagramPacket=new DatagramPacket(data,data.length);
						try {
							datagramSocket.receive(datagramPacket);
							lock.acquire();
							String msg= new String(datagramPacket.getData(),0,datagramPacket.getLength(),charset);
							
							Log.i(TAG, "Receive："+msg);
							new ReceiveThread(msg, datagramPacket.getAddress(), listener).start();
						} catch (IOException e) {
							
							e.printStackTrace();
						}finally{
							lock.release();
						}
					}
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				
				}finally{
					
				}
			}
		}).start();
	}
	public void stopListen(){
		listening=false;
	}
	public void sendMulticast(String msg){
		send(multicastAddress,msg);
	}
	public void send(final InetAddress address,final String msg){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				DatagramSocket s;
				try {
					s = new DatagramSocket();
					byte[] messageByte = msg.getBytes();
					DatagramPacket p = new DatagramPacket(
							messageByte,
							messageByte.length, 
							address,
							port
							);
					s.send(p);
					
					Log.d(TAG, "Send："+msg);
					if(sendListener!=null) sendListener.success();
					s.close();
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if(sendListener!=null) sendListener.error();
				}catch( IOException e){
					e.printStackTrace();
					if(sendListener!=null) sendListener.error();
				}
			}
		}).start();
	}
	
	private void check() throws WifiDisableException 
	{
		if(wifiManager.isWifiEnabled()){
			
		}else{
			throw new WifiDisableException();
		}
	}
	public int getPacketLength() {
		return packetLength;
	}
	public void setPacketLength(int packetLength) {
		this.packetLength = packetLength;
	}
	public Charset getCharset() {
		return charset;
	}
	public void setCharset(Charset charset) {
		this.charset = charset;
	}
	public SendStateListener getSendListener() {
		return sendListener;
	}
	public void setSendListener(SendStateListener sendListener) {
		this.sendListener = sendListener;
	}
	
	
}

class ReceiveThread extends Thread{
	private String msg;
	private InetAddress address;
	private UdpReceiveListener listener;
	

	public ReceiveThread(String msg, InetAddress address,
			UdpReceiveListener listener) {
		
		this.msg = msg;
		this.address = address;
		this.listener = listener;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		listener.receive(address, msg);
	}
}

