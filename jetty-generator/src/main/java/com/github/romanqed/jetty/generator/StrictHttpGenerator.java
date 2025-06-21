package com.github.romanqed.jetty.generator;

import org.eclipse.jetty.http.*;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Index;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.jetty.http.HttpStatus.HTTP_VERSION_NOT_SUPPORTED_505;
import static org.eclipse.jetty.http.HttpStatus.INTERNAL_SERVER_ERROR_500;

public final class StrictHttpGenerator extends HttpGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(StrictHttpGenerator.class);
    private static final byte[] HTTP_1_1_SPACE = Util.getConstant("HTTP_1_1_SPACE");
    private static final byte[] HTTP_1_0_SPACE = StringUtil.getBytes(HttpVersion.HTTP_1_0 + " ");
    private static final PreparedResponse[] PREPARED = Util.getPreparedResponsesSafely();
    private static final Index<Boolean> ASSUMED_CONTENT_METHODS = Util.getConstant("ASSUMED_CONTENT_METHODS");

    private static final byte[] CONTENT_LENGTH_0 = Util.getConstant("CONTENT_LENGTH_0");
    private static final byte[] TRANSFER_ENCODING_CHUNKED = Util.getConstant("TRANSFER_ENCODING_CHUNKED");
    private static final byte[] CONNECTION_CLOSE = Util.getConstant("CONNECTION_CLOSE");
    private static final byte[] ZERO_CHUNK = Util.getConstant("ZERO_CHUNK");
    private static final byte[] LAST_CHUNK = Util.getConstant("LAST_CHUNK");

    private static final byte LINE_FEED = 0x0A;
    private static final byte CARRIAGE_RETURN = 0x0D;
    private static final byte[] CRLF = {CARRIAGE_RETURN, LINE_FEED};
    private static final int SEND_SERVER = 0x01;
    private static final int SEND_POWERED_BY = 0x02;

    private static final byte[][] SEND = prepareVersionHeaders(HttpConfiguration.SERVER_VERSION);

    private final int send;
    private State state = State.START;
    private MetaData info;
    private Boolean persistent = null;
    private boolean noContentResponse = false;
    private long contentPrepared = 0;
    private HttpTokens.EndOfContent endOfContent = HttpTokens.EndOfContent.UNKNOWN_CONTENT;
    private boolean needCRLF = false;

    public StrictHttpGenerator(boolean sendServerVersion, boolean sendXPoweredBy) {
        this.send = (sendServerVersion ? SEND_SERVER : 0) | (sendXPoweredBy ? SEND_POWERED_BY : 0);
    }

    public StrictHttpGenerator() {
        this.send = 0;
    }

    private static byte[][] prepareVersionHeaders(String version) {
        var ret = new byte[][]{new byte[0], null, null, null};
        var server = "Server: " + version + "\r\n";
        var poweredBy = "X-Powered-By: " + version + "\r\n";
        ret[SEND_SERVER] = StringUtil.getBytes(server);
        ret[SEND_POWERED_BY] = StringUtil.getBytes(poweredBy);
        ret[SEND_SERVER | SEND_POWERED_BY] = StringUtil.getBytes(server + poweredBy);
        return ret;
    }

    private static void generateRequestLine(MetaData.Request request, ByteBuffer header) {
        header.put(StringUtil.getBytes(request.getMethod()));
        header.put((byte) ' ');
        header.put(StringUtil.getBytes(request.getURIString()));
        header.put((byte) ' ');
        header.put(request.getHttpVersion().toBytes());
        header.put(CRLF);
    }

    private static void putContentLength(ByteBuffer header, long contentLength) {
        if (contentLength == 0) {
            header.put(CONTENT_LENGTH_0);
            return;
        }
        header.put(HttpHeader.CONTENT_LENGTH.getBytesColonSpace());
        BufferUtil.putDecLong(header, contentLength);
        header.put(CRLF);
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

    @Override
    public void reset() {
        state = State.START;
        info = null;
        endOfContent = HttpTokens.EndOfContent.UNKNOWN_CONTENT;
        noContentResponse = false;
        persistent = null;
        contentPrepared = 0;
        needCRLF = false;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public boolean isState(State state) {
        return this.state == state;
    }

    @Override
    public boolean isIdle() {
        return state == State.START;
    }

    @Override
    public boolean isEnd() {
        return state == State.END;
    }

    @Override
    public boolean isCommitted() {
        return state.ordinal() >= State.COMMITTED.ordinal();
    }

    @Override
    public boolean isChunking() {
        return endOfContent == HttpTokens.EndOfContent.CHUNKED_CONTENT;
    }

    @Override
    public boolean isNoContent() {
        return noContentResponse;
    }

    @Override
    public boolean isPersistent() {
        return persistent == Boolean.TRUE;
    }

    @Override
    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    @Override
    public boolean isWritten() {
        return contentPrepared > 0;
    }

    @Override
    public long getContentPrepared() {
        return contentPrepared;
    }

    @Override
    public void abort() {
        persistent = false;
        state = State.END;
        endOfContent = null;
    }

    @Override
    public void servletUpgrade() {
        noContentResponse = false;
        state = State.COMMITTED;
    }

    private void generateHeaders(ByteBuffer header, ByteBuffer content, boolean last) {
        final var request = (info instanceof MetaData.Request) ? (MetaData.Request) info : null;
        final var response = (info instanceof MetaData.Response) ? (MetaData.Response) info : null;

        if (LOG.isDebugEnabled()) {
            LOG.debug("generateHeaders {} last={} content={}", info, last, BufferUtil.toDetailString(content));
            LOG.debug(info.getFields().toString());
        }

        // default field values
        var send = this.send;
        HttpField transferEncoding = null;
        var http11 = info.getHttpVersion() == HttpVersion.HTTP_1_1;
        var close = false;
        var chunkedHint = info.getTrailerSupplier() != null;
        var contentType = false;
        var contentLength = info.getContentLength();
        var contentLengthField = false;

        // Generate fields
        var fields = info.getFields();
        if (fields != null) {
            for (var field : fields) {
                var h = field.getHeader();
                if (h == null) {
                    putTo(field, header);
                    continue;
                }
                switch (h) {
                    case CONTENT_LENGTH:
                        if (contentLength < 0) {
                            contentLength = field.getLongValue();
                        } else if (contentLength != field.getLongValue()) {
                            throw new BadMessageException(
                                    INTERNAL_SERVER_ERROR_500,
                                    String.format("Incorrect Content-Length %d!=%d", contentLength, field.getLongValue())
                            );
                        }
                        contentLengthField = true;
                        break;

                    case CONTENT_TYPE: {
                        // write the field to the header
                        contentType = true;
                        putTo(field, header);
                        break;
                    }

                    case TRANSFER_ENCODING: {
                        if (http11) {
                            // Don't add yet, treat this only as a hint that there is content
                            // with a preference to chunk if we can
                            transferEncoding = field;
                            chunkedHint = field.contains(HttpHeaderValue.CHUNKED.asString());
                        }
                        break;
                    }

                    case CONNECTION: {
                        var keepAlive = field.contains(HttpHeaderValue.KEEP_ALIVE.asString());
                        if (keepAlive && info.getHttpVersion() == HttpVersion.HTTP_1_0 && persistent == null) {
                            persistent = true;
                        }
                        if (field.contains(HttpHeaderValue.CLOSE.asString())) {
                            close = true;
                            persistent = false;
                        }
                        if (keepAlive && persistent == Boolean.FALSE) {
                            field = new HttpField(
                                    HttpHeader.CONNECTION,
                                    Stream.of(field.getValues())
                                            .filter(s -> !HttpHeaderValue.KEEP_ALIVE.is(s))
                                            .collect(Collectors.joining(", "))
                            );
                        }
                        putTo(field, header);
                        break;
                    }

                    case SERVER: {
                        send &= ~SEND_SERVER;
                        putTo(field, header);
                        break;
                    }

                    default:
                        putTo(field, header);
                }
            }
        }

        // Can we work out the content length?
        if (last && contentLength < 0 && info.getTrailerSupplier() == null) {
            contentLength = contentPrepared + BufferUtil.length(content);
        }

        // Calculate how to end _content and connection, _content length and transfer encoding
        // settings from http://tools.ietf.org/html/rfc7230#section-3.3.3
        var assumedContentRequest = request != null && ASSUMED_CONTENT_METHODS.get(request.getMethod()) != null;
        var assumedContent = assumedContentRequest || contentType || chunkedHint;
        var noContentRequest = request != null && contentLength <= 0 && !assumedContent;

        if (persistent == null) {
            persistent = http11 || (request != null && HttpMethod.CONNECT.is(request.getMethod()));
        }

        // If the message is known not to have content
        if (noContentResponse || noContentRequest) {
            // We don't need to indicate a body length
            endOfContent = HttpTokens.EndOfContent.NO_CONTENT;

            // But it is an error if there actually is content
            if (contentPrepared > 0) {
                throw new BadMessageException(INTERNAL_SERVER_ERROR_500, "Content for no content response");
            }

            if (contentLengthField) {
                if (response != null && response.getStatus() == HttpStatus.NOT_MODIFIED_304) {
                    putContentLength(header, contentLength);
                } else if (contentLength > 0) {
                    if (contentPrepared == 0 && last) {
                        content.clear();
                    } else {
                        throw new BadMessageException(INTERNAL_SERVER_ERROR_500, "Content for no content response");
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
                putTo(transferEncoding, header);
                transferEncoding = null;
            } else if (!chunkedHint) {
                putTo(new HttpField(HttpHeader.TRANSFER_ENCODING, transferEncoding.getValue() + ",chunked"), header);
                transferEncoding = null;
            } else {
                throw new BadMessageException(INTERNAL_SERVER_ERROR_500, "Bad Transfer-Encoding");
            }
        }
        // Else if we known the content length and are a request or a persistent response,
        else if (contentLength >= 0 && (request != null || persistent)) {
            // Use the content length
            endOfContent = HttpTokens.EndOfContent.CONTENT_LENGTH;
            putContentLength(header, contentLength);
        }
        // Else if we are a response
        else if (response != null) {
            // We must use EOF - even if we were trying to be persistent
            endOfContent = HttpTokens.EndOfContent.EOF_CONTENT;
            persistent = false;
            if (contentLength >= 0 && (contentLength > 0 || assumedContent || contentLengthField)) {
                putContentLength(header, contentLength);
            }
            if (http11 && !close) {
                header.put(CONNECTION_CLOSE);
            }
        }
        // Else we must be a request
        else {
            // with no way to indicate body length
            throw new BadMessageException(INTERNAL_SERVER_ERROR_500, "Unknown content length for request");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(endOfContent.toString());
        }

        // Add transfer encoding if it is not chunking
        if (transferEncoding != null) {
            if (chunkedHint) {
                var v = transferEncoding.getValue();
                var c = v.lastIndexOf(',');
                if (c > 0 && v.lastIndexOf(HttpHeaderValue.CHUNKED.toString(), c) > c) {
                    putTo(new HttpField(HttpHeader.TRANSFER_ENCODING, v.substring(0, c).trim()), header);
                }
            } else {
                putTo(transferEncoding, header);
            }
        }

        // Send server?
        var status = response != null ? response.getStatus() : -1;
        if (status > 199) {
            header.put(SEND[send]);
        }

        // end the header.
        header.put(CRLF);
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

    private Result committed(ByteBuffer chunk, ByteBuffer content, boolean last) {
        var len = BufferUtil.length(content);

        // handle the content.
        if (len > 0) {
            if (isChunking()) {
                if (chunk == null)
                    return Result.NEED_CHUNK;
                BufferUtil.clearToFill(chunk);
                prepareChunk(chunk, len);
                BufferUtil.flipToFlush(chunk, 0);
            }
            contentPrepared += len;
        }

        if (last) {
            state = State.COMPLETING;
            return len > 0 ? Result.FLUSH : Result.CONTINUE;
        }
        return len > 0 ? Result.FLUSH : Result.DONE;
    }

    private void generateTrailers(ByteBuffer buffer, HttpFields trailer) {
        // if we need CRLF add this to header
        if (needCRLF) {
            BufferUtil.putCRLF(buffer);
        }

        // Add the chunk size to the header
        buffer.put(ZERO_CHUNK);

        for (var field : trailer) {
            putTo(field, buffer);
        }

        BufferUtil.putCRLF(buffer);
    }

    private Result completing(ByteBuffer chunk, ByteBuffer content) {
        if (BufferUtil.hasContent(content)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("discarding content in COMPLETING");
            }
            BufferUtil.clear(content);
        }

        if (isChunking()) {
            if (info.getTrailerSupplier() != null) {
                // Do we need a chunk buffer?
                if (chunk == null || chunk.capacity() <= CHUNK_SIZE) {
                    return Result.NEED_CHUNK_TRAILER;
                }

                var trailers = info.getTrailerSupplier().get();

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

    @Override
    public Result generateRequest(MetaData.Request info,
                                  ByteBuffer header,
                                  ByteBuffer chunk,
                                  ByteBuffer content,
                                  boolean last) {
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
                    // generate ResponseLine
                    generateRequestLine(info, header);

                    if (info.getHttpVersion() == HttpVersion.HTTP_0_9) {
                        throw new BadMessageException(HTTP_VERSION_NOT_SUPPORTED_505, "HTTP/0.9 not supported");
                    }

                    generateHeaders(header, content, last);

                    var expect100 = info.getFields().contains(HttpHeader.EXPECT, HttpHeaderValue.CONTINUE.asString());

                    if (expect100) {
                        state = State.COMMITTED;
                    } else {
                        // handle the content.
                        var len = BufferUtil.length(content);
                        if (len > 0) {
                            contentPrepared += len;
                            if (isChunking()) {
                                prepareChunk(header, len);
                            }
                        }
                        state = last ? State.COMPLETING : State.COMMITTED;
                    }

                    return Result.FLUSH;
                } catch (BadMessageException e) {
                    throw e;
                } catch (BufferOverflowException e) {
                    LOG.trace("IGNORED", e);
                    return Result.HEADER_OVERFLOW;
                } catch (Exception e) {
                    throw new BadMessageException(INTERNAL_SERVER_ERROR_500, e.getMessage(), e);
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
                        LOG.debug("discarding content in END");
                    }
                    BufferUtil.clear(content);
                }
                return Result.DONE;

            default:
                throw new IllegalStateException();
        }
    }

    private static void generateResponseLine(MetaData.Response response, ByteBuffer header) {
        // Look for prepared response line
        var version = response.getHttpVersion();
        var status = response.getStatus();
        var reason = response.getReason();
        var offset = status - 100;
        var prepared = offset < PREPARED.length ? PREPARED[offset] : null;
        if (prepared != null) {
            if (reason == null) {
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
            header.put(CRLF);
            return;
        }
        // Or generate response line
        header.put(version == HttpVersion.HTTP_1_0 ? HTTP_1_0_SPACE : HTTP_1_1_SPACE);
        header.put((byte) ('0' + status / 100));
        header.put((byte) ('0' + (status % 100) / 10));
        header.put((byte) ('0' + (status % 10)));
        header.put((byte) ' ');
        if (reason == null) {
            header.put((byte) ('0' + status / 100));
            header.put((byte) ('0' + (status % 100) / 10));
            header.put((byte) ('0' + (status % 10)));
        } else {
            header.put(getReasonBytes(reason));
        }
        header.put(CRLF);
    }

    @Override
    public Result generateResponse(MetaData.Response info,
                                   boolean head,
                                   ByteBuffer header,
                                   ByteBuffer chunk,
                                   ByteBuffer content,
                                   boolean last) {
        switch (state) {
            case START: {
                if (info == null) {
                    return Result.NEED_INFO;
                }
                this.info = info;

                var version = info.getHttpVersion();
                if (version == null) {
                    throw new BadMessageException(INTERNAL_SERVER_ERROR_500, "No version");
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
                int pos = BufferUtil.flipToFill(header);
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
                            case HttpStatus.EARLY_HINT_103:
                                generateHeaders(header, content, last);
                                state = State.COMPLETING_1XX;
                                return Result.FLUSH;
                            default:
                                header.put(CRLF);
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
                } catch (BadMessageException e) {
                    throw e;
                } catch (BufferOverflowException e) {
                    LOG.trace("IGNORED", e);
                    return Result.HEADER_OVERFLOW;
                } catch (Exception e) {
                    throw new BadMessageException(INTERNAL_SERVER_ERROR_500, e.getMessage(), e);
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
                        LOG.debug("discarding content in END");
                    }
                    BufferUtil.clear(content);
                }
                return Result.DONE;

            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return String.format("%s@%x{s=%s}", getClass().getSimpleName(), hashCode(), state);
    }
}
