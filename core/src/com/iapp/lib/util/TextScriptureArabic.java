package com.iapp.lib.util;

import com.badlogic.gdx.utils.IntSet;

/**
 * Utility class for Arabic script (and Farsi, i.e. Persian language). Also, possibly supported already is
 * Urdu(?) and Kurdish(?) etc.
 * <p>
 * NOTE: Some characters returned are 0xFFFF (i.e. non-visible!).
 * <p>
 * Reference:
 * <ul>
 *   <li>Arab Presentation Forms B: https://en.wikipedia.org/wiki/Arabic_Presentation_Forms-B</li>
 *   <li>Arabtype (C example): https://github.com/eloraiby/arabtype
 *   <li>Farsi (Person) on 2020-09-22: https://github.com/font-store/persian-reshaper/blob/master/js/persian-script-reshaper.js
 *   <li>Full(?) Example: https://github.com/smmoosavi/android-Farsi-Tools/blob/master/src/farsiTools/ArabicLigaturizer.java
 * </ul>
 *
 * @author noblemaster
 */
public final class TextScriptureArabic {

    /** Arabic letter that needs presentation form change. */
    private static final char ARABIC_LETTER_START = 0x0621;
    /** Arabic letter that needs presentation form change. */
    private static final char ARABIC_LETTER_FINAL = 0x06D3;


    private TextScriptureArabic() {
        // not used
    }

    /** Returns true for Arabic letters. */
    public static boolean isLetterAR(char ch) {
        // check the range...
        if (ch < 0x0600) {
            return false;
        }
        else {
            return (                  (ch <= 0x06FF)) ||
                    ((ch >= 0x0750) && (ch <= 0x077F)) ||
                    ((ch >= 0x08A0) && (ch <= 0x08FF)) ||
                    ((ch >= 0xFB50) && (ch <= 0xFDFF)) ||
                    ((ch >= 0xFE70) && (ch <= 0xFEFF));
        }

        // SIMPLIFIED (but slower):
        // return ((ch >= 0x0600) && (ch <= 0x06FF)) ||
        //        ((ch >= 0x0750) && (ch <= 0x077F)) ||
        //        ((ch >= 0x08A0) && (ch <= 0x08FF)) ||
        //        ((ch >= 0xFB50) && (ch <= 0xFDFF)) ||
        //        ((ch >= 0xFE70) && (ch <= 0xFEFF));
    }

    /** Returns true if this is an arabic letter that needs presentation form fixing. */
    public static boolean isLetterARConvert(char ch) {
        return (ch >= ARABIC_LETTER_START) && (ch <= ARABIC_LETTER_FINAL);
    }

    // -------------------------------------------------------------------------------------------------------------------

    /** Returns the presentation char: check isLetterARConvert beforehand! */
    public static char toCorrect(CharSequence text, int index) {
        // possibly bad hack to get special stuff to work?
        char ch = text.charAt(index);
        if (TRANSPARENT.contains(ch)) {
            // "transparent" character that is rendered as is
            switch (ch) {
                case 0x0654: {
                    // non-existing glyph in font: replace /w higher-up version (maybe too high)
                    ch = 0x035B;
                    break;
                }
                default:
                    // unchanged...
            }
            return ch;
        }
        else {
            // arabic: determine previous & next
            int textCount = text.length();

            int prevTries = 4;  // <-- we try to go back 4 at max.!
            int prevIndex = index - 1;
            while ((prevTries > 0) && (prevIndex >= 0) && (TRANSPARENT.contains(text.charAt(prevIndex)))) {
                prevTries--;
                prevIndex--;
            }
            char prev = prevIndex >= 0 ? text.charAt(prevIndex) : 0x0000;

            int nextTries = 4;  // <-- we try to go back 4 at max.!
            int nextIndex = index + 1;
            while ((nextTries > 0) && (nextIndex < textCount) && (TRANSPARENT.contains(text.charAt(nextIndex)))) {
                nextTries--;
                nextIndex++;
            }
            char next = nextIndex < textCount ? text.charAt(nextIndex) : 0x0000;

            // covert Arabic letter
            boolean isLa  = isLamAlef(ch, next);
            boolean isApl = isAlefPrevLam(prev, ch);
            boolean isLapl = isLa | isApl;

            // determine character to return
            int col = ((((isLapl | isLetterARConvert(next)) & isLinkingType(ch)) ? 1 : 0) << 1) | (isLinkingType(prev) ? 1 : 0);
            int ref = (next * (isLa ? 1 : 0)) + (ch * (isLa ? 0 : 1));
            return (char)ARABIC_FORMS_B[ref - ARABIC_LETTER_START][isLapl ? 1 : 0][col];
        }
    }

