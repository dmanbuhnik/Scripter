package com.faziklogic.scripter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.Log;

public class Utils {
	public final static String TAG = "Utils";

	public static ArrayList<String> runShellCommand(String command) {
		ArrayList<String> output = new ArrayList<String>();

		Process process = null;
		BufferedReader input = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("sh");
			os = new DataOutputStream(process.getOutputStream());
			Log.d(TAG, "Running command - [" + command + "]");
			os.writeBytes(command + "\n");
			os.flush();
			os.writeBytes("exit\n");
			os.flush();
			input = new BufferedReader(new InputStreamReader(process
					.getInputStream()));
			String line;
			process.waitFor();
			while ((line = input.readLine()) != null) {
				output.add(line);
			}
			input.close();
			return output;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (os != null)
					os.close();
				if (process != null)
					process.destroy();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

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
}
