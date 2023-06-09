/*
 * LZMA2Coder
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package rocks.palaiologos.cask.xz;

abstract class LZMA2Coder implements rocks.palaiologos.cask.xz.FilterCoder {
    public static final long FILTER_ID = 0x21;

    public boolean changesSize() {
        return true;
    }

    public boolean nonLastOK() {
        return false;
    }

    public boolean lastOK() {
        return true;
    }
}
