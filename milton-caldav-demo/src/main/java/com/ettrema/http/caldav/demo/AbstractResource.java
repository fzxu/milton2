package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestGenerator;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import java.util.Date;

/**
 *
 * @author alex
 */
public class AbstractResource implements Resource
{

  private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractResource.class);
  protected String name;
  protected String user;
  protected String password;
  protected Date modDate;
  protected Date createdDate;
  protected TFolderResource parent;

  public AbstractResource(TFolderResource parent, String name)
  {
    this.parent = parent;
    this.name = name;
    modDate = new Date();
    createdDate = new Date();
    if (parent != null)
    {
      checkAndRemove(parent, name);
      parent.children.add(this);
    }
  }

  public Object authenticate(String user, String requestedPassword)
  {
    log.debug("authentication: " + user + " - " + requestedPassword + " = " + password);
    if (this.user == null)
    {
      log.debug("no user defined, so allow access");
      return true;
    }
    if (!user.equals(this.user))
    {
      return null;
    }
    if (password == null)
    {
      if (requestedPassword == null || requestedPassword.length() == 0)
      {
        return "ok";
      }
      else
      {
        return null;
      }
    }
    else
    {
      if (password.equals(requestedPassword))
      {
        return "ok";
      }
      else
      {
        return null;
      }
    }
  }

  public Object authenticate(DigestResponse digestRequest)
  {
    DigestGenerator dg = new DigestGenerator();
    String serverResponse = dg.generateDigest(digestRequest, password);
    String clientResponse = digestRequest.getResponseDigest();
    log.debug("server resp: " + serverResponse);
    log.debug("given response: " + clientResponse);
    if (serverResponse.equals(clientResponse))
    {
      return "ok";
    }
    else
    {
      return null;
    }
  }

  public String getUniqueId()
  {
    return this.hashCode() + "";
  }

  public String checkRedirect(Request request)
  {
    return null;
  }

  public String getName()
  {
    return name;
  }

  public boolean authorise(Request request, Method method, Auth auth)
  {
    log.debug("authorise");
    if (auth == null)
    {
      if (this.user == null)
      {
        return true;
      }
      else
      {
        return false;
      }
    }
    else
    {
      return this.user == null || auth.getUser().equals(this.user);
    }
  }

  public String getRealm()
  {
    return "testrealm@host.com";
  }

  public Date getModifiedDate()
  {
    return modDate;
  }

  private void checkAndRemove(TFolderResource parent, String name)
  {
    TResource r = (TResource) parent.child(name);
    if (r != null)
    {
      parent.children.remove(r);
    }
  }
}
