/*
 * package-info
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

/**
 * XZ data compression support.
 *
 * <h2>Introduction</h2>
 * <p>
 * This aims to be a complete implementation of XZ data compression
 * in pure Java. Features:
 * <ul>
 * <li>Full support for the .xz file format specification version 1.0.4</li>
 * <li>Single-threaded streamed compression and decompression</li>
 * <li>Single-threaded decompression with limited random access support</li>
 * <li>Raw streams (no .xz headers) for advanced users, including LZMA2
 *     with preset dictionary</li>
 * </ul>
 * <p>
 * Threading is planned but it is unknown when it will be implemented.
 * <p>
 * For the latest source code, see the
 * <a href="https://tukaani.org/xz/java.html">home page of XZ for Java</a>.
 *
 * <h2>Getting started</h2>
 * <p>
 * Start by reading the documentation of {@link rocks.palaiologos.cask.xz.XZOutputStream}
 * and {@link rocks.palaiologos.cask.xz.XZInputStream}.
 * If you use XZ inside another file format or protocol,
 * see also {@link rocks.palaiologos.cask.xz.SingleXZInputStream}.
 *
 * <h2>Licensing</h2>
 * <p>
 * XZ for Java has been put into the public domain, thus you can do
 * whatever you want with it. All the files in the package have been
 * written by Lasse Collin, Igor Pavlov, and/or Brett Okken.
 * <p>
 * This software is provided "as is", without any warranty.
 */
package rocks.palaiologos.cask.xz;
