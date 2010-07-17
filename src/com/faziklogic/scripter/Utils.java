package com.faziklogic.scripter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {
	public final static String TAG = "Utils";

	public static String readFile(String file) {
		try {
			FileReader inputFile = new FileReader(file);
			BufferedReader bufRead = new BufferedReader(inputFile);
			String output = "";
			String s;
			while ((s = bufRead.readLine()) != null) {
				output += s + "\n";
			}
			bufRead.close();
			inputFile.close();
			if (output.equals(""))
				return null;
			return output;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static boolean writeFile(String file, String content) {
		try {
			File fOut = new File(file);
			FileWriter fWriter = new FileWriter(fOut);
			BufferedWriter out = new BufferedWriter(fWriter);
			out.write(content);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}