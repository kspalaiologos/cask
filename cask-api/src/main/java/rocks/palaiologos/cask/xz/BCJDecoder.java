/*
 * BCJDecoder
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package rocks.palaiologos.cask.xz;

import java.io.InputStream;

import rocks.palaiologos.cask.xz.simple.SPARC;

class BCJDecoder extends BCJCoder implements FilterDecoder {
    private final long filterID;
    private final int startOffset;

    BCJDecoder(long filterID, byte[] props)
            throws UnsupportedOptionsException {
        assert isBCJFilterID(filterID);
        this.filterID = filterID;

        if (props.length == 0) {
            startOffset = 0;
        } else if (props.length == 4) {
            int n = 0;
            for (int i = 0; i < 4; ++i)
                n |= (props[i] & 0xFF) << (i * 8);

            startOffset = n;
        } else {
            throw new UnsupportedOptionsException(
                    "Unsupported BCJ filter properties");
        }
    }

    public int getMemoryUsage() {
        return SimpleInputStream.getMemoryUsage();
    }

    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        rocks.palaiologos.cask.xz.simple.SimpleFilter simpleFilter = null;

        if (filterID == X86_FILTER_ID)
            simpleFilter = new rocks.palaiologos.cask.xz.simple.X86(false, startOffset);
        else if (filterID == POWERPC_FILTER_ID)
            simpleFilter = new rocks.palaiologos.cask.xz.simple.PowerPC(false, startOffset);
        else if (filterID == IA64_FILTER_ID)
            simpleFilter = new rocks.palaiologos.cask.xz.simple.IA64(false, startOffset);
        else if (filterID == ARM_FILTER_ID)
            simpleFilter = new rocks.palaiologos.cask.xz.simple.ARM(false, startOffset);
        else if (filterID == ARMTHUMB_FILTER_ID)
            simpleFilter = new rocks.palaiologos.cask.xz.simple.ARMThumb(false, startOffset);
        else if (filterID == SPARC_FILTER_ID)
            simpleFilter = new SPARC(false, startOffset);
        else
            assert false;

        return new SimpleInputStream(in, simpleFilter);
    }
}
