/**
 *
 *
 * @author Octavio Gutierrez
 */
package com.gnostech.webdav.dao;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.gnostech.webdav.dav.DavFolder;
import com.gnostech.webdav.dav.MyResourceFactory;
import com.gnostech.webdav.model.FileInfo;
import com.gnostech.webdav.model.FolderInfo;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;


public class FileDAO {
	private static final Logger log = LoggerFactory.getLogger(FileDAO.class);


	//Method to obtain file listing from a FolderInfo Object 
	@SuppressWarnings({ "deprecation", "null" })
	public static List<FileInfo> getFilesInFolder(FolderInfo folder) {
		List<FileInfo> list = new ArrayList<FileInfo>();
		
		if(folder.getHref() != null){
		List<DavResource> resources = null;
		try {
			Sardine sardine = SardineFactory.begin();
			resources = sardine.getResources(MyResourceFactory.remoteClient + folder.getHref());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (DavResource res : resources)
		{
			Path path = Path.path(res.getName());
		
			if(!res.isDirectory())
			list.add(new FileInfo(res));
		}
		}
		
		if(folder.getName()== null){ //change from "/" and from getAbsolute();
		List<DavResource> resources = null;
		try {
			Sardine sardine = SardineFactory.begin();
			resources = sardine.getResources(MyResourceFactory.remoteClient);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (DavResource res : resources)
		{
			Path path = Path.path(res.getName());
			
			if(!res.isDirectory())
			list.add(new FileInfo(res));
		}
		}
		return list;
	}

	public static String getFullyQualifiedFileName(final FileInfo p) {
		return null;
				//FolderDAO.getFullyQualifiedFolderPath(FolderDAO.getFolderById(p.getFolderId()))+p.getName();
	}
	
	//In progress
	public static FileInfo createFile(final String name, FolderInfo fi, long length) {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			//String ename = URLEncoder.encode(name).replace("+", "%20");
			//Sardine sardine = SardineFactory.begin();
			//byte[] empty = new byte[1];
			//if(fi.getHref() == null)
			//	sardine.put(MyResourceFactory.remoteClient + "/" + ename, empty);
			//else
			//	sardine.put(MyResourceFactory.remoteClient +  fi.getHref() + ename, empty);
			return new FileInfo(name, length);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {

		}
	}

	public static void moveFile(final FileInfo fileInfo, final DavFolder destination, final String filename) {
	}

	public static void deleteFile(FileInfo f) {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			Sardine sardine = SardineFactory.begin("", "");
			sardine.delete(MyResourceFactory.remoteClient + f.getHref());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}
}

