package dance.dance;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;
import java.io.File;

/**
 * Created by Crab on 01.11.2017.
 */

public final class LogThread implements Runnable {
    private String s;
    private File logger;
    public void run() {
        try {
            BufferedWriter b = new BufferedWriter(new FileWriter(logger, true));
            b.append(Calendar.getInstance().getTime() + "$");
            b.append(s);
            b.newLine();
            b.close();
        }catch (Exception e){e.printStackTrace();}
    }

    public LogThread(String str,File logger){
        this.s=str;
        this.logger=logger;
    }
}
