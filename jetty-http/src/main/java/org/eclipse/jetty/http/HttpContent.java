//
//  ========================================================================
//  Copyright (c) 1995-2014 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.eclipse.jetty.http.MimeTypes.Type;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.resource.Resource;

/* ------------------------------------------------------------ */
/** HttpContent.
 * 
 *
 */
public interface HttpContent
{
    HttpField getContentType();
    String getContentTypeValue();
    String getCharacterEncoding();
    Type getMimeType();

    HttpField getContentLength();
    long getContentLengthValue();
    
    HttpField getLastModified();
    String getLastModifiedValue();
    
    HttpField getETag();
    String getETagValue();
    
    ByteBuffer getIndirectBuffer();
    ByteBuffer getDirectBuffer();
    Resource getResource();
    InputStream getInputStream() throws IOException;
    ReadableByteChannel getReadableByteChannel() throws IOException;
    void release();

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public class ResourceAsHttpContent implements HttpContent
    {
        final Resource _resource;
        final String _contentType;
        final int _maxBuffer;
        final String _etag;

        /* ------------------------------------------------------------ */
        public ResourceAsHttpContent(final Resource resource, final String contentType)
        {
            this(resource,contentType,-1,false);
        }

        /* ------------------------------------------------------------ */
        public ResourceAsHttpContent(final Resource resource, final String contentType, int maxBuffer)
        {
            this(resource,contentType,maxBuffer,false);
        }

        /* ------------------------------------------------------------ */
        public ResourceAsHttpContent(final Resource resource, final String contentType, boolean etag)
        {
            this(resource,contentType,-1,etag);
        }

        /* ------------------------------------------------------------ */
        public ResourceAsHttpContent(final Resource resource, final String contentType, int maxBuffer, boolean etag)
        {
            _resource=resource;
            _contentType=contentType;
            _maxBuffer=maxBuffer;
            _etag=etag?resource.getWeakETag():null;
        }

        /* ------------------------------------------------------------ */
        @Override
        public String getContentTypeValue()
        {
            return _contentType;
        }
        
        /* ------------------------------------------------------------ */
        @Override
        public HttpField getContentType()
        {
            return _contentType==null?null:new HttpField(HttpHeader.CONTENT_TYPE,_contentType);
        }

        /* ------------------------------------------------------------ */
        @Override
        public String getCharacterEncoding()
        {
            return _contentType==null?null:MimeTypes.getCharsetFromContentType(_contentType);
        }

        /* ------------------------------------------------------------ */
        @Override
        public Type getMimeType()
        {
            return _contentType==null?null:MimeTypes.CACHE.get(MimeTypes.getContentTypeWithoutCharset(_contentType));
        }

        /* ------------------------------------------------------------ */
        @Override
        public HttpField getLastModified()
        {
            long lm = _resource.lastModified();
            return lm>=0?new HttpField(HttpHeader.LAST_MODIFIED,DateGenerator.formatDate(lm)):null;
        }

        /* ------------------------------------------------------------ */
        @Override
        public String getLastModifiedValue()
        {
            long lm = _resource.lastModified();
            return lm>=0?DateGenerator.formatDate(lm):null;
        }

        /* ------------------------------------------------------------ */
        @Override
        public ByteBuffer getDirectBuffer()
        {
            if (_resource.length()<=0 || _maxBuffer<_resource.length())
                return null;
            try
            {
                return BufferUtil.toBuffer(_resource,true);
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        
        /* ------------------------------------------------------------ */
        @Override
        public HttpField getETag()
        {
            return _etag==null?null:new HttpField(HttpHeader.ETAG,_etag);
        }
        
        /* ------------------------------------------------------------ */
        @Override
        public String getETagValue()
        {
            return _etag;
        }

        /* ------------------------------------------------------------ */
        @Override
        public ByteBuffer getIndirectBuffer()
        {
            if (_resource.length()<=0 || _maxBuffer<_resource.length())
                return null;
            try
            {
                return BufferUtil.toBuffer(_resource,false);
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        /* ------------------------------------------------------------ */
        @Override
        public HttpField getContentLength()
        {
            long l=_resource.length();
            return l==-1?null:new HttpField.LongValueHttpField(HttpHeader.CONTENT_LENGTH,_resource.length());
        }

        /* ------------------------------------------------------------ */
        @Override
        public long getContentLengthValue()
        {
            return _resource.length();
        }

        /* ------------------------------------------------------------ */
        @Override
        public InputStream getInputStream() throws IOException
        {
            return _resource.getInputStream();
        }
        
        /* ------------------------------------------------------------ */
        @Override
        public ReadableByteChannel getReadableByteChannel() throws IOException
        {
            return _resource.getReadableByteChannel();
        }

        /* ------------------------------------------------------------ */
        @Override
        public Resource getResource()
        {
            return _resource;
        }

        /* ------------------------------------------------------------ */
        @Override
        public void release()
        {
            _resource.close();
        }
        
        /* ------------------------------------------------------------ */
        @Override
        public String toString()
        {
            return String.format("%s@%x{r=%s}",this.getClass().getSimpleName(),hashCode(),_resource);
        }
    }

}
