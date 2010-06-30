package com.faziklogic.scripter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.Log;

public class ShellInterface {
	public final static String TAG = "ShellInterface";

	private String[] path = null;
	private String suBinary;
	private String busyboxBinary;

	public ShellInterface(String suBinary, String busyboxBinary) {
		if (path == null)
			setPath();
		if (suBinary == null)
			setSuBinary();
		else
			this.suBinary = suBinary;
		if (busyboxBinary == null)
			setBusyboxBinary();
		else
			this.busyboxBinary = busyboxBinary;
	}

	private void setPath() {
		ArrayList<String> output = Utils.runShellCommand("echo $PATH");
		if (output != null) {
			path = output.get(0).split(":");
		}
	}

	private void setSuBinary() {
		for (String binPath : path)
			if (new File(binPath + "/su").exists())
				suBinary = binPath + "/su";
	}

	private void setBusyboxBinary() {
		for (String binPath : path)
			if (new File(binPath + "/busybox").exists())
				this.busyboxBinary = binPath + "/busybox";
	}

	public String[] getPath() {
		return path;
	}

	public String getSuBinary() {
		return suBinary;
	}

	public String getBusyboxBinary() {
		return busyboxBinary;
	}

	public boolean isSuAvailable() {
		if (this.suBinary == null)
			return false;
		return true;
	}

	public boolean isBusyboxAvailable() {
		if (this.busyboxBinary == null)
			return false;
		return true;
	}

	public synchronized boolean runCommands(ArrayList<String> commands) {
		Process process = null;
		BufferedReader input = null;
		DataOutputStream os = null;
		try {
			if (suBinary == null)
				return false;
			process = Runtime.getRuntime().exec(suBinary);
			os = new DataOutputStream(process.getOutputStream());
			for (String single : commands) {
				Log.d(TAG, "Running command - [" + single + "]");
				os.writeBytes(single + "\n");
				os.flush();
			}
			os.writeBytes("exit\n");
			os.flush();
			input = new BufferedReader(new InputStreamReader(process
					.getInputStream()));
			String line;
			process.waitFor();
			while ((line = input.readLine()) != null) {
				Log.d(TAG, "output - [" + line + "]");
			}
			input.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (os != null)
					os.close();
				if (process != null)
					process.destroy();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
