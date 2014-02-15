package org.sugarj;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;
import org.sugarj.common.ATermCommands;
import org.sugarj.common.Environment;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.errors.SourceCodeException;
import org.sugarj.common.path.Path;

public abstract class AbstractBaseProcessor implements IBaseProcessor, Serializable {

  private static final long serialVersionUID = -6712835686318143995L;

  protected HybridInterpreter interp;

  public void setInterpreter(HybridInterpreter interp) {
    this.interp = interp;
  }
  
  public HybridInterpreter getInterpreter() {
    return interp;
  }

  public String getRelativeNamespaceSep() {
		String rel = getNamespace();
		if (rel == null || rel.isEmpty())
			return "";
		return rel + Environment.sep;
	}
	
	// Returns true is files were generated or if there were no files to generate
	// Returns false if generation was skipped due to generateFiles being false
	public Set<Path> compile(
	    Path outFile, 
	    String source, 
	    Path bin,
	    List<Path> path,
			Set<Path> deferredSourceFilesForSourceFile
			) throws IOException, ClassNotFoundException, SourceCodeException {
	  
	  
    List<Path> outSourceFiles = new ArrayList<>();
    Set<Path> generatedFiles = new HashSet<>();

    outSourceFiles.addAll(deferredSourceFilesForSourceFile);
    
    if (!source.isEmpty()) {
      writeToFile(generatedFiles, outFile, source);
      outSourceFiles.add(outFile);
    }
    
    if (!outSourceFiles.isEmpty()) {
      Log.log.log("Compile desugared source files " + outSourceFiles, Log.CORE);
      List<Path> generatedByCompiler = compile(outSourceFiles, bin, path);
      for (Path p : generatedByCompiler)
        generatedFiles.add(p);
    }
    
    return generatedFiles;
 	}

  private void writeToFile(Set<Path> generatedFiles, Path file, String content) throws IOException { 
		FileCommands.writeToFile(file, content);
		generatedFiles.add(file);
	}

  public          String getImportLocalName(IStrategoTerm decl) { return null; }
  public          String getModulePath(IStrategoTerm decl) { return null; }
  public          IStrategoTerm reconstructImport(String modulePath, IStrategoTerm original) { return null; }
	
  /**
   * Computes the path of the given transformation application term.
   */
  public String getTransformedModulePath(IStrategoTerm appl) {
    if (ATermCommands.isApplication(appl, "TransApp")) {
      String trans = getTransformedModulePath(appl.getSubterm(0));
      String model = getTransformedModulePath(appl.getSubterm(1));
      return model + "__" + trans.replace('/', '_');
    }
    return getModulePath(appl);
  }
}