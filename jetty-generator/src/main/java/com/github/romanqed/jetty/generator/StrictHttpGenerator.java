//
// ========================================================================
// Copyright (c) 1995 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

// Copyright 2024 Roman Bakaldin
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.romanqed.jetty.generator;

import org.eclipse.jetty.http.*;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Index;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import static org.eclipse.jetty.http.HttpStatus.HTTP_VERSION_NOT_SUPPORTED_505;
import static org.eclipse.jetty.http.HttpStatus.INTERNAL_SERVER_ERROR_500;

public final class StrictHttpGenerator {
    public static final int CHUNK_SIZE = 12;

    private static final Logger LOG = LoggerFactory.getLogger(StrictHttpGenerator.class);

    private static final PreparedResponse[] PREPARED = PrepareUtil.prepare();
    private static final Index<Boolean> ASSUMED_CONTENT_METHODS = new Index.Builder<Boolean>()
            .caseSensitive(false)
            .with(HttpMethod.POST.asString(), Boolean.TRUE)
            .with(HttpMethod.PUT.asString(), Boolean.TRUE)
            .build();

    private static final byte[] ZERO_CHUNK = {(byte) '0', (byte) '\r', (byte) '\n'};
    private static final byte[] LAST_CHUNK = {(byte) '0', (byte) '\r', (byte) '\n', (byte) '\r', (byte) '\n'};
    private static final HttpField CONNECTION_KEEP_ALIVE = new PreEncodedHttpField(
            HttpHeader.CONNECTION, HttpHeaderValue.KEEP_ALIVE.asString()
    );
    private static final HttpField CONNECTION_CLOSE = new PreEncodedHttpField(
            HttpHeader.CONNECTION, HttpHeaderValue.CLOSE.asString()
    );
    private static final byte[] HTTP_1_0_SPACE = StringUtil.getBytes(HttpVersion.HTTP_1_0 + " ");
    private static final byte[] HTTP_1_1_SPACE = StringUtil.getBytes(HttpVersion.HTTP_1_1 + " ");
    private static final byte[] TRANSFER_ENCODING_CHUNKED = StringUtil.getBytes("Transfer-Encoding: chunked\r\n");

    private final HttpMessageBuffer buffer;
    private State state;
    private HttpTokens.EndOfContent endOfContent;
    private MetaData info;
    private long contentPrepared;
    private boolean noContentResponse;
    private Boolean persistent;
    private boolean needCRLF;
    private int maxHeaderBytes;

    public StrictHttpGenerator(HttpMessageBuffer buffer) {
        this.buffer = buffer;
        this.state = State.START;
        this.endOfContent = HttpTokens.EndOfContent.UNKNOWN_CONTENT;
        this.info = null;
        this.contentPrepared = 0;
        this.noContentResponse = false;
        this.persistent = null;
        this.needCRLF = false;
        this.maxHeaderBytes = 0;
    }

    public StrictHttpGenerator() {
        this(null);
    }

    private static void generateRequestLine(MetaData.Request request, ByteBuffer header) {
        header.put(StringUtil.getBytes(request.getMethod()));
        header.put((byte) ' ');
        header.put(StringUtil.getBytes(request.getHttpURI().toString()));
        header.put((byte) ' ');
        header.put(request.getHttpVersion().toBytes());
        header.put(Tokens.CRLF);
    }

    private static byte[] getReasonBytes(String reason) {
        if (reason.length() > 1024) {
            reason = reason.substring(0, 1024);
        }
        var bytes = StringUtil.getBytes(reason);
        // Originally, here is for (var i = bytes.length; i-- > 0; )
        for (var i = bytes.length - 1; i >= 0; --i) {
            if (bytes[i] == '\r' || bytes[i] == '\n') {
                bytes[i] = '?';
            }
        }
        return bytes;
    }

    private String lookupMessage(int code) {
        if (buffer == null) {
            return null;
        }
        return buffer.get(code);
    }

