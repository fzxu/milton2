/**
 *
 *
 * @author Octavio Gutierrez
 */
package com.gnostech.webdav.dao;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gnostech.webdav.dav.DavFolder;
import com.gnostech.webdav.dav.MyResourceFactory;
import com.gnostech.webdav.model.FolderInfo;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import org.apache.http.client.utils.*;


public class FolderDAO {

	private static final Logger log = LoggerFactory.getLogger(FolderDAO.class);
	private static String FOLDER_SEP = "/";
	
	public static FolderInfo getRootFolder() {
		return new FolderInfo();
	}
	
	public static FolderInfo FolderInfo(String s) {
		return new FolderInfo(s);
	}
	

	public static FolderInfo getParentFolder(final FolderInfo f) {
		//return getFolderById(f.getParentId());
		return null;
	}

	public static List<FolderInfo> getSubFolders(final FolderInfo f) {
		List<FolderInfo> folders = new ArrayList<FolderInfo>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			if(f.getName() == null){
				Sardine sardine = SardineFactory.begin();
				@SuppressWarnings("deprecation")
				List<DavResource> resources = sardine.getResources(MyResourceFactory.remoteClient);
				for (DavResource res : resources)
				{
					if((!(res.toString().equals("/")))&& res.isDirectory()){
				     folders.add(new FolderInfo(res));
					}
					
				}
	
			}else{
				
				Sardine sardine = SardineFactory.begin();
				@SuppressWarnings("deprecation")
				
				List<DavResource> resources = sardine.getResources(MyResourceFactory.remoteClient + f.getHref());
				for (DavResource res : resources)
				{
					if(res.toString() != resources.get(0).toString() && res.isDirectory() )
				     folders.add(new FolderInfo(res));
				}
				
			}
			return folders;
		} catch (Exception e) {
			e.printStackTrace();
			return folders;
		} finally {

		}
		
		//return null;
		 
		//folders.add(new FolderInfo(MyResourceFactory.folders));
		//return folders;
	}

	public static FolderInfo createFolder(final String name, final FolderInfo parentFolder) {
		String ename = URLEncoder.encode(name).replace("+", "%20");
		try {
			
			if(parentFolder.getHref() == null){
				Sardine sardine = SardineFactory.begin();
				sardine.createDirectory(MyResourceFactory.remoteClient + "/" + ename);
			}
			else{
				Sardine sardine = SardineFactory.begin();
				sardine.createDirectory(MyResourceFactory.remoteClient + parentFolder.getHref() + ename);
			}
			return new FolderInfo(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			//Perform cleanup of connections - Sardine handles it's connections internally in it's API
		}
	}
	
	public static void deleteFolder(FolderInfo f) {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			Sardine sardine = SardineFactory.begin("", "");
			sardine.delete(MyResourceFactory.remoteClient + f.getHref());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//Perform cleanup of connections - Sardine handles it's connections internally in it's API
		}
	}
	
	public static void moveFolder(final FolderInfo folderInfo, final DavFolder destination, final String foldername) {
		/*
		try {
			Sardine sardine = SardineFactory.begin();		
			if(destination.getFolderInfo().getName()==null)//changed from "root"
				sardine.move(MyResourceFactory.remoteClient + folderInfo.getHref(), MyResourceFactory.remoteClient + "/" + URLEncoder.encode(foldername).replace("+", "%20"));	
			else
				sardine.move(MyResourceFactory.remoteClient + folderInfo.getHref(), MyResourceFactory.remoteClient + destination.getFolderInfo().getHref() + URLEncoder.encode(foldername).replace("+", "%20"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}
	
	public static String getFullyQualifiedFolderPath(final FolderInfo f) {
		return null;
	*/
	}
	
}
