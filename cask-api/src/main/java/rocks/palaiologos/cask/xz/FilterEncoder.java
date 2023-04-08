/*
 * FilterEncoder
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package rocks.palaiologos.cask.xz;

interface FilterEncoder extends rocks.palaiologos.cask.xz.FilterCoder {
    long getFilterID();
    byte[] getFilterProps();
    boolean supportsFlushing();
    rocks.palaiologos.cask.xz.FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                                     ArrayCache arrayCache);
}
