package ru.muffinnorth.w4j.controller;

import jep.JepConfig;
import jep.SharedInterpreter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public abstract class PyBridge {
    protected OutputStream stream = new ByteArrayOutputStream();


    public PyBridge(){
        JepConfig config = new JepConfig();
        config.redirectStdout(System.out);
        SharedInterpreter.setConfig(config);
    }
}