    private void generateResponseLine(MetaData.Response response, ByteBuffer header) {
        // Look for prepared response line
        var version = response.getHttpVersion();
        var status = response.getStatus();
        var offset = status - 100;
        var prepared = offset < PREPARED.length ? PREPARED[offset] : null;
        var reason = response.getReason();
        if (prepared != null) {
            if (reason == null || prepared.reason.equals(reason)) {
                header.put(prepared.line);
                // Check for http/1.0 and patch it
                // Magic constant is 7: H[0]T[1]T[2]P[3]/[4]1[5].[6]<HERE>[7]
                if (version == HttpVersion.HTTP_1_0) {
                    header.put(7, (byte) '0');
                }
                return;
            }
            header.put(prepared.scheme);
            header.put(getReasonBytes(reason));
            header.put(Tokens.CRLF);
            return;
        }
        // Or generate response line
        header.put(version == HttpVersion.HTTP_1_0 ? HTTP_1_0_SPACE : HTTP_1_1_SPACE);
        header.put((byte) ('0' + status / 100));
        header.put((byte) ('0' + (status % 100) / 10));
        header.put((byte) ('0' + (status % 10)));
        header.put((byte) ' ');
        // Trying lookup reason
        if (reason == null) {
            reason = lookupMessage(status);
        }
        // If reason not found, replace it with string code repr
        if (reason == null) {
            header.put((byte) ('0' + status / 100));
            header.put((byte) ('0' + (status % 100) / 10));
            header.put((byte) ('0' + (status % 10)));
        } else {
            header.put(getReasonBytes(reason));
        }
        header.put(Tokens.CRLF);
    }

    public void reset() {
        state = State.START;
        info = null;
        endOfContent = HttpTokens.EndOfContent.UNKNOWN_CONTENT;
        noContentResponse = false;
        persistent = null;
        contentPrepared = 0;
        needCRLF = false;
    }

    public int getMaxHeaderBytes() {
        return maxHeaderBytes;
    }

    public void setMaxHeaderBytes(int maxHeaderBytes) {
        this.maxHeaderBytes = maxHeaderBytes;
    }

    public State getState() {
        return state;
    }

    public boolean isState(State state) {
        return this.state == state;
    }

    public boolean isIdle() {
        return state == State.START;
    }

    public boolean isEnd() {
        return state == State.END;
    }

    public boolean isCommitted() {
        return state.ordinal() >= State.COMMITTED.ordinal();
    }

    public boolean isChunking() {
        return endOfContent == HttpTokens.EndOfContent.CHUNKED_CONTENT;
    }

    public boolean isNoContent() {
        return noContentResponse;
    }

