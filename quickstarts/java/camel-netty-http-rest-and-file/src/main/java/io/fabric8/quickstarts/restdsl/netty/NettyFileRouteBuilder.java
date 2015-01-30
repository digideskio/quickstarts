/**
 *  Copyright 2005-2014 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package io.fabric8.quickstarts.restdsl.netty;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.netty.NettyConstants;
import org.apache.camel.component.netty.http.NettyHttpMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * Serve static files with Netty.
 *
 */
public class NettyFileRouteBuilder extends RouteBuilder {

    static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    static final int HTTP_CACHE_SECONDS = 86400;

    @Override
    public void configure() throws Exception {
        from("netty-http:http://localhost:9003/public?matchOnUriPrefix=true&bootstrapConfiguration=#serverBootstrapConfiguration")
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    NettyHttpMessage in = exchange.getIn(NettyHttpMessage.class);

                    HttpRequest request = in.getHttpRequest();
                    final Message out = exchange.getOut();
                    if (request.getMethod() != GET) {
                        setErrorResponse(out, METHOD_NOT_ALLOWED);
                        return;
                    }

                    final String path = sanitizeUri(request.getUri());
                    if (path == null) {
                        setErrorResponse(out, FORBIDDEN);
                        return;
                    }

                    File file = new File(path);
                    if (file.isHidden() || !file.exists()) {
                        setErrorResponse(out, NOT_FOUND);
                        return;
                    }
                    if (!file.isFile()) {
                        setErrorResponse(out, FORBIDDEN);
                        return;
                    }

                    // Cache Validation
                    String ifModifiedSince = request.headers().get(IF_MODIFIED_SINCE);
                    if (ifModifiedSince != null && ifModifiedSince.length() != 0) {
                        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
                        Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

                        // Only compare up to the second because the datetime format we send to the client does
                        // not have milliseconds
                        long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
                        long fileLastModifiedSeconds = file.lastModified() / 1000;
                        if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {

                            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, NOT_MODIFIED);

                            dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

                            Calendar time = new GregorianCalendar();
                            out.setHeader(DATE, dateFormatter.format(time.getTime()));
                            out.setBody(response);

                            return;
                        }
                    }

                    HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, OK);
                    setDateAndCacheHeaders(response, file);
                    final byte[] content = Files.readAllBytes(file.toPath());
                    response.setContent(ChannelBuffers.copiedBuffer(content));
                    response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.length);

                    String mimeType = Files.probeContentType(file.toPath());
                    out.setHeader(Exchange.CONTENT_TYPE, mimeType);

                    out.setBody(response);
                }
            });
    }

    private static String sanitizeUri(String uri) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + '.') ||
            uri.contains('.' + File.separator) ||
            uri.startsWith(".") || uri.endsWith(".")) {
            return null;
        }

        // Convert to absolute path.
        return System.getProperty("user.dir") + File.separator + uri;
    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response The HTTP response.
     * @param fileToCache the file to extract content type
     */
    private void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    private void setErrorResponse(Message outMessage, HttpResponseStatus status) {
        outMessage.setFault(true);
        outMessage.setBody("Failure: " + status);
    }
}