    // -------------------------------------------------------------------------------------------------------------------

    private static final int UNICODE_LAM = 0x644;

    private static boolean isLamAlef(char cp, char next)  {
        return (cp == UNICODE_LAM) && (isLetterARConvert(next)) && (ARABIC_FORMS_B[next - ARABIC_LETTER_START][1][INITIAL] != 0);
    }

    private static boolean isAlefPrevLam(char prev, char cp)  {
        return (prev == UNICODE_LAM) && (isLetterARConvert(cp)) && (ARABIC_FORMS_B[cp - ARABIC_LETTER_START][1][INITIAL] != 0);
    }

    private static boolean isLinkingType(char cp) {
        return (isLetterARConvert(cp)) && (ARABIC_FORMS_B[cp - ARABIC_LETTER_START][0][MEDIAL] != 0);
    }

    // TABLE COLUMNS:
    //   0: isolated form
    //   1: ending form
    //   2: beginning form (if 0, it's a cutting type)
    //   3: middle form
    private static final int ISOLATED = 0;
    private static final int ENDING   = 1;
    private static final int INITIAL  = 2;
    private static final int MEDIAL   = 3;

    /** Transparent characters that are rendered as is and not used for combining. */
    private static final IntSet TRANSPARENT = new IntSet();

    static {
        TRANSPARENT.addAll(0x0610, // ARABIC SIGN SALLALLAHOU ALAYHE WASSALLAM
                0x0612, // ARABIC SIGN ALAYHE ASSALLAM
                0x0613, // ARABIC SIGN RADI ALLAHOU ANHU
                0x0614, // ARABIC SIGN TAKHALLUS
                0x0615, // ARABIC SMALL HIGH TAH
                0x064B, // ARABIC FATHATAN
                0x064C, // ARABIC DAMMATAN
                0x064D, // ARABIC KASRATAN
                0x064E, // ARABIC FATHA
                0x064F, // ARABIC DAMMA
                0x0650, // ARABIC KASRA
                0x0651, // ARABIC SHADDA
                0x0652, // ARABIC SUKUN
                0x0653, // ARABIC MADDAH ABOVE
                0x0654, // ARABIC HAMZA ABOVE
                0x0655, // ARABIC HAMZA BELOW
                0x0656, // ARABIC SUBSCRIPT ALEF
                0x0657, // ARABIC INVERTED DAMMA
                0x0658, // ARABIC MARK NOON GHUNNA
                0x0660, // Arabic #0 (Chris)
                0x0661, // Arabic #1 (Chris)
                0x0662, // Arabic #2 (Chris)
                0x0663, // Arabic #3 (Chris)
                0x0664, // Arabic #4 (Chris)
                0x0665, // Arabic #5 (Chris)
                0x0666, // Arabic #6 (Chris)
                0x0667, // Arabic #7 (Chris)
                0x0668, // Arabic #8 (Chris)
                0x0669, // Arabic #9 (Chris)
                0x0670, // ARABIC LETTER SUPERSCRIPT ALEF
                0x06D6, // ARABIC SMALL HIGH LIGATURE SAD WITH LAM WITH ALEF MAKSURA
                0x06D7, // ARABIC SMALL HIGH LIGATURE QAF WITH LAM WITH ALEF MAKSURA
                0x06D8, // ARABIC SMALL HIGH MEEM INITIAL FORM
                0x06D9, // ARABIC SMALL HIGH LAM ALEF
                0x06DA, // ARABIC SMALL HIGH JEEM
                0x06DB, // ARABIC SMALL HIGH THREE DOTS
                0x06DC, // ARABIC SMALL HIGH SEEN
                0x06DF, // ARABIC SMALL HIGH ROUNDED ZERO
                0x06E0, // ARABIC SMALL HIGH UPRIGHT RECTANGULAR ZERO
                0x06E1, // ARABIC SMALL HIGH DOTLESS HEAD OF KHAH
                0x06E2, // ARABIC SMALL HIGH MEEM ISOLATED FORM
                0x06E3, // ARABIC SMALL LOW SEEN
                0x06E4, // ARABIC SMALL HIGH MADDA
                0x06E7, // ARABIC SMALL HIGH YEH
                0x06E8, // ARABIC SMALL HIGH NOON
                0x06EA, // ARABIC EMPTY CENTRE LOW STOP
                0x06EB, // ARABIC EMPTY CENTRE HIGH STOP
                0x06EC, // ARABIC ROUNDED HIGH STOP WITH FILLED CENTRE
                0x06ED  // ARABIC SMALL LOW MEEM
        );
    }

