/*
 * DeltaCoder
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package rocks.palaiologos.cask.xz;

abstract class DeltaCoder implements rocks.palaiologos.cask.xz.FilterCoder {
    public static final long FILTER_ID = 0x03;

    public boolean changesSize() {
        return false;
    }

    public boolean nonLastOK() {
        return true;
    }

    public boolean lastOK() {
        return false;
    }
}
