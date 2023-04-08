/*
 * SPARCOptions
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package rocks.palaiologos.cask.xz;

import java.io.InputStream;

import rocks.palaiologos.cask.xz.simple.SPARC;

/**
 * BCJ filter for SPARC.
 */
public class SPARCOptions extends rocks.palaiologos.cask.xz.BCJOptions {
    private static final int ALIGNMENT = 4;

    public SPARCOptions() {
        super(ALIGNMENT);
    }

    public rocks.palaiologos.cask.xz.FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                                            rocks.palaiologos.cask.xz.ArrayCache arrayCache) {
        return new rocks.palaiologos.cask.xz.SimpleOutputStream(out, new SPARC(true, startOffset));
    }

    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new rocks.palaiologos.cask.xz.SimpleInputStream(in, new SPARC(false, startOffset));
    }

    rocks.palaiologos.cask.xz.FilterEncoder getFilterEncoder() {
        return new rocks.palaiologos.cask.xz.BCJEncoder(this, rocks.palaiologos.cask.xz.BCJCoder.SPARC_FILTER_ID);
    }
}
