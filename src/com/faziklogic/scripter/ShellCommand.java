package com.faziklogic.scripter;

/**
 * ShellCommand.java runs commands as if in a native shell instance, and can return stdio.
 *
 * Code by Kevin(at)TeslaCoil
 * Adapted by LouZiffer(at)SDX
 *
 * Example usage (use cmd.su.runWaitFor instead of cmd.sh.runWaitFor to run as su):
 *
 * ShellCommand cmd = new ShellCommand();
 * CommandResult r = cmd.sh.runWaitFor("/system/bin/getprop wifi.interface");
 *
 * if (!r.success()) {
 *     Log.d(MSG_TAG, "Error " + r.stderr);
 * } else {
 *     Log.d(MSG_TAG, "Successfully executed getprop wifi.interface. Result: " + r.stdout);
 *     this.tetherNetworkDevice = (r.stdout);
 * }
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import android.util.Log;

public class ShellCommand {
	private static final String TAG = "ShellCommand";
	private Boolean can_su;

	public SH sh;
	public SH su;

	public ShellCommand() {
		sh = new SH("sh");
		su = new SH("su");
	}

	public boolean canSU() {
		return canSU(false);
	}

	public boolean canSU(boolean force_check) {
		if (can_su == null || force_check) {
			CommandResult r = su.runWaitFor("id");
			StringBuilder out = new StringBuilder();

			if (r.stdout != null)
				out.append(r.stdout).append(" ; ");
			if (r.stderr != null)
				out.append(r.stderr);

			Log.v(TAG, "canSU() su[" + r.exit_value + "]: " + out);
			can_su = r.success();
		}
		return can_su;
	}

	public boolean hasBB() {
		CommandResult r = su.runWaitFor("busybox");
		StringBuilder out = new StringBuilder();

		if (r.stdout != null)
			out.append(r.stdout).append(" ; ");
		if (r.stderr != null)
			out.append(r.stderr);

		Log.v(TAG, "hasBB() busybox[" + r.exit_value + "]: "
				+ out.toString().split("\n")[0]);
		return r.success();
	}

	public SH suOrSH() {
		return canSU() ? su : sh;
	}

	public class CommandResult {
		public final String stdout;
		public final String stderr;
		public final Integer exit_value;

		CommandResult(Integer exit_value_in, String stdout_in, String stderr_in) {
			exit_value = exit_value_in;
			stdout = stdout_in;
			stderr = stderr_in;
		}

		CommandResult(Integer exit_value_in) {
			this(exit_value_in, null, null);
		}

		public boolean success() {
			if ((stderr != null) && (!stderr.equals("")))
				return false;
			return exit_value != null && exit_value == 0;
		}
	}

	public class SH {
		private String SHELL = "sh";

		public SH(String SHELL_in) {
			SHELL = SHELL_in;
		}

		public Process run(String s) {
			Process process = null;
			try {
				process = Runtime.getRuntime().exec(SHELL);
				DataOutputStream toProcess = new DataOutputStream(process
						.getOutputStream());
				for (String command : s.split("\n")) {
					Log.i(TAG, "command [" + command + "]");
					toProcess.writeBytes(command + "\n");
					toProcess.flush();
				}
				toProcess.writeBytes("exit\n");
				toProcess.flush();
			} catch (Exception e) {
				Log.e(TAG, "Exception while trying to run: '" + s + "' "
						+ e.getMessage());
				process = null;
			}
			return process;
		}

		private String getStreamLines(InputStream is) {
			String out = null;
			StringBuffer buffer = null;
			DataInputStream dis = new DataInputStream(is);

			try {
				buffer = new StringBuffer();
				while (dis.available() > 0) {
					String line = dis.readLine().trim();
					if ((!line.equals("\n") && !line.equals(""))) {
						buffer.append("\n").append(line);
						Log.i(TAG, "output [" + line + "]");
					}
				}
				dis.close();
			} catch (Exception ex) {
				Log.e(TAG, ex.getMessage());
			}
			if ((buffer != null) && (buffer.length() > 0))
				out = buffer.toString().substring(1);
			return out;
		}

		public CommandResult runWaitFor(String s) {
			Process process = run(s);
			Integer exit_value = null;
			String stdout = null;
			String stderr = null;
			if (process != null) {
				try {
					exit_value = process.waitFor();

					stdout = getStreamLines(process.getInputStream());
					stderr = getStreamLines(process.getErrorStream());

				} catch (InterruptedException e) {
					Log.e(TAG, "runWaitFor " + e.toString());
				} catch (NullPointerException e) {
					Log.e(TAG, "runWaitFor " + e.toString());
				}
			}
			return new CommandResult(exit_value, stdout, stderr);
		}
	}
}
