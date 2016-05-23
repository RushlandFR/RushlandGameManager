package fr.rushland.gamemanager;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi.Attribute;
import com.diogonunes.jcdp.color.api.Ansi.BColor;
import com.diogonunes.jcdp.color.api.Ansi.FColor;

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
