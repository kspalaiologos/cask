/*
 * IA64Options
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package rocks.palaiologos.cask.xz;

import rocks.palaiologos.cask.xz.simple.IA64;

import java.io.InputStream;

/**
 * BCJ filter for Itanium (IA-64) instructions.
 */
public class IA64Options extends BCJOptions {
    private static final int ALIGNMENT = 16;

    public IA64Options() {
        super(ALIGNMENT);
    }

    public FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                  ArrayCache arrayCache) {
        return new SimpleOutputStream(out, new rocks.palaiologos.cask.xz.simple.IA64(true, startOffset));
    }

    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new SimpleInputStream(in, new IA64(false, startOffset));
    }

    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, BCJCoder.IA64_FILTER_ID);
    }
}
