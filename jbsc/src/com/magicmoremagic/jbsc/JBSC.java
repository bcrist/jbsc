package com.magicmoremagic.jbsc;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.parser.Parser;
import com.magicmoremagic.jbsc.util.IndentingPrintWriter;

public class JBSC {
	
	private static final boolean USE_STDOUT = true;
	
	public static final Path INTERNAL_PATH = Paths.get("__internal__");
	
	public static JBSC app;
	
	private static IErrorHandler errorHandler = new ErrorHandler();
	
	private Path specDirectory;
	private Path includeOutputDirectory;
	private Path sourceOutputDirectory;
	
	private Spec internal;
	private Map<Path, Spec> parsedSpecs;
	
	public JBSC(String specDirectory, String includeOutputDirectory, String sourceOutputDirectory) throws Exception {
		this.specDirectory = getDirectoryPath(specDirectory);
		this.includeOutputDirectory = getDirectoryPath(includeOutputDirectory);
		this.sourceOutputDirectory = getDirectoryPath(sourceOutputDirectory);
		parsedSpecs = new HashMap<>();
		
		ErrorHandler errorHandler = new ErrorHandler();
		InputStreamReader reader = new InputStreamReader(JBSC.class.getResourceAsStream("/com/magicmoremagic/jbsc/internal.bs"));
		Parser parser = new Parser(reader, INTERNAL_PATH, errorHandler);
		internal = parser.parse();
		internal.setErrorCount(errorHandler.getErrorCount());
		internal.setWarningCount(errorHandler.getWarningCount());
	}
	
	public Path getDirectoryPath(String dir) throws IOException {
		Path path;
		if (dir == null || dir.isEmpty()) {
			path = Paths.get("./");
		} else {
			path = Paths.get(dir);	
		}
		
		return Files.createDirectories(path);
	}
	
	public Spec getSpec(String specFileName) {
		return getSpec(specDirectory.resolve(specFileName));
	}
	
	public Spec getInternalSpec() {
		return internal;
	}
	
	public Spec getSpec(Path specPath) {
		specPath = canonicalize(specPath);
		if (specPath == null) {
			return null;
		}
		
		if (parsedSpecs.containsKey(specPath)) {
			return parsedSpecs.get(specPath);
		}
		
		ErrorHandler errorHandler = new ErrorHandler();
		Parser parser = new Parser(specPath, errorHandler);
		Spec spec = parser.parse();
		spec.setErrorCount(errorHandler.getErrorCount());
		spec.setWarningCount(errorHandler.getWarningCount());
		parsedSpecs.put(specPath, spec);
		return spec;
	}
	
	public void addSpec(Path specPath, Spec spec) {
		specPath = canonicalize(specPath);
		if (specPath == null) {
			return;
		}
		
		parsedSpecs.put(specPath, spec);
	}
	
	private Path canonicalize(Path specPath) {
		try {	
			if (!Files.exists(specPath) || Files.isDirectory(specPath)) {
				errorHandler.handleError(ErrorCategory.FILE, ErrorType.FILE_NOT_FOUND, specPath.toString(), null);
				return null;
			}
			
			specPath = specPath.toRealPath();
		} catch (IOException e) {
			errorHandler.handleError(ErrorCategory.FILE, ErrorType.IO_EXCEPTION, specPath.toString(), e);
			return null;
		}
		
		return specPath;
	}
	
	private PrintWriter getFilePrintWriter(Path directory, String filename) throws FileNotFoundException {
		Path path = directory.resolve(filename);
		try {
			return new IndentingPrintWriter(path.toFile(), "UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF8 not supported", e);
		}
	}
	
	public static void main(String[] args) {
		String globStr = "*.bs";
		String parentDir = "";
		String specDir = "bs/";
		String includeDir = "include/";
		String srcDir = "src/";
		
		for (int i = 0, n = 0; i < args.length; ++i) {
			String param = args[i];
			if (param.endsWith(".bs")) {
				globStr = param;
			} else {
				switch (n) {
				case 0:	parentDir = param; break;
				case 1: specDir = param; break;
				case 2: includeDir = param; break;
				case 3: srcDir = param; break;
				}
				++n;
			}
		}
		
		globStr = "glob:" + globStr;
		
		specDir = Paths.get(parentDir, specDir).toString();
		includeDir = Paths.get(parentDir, includeDir).toString();
		srcDir = Paths.get(parentDir, srcDir).toString();
		
		try {
			app = new JBSC(specDir, includeDir, srcDir);
		
			final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(globStr);
			Files.walkFileTree(app.specDirectory, new SimpleFileVisitor<Path>() {
				
				@SuppressWarnings("unused")
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					if (pathMatcher.matches(path.getFileName())) {
						
						Spec spec = app.getSpec(path);
						
						if ((spec.getErrorCount() > 0 || spec.getWarningCount() > 0) && !USE_STDOUT) {
							return FileVisitResult.CONTINUE;
						}
						
						for (OutputFileType type : OutputFileType.values()) {
							
							if (!type.shouldPrintCode(spec))
								continue;
							
							PrintWriter writer;
							if (USE_STDOUT) {
								writer = new IndentingPrintWriter(System.out, true);
								
								writer.write("@@@@@ ");
								String filename = spec.getOutputFileName(type);
								writer.print(filename);
								writer.print(' ');
								for (int i = 72 - filename.length(); i > 0; --i) {
									writer.print('@');
								}
								writer.println();
							} else {
								Path directory = type.shouldUseSourceDirectory()
										? app.sourceOutputDirectory : app.includeOutputDirectory;
								
								writer = app.getFilePrintWriter(directory, spec.getOutputFileName(type));
							}
							
							spec.visit(type.getPrintVisitor(spec, writer));
							writer.flush();
						}
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
					if (pathMatcher.matches(path.getFileName())) {
						errorHandler.handleError(ErrorCategory.FILE, ErrorType.IO_EXCEPTION, "A problem occurred accessing " + path.toString(), exc);
					}
					return FileVisitResult.CONTINUE;
				}
			});
			
		} catch (Exception e) {
			errorHandler.handleError(ErrorCategory.ERROR, ErrorType.UNKNOWN, e.getMessage(), e);
			System.exit(-1);
		}
	}
	
	private static class ErrorHandler implements IErrorHandler {
		
		private int warningCount;
		private int errorCount;
		
		@Override
		public void handleError(ErrorCategory category, ErrorType type, String message, Exception ex) {
			if (type == ErrorType.WARNING)
				++warningCount;
			else
				++errorCount;
			
			System.err.print(category + "." + type);
			if (message != null) {
				System.err.print(": " + message);
			}
			System.err.println();
			if (ex != null) {
				ex.printStackTrace();
			}
		}
		
		public int getErrorCount() {
			return errorCount;
		}
		
		public int getWarningCount() {
			return warningCount;
		}
		
	}

}
