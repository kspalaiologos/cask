/*
 * LZMAEncoderFast
 *
 * Authors: Lasse Collin <lasse.collin@tukaani.org>
 *          Igor Pavlov <http://7-zip.org/>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package rocks.palaiologos.cask.xz.lzma;

import rocks.palaiologos.cask.xz.ArrayCache;
import rocks.palaiologos.cask.xz.rangecoder.RangeEncoder;
import rocks.palaiologos.cask.xz.lz.LZEncoder;
import rocks.palaiologos.cask.xz.lz.Matches;

final class LZMAEncoderFast extends LZMAEncoder {
    private static final int EXTRA_SIZE_BEFORE = 1;
    private static final int EXTRA_SIZE_AFTER = LZMACoder.MATCH_LEN_MAX - 1;

    private Matches matches = null;

    static int getMemoryUsage(int dictSize, int extraSizeBefore, int mf) {
        return rocks.palaiologos.cask.xz.lz.LZEncoder.getMemoryUsage(
                dictSize, Math.max(extraSizeBefore, EXTRA_SIZE_BEFORE),
                EXTRA_SIZE_AFTER, LZMACoder.MATCH_LEN_MAX, mf);
    }

    LZMAEncoderFast(RangeEncoder rc, int lc, int lp, int pb,
                           int dictSize, int extraSizeBefore,
                           int niceLen, int mf, int depthLimit,
                           ArrayCache arrayCache) {
        super(rc, LZEncoder.getInstance(dictSize,
                                        Math.max(extraSizeBefore,
                                                 EXTRA_SIZE_BEFORE),
                                        EXTRA_SIZE_AFTER,
                                        niceLen, LZMACoder.MATCH_LEN_MAX,
                                        mf, depthLimit, arrayCache),
              lc, lp, pb, dictSize, niceLen);
    }

    private boolean changePair(int smallDist, int bigDist) {
        return smallDist < (bigDist >>> 7);
    }

    int getNextSymbol() {
        // Get the matches for the next byte unless readAhead indicates
        // that we already got the new matches during the previous call
        // to this function.
        if (readAhead == -1)
            matches = getMatches();

        back = -1;

        // Get the number of bytes available in the dictionary, but
        // not more than the maximum match length. If there aren't
        // enough bytes remaining to encode a match at all, return
        // immediately to encode this byte as a literal.
        int avail = Math.min(lz.getAvail(), LZMACoder.MATCH_LEN_MAX);
        if (avail < LZMACoder.MATCH_LEN_MIN)
            return 1;

        // Look for a match from the previous four match distances.
        int bestRepLen = 0;
        int bestRepIndex = 0;
        for (int rep = 0; rep < LZMACoder.REPS; ++rep) {
            int len = lz.getMatchLen(reps[rep], avail);
            if (len < LZMACoder.MATCH_LEN_MIN)
                continue;

            // If it is long enough, return it.
            if (len >= niceLen) {
                back = rep;
                skip(len - 1);
                return len;
            }

            // Remember the index and length of the best repeated match.
            if (len > bestRepLen) {
                bestRepIndex = rep;
                bestRepLen = len;
            }
        }

        int mainLen = 0;
        int mainDist = 0;

        if (matches.count > 0) {
            mainLen = matches.len[matches.count - 1];
            mainDist = matches.dist[matches.count - 1];

            if (mainLen >= niceLen) {
                back = mainDist + LZMACoder.REPS;
                skip(mainLen - 1);
                return mainLen;
            }

            while (matches.count > 1
                    && mainLen == matches.len[matches.count - 2] + 1) {
                if (!changePair(matches.dist[matches.count - 2], mainDist))
                    break;

                --matches.count;
                mainLen = matches.len[matches.count - 1];
                mainDist = matches.dist[matches.count - 1];
            }

            if (mainLen == LZMACoder.MATCH_LEN_MIN && mainDist >= 0x80)
                mainLen = 1;
        }

        if (bestRepLen >= LZMACoder.MATCH_LEN_MIN) {
            if (bestRepLen + 1 >= mainLen
                    || (bestRepLen + 2 >= mainLen && mainDist >= (1 << 9))
                    || (bestRepLen + 3 >= mainLen && mainDist >= (1 << 15))) {
                back = bestRepIndex;
                skip(bestRepLen - 1);
                return bestRepLen;
            }
        }

        if (mainLen < LZMACoder.MATCH_LEN_MIN || avail <= LZMACoder.MATCH_LEN_MIN)
            return 1;

        // Get the next match. Test if it is better than the current match.
        // If so, encode the current byte as a literal.
        matches = getMatches();

        if (matches.count > 0) {
            int newLen = matches.len[matches.count - 1];
            int newDist = matches.dist[matches.count - 1];

            if ((newLen >= mainLen && newDist < mainDist)
                    || (newLen == mainLen + 1
                        && !changePair(mainDist, newDist))
                    || newLen > mainLen + 1
                    || (newLen + 1 >= mainLen
                        && mainLen >= LZMACoder.MATCH_LEN_MIN + 1
                        && changePair(newDist, mainDist)))
                return 1;
        }

        int limit = Math.max(mainLen - 1, LZMACoder.MATCH_LEN_MIN);
        for (int rep = 0; rep < LZMACoder.REPS; ++rep)
            if (lz.getMatchLen(reps[rep], limit) == limit)
                return 1;

        back = mainDist + LZMACoder.REPS;
        skip(mainLen - 2);
        return mainLen;
    }
}
