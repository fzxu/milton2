package com.gnostech.webdav.dav;
/*

Copyright 2011 North Concepts Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/


import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.http.http11.auth.DigestGenerator;

public abstract class Dav implements PropFindableResource, DigestResource {

	@Override
	public Object authenticate(String user, String password) {
		if(user.equals("tave")){
			if(password.equals("casper")){
				return "tave";
                }else
				return null;
                }
                
		///}
		return null;
	}
        

	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		if(auth == null)
			return false;
		else
			return true;
	}

	@Override
	public String checkRedirect(Request request) {
		return null;
	}

	@Override
	public String getRealm() {
		return "MLFS";
	}

	@Override
	public Object authenticate(DigestResponse arg0) {
		if(arg0.getUser().equals("testuser")){
			//System.out.println("Activity from " + arg0.getUser());
			return true;
		}
		else
			return null;
	}

	@Override
	public boolean isDigestAllowed() {
		// TODO Auto-generated method stub
		return true;
	}

}