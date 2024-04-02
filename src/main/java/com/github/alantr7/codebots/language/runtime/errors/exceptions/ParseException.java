package com.github.alantr7.codebots.language.runtime.errors.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;

public class ParseException extends Exception {

    public ParseException(String message) {
        super(message);
    }

    @Override
    public void printStackTrace() {
        System.err.println(getMessage());
    }

    @Override
    public void printStackTrace(PrintStream s) {
        s.println(getMessage());
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        s.println(getMessage());
    }

}
