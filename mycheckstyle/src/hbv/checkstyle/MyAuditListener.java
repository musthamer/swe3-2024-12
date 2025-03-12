package hbv.checkstyle;

import com.puppycrawl.tools.checkstyle.*;
import com.puppycrawl.tools.checkstyle.api.*;
import java.io.*;

public class MyAuditListener implements AuditListener {
  int errors = 0;
  PrintWriter out = new PrintWriter(System.out, true);

  public void setFile(String name) {
    try {
      out = new PrintWriter(new FileOutputStream(name), true);
      out.println("Filename being set:" + name);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public void addError(AuditEvent e) {
    // you could collect or print more information
    // out.println(e.getLine()+" "+e.getColumn()+" "+e.getFileName()+" "+e.getMessage());
    errors++;
  }

  public void addException(AuditEvent e, Throwable t) {}

  public void finishLocalSetup() {}

  public void fileStarted(AuditEvent e) {
    out.println("file start :" + e.getFileName());
  }

  public void fileFinished(AuditEvent e) {
    out.println("file finish:" + e.getFileName());
  }

  public void auditStarted(AuditEvent e) {
    out.println("audit started");
  }

  public void auditFinished(AuditEvent e) {
    out.println("audit finished with errors/warnings:" + errors);
  }
}
