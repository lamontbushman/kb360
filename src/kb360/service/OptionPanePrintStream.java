package kb360.service;

import java.io.*;
import javax.swing.*;


public class OptionPanePrintStream extends PrintStream
{
    public OptionPanePrintStream(OutputStream out)
    {
	super(out);
    }

    public void print(String string)
    {
	JOptionPane.showMessageDialog(null,string);
    }

    public void println(String string)
    {
	print(string);
    }
}
