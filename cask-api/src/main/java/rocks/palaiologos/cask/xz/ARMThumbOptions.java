/*
 * ARMThumbOptions
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package rocks.palaiologos.cask.xz;

import java.io.InputStream;

import rocks.palaiologos.cask.xz.simple.ARMThumb;

/**
 * BCJ filter for little endian ARM-Thumb instructions.
 */
public class ARMThumbOptions extends rocks.palaiologos.cask.xz.BCJOptions {
    private static final int ALIGNMENT = 2;

    public ARMThumbOptions() {
        super(ALIGNMENT);
    }

    public rocks.palaiologos.cask.xz.FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                                            rocks.palaiologos.cask.xz.ArrayCache arrayCache) {
        return new rocks.palaiologos.cask.xz.SimpleOutputStream(out, new ARMThumb(true, startOffset));
    }

    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new rocks.palaiologos.cask.xz.SimpleInputStream(in, new ARMThumb(false, startOffset));
    }

    FilterEncoder getFilterEncoder() {
        return new rocks.palaiologos.cask.xz.BCJEncoder(this, rocks.palaiologos.cask.xz.BCJCoder.ARMTHUMB_FILTER_ID);
    }
}
