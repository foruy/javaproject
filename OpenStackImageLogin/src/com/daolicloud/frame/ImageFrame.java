package com.daolicloud.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import com.daolicloud.bean.Image;
import com.daolicloud.service.ImageDownload;


public class ImageFrame {

	private List<Image> images;
	private String token;
	private JList list;
	private Map<Integer, ImageDownload> tlist = new HashMap<Integer, ImageDownload>();
	private static final String IMAGE_PATH = "/var/daolicloud";
	
	public ImageFrame(List<Image> images, String token) {
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
						Runtime.getRuntime().exec("killall qemu-kvm");
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
					File file = new File(IMAGE_PATH + File.separator + img.getUid());
					if(file.exists() && img.getSize() <= file.length()) {
						download.setEnabled(false);
						cancel.setEnabled(false);
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
				tlist.get(idx).interrupt();
				tlist.get(idx).setBstop(true);
				File file = new File(IMAGE_PATH + File.separator + images.get(idx).getUid());
				file.delete();
			}		
		});
		
		open.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent evt) {
				int idx = list.getSelectedIndex();
				Image select = (Image)list.getModel().getElementAt(idx);
				File file = new File(IMAGE_PATH + File.separator + select.getUid());
				if(file.length() >= select.getSize()) {
					Runtime runTime = Runtime.getRuntime();
					int uid = 50 + idx;
					try {
						String cmd = "/usr/libexec/qemu-kvm -hda " + file.getAbsolutePath() + " -net nic " +
								"-net tap,ifname=tap" + uid + ",script=no -vnc :" + uid + " -daemonize";
						runTime.exec(cmd);
						cmd = "ifconfig tap" + uid +" up";
						runTime.exec(cmd);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							Thread.sleep(2000);
							runTime.exec("/usr/bin/vncviewer :" + uid);
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} else {
					JOptionPane.showMessageDialog(ijf, "文件已损坏!",
							"error", JOptionPane.ERROR_MESSAGE);
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