    /**
     * @return true if known to be persistent
     */
    public boolean isPersistent() {
        return persistent == Boolean.TRUE;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * @return true if known to be persistent
     */
    public boolean isPersistent(HttpVersion version) {
        // return version == HttpVersion.HTTP_1_1 ?
        // !Boolean.FALSE.equals(_persistent)
        // : Boolean.TRUE.equals(_persistent);
        return version == HttpVersion.HTTP_1_1 ? persistent != Boolean.FALSE : persistent == Boolean.TRUE;
    }

    public boolean isWritten() {
        return contentPrepared > 0;
    }

    public long getContentPrepared() {
        return contentPrepared;
    }

    public void abort() {
        persistent = false;
        state = State.END;
        endOfContent = null;
    }

    public void servletUpgrade() {
        noContentResponse = false;
        state = State.COMMITTED;
    }

    private void prepareChunk(ByteBuffer chunk, int remaining) {
        // if we need CRLF add this to header
        if (needCRLF) {
            BufferUtil.putCRLF(chunk);
        }
        // Add the chunk size to the header
        if (remaining > 0) {
            BufferUtil.putHexInt(chunk, remaining);
            BufferUtil.putCRLF(chunk);
            needCRLF = true;
        } else {
            chunk.put(LAST_CHUNK);
            needCRLF = false;
        }
    }

    private void generateTrailers(ByteBuffer buffer, HttpFields trailer) {
        // if we need CRLF add this to header
        if (needCRLF) {
            BufferUtil.putCRLF(buffer);
        }
        // Add the chunk size to the header
        buffer.put(ZERO_CHUNK);
        for (var field : trailer) {
            Util.putTo(field, buffer);
        }
        BufferUtil.putCRLF(buffer);
    }

    private void checkMaxHeaderBytes(ByteBuffer header) {
        if (maxHeaderBytes > 0 && header.position() > maxHeaderBytes) {
            throw new BufferOverflowException();
        }
    }

    private void generateHeaders(ByteBuffer header, ByteBuffer content, boolean last) {
        final var request = (info instanceof MetaData.Request) ? (MetaData.Request) info : null;
        final var response = (info instanceof MetaData.Response) ? (MetaData.Response) info : null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateHeaders {} last={} content={}", info, last, BufferUtil.toDetailString(content));
            LOG.debug(info.getHttpFields().toString());
        }
        // default field values
        HttpField transferEncoding = null;
        HttpField connection = null;
        var version = info.getHttpVersion();
        var http10 = version == HttpVersion.HTTP_1_0;
        var http11 = version == HttpVersion.HTTP_1_1;
        var connectionClose = false;
        var connectionKeepAlive = false;
        var connectionUpgrade = false;
        var chunkedHint = info.getTrailersSupplier() != null;
        var contentType = false;
        var contentLength = info.getContentLength();
        var contentLengthField = false;
        // Generate fields
        var fields = info.getHttpFields();
        if (fields != null) {
            for (var field : fields) {
                var h = field.getHeader();
                if (h == null) {
                    Util.putTo(field, header);
                    checkMaxHeaderBytes(header);
                    continue;
                }
                switch (h) {
                    case CONTENT_LENGTH -> {
                        if (contentLength < 0) {
                            contentLength = field.getLongValue();
                        } else if (contentLength != field.getLongValue()) {
                            throw new HttpException.RuntimeException(
                                    INTERNAL_SERVER_ERROR_500,
                                    String.format("Incorrect Content-Length %d!=%d", contentLength, field.getLongValue())
                            );
                        }
                        contentLengthField = true;
                    }
                    case CONTENT_TYPE -> {
                        // write the field to the header
                        contentType = true;
                        Util.putTo(field, header);
                    }
                    case TRANSFER_ENCODING -> {
                        if (http11) {
                            // Don't add yet, treat this only as a hint that there is content
                            // with a preference to chunk if we can
                            if (transferEncoding == null) {
                                transferEncoding = field;
                            } else {
                                transferEncoding = transferEncoding.withValues(field.getValues());
                            }
                            chunkedHint |= field.contains(HttpHeaderValue.CHUNKED.asString());
                        }
                    }
                    case CONNECTION -> {
                        // Save to connection field for processing when all other fields are known
                        if (connection == null) {
                            connection = field;
                        } else {
                            connection = connection.withValues(field.getValues());
                        }
                        connectionClose |= field.contains(HttpHeaderValue.CLOSE.asString());
                        connectionKeepAlive |= field.contains(HttpHeaderValue.KEEP_ALIVE.asString());
                        connectionUpgrade |= field.contains(HttpHeaderValue.UPGRADE.asString());
                    }
                    default -> Util.putTo(field, header);
                }
                checkMaxHeaderBytes(header);
            }
        }

        // Can we work out the content length?
        if (last && contentLength < 0 && info.getTrailersSupplier() == null) {
            contentLength = contentPrepared + BufferUtil.length(content);
        }

        // Calculate how to end _content and connection, _content length and transfer encoding
        // settings from http://tools.ietf.org/html/rfc7230#section-3.3.3

        var assumedContentRequest = request != null && ASSUMED_CONTENT_METHODS.get(request.getMethod()) != null;
        var assumedContent = assumedContentRequest || contentType || chunkedHint;
        var noContentRequest = request != null && contentLength <= 0 && !assumedContent;

        // Handle CONNECT requests.
        if (request != null && HttpMethod.CONNECT.is(request.getMethod())) {
            persistent = true;
            if (http10 && !connectionKeepAlive) {
                connectionKeepAlive = true;
            }
            if (connectionClose) {
                connectionClose = false;
            }
        }
        // Handle Upgrade responses.
        if (request != null && connectionUpgrade) {
            persistent = true;
            if (connectionClose) {
                connection = connection.withoutValue(HttpHeaderValue.CLOSE.asString());
                connectionClose = false;
            }
        }

        // Handle persistence and adjust connection header if necessary.
        if (http11) {
            // Don't use keepAlive
            if (connectionKeepAlive) {
                connection = connection.withoutValue(HttpHeaderValue.KEEP_ALIVE.asString());
                connectionKeepAlive = false;
            }
            if (persistent == null) {
                // Default to persistent unless explicitly closed
                persistent = !connectionClose;
            } else if (persistent) {
                if (connectionClose) {
                    persistent = false;
                }
            } else if (!connectionClose) {
                if (connection == null) connection = CONNECTION_CLOSE;
                else connection = connection.withValue(HttpHeaderValue.CLOSE.asString());
                connectionClose = true;
            }
        } else if (http10) {
            if (persistent == null) {
                // If persistence has not been set,
                // then it must be explicitly requested with keep-alive, or a connect request
                if (connectionClose) {
                    persistent = false;
                    if (connectionKeepAlive) {
                        connection = connection.withoutValue(HttpHeaderValue.KEEP_ALIVE.asString());
                        connectionKeepAlive = false;
                    }
                } else {
                    persistent = connectionKeepAlive;
                }
            } else if (persistent) {
                if (connectionClose) {
                    persistent = false;
                    if (connectionKeepAlive) {
                        connection = connection.withoutValue(HttpHeaderValue.KEEP_ALIVE.asString());
                        connectionKeepAlive = false;
                    }
                } else if (!connectionKeepAlive) {
                    if (connection == null) connection = CONNECTION_KEEP_ALIVE;
                    else {
                        connection = connection.withValue(HttpHeaderValue.KEEP_ALIVE.asString());
                    }
                    connectionKeepAlive = true;
                }
            } else if (connectionKeepAlive) {
                connection = connection.withoutValue(HttpHeaderValue.KEEP_ALIVE.asString());
                connectionKeepAlive = false;
            }
        } else {
            persistent = false;
        }

        // Work out how the message will be framed:

        // If the message is known not to have content
        if (noContentResponse || noContentRequest) {
            // We don't need to indicate a body length
            endOfContent = HttpTokens.EndOfContent.NO_CONTENT;

            // But it is an error if there actually is content
            if (contentPrepared > 0) {
                throw new HttpException.RuntimeException(INTERNAL_SERVER_ERROR_500, "Content for no content response");
            }

            if (contentLengthField) {
                if (response != null && response.getStatus() == HttpStatus.NOT_MODIFIED_304) {
                    Util.putContentLength(header, contentLength);
                } else if (contentLength > 0) {
                    if (contentPrepared == 0 && last) {
                        content.clear();
                    } else {
                        throw new HttpException.RuntimeException(
                                INTERNAL_SERVER_ERROR_500,
                                "Content for no content response"
                        );
                    }
                }
            }
        }
        // Else if we are HTTP/1.1 and the content length is unknown and we are either persistent
        // or it is a request with content (which cannot EOF) or the app has requested chunking
        else if (http11 && (chunkedHint || contentLength < 0 && (persistent || assumedContentRequest))) {
            // we use chunking
            endOfContent = HttpTokens.EndOfContent.CHUNKED_CONTENT;

            // try to use user supplied encoding as it may have other values.
            if (transferEncoding == null) {
                header.put(TRANSFER_ENCODING_CHUNKED);
            } else if (transferEncoding.toString().endsWith(HttpHeaderValue.CHUNKED.toString())) {
                Util.putTo(transferEncoding, header);
                transferEncoding = null;
            } else if (!chunkedHint) {
                Util.putTo(
                        new HttpField(HttpHeader.TRANSFER_ENCODING, transferEncoding.getValue() + ",chunked"),
                        header
                );
                transferEncoding = null;
            } else {
                throw new HttpException.RuntimeException(INTERNAL_SERVER_ERROR_500, "Bad Transfer-Encoding");
            }
        }
        // Else if we know the content length and are a request or a persistent response,
        else if (contentLength >= 0 && (request != null || persistent)) {
            // Use the content length
            endOfContent = HttpTokens.EndOfContent.CONTENT_LENGTH;
            Util.putContentLength(header, contentLength);
        }
        // Else if we are a response
        else if (response != null) {
            // We must use EOF - even if we were trying to be persistent
            endOfContent = HttpTokens.EndOfContent.EOF_CONTENT;
            persistent = false;
            if (contentLength >= 0 && (contentLength > 0 || assumedContent || contentLengthField)) {
                Util.putContentLength(header, contentLength);
            }
            if (http11) {
                if (!connectionClose) {
                    if (connection == null) {
                        connection = CONNECTION_CLOSE;
                    } else {
                        connection = connection.withValue(HttpHeaderValue.CLOSE.asString());
                    }
                    // connectionClose = true;
                }
            } else if (http10) {
                if (connectionKeepAlive) {
                    connection = connection.withoutValue(HttpHeaderValue.KEEP_ALIVE.asString());
                    // connectionKeepAlive = false;
                }
            }
        }
        // Else we must be a request
        else {
            // with no way to indicate body length
            throw new HttpException.RuntimeException(INTERNAL_SERVER_ERROR_500, "Unknown content length for request");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("endOfContent {} content-Length {}", endOfContent.toString(), contentLength);
        }

        // Add the connection header if we have one
        if (connection != null) {
            Util.putTo(connection, header);
        }

        // Add transfer encoding if it is not chunking
        if (transferEncoding != null) {
            if (chunkedHint) {
                var v = transferEncoding.getValue();
                var c = v.lastIndexOf(',');
                if (c > 0 && v.lastIndexOf(HttpHeaderValue.CHUNKED.toString(), c) > c) {
                    Util.putTo(new HttpField(HttpHeader.TRANSFER_ENCODING, v.substring(0, c).trim()), header);
                }
            } else {
                Util.putTo(transferEncoding, header);
            }
        }
        // end the header.
        header.put(Tokens.CRLF);
        checkMaxHeaderBytes(header);
    }

