package id.zelory.compressor;

import java.io.File;

/**
 * Created on : January 24, 2020
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public interface Constraint {
    boolean isSatisfied(File imageFile);
    File satisfy(File imageFile);
}
