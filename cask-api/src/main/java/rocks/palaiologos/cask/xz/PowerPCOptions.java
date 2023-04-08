/*
 * PowerPCOptions
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package rocks.palaiologos.cask.xz;

import java.io.InputStream;

import rocks.palaiologos.cask.xz.simple.PowerPC;

/**
 * BCJ filter for big endian PowerPC instructions.
 */
public class PowerPCOptions extends rocks.palaiologos.cask.xz.BCJOptions {
    private static final int ALIGNMENT = 4;

    public PowerPCOptions() {
        super(ALIGNMENT);
    }

    public rocks.palaiologos.cask.xz.FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                                            rocks.palaiologos.cask.xz.ArrayCache arrayCache) {
        return new rocks.palaiologos.cask.xz.SimpleOutputStream(out, new PowerPC(true, startOffset));
    }

    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new rocks.palaiologos.cask.xz.SimpleInputStream(in, new PowerPC(false, startOffset));
    }

    FilterEncoder getFilterEncoder() {
        return new rocks.palaiologos.cask.xz.BCJEncoder(this, rocks.palaiologos.cask.xz.BCJCoder.POWERPC_FILTER_ID);
    }
}
