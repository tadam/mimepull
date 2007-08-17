package org.jvnet.mimepull;

import java.io.InputStream;
import java.io.IOException;

/**
 * Constructs a InputStream from a linked list of {@link Chunk}s.
 * 
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
final class ChunkInputStream extends InputStream {
    Chunk current;
    int offset;
    int len;
    final MIMEMessage msg;
    final MIMEPart part;
    byte[] buf;

    public ChunkInputStream(MIMEMessage msg, MIMEPart part, Chunk startPos) {
        this.current = startPos;
        len = current.data.size();
        buf = current.data.read();
        this.msg = msg;
        this.part = part;
    }


    public int read(byte b[], int off, int sz) throws IOException {
        if(!fetch())    return -1;

        sz = Math.min(sz, len-offset);
        System.arraycopy(buf,offset,b,off,sz);
        return sz;
    }

    public int read() throws IOException {
        if(!fetch()) return -1;
        return (buf[offset++] & 0xff);
    }

    /**
     * Gets to the next chunk if we are done with the current one.
     */
    private boolean fetch() {
        if(current==null)
            throw new IllegalStateException("Stream already closed");
        while(offset==len) {
            while(current.next==null || !part.parsed)
                msg.makeProgress();
            current = current.next;

            if(current==null)
                return false;

            this.offset = 0;
            this.len = current.data.size();
        }
        return true;
    }

    public void close() throws IOException {
        super.close();
        current = null;
    }
}