    private Result committed(ByteBuffer chunk, ByteBuffer content, boolean last) {
        var length = BufferUtil.length(content);
        // handle the content.
        if (length > 0) {
            if (isChunking()) {
                if (chunk == null) {
                    return Result.NEED_CHUNK;
                }
                BufferUtil.clearToFill(chunk);
                prepareChunk(chunk, length);
                BufferUtil.flipToFlush(chunk, 0);
            }
            contentPrepared += length;
        }
        if (last) {
            state = State.COMPLETING;
            return length > 0 ? Result.FLUSH : Result.CONTINUE;
        }
        return length > 0 ? Result.FLUSH : Result.DONE;
    }

    private Result completing(ByteBuffer chunk, ByteBuffer content) {
        if (BufferUtil.hasContent(content)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("discarding content in COMPLETING");
            }
            BufferUtil.clear(content);
        }
        if (isChunking()) {
            var trailersSupplier = info.getTrailersSupplier();
            if (trailersSupplier != null) {
                // Do we need a chunk buffer?
                if (chunk == null || chunk.capacity() <= CHUNK_SIZE) {
                    return Result.NEED_CHUNK_TRAILER;
                }
                var trailers = trailersSupplier.get();
                if (trailers != null) {
                    // Write the last chunk
                    BufferUtil.clearToFill(chunk);
                    generateTrailers(chunk, trailers);
                    BufferUtil.flipToFlush(chunk, 0);
                    endOfContent = HttpTokens.EndOfContent.UNKNOWN_CONTENT;
                    return Result.FLUSH;
                }
            }
            // Do we need a chunk buffer?
            if (chunk == null) {
                return Result.NEED_CHUNK;
            }
            // Write the last chunk
            BufferUtil.clearToFill(chunk);
            prepareChunk(chunk, 0);
            BufferUtil.flipToFlush(chunk, 0);
            endOfContent = HttpTokens.EndOfContent.UNKNOWN_CONTENT;
            return Result.FLUSH;
        }
        state = State.END;
        return persistent == Boolean.TRUE ? Result.DONE : Result.SHUTDOWN_OUT;
    }

    public Result generateRequest(MetaData.Request info, ByteBuffer header, ByteBuffer chunk, ByteBuffer content, boolean last) {
        switch (state) {
            case START: {
                if (info == null) {
                    return Result.NEED_INFO;
                }
                this.info = info;
                if (header == null) {
                    return Result.NEED_HEADER;
                }
                // prepare the header
                var pos = BufferUtil.flipToFill(header);
                try {
                    // generate request line
                    generateRequestLine(info, header);
                    if (info.getHttpVersion() == HttpVersion.HTTP_0_9) {
                        throw new HttpException.RuntimeException(HTTP_VERSION_NOT_SUPPORTED_505, "HTTP/0.9 not supported");
                    }
                    generateHeaders(header, content, last);
                    var expect100 = info.getHttpFields().contains(HttpHeader.EXPECT, HttpHeaderValue.CONTINUE.asString());
                    if (expect100) {
                        state = State.COMMITTED;
                    } else {
                        // handle the content.
                        var len = BufferUtil.length(content);
                        if (len > 0) {
                            contentPrepared += len;
                            if (isChunking()) prepareChunk(header, len);
                        }
                        state = last ? State.COMPLETING : State.COMMITTED;
                    }
                    return Result.FLUSH;
                } catch (BufferOverflowException e) {
                    LOG.trace("IGNORED", e);
                    return Result.HEADER_OVERFLOW;
                } catch (Exception e) {
                    if (e instanceof HttpException) {
                        throw e;
                    }
                    throw new HttpException.RuntimeException(INTERNAL_SERVER_ERROR_500, e.getMessage(), e);
                } finally {
                    BufferUtil.flipToFlush(header, pos);
                }
            }

            case COMMITTED: {
                return committed(chunk, content, last);
            }

            case COMPLETING: {
                return completing(chunk, content);
            }

            case END:
                if (BufferUtil.hasContent(content)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("discarding content in COMPLETING");
                    }
                    BufferUtil.clear(content);
                }
                return Result.DONE;

            default:
                throw new IllegalStateException();
        }
    }

    public Result generateResponse(MetaData.Response info, boolean head, ByteBuffer header, ByteBuffer chunk, ByteBuffer content, boolean last) {
        switch (state) {
            case START: {
                if (info == null) {
                    return Result.NEED_INFO;
                }
                this.info = info;
                var version = info.getHttpVersion();
                if (version == null) {
                    throw new HttpException.RuntimeException(INTERNAL_SERVER_ERROR_500, "No version");
                }
                if (version == HttpVersion.HTTP_0_9) {
                    persistent = false;
                    endOfContent = HttpTokens.EndOfContent.EOF_CONTENT;
                    if (BufferUtil.hasContent(content)) {
                        contentPrepared += content.remaining();
                    }
                    state = last ? State.COMPLETING : State.COMMITTED;
                    return Result.FLUSH;
                }

                // Do we need a response header
                if (header == null) {
                    return Result.NEED_HEADER;
                }

                // prepare the header
                var pos = BufferUtil.flipToFill(header);
                try {
                    // generate ResponseLine
                    generateResponseLine(info, header);
                    // Handle 1xx and no content responses
                    var status = info.getStatus();
                    if (HttpStatus.isInformational(status)) {
                        noContentResponse = true;
                        switch (status) {
                            case HttpStatus.SWITCHING_PROTOCOLS_101:
                                break;
                            case HttpStatus.EARLY_HINTS_103:
                                generateHeaders(header, content, last);
                                state = State.COMPLETING_1XX;
                                return Result.FLUSH;
                            default:
                                header.put(Tokens.CRLF);
                                state = State.COMPLETING_1XX;
                                return Result.FLUSH;
                        }
                    } else if (status == HttpStatus.NO_CONTENT_204 || status == HttpStatus.NOT_MODIFIED_304) {
                        noContentResponse = true;
                    }
                    generateHeaders(header, content, last);
                    // handle the content.
                    var len = BufferUtil.length(content);
                    if (len > 0) {
                        contentPrepared += len;
                        if (isChunking() && !head) {
                            prepareChunk(header, len);
                        }
                    }
                    state = last ? State.COMPLETING : State.COMMITTED;
                } catch (BufferOverflowException e) {
                    LOG.trace("IGNORED", e);
                    return Result.HEADER_OVERFLOW;
                } catch (Exception e) {
                    if (e instanceof HttpException) {
                        throw e;
                    }
                    throw new HttpException.RuntimeException(INTERNAL_SERVER_ERROR_500, e.getMessage(), e);
                } finally {
                    BufferUtil.flipToFlush(header, pos);
                }

                return Result.FLUSH;
            }

            case COMMITTED: {
                return committed(chunk, content, last);
            }

            case COMPLETING_1XX: {
                reset();
                return Result.DONE;
            }

            case COMPLETING: {
                return completing(chunk, content);
            }

            case END:
                if (BufferUtil.hasContent(content)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("discarding content in COMPLETING");
                    }
                    BufferUtil.clear(content);
                }
                return Result.DONE;

            default:
                throw new IllegalStateException();
        }
    }

    public String toString() {
        return String.format("%s@%x{s=%s}", getClass().getSimpleName(), hashCode(), state);
    }

    public enum State {
        START, COMMITTED, COMPLETING, COMPLETING_1XX, END
    }

    public enum Result {
        NEED_CHUNK,             // Need a small chunk buffer of CHUNK_SIZE
        NEED_INFO,              // Need the request/response metadata info
        NEED_HEADER,            // Need a buffer to build HTTP headers into
        HEADER_OVERFLOW,        // The header buffer overflowed
        NEED_CHUNK_TRAILER,     // Need a large chunk buffer for last chunk and trailers
        FLUSH,                  // The buffers previously generated should be flushed
        CONTINUE,               // Continue generating the message
        SHUTDOWN_OUT,           // Need EOF to be signaled
        DONE                    // The current phase of generation is complete
    }
}
