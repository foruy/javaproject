package com.daolicloud.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import com.daolicloud.bean.Image;
import com.daolicloud.utils.GenUtil;


public class ImageDownload extends Thread {

	private static final String IMAGE_PATH = "/var/daolicloud";
	private static final int BUFFER_SIZE = 8096;
	private String token;
	private Image image;
	private DefaultListModel list;
	private int idx;
	private boolean bstop = false;
	
	public ImageDownload(Image image, String token, DefaultListModel list, int idx) {
		this.image = image;
		this.token = token;
		this.list = list;
		this.idx = idx;
	}
	
	public boolean getBstop() {
		return bstop;
	}
	
	public void setBstop(boolean bstop) {
		this.bstop = bstop;
	}
	
	@Override
	public void run() {
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		InputStream input = null;
		byte[] buf = new byte[BUFFER_SIZE];
		int size = 0;
		long current = 0;
		File file = new File(IMAGE_PATH + File.separator + image.getUid());
		HttpClient httpClient = new HttpClient();
		GetMethod get = new GetMethod(image.getFile());
		String name = image.getCname();
		get.addRequestHeader("X-Auth-Token", token);
		try {
			if(httpClient.executeMethod(get) == HttpStatus.SC_OK) {
				input = get.getResponseBodyAsStream();
				Header[] head = get.getResponseHeaders();
				System.out.println("File Size:" + image.getSize());
				for(Header h : head) {
					System.out.println(h.getName() + ":" + h.getValue());
				}
				fos = new FileOutputStream(file);
				bos = new BufferedOutputStream(fos);
				while((size = input.read(buf)) != -1) {
					if(getBstop()) {
						image.setName(name + " (下载未完成)");
						list.set(idx, image);
						break;
					}
					bos.write(buf, 0, size);
					current += size;
					String suf = " (" + String.format("%.1f",(1.0 * current / image.getSize() * 100)) + "%)";
					image.setName(name + suf);
					list.set(idx, image);
				}
				if(!getBstop()) {
					setBstop(true);
					Runtime runTime = Runtime.getRuntime();
					String uid = GenUtil.convert(image.getUid()).toString();
					String cmd = "vboxmanage createvm --name " + image.getCname() +
									" --uuid " + uid + " --register";
					runTime.exec(cmd);
					cmd = "vboxmanage modifyvm " + uid + " --nic1 nat --memory 512";
					runTime.exec(cmd);
					cmd = "vboxmanage storagectl " + uid +
							" --name \"IDE Controller\" --add ide --bootable on";
					runTime.exec(cmd);
					cmd = "vboxmanage storageattach " + uid +
							" --storagectl \"IDE Controller\" --port 0 --device 0 --type hdd --medium " +
							file.getAbsoluteFile();
					runTime.exec(cmd);
					
				}
			}
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "镜像安装失败!",
					"镜像安装失败",JOptionPane.ERROR_MESSAGE);
		} finally {
			try {
				if(bos != null)
					bos.close();
				if(fos != null)
					fos.close();
				if(input != null)
					input.close();
			} catch (IOException e) {}
		}
	}
}
