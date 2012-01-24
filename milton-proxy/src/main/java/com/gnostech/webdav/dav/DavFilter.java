  /**
 *
 *
 * @author Octavio Gutierrez
 */
package com.gnostech.webdav.dav;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


//import waffle.servlet.AutoDisposableWindowsPrincipal;
//import waffle.servlet.NegotiateRequestWrapper;


public class DavFilter implements Filter {

    public DavFilter() {}

    @Override
	public void destroy() {
	}

    @Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		WebDavService.get().handleRequest((HttpServletRequest)request, (HttpServletResponse)response);
	}

    @Override
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
	