    /** Table to convert to Arabic presentation form B. */
    private static final int[][][] ARABIC_FORMS_B = {
            { {0xFE80, 0xFE80,      0,      0}, {-1, -1, 0, 0} },            // 0x0621 | Arabic: hamza
            { {0xFE81, 0xFE82,      0,      0}, {-1, -1, 0xFEF5, 0xFEF6} },  // 0x0622 | Arabic: 2alif madda
            { {0xFE83, 0xFE84,      0,      0}, {-1, -1, 0xFEF7, 0xFEF8} },  // 0x0623 | Arabic: 2alif hamza
            { {0xFE85, 0xFE86,      0,      0}, {-1, -1, 0, 0} },            // 0x0624 | Arabic: waw hamza
            { {0xFE87, 0xFE88,      0,      0}, {-1, -1, 0xFEF9, 0xFEFA} },  // 0x0625 | Arabic: 2alif hamza maksoura
            { {0xFE89, 0xFE8A, 0xFE8B, 0xFE8C}, {-1, -1, 0, 0} },            // 0x0626 | Arabic: 2alif maqsoura hamza
            { {0xFE8D, 0xFE8E,      0,      0}, {-1, -1, 0xFEFB, 0xFEFC} },  // 0x0627 | Arabic: 2alif
            { {0xFE8F, 0xFE90, 0xFE91, 0xFE92}, {-1, -1, 0, 0} },            // 0x0628 | Arabic: ba2
            { {0xFE93, 0xFE94,      0,      0}, {-1, -1, 0, 0} },            // 0x0629 | Arabic: ta2 marbouta
            { {0xFE95, 0xFE96, 0xFE97, 0xFE98}, {-1, -1, 0, 0} },            // 0x062a | Arabic: ta2
            { {0xFE99, 0xFE9A, 0xFE9B, 0xFE9C}, {-1, -1, 0, 0} },            // 0x062b | Arabic: tha2
            { {0xFE9D, 0xFE9E, 0xFE9F, 0xFEA0}, {-1, -1, 0, 0} },            // 0x062c | Arabic: jim
            { {0xFEA1, 0xFEA2, 0xFEA3, 0xFEA4}, {-1, -1, 0, 0} },            // 0x062d | Arabic: 7a2
            { {0xFEA5, 0xFEA6, 0xFEA7, 0xFEA8}, {-1, -1, 0, 0} },            // 0x062e | Arabic: kha2
            { {0xFEA9, 0xFEAA,      0,      0}, {-1, -1, 0, 0} },            // 0x062f | Arabic: dal
            { {0xFEAB, 0xFEAC,      0,      0}, {-1, -1, 0, 0} },            // 0x0630 | Arabic: dhal
            { {0xFEAD, 0xFEAE,      0,      0}, {-1, -1, 0, 0} },            // 0x0631 | Arabic: ra2
            { {0xFEAF, 0xFEB0,      0,      0}, {-1, -1, 0, 0} },            // 0x0632 | Arabic: zayn
            { {0xFEB1, 0xFEB2, 0xFEB3, 0xFEB4}, {-1, -1, 0, 0} },            // 0x0633 | Arabic: syn
            { {0xFEB5, 0xFEB6, 0xFEB7, 0xFEB8}, {-1, -1, 0, 0} },            // 0x0634 | Arabic: shin
            { {0xFEB9, 0xFEBA, 0xFEBB, 0xFEBC}, {-1, -1, 0, 0} },            // 0x0635 | Arabic: sad
            { {0xFEBD, 0xFEBE, 0xFEBF, 0xFEC0}, {-1, -1, 0, 0} },            // 0x0636 | Arabic: dad
            { {0xFEC1, 0xFEC2, 0xFEC3, 0xFEC4}, {-1, -1, 0, 0} },            // 0x0637 | Arabic: tah
            { {0xFEC5, 0xFEC6, 0xFEC7, 0xFEC8}, {-1, -1, 0, 0} },            // 0x0638 | Arabic: thah
            { {0xFEC9, 0xFECA, 0xFECB, 0xFECC}, {-1, -1, 0, 0} },            // 0x0639 | Arabic: 3ayn
            { {0xFECD, 0xFECE, 0xFECF, 0xFED0}, {-1, -1, 0, 0} },            // 0x063a | Arabic: ghayn
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x063b -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x063c -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x063d -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x063e -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x063f -
            { {0x0640, 0x0640, 0x0640, 0x0640}, {-1, -1, 0, 0} },            // 0x0640 | Arabic: wasla
            { {0xFED1, 0xFED2, 0xFED3, 0xFED4}, {-1, -1, 0, 0} },            // 0x0641 | Arabic: fa2
            { {0xFED5, 0xFED6, 0xFED7, 0xFED8}, {-1, -1, 0, 0} },            // 0x0642 | Arabic: qaf
            { {0xFED9, 0xFEDA, 0xFEDB, 0xFEDC}, {-1, -1, 0, 0} },            // 0x0643 | Arabic: kaf
            { {0xFEDD, 0xFEDE, 0xFEDF, 0xFEE0}, {-1, -1, 0, 0} },            // 0x0644 | Arabic: lam
            { {0xFEE1, 0xFEE2, 0xFEE3, 0xFEE4}, {-1, -1, 0, 0} },            // 0x0645 | Arabic: mim
            { {0xFEE5, 0xFEE6, 0xFEE7, 0xFEE8}, {-1, -1, 0, 0} },            // 0x0646 | Arabic: noon
            { {0xFEE9, 0xFEEA, 0xFEEB, 0xFEEC}, {-1, -1, 0, 0} },            // 0x0647 | Arabic: ha2
            { {0xFEED, 0xFEEE,      0,      0}, {-1, -1, 0, 0} },            // 0x0648 | Arabic: waw
            { {0xFEEF, 0xFEF0,      0,      0}, {-1, -1, 0, 0} },            // 0x0649 | Arabic: 2alif maksoura
            { {0xFEF1, 0xFEF2, 0xFEF3, 0xFEF4}, {-1, -1, 0, 0} },            // 0x064a | Arabic: ya2
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x064b -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x064c -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x064d -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x064e -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x064f -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0650 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0651 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0652 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0653 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0654 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0655 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0656 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0657 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0658 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0659 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x065a -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x065b -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x065c -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x065d -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x065e -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x065f -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0660 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0661 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0662 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0663 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0664 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0665 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0666 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0667 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0668 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0669 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x066a -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x066b -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x066c -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x066d -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x066e -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x066f -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0670 -
            { {0xFB50, 0xFB51,      0,      0}, {-1, -1, 0, 0} },            // 0x0671 | ALEF WASLA
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0672 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0673 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0674 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0675 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0676 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0677 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0678 -
            { {0xFB66, 0xFB67, 0xFB68, 0xFB69}, {-1, -1, 0, 0} },            // 0x0679 | TTEH
            { {0xFB5E, 0xFB5F, 0xFB60, 0xFB61}, {-1, -1, 0, 0} },            // 0x067a | TTEHEH
            { {0xFB52, 0xFB53, 0xFB54, 0xFB55}, {-1, -1, 0, 0} },            // 0x067b | BEEH
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x067c -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x067d -
            { {0xFB56, 0xFB57, 0xFB58, 0xFB59}, {-1, -1, 0, 0} },            // 0x067e | PEH
            { {0xFB62, 0xFB63, 0xFB64, 0xFB65}, {-1, -1, 0, 0} },            // 0x067f | TEHEH
            { {0xFB5A, 0xFB5B, 0xFB5C, 0xFB5D}, {-1, -1, 0, 0} },            // 0x0680 | BEHEH
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0681 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0682 -
            { {0xFB76, 0xFB77, 0xFB78, 0xFB79}, {-1, -1, 0, 0} },            // 0x0683 | NYEH
            { {0xFB72, 0xFB73, 0xFB74, 0xFB75}, {-1, -1, 0, 0} },            // 0x0684 | DYEH
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0685 -
            { {0xFB7A, 0xFB7B, 0xFB7C, 0xFB7D}, {-1, -1, 0, 0} },            // 0x0686 | TCHEH
            { {0xFB7E, 0xFB7F, 0xFB80, 0xFB81}, {-1, -1, 0, 0} },            // 0x0687 | TCHEHEH
            { {0xFB88, 0xFB89,      0,      0}, {-1, -1, 0, 0} },            // 0x0688 | DDAL
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0689 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x068a -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x068b -
            { {0xFB84, 0xFB85,      0,      0}, {-1, -1, 0, 0} },            // 0x068c | DAHAL
            { {0xFB82, 0xFB83,      0,      0}, {-1, -1, 0, 0} },            // 0x068d | DDAHAL
            { {0xFB86, 0xFB87,      0,      0}, {-1, -1, 0, 0} },            // 0x068e | DUL
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x068f -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0690 -
            { {0xFB8C, 0xFB8D,      0,      0}, {-1, -1, 0, 0} },            // 0x0691 | RREH
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0692 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0693 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0694 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0695 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0696 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0697 -
            { {0xFB8A, 0xFB8B,      0,      0}, {-1, -1, 0, 0} },            // 0x0698 | JEH
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x0699 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x069a -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x069b -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x069c -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x069d -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x069e -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x069f -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06a0 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06a1 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06a2 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06a3 -
            { {0xFB6A, 0xFB6B, 0xFB6C, 0xFB6D}, {-1, -1, 0, 0} },            // 0x06a4 | VEH
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06a5 -
            { {0xFB6E, 0xFB6F, 0xFB70, 0xFB71}, {-1, -1, 0, 0} },            // 0x06a6 | PEHEH
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06a7 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06a8 -
            { {0xFB8E, 0xFB8F, 0xFB90, 0xFB91}, {-1, -1, 0, 0} },            // 0x06a9 | KEHEH
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06aa -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06ab -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06ac -
            { {0xFBD3, 0xFBD4, 0xFBD5, 0xFBD6}, {-1, -1, 0, 0} },            // 0x06ad | NG
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06ae -
            { {0xFB92, 0xFB93, 0xFB94, 0xFB95}, {-1, -1, 0, 0} },            // 0x06af | GAF
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06b0 -
            { {0xFB9A, 0xFB9B, 0xFB9C, 0xFB9D}, {-1, -1, 0, 0} },            // 0x06b1 | NGOEH
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06b2 -
            { {0xFB96, 0xFB97, 0xFB98, 0xFB99}, {-1, -1, 0, 0} },            // 0x06b3 | GUEH
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06b4 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06b5 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06b6 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06b7 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06b8 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06b9 -
            { {0xFB9E, 0xFB9F,      0,      0}, {-1, -1, 0, 0} },            // 0x06ba | NOON GHUNNA
            { {0xFBA0, 0xFBA1, 0xFBA2, 0xFBA3}, {-1, -1, 0, 0} },            // 0x06bb | RNOON
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06bc -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06bd -
            { {0xFBAA, 0xFBAB, 0xFBAC, 0xFBAD}, {-1, -1, 0, 0} },            // 0x06be | HEH DOACHASHMEE
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06bf -
            { {0xFBA4, 0xFBA5,      0,      0}, {-1, -1, 0, 0} },            // 0x06c0 | HEH WITH YEH ABOVE
            { {0xFBA6, 0xFBA7, 0xFBA8, 0xFBA9}, {-1, -1, 0, 0} },            // 0x06c1 | HEH GOAL
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06c2 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06c3 -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06c4 -
            { {0xFBE0, 0xFBE1,      0,      0}, {-1, -1, 0, 0} },            // 0x06c5 | KIRGHIZ OE
            { {0xFBD9, 0xFBDA,      0,      0}, {-1, -1, 0, 0} },            // 0x06c6 | OE
            { {0xFBD7, 0xFBD8,      0,      0}, {-1, -1, 0, 0} },            // 0x06c7 | U
            { {0xFBDB, 0xFBDC,      0,      0}, {-1, -1, 0, 0} },            // 0x06c8 | YU
            { {0xFBE2, 0xFBE3,      0,      0}, {-1, -1, 0, 0} },            // 0x06c9 | KIRGHIZ YU
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06ca -
            { {0xFBDE, 0xFBDF,      0,      0}, {-1, -1, 0, 0} },            // 0x06cb | VE
            { {0xFBFC, 0xFBFD, 0xFBFE, 0xFBFF}, {-1, -1, 0, 0} },            // 0x06cc | FARSI YEH
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06cd -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06ce -
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06cf -
            { {0xFBE4, 0xFBE5, 0xFBE6, 0xFBE7}, {-1, -1, 0, 0} },            // 0x06d0 | E
            { {     0,      0,      0,      0}, {-1, -1, 0, 0} },            // 0x06d1 -
            { {0xFBAE, 0xFBAF,      0,      0}, {-1, -1, 0, 0} },            // 0x06d2 | YEH BARREE
            { {0xFBB0, 0xFBB1,      0,      0}, {-1, -1, 0, 0} },            // 0x06d3 | YEH BARREE WITH HAMZA ABOVE
    };
}
