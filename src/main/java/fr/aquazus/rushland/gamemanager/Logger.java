package fr.aquazus.rushland.gamemanager;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi.Attribute;
import com.diogonunes.jcdp.color.api.Ansi.BColor;
import com.diogonunes.jcdp.color.api.Ansi.FColor;

/*
 * Ce fichier est soumis à des droits d'auteur.
 * Dépot http://www.copyrightdepot.com/cd88/00056542.htm
 * Numéro du détenteur - 00056542
 * Le détenteur des copyrights publiés dans cette page n'autorise 
 * aucun usage de ses créations, en tout ou en partie. 
 * Les archives de CopyrightDepot.com conservent les documents 
 * qui permettent au détenteur de démontrer ses droits d'auteur et d’éventuellement
 * réclamer légalement une compensation financière contre toute personne ayant utilisé 
 * une de ses créations sans autorisation. Conformément à nos règlements, 
 * ces documents sont assermentés, à nos frais, 
 * en cas de procès pour violation de droits d'auteur.
 */

public class Logger {
    private static Logger instance;
    private ColoredPrinter printer;

    public Logger() {
        printer = new ColoredPrinter.Builder(1, false).foreground(FColor.WHITE).background(BColor.NONE).build();
    }

    public void println(String msg) {
        printer.println(msg);
    }

    public void println(String msg, Attribute attr, FColor fg, BColor bg) {
        printer.print(msg + "\n", attr, fg, bg);
    }

    public void success(String msg) {
        printer.print(msg + "\n", Attribute.NONE, FColor.GREEN, BColor.NONE);
    }

    public void debug(String msg, int level) {
        printer.debugPrintln(msg, level);
    }

    public void debug(String msg) {
        printer.debugPrintln(msg);
    }

    public void error(String msg) {
        printer.errorPrintln(msg);
    }

    public void clear() {
        printer.clear();
    }

    public void setAttribute(Attribute attr) {
        printer.setAttribute(attr);
    }

    public ColoredPrinter getPrinter() {
        return printer;
    }

    public void setLogLevel(int level) {
        printer.setLevel(level);
    }

    public void setTimestamping(boolean state) {
        printer = new ColoredPrinter.Builder(1, state).foreground(FColor.WHITE).background(BColor.NONE).build();
    }

    public static Logger getLogger() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
}
