package org.sugarj.baselang;

import java.util.List;

import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public interface IORelay {
  public RelativePath createOutPath(String relativePath);
  public List<Path> getSourcePath();
  public void addToIncludePath(Path p);
}
