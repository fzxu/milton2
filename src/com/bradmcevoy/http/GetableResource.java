package com.bradmcevoy.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/** Copyright 2008 Brad Mcevoy Licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable 
 * law or agreed to in writing, software distributed under the License is 
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
 * 
 * @author brad
 */
public interface GetableResource extends Resource {
    /** If range not null is a Partial content request
     */
    public void sendContent( OutputStream out, Range range, Map<String,String> params ) throws IOException;

    /** How many seconds to allow the content to be cached for, or null if caching is not allowed
     */
    Long getMaxAgeSeconds();
    
}
