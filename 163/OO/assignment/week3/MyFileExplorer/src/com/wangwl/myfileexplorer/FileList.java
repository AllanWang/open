package com.wangwl.myfileexplorer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileSystemView;

public class FileList extends JList implements MouseListener{
	private FileListModel dataModel;
	public FileList(){
		//setLayoutOrientation(HORIZONTAL_WRAP);
		dataModel = new FileListModel();
		setModel(dataModel);
		setCellRenderer(new MyCellRenderer());
		addMouseListener(this);
	}
	public void setNode(FileNode file){
		clearSelection();
		dataModel.setNode(file);
		updateUI();
	}
	public FileNode getNode(){
		return dataModel.getNode();
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2){
			FileNode node = (FileNode) ((JList)arg0.getSource()).getSelectedValue();
			
			File file = node.getFile();
			if(file.isDirectory()){
				setNode(node);
			}
			else if(file.isFile())
			{
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//System.out.println(file.toString());
		}
		
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getButton()==MouseEvent.BUTTON3){
			MyPopupMenu.getInstance(this).show(this,arg0.getX(),arg0.getY());
		}
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}

class FileListModel implements ListModel {
	private FileNode fileNode;
	private FileSystemView fsv;
	public FileListModel(){
		fsv = FileSystemView.getFileSystemView();
		fileNode = null;
	}
	public void setNode(FileNode f){
		fileNode = f;
	}
	public FileNode getNode(){
		return fileNode;
	}

	@Override
	public void addListDataListener(ListDataListener arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getElementAt(int arg0) {
		// TODO Auto-generated method stub
		if(fileNode != null){
			if(fileNode.getFile().isDirectory()){
				File[] files = fsv.getFiles(fileNode.getFile(), true);
				if(files != null && files.length>0){
					return new FileNode(files[arg0]);
				}
			}
			else if(fileNode.getFile().isFile()){
				return fileNode;
			}
		}
		return null;
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		if(fileNode != null){
			if(fileNode.getFile().isDirectory()){
				return fsv.getFiles(fileNode.getFile(), true).length;
			}
			else if(fileNode.getFile().isFile()){
				return 1;
			}
		}
		return 0;
	}

	@Override
	public void removeListDataListener(ListDataListener arg0) {
		// TODO Auto-generated method stub
		
	}
}

class MyCellRenderer extends JLabel implements ListCellRenderer {
	private FileSystemView fsv;
    public MyCellRenderer() {
    	fsv = FileSystemView.getFileSystemView();
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

    	File file = ((FileNode)value).getFile();
    	setIcon(fsv.getSystemIcon(file));
    	setText(fsv.getSystemDisplayName(file));

        Color background;
        Color foreground;

        // check if this cell represents the current DnD drop location
        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == index) {

            background = Color.BLUE;
            foreground = Color.WHITE;

        // check if this cell is selected
        } else if (isSelected) {
            background = Color.RED;
            foreground = Color.WHITE;

        // unselected, and not the DnD drop location
        } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
        };
        
        setBackground(background);
        setForeground(foreground);

        return this;
    }
}

class MyPopupMenu extends JPopupMenu {
	private FileList filelist;
	private Object[] srcfilenodes;
	private static MyPopupMenu popMenu = null;
	public static MyPopupMenu getInstance(FileList fl){
		if(popMenu == null){
			popMenu = new MyPopupMenu(fl);
		}
		popMenu.setFileList(fl);
		return popMenu;
	}
	private void setFileList(FileList fl){
		filelist = fl;
	}
	private MyPopupMenu(FileList fl){
		filelist =fl;
		srcfilenodes = null;
		JMenuItem menuItemCopy = new JMenuItem("Copy");
		JMenuItem menuItemPaste = new JMenuItem("Paste");
		JMenuItem menuItemDelete = new JMenuItem("Delete");
		JMenuItem menuItemMove = new JMenuItem("Move");
		
		menuItemCopy.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK));
		menuItemPaste.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_MASK));
		menuItemDelete.setAccelerator(KeyStroke.getKeyStroke('D', InputEvent.CTRL_MASK));
		menuItemCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				srcfilenodes = filelist.getSelectedValues();
				
			}});

		menuItemPaste.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	new Thread(){
		    		public void run(){
		    			if(srcfilenodes != null && srcfilenodes.length>0){
		    				WaitDialog.launchWaitDialog("Copy Files", null);;
		    				
			    			for(int i=0;i<srcfilenodes.length;i++){
			    				try {
									Utils.copy(((FileNode)srcfilenodes[i]).getFile(), filelist.getNode().getFile());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
			    			}
			    			WaitDialog.endWaitDialog();
			    			filelist.setNode(filelist.getNode());
		    			}
		    				
		    		}
		    	}.start();
		    }});

		menuItemDelete.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	new Thread(){
		    		public void run(){
						Object[] delObjs = filelist.getSelectedValues();
						if (delObjs.length > 0) {
							WaitDialog.launchWaitDialog("Delete Files", null);

							for (int i = 0; i < delObjs.length; i++) {
								Utils.delete(((FileNode) delObjs[i]).getFile());
							}
							WaitDialog.endWaitDialog();
							filelist.setNode(filelist.getNode());
						}
		    		}
		    	}.start();

		    }});

		add(menuItemCopy);
		add(menuItemPaste);
		add(menuItemDelete);
		//add(menuItemMove);
	}
}
