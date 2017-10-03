package edu.utexas.libra.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MyTest {

	int foo() {
		File f = new File("dummy");
		String str = f.getPath();
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter("writePath", true)));
			out.println(str);
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
		return 1;
	}

}
