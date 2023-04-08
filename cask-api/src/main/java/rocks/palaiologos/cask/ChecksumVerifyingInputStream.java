/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package rocks.palaiologos.cask;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Checksum;

/**
 * A stream that verifies the checksum of the data read once the stream is
 * exhausted.
 * @NotThreadSafe
 * @since 1.7
 */
class ChecksumVerifyingInputStream extends InputStream {

    private final InputStream in;
    private long bytesRemaining;
    private final long expectedChecksum;
    private final Checksum checksum;

    /**
     * Constructs a new instance.
     *
     * @param checksum Checksum implementation.
     * @param in the stream to wrap
     * @param size the of the stream's content
     * @param expectedChecksum the expected checksum
     */
    public ChecksumVerifyingInputStream(final Checksum checksum, final InputStream in,
                                        final long size, final long expectedChecksum) {
        this.checksum = checksum;
        this.in = in;
        this.expectedChecksum = expectedChecksum;
        this.bytesRemaining = size;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    /**
     * @return bytes remaining to read
     * @since 1.21
     */
    public long getBytesRemaining() {
        return bytesRemaining;
    }

    /**
     * Reads a single byte from the stream
     * @throws IOException if the underlying stream throws or the
     * stream is exhausted and the Checksum doesn't match the expected
     * value
     */
    @Override
    public int read() throws IOException {
        if (bytesRemaining <= 0) {
            return -1;
        }
        final int ret = in.read();
        if (ret >= 0) {
            checksum.update(ret);
            --bytesRemaining;
        }
        verify();
        return ret;
    }

    /**
     * Reads a byte array from the stream
     * @throws IOException if the underlying stream throws or the
     * stream is exhausted and the Checksum doesn't match the expected
     * value
     */
    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Reads from the stream into a byte array.
     * @throws IOException if the underlying stream throws or the
     * stream is exhausted and the Checksum doesn't match the expected
     * value
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        final int ret = in.read(b, off, len);
        if (ret >= 0) {
            checksum.update(b, off, ret);
            bytesRemaining -= ret;
        }
        verify();
        return ret;
    }

    @Override
    public long skip(final long n) throws IOException {
        // Can't really skip, we have to hash everything to verify the checksum
        return read() >= 0 ? 1 : 0;
    }

    private void verify() throws IOException {
        if (bytesRemaining <= 0 && expectedChecksum != checksum.getValue()) {
            throw new IOException("Checksum verification failed");
        }
    }
}
