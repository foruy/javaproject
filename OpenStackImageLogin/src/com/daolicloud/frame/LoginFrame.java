package com.daolicloud.frame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.daolicloud.bean.Image;
import com.daolicloud.service.ImageAuthtencation;

public class LoginFrame extends JFrame {

	JPanel rootPanel = new JPanel();
	JPanel content = new JPanel();
	JPanel footer = new JPanel();
	JPanel btnPanel = new JPanel();
	JLabel ip = new JLabel("IP");
	JLabel name = new JLabel("用户名:");
	JLabel password = new JLabel("密码:");
	JTextField fieldIP = new JTextField();
	JTextField fieldName = new JTextField(10);
	JPasswordField fieldPwd = new JPasswordField(10);
	JButton ok = new JButton("登录");
	JButton cancel = new JButton("清空");
	GridLayout grid = new GridLayout(3,2);
	private ImageAuthtencation client;
	
	public LoginFrame() {
		content.setLayout(grid);
		content.add(ip);
		content.add(fieldIP);
		content.add(name);
		content.add(fieldName);
		content.add(password);
		content.add(fieldPwd);
		footer.setLayout(new FlowLayout(FlowLayout.RIGHT));
		footer.add(ok);
		footer.add(cancel);
		rootPanel.setLayout(new BorderLayout());
		rootPanel.add(content, BorderLayout.CENTER);
		rootPanel.add(footer, BorderLayout.PAGE_END);
		getContentPane().add(rootPanel);
		setVisible(true);
		setResizable(false);
		setSize(260, 120);
		setTitle("登录认证");
		//setContentPane(rootPanel);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ok.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(fieldName.getText().equals("")){
					JOptionPane.showMessageDialog(null, "用户名不能为空!",
							"错误信息",JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if("".equals(fieldIP.getText())){
					JOptionPane.showMessageDialog(null, "服务器地址不能为空!",
							"错误信息",JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if("".equals(String.valueOf(fieldPwd.getPassword()))){
					JOptionPane.showMessageDialog(null, "密码不能为空!",
							"错误信息",JOptionPane.ERROR_MESSAGE);
					return;
				}
				client = new ImageAuthtencation(fieldIP.getText());
				boolean auth = client.authentication(fieldName.getText().trim(),
													 String.valueOf(fieldPwd.getPassword()).trim());
				if(auth) {
					List<Image> images = client.getImages();
					LoginFrame.this.setVisible(false);
					/*ImageFrame imgFrame = new ImageFrame(images, client.getToken());
					imgFrame.init(fieldName.getText());*/
					VboxImageFrame boxFrame = new VboxImageFrame(images, client.getToken());
					boxFrame.init(fieldName.getText());
				}
			}});
		cancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				fieldIP.setText("");
				fieldName.setText("");
				fieldPwd.setText("");
			}});
	}
}
