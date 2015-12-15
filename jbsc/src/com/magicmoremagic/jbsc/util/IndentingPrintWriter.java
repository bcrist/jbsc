package com.magicmoremagic.jbsc.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class IndentingPrintWriter extends PrintWriter {

	private int indentCount;
	boolean autoFlush;
	
	public IndentingPrintWriter(File file) throws FileNotFoundException {
		super(file);
	}
	
	public IndentingPrintWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(file, csn);
	}
	
	public IndentingPrintWriter(OutputStream out) {
		super(out);
	}
	
	public IndentingPrintWriter(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
		this.autoFlush = autoFlush;
	}
	
	public IndentingPrintWriter(String fileName) throws FileNotFoundException {
		super(fileName);
	}
	
	public IndentingPrintWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(fileName, csn);
	}
	
	public IndentingPrintWriter(Writer out) {
		super(out);
	}
	
	public IndentingPrintWriter(Writer out, boolean autoFlush) {
		super(out, autoFlush);
		this.autoFlush = autoFlush;
	}

	public void indent() {
		++indentCount;
	}
	
	public void unindent() {
		indentCount = Math.max(0, indentCount - 1);
	}
	
	public void setIndent(int indent) {
		indentCount = Math.max(0, indent);
	}
	
	public int getIndent() {
		return indentCount;
	}
	
	@Override
	public void println() {
		 try {
            synchronized (lock) {
                ensureOpen();
                out.write(CodeGenConfig.NL);
                for (int i = 0; i < indentCount; ++i) {
        			out.write(CodeGenConfig.INDENT);
        		}
                if (autoFlush)
                    out.flush();
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            setError();
        }
	}

	@Override
	public void println(boolean x) {
		synchronized (lock) {
			print(x);
			println();
		}
	}
	
	@Override
	public void println(char x) {
		synchronized (lock) {
			print(x);
			println();
		}
	}
	
	@Override
	public void println(char[] x) {
		synchronized (lock) {
			print(x);
			println();
		}
	}
	
	@Override
	public void println(double x) {
		synchronized (lock) {
			print(x);
			println();
		}
	}
	
	@Override
	public void println(float x) {
		synchronized (lock) {
			print(x);
			println();
		}
	}
	
	@Override
	public void println(int x) {
		synchronized (lock) {
			print(x);
			println();
		}
	}
	
	@Override
	public void println(long x) {
		synchronized (lock) {
			print(x);
			println();
		}
	}
	
	@Override
	public void println(Object x) {
		synchronized (lock) {
			print(x);
			println();
		}
	}
	
	@Override
	public void println(String x) {
		synchronized (lock) {
			print(x);
			println();
		}
	}
	
    /** Checks to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (out == null)
            throw new IOException("Stream closed");
    }
	
}
