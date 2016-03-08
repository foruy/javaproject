package com.daolicloud.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import com.daolicloud.bean.Image;
import com.daolicloud.service.ImageDownload;
import com.daolicloud.utils.GenUtil;

public class VboxImageFrame {

	private List<Image> images;
	private String token;
	private JList list;
	private Map<Integer, ImageDownload> tlist = new HashMap<Integer, ImageDownload>();
	private static final String IMAGE_PATH = "/var/daolicloud";
	
	public VboxImageFrame(List<Image> images, String token) {
		this.images = images;
		this.token = token;
	}
	
	public void init(String name) {
		final JFrame ijf = new JFrame();
		final JPopupMenu menu = new JPopupMenu();
		final JMenuItem download = new JMenuItem("Download");
		final JMenuItem cancel = new JMenuItem("Cancel");
		final JMenuItem open = new JMenuItem("Open");
		final DefaultListModel model = new DefaultListModel();
		//JSeparator sep = new JSeparator();
		menu.add(download);
		menu.add(cancel);
		menu.addSeparator();
		menu.add(open);
		JScrollPane pane = new JScrollPane();
		ijf.setTitle("镜像列表 <用户:" + name + ">");
		ijf.setLocationRelativeTo(null);
		ijf.setSize(280, 600);
		//ijf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ijf.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		ijf.setVisible(true);
		File file = new File(IMAGE_PATH);
		if(!file.exists() || !file.isDirectory())
			file.mkdir();
		for(Image img : images) {
			model.addElement(img);
		}
		list = new JList(model);
		list.setFixedCellHeight(40);
		list.add(menu);
		pane.getViewport().add(list);
		ijf.setContentPane(pane);

		ijf.addWindowListener(new WindowAdapter(){

			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				int ret = JOptionPane.showConfirmDialog(null, "有未完成的任务，您确定要退出吗!",
														"确认退出", JOptionPane.OK_CANCEL_OPTION);
				if(ret == JOptionPane.OK_OPTION) {
					try {
						Runtime.getRuntime().exec("killall VirtualBox");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					System.exit(0);
				}
			}
			
		});
		list.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent evt) {
				//showPopup(evt);
			}
			public void mousePressed(MouseEvent evt) {
				list.setSelectedIndex(list.locationToIndex(evt.getPoint()));
				showPopup(evt);
			}
			
			private void showPopup(MouseEvent evt) {
				if(evt.isPopupTrigger() && list.getSelectedIndex() != -1){
					Image img = images.get(list.getSelectedIndex());
					//File file = new File(IMAGE_PATH + File.separator + img.getCname()
					//		+ File.separator + img.getUid());
					File file = new File(IMAGE_PATH + File.separator + img.getUid());
					if(file.exists() && img.getSize() <= file.length()) {
						download.setEnabled(false);
						cancel.setEnabled(true);
						open.setEnabled(true);
					} else {
						download.setEnabled(true);
						cancel.setEnabled(false);
						open.setEnabled(false);
					}
					ImageDownload idl = tlist.get(list.getSelectedIndex());
					if(idl != null) {
						if(!idl.getBstop()) {
							download.setEnabled(false);
							cancel.setEnabled(true);
						}
					}
					menu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
				
			}
		});
		
		download.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent evt) {
				//System.out.println(list.getSelectedIndex());
				Object select = list.getModel().getElementAt(list.getSelectedIndex());
				int idx = getImageIndex(select);
				if(idx != -1) {
					ImageDownload idl = new ImageDownload(images.get(idx), token, model, idx);
					tlist.put(idx, idl);
					idl.start();
				}
			}
			
		});
		
		cancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent evt) {
				int idx = list.getSelectedIndex();
				ImageDownload idl = tlist.get(idx);
				if(idl != null) {
					idl.interrupt();
					idl.setBstop(true);
				}
				Runtime runTime = Runtime.getRuntime();
				String uid = GenUtil.convert(images.get(idx).getUid()).toString();
				try {
					String cmd = "vboxmanage controlvm " + uid + " poweroff";
					runTime.exec(cmd);
					cmd = "vboxmanage storagectl " + uid + " --name \"IDE Controller\" --remove";
					runTime.exec(cmd);
					cmd = "vboxmanage unregistervm "+ uid;
					runTime.exec(cmd);
					/* 删除vpn */
					cmd = "ip route del default dev ppp0 table rds";
					runTime.exec(cmd);
					cmd = "ip rule del from all fwmark 1 table rds";
					runTime.exec(cmd);
					cmd = "iptables -D PREROUTING -t mangle -i vboxnet0 -j MARK --set-mark 1";
					runTime.exec(cmd);
					cmd = "iptables -t nat  -D POSTROUTING -o ppp0 -j MASQUERADE";
					runTime.exec(cmd);
					/* 删除vpn结束 */
				} catch (IOException e) {
					e.printStackTrace();
				}
				File file = new File(IMAGE_PATH + File.separator + images.get(idx).getUid());
				file.delete();
			}		
		});
		
		open.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent evt) {
				int idx = list.getSelectedIndex();
				Image select = (Image)list.getModel().getElementAt(idx);
				String uid = GenUtil.convert(select.getUid()).toString();
				File file = new File(IMAGE_PATH + File.separator + uid);
				if(file.length() >= select.getSize()) {
					Runtime runTime = Runtime.getRuntime();
					try {
						/* 执行vpn命令开始 */
						String cmd = "echo \"c vpn-connection\" > /var/run/xl2tpd/l2tp-control";
						runTime.exec(cmd);
						cmd = "vboxmanage startvm "+ uid +" --type gui";
						runTime.exec(cmd);
						cmd = "ip route del default dev ppp0 table rds";
						runTime.exec(cmd);
						cmd = "ip route add default dev ppp0 table rds";
						runTime.exec(cmd);
						cmd = "ip rule del from all fwmark 1 table rds";
						runTime.exec(cmd);
						cmd = "ip rule add from all fwmark 1 table rds";
						runTime.exec(cmd);
						cmd = "iptables -D PREROUTING -t mangle -i vboxnet0 -j MARK --set-mark 1";
						runTime.exec(cmd);
						cmd = "iptables -I PREROUTING -t mangle -i vboxnet0 -j MARK --set-mark 1";
						runTime.exec(cmd);
						cmd = "iptables -t nat  -D POSTROUTING -o ppp0 -j MASQUERADE";
						runTime.exec(cmd);
						cmd = "iptables -t nat  -A POSTROUTING -o ppp0 -j MASQUERADE";
						runTime.exec(cmd);
						/* 执行vpn命令结束*/
					} catch (IOException e) {
						JOptionPane.showMessageDialog(ijf, "虚拟机启动失败!",
								"错误信息", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(ijf, "文件已损坏!",
							"错误信息", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});
	}

	private int getImageIndex(Object select) {
		for(int i=0;i<images.size();i++) {
			if(select == images.get(i))
				return i;
		}
		return -1;
	}
}
