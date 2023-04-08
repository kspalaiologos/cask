/*
 * IndexHash
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package rocks.palaiologos.cask.xz.index;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CheckedInputStream;
import rocks.palaiologos.cask.xz.common.DecoderUtil;
import rocks.palaiologos.cask.xz.CorruptedInputException;
import rocks.palaiologos.cask.xz.XZIOException;
import rocks.palaiologos.cask.xz.check.CRC32;
import rocks.palaiologos.cask.xz.check.Check;
import rocks.palaiologos.cask.xz.check.SHA256;

public class IndexHash extends rocks.palaiologos.cask.xz.index.IndexBase {
    private Check hash;

    public IndexHash() {
        super(new rocks.palaiologos.cask.xz.CorruptedInputException());

        try {
            hash = new SHA256();
        } catch (java.security.NoSuchAlgorithmException e) {
            hash = new CRC32();
        }
    }

    public void add(long unpaddedSize, long uncompressedSize)
            throws rocks.palaiologos.cask.xz.XZIOException {
        super.add(unpaddedSize, uncompressedSize);

        ByteBuffer buf = ByteBuffer.allocate(2 * 8);
        buf.putLong(unpaddedSize);
        buf.putLong(uncompressedSize);
        hash.update(buf.array());
    }

    public void validate(InputStream in) throws IOException {
        // Index Indicator (0x00) has already been read by BlockInputStream
        // so add 0x00 to the CRC32 here.
        java.util.zip.CRC32 crc32 = new java.util.zip.CRC32();
        crc32.update('\0');
        CheckedInputStream inChecked = new CheckedInputStream(in, crc32);

        // Get and validate the Number of Records field.
        // If Block Header Size was corrupt and became Index Indicator,
        // this error would actually be about corrupt Block Header.
        // This is why the error message mentions both possibilities.
        long storedRecordCount = DecoderUtil.decodeVLI(inChecked);
        if (storedRecordCount != recordCount)
            throw new rocks.palaiologos.cask.xz.CorruptedInputException(
                    "XZ Block Header or the start of XZ Index is corrupt");

        // Decode and hash the Index field and compare it to
        // the hash value calculated from the decoded Blocks.
        IndexHash stored = new IndexHash();
        for (long i = 0; i < recordCount; ++i) {
            long unpaddedSize = DecoderUtil.decodeVLI(inChecked);
            long uncompressedSize = DecoderUtil.decodeVLI(inChecked);

            try {
                stored.add(unpaddedSize, uncompressedSize);
            } catch (XZIOException e) {
                throw new rocks.palaiologos.cask.xz.CorruptedInputException("XZ Index is corrupt");
            }

            if (stored.blocksSum > blocksSum
                    || stored.uncompressedSum > uncompressedSum
                    || stored.indexListSize > indexListSize)
                throw new rocks.palaiologos.cask.xz.CorruptedInputException("XZ Index is corrupt");
        }

        if (stored.blocksSum != blocksSum
                || stored.uncompressedSum != uncompressedSum
                || stored.indexListSize != indexListSize
                || !Arrays.equals(stored.hash.finish(), hash.finish()))
            throw new rocks.palaiologos.cask.xz.CorruptedInputException("XZ Index is corrupt");

        // Index Padding
        DataInputStream inData = new DataInputStream(inChecked);
        for (int i = getIndexPaddingSize(); i > 0; --i)
            if (inData.readUnsignedByte() != 0x00)
                throw new rocks.palaiologos.cask.xz.CorruptedInputException("XZ Index is corrupt");

        // CRC32
        long value = crc32.getValue();
        for (int i = 0; i < 4; ++i)
            if (((value >>> (i * 8)) & 0xFF) != inData.readUnsignedByte())
                throw new CorruptedInputException("XZ Index is corrupt");
    }
}
