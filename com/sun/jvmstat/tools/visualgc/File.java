package com.sun.jvmstat.tools.visualgc;
import java.util.Calendar;
import java.io.*;

class File {
  // private BufferedWriter writer;
  private FileWriter writer;
  private String File;

  public File(String FileName) {
	this.File = FileName;
  }

  public void AppendToFile(String FileName, String Data) {
     if (this.writer == null || this.File.compareTo(FileName) != 0) {
        try {
           if (this.writer != null) {
              this.writer.close();
           }

           FileWriter out = new FileWriter(FileName, true);
           // PrintWriter out = new PrintWriter(fstream, true);

           out.write(Data + "\n");
           this.writer = out;
           // this.File = FileName;
           // out.close();
	   // System.out.println("Writing: "+ Data);
        } catch (Exception e) {
           System.err.println("Error: " + e.getMessage());
        }
     } else {
           try {
              this.writer.write(Data + "\n");
              this.writer.flush();
	      // System.out.println("Writing: "+ Data);
           } catch (Exception e) {
              System.err.println("Error: " + e.getMessage());
           }
     }
  }
 
  public String Stamp() {
        Calendar cal = Calendar.getInstance();
        // int Min=cal.get(Calendar.MINUTE);
        // int Hour=cal.get(Calendar.HOUR_OF_DAY);
	// Date date = new Date();
        int Day=cal.get(Calendar.DATE);
        int Month=cal.get(Calendar.MONTH) + 1; // In Current date Add 1 in month
        int Year=cal.get(Calendar.YEAR);
	// %tn%td%tY
        // String X = String.format("%4s%2s%2s-%2s%2s", Year,  Month, Day), Hour, Min);
        String Stamp = String.format("%04d%02d%02d", Year,  Month, Day);
	return Stamp;
  }

  public String FileName() {
	return this.File;
  }
}
