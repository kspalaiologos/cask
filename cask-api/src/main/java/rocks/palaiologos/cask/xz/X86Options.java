/*
 * X86Options
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package rocks.palaiologos.cask.xz;

import rocks.palaiologos.cask.xz.simple.X86;

import java.io.InputStream;

/**
 * BCJ filter for x86 (32-bit and 64-bit) instructions.
 */
public class X86Options extends BCJOptions {
    private static final int ALIGNMENT = 1;

    public X86Options() {
        super(ALIGNMENT);
    }

    public FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                  ArrayCache arrayCache) {
        return new SimpleOutputStream(out, new rocks.palaiologos.cask.xz.simple.X86(true, startOffset));
    }

    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new SimpleInputStream(in, new X86(false, startOffset));
    }

    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, BCJCoder.X86_FILTER_ID);
    }
}
