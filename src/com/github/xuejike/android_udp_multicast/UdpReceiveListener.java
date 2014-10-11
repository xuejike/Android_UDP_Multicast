package com.github.xuejike.android_udp_multicast;

import java.net.InetAddress;

public interface UdpReceiveListener {
	public void receive(InetAddress address,String msg);
}
