package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;

public class PropPatchHandler  extends ExistingEntityHandler {
    
    //private Logger log = LoggerFactory.getLogger(PropPatchHandler.class);
    
    PropPatchHandler(HttpManager manager) {
        super(manager);
    }
    
    public Request.Method method() {
        return Method.PROPPATCH;
    }
    
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof PropFindableResource);
    }

    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        // TODO - MS webdav uses this to set MS properties after upload. safe to ignore but should implement
//        try {
//            InputStream in = request.getInputStream();
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            StreamToStream.readTo(in,out);
//            log.debug("PropPatch: " + out.toString());
//        } catch (WritingException ex) {
//            throw new RuntimeException(ex);
//        } catch (ReadingException ex) {
//            throw new RuntimeException(ex);
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
    }
}
