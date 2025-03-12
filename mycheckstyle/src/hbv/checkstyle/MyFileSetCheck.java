package hbv.checkstyle;

import java.io.File;
import java.util.List;
import com.puppycrawl.tools.checkstyle.api.*;

public class MyFileSetCheck extends AbstractFileSetCheck {
  private static final int DEFAULT_MAX = 100;
  private int fileCount;
  private int max = DEFAULT_MAX;
  public void setMax(int aMax){
    this.max = aMax;
  }

  @Override
  public void beginProcessing(String aCharset){
    super.beginProcessing(aCharset);
    this.fileCount = 0;
  }

  @Override
  public void processFiltered(File file, FileText fileText){
    this.fileCount++;

    if (this.fileCount > this.max) {
      log(0, "max.files.exceeded "+ Integer.valueOf(this.max));
    }
  }
}

