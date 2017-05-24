/***************************************************************************
 *                                                                         *
 *          ###########   ###########   ##########    ##########           *
 *         ############  ############  ############  ############          *
 *         ##            ##            ##   ##   ##  ##        ##          *
 *         ##            ##            ##   ##   ##  ##        ##          *
 *         ###########   ####  ######  ##   ##   ##  ##    ######          *
 *          ###########  ####  #       ##   ##   ##  ##    #    #          *
 *                   ##  ##    ######  ##   ##   ##  ##    #    #          *
 *                   ##  ##    #       ##   ##   ##  ##    #    #          *
 *         ############  ##### ######  ##   ##   ##  ##### ######          *
 *         ###########    ###########  ##   ##   ##   ##########           *
 *                                                                         *
 *            S E C U R E   M O B I L E   N E T W O R K I N G              *
 *                                                                         *
 * License:                                                                *
 *                                                                         *
 * Copyright (c) 2017 Secure Mobile Networking Lab (SEEMOO)                *
 *                                                                         *
 * Permission is hereby granted, free of charge, to any person obtaining a *
 * copy of this software and associated documentation files (the           *
 * "Software"), to deal in the Software without restriction, including     *
 * without limitation the rights to use, copy, modify, merge, publish,     *
 * distribute, sublicense, and/or sell copies of the Software, and to      *
 * permit persons to whom the Software is furnished to do so, subject to   *
 * the following conditions:                                               *
 *                                                                         *
 * 1. The above copyright notice and this permission notice shall be       *
 *    include in all copies or substantial portions of the Software.       *
 *                                                                         *
 * 2. Any use of the Software which results in an academic publication or  *
 *    other publication which includes a bibliography must include         *
 *    citations to the nexmon project a) and the paper cited under b):     *
 *                                                                         *
 *    a) "Matthias Schulz, Daniel Wegemer and Matthias Hollick. Nexmon:    *
 *        The C-based Firmware Patching Framework. https://nexmon.org"     *
 *                                                                         *
 *    b) "Matthias Schulz, Francesco Gringoli, Daniel Steinmetzer, Michael *
 *        Koch and Matthias Hollick. Massive Reactive Smartphone-Based     *
 *        Jamming using Arbitrary Waveforms and Adaptive Power Control.    *
 *        Proceedings of the 10th ACM Conference on Security and Privacy   *
 *        in Wireless and Mobile Networks (WiSec 2017), July 2017."        *
 *                                                                         *
 * 3. The Software is not used by, in cooperation with, or on behalf of    *
 *    any armed forces, intelligence agencies, reconnaissance agencies,    *
 *    defense agencies, offense agencies or any supplier, contractor, or   *
 *    research associated.                                                 *
 *                                                                         *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS *
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF              *
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  *
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY    *
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,    *
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE       *
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                  *
 *                                                                         *
 **************************************************************************/

package de.seemoo.nexmon.jammer.utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by matthias on 24.05.17.
 */

public class Nexutil {
    private static Shell.Interactive rootShell;
    private static boolean isInitialised = false;
    private static Context activity;

    public Nexutil(Context activity) {
        this.activity = activity;
        rootShell = new Shell.Builder().
                useSU().
                setWantSTDERR(false).
                setMinimalLogging(true).
                open(new Shell.OnCommandResultListener() {
                    @Override
                    public void onCommandResult(int commandVal, int exitVal, List<String> out) {
                        //Callback checking successful shell start.
                        if (exitVal == Shell.OnCommandResultListener.SHELL_RUNNING) {
                            isInitialised = true;
                            Log.i("NEXUTIL", "Superuser initialized");
                        } else {
                            Toast.makeText(Nexutil.activity.getApplicationContext(), "Root privileges are needed. Please grant root permissions or try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static boolean isInitialized() {
        return isInitialised;
    }

    public static String getIoctl(int cmd, int length) {
        List<String> out = Shell.SU.run("nexutil -l" + length + " -g" + cmd);
        return out.toString();
    }

    public static String getIoctl(int cmd) {
        List<String> out = Shell.SU.run("nexutil -g" + cmd);
        return out.toString();
    }

    public static String setIoctl(int cmd, int value) {
        List<String> out = Shell.SU.run("nexutil -s" + cmd + " -l4 -i -v" + value);
        return out.toString();
    }

    public static String setIoctl(int cmd, String value) {
        byte[] valueBytes = value.getBytes();
        byte[] valueBytesTerminated = new byte[valueBytes.length + 1];
        System.arraycopy(valueBytes, 0, valueBytesTerminated, 0, valueBytes.length);
        return setIoctl(cmd, valueBytesTerminated);
    }

    public static String setIoctl(int cmd, byte buf[]) {
        String value = Base64.encodeToString(buf, Base64.NO_WRAP);
        Log.i("Nexutil", value);
        List<String> out = Shell.SU.run("nexutil -s" + cmd + " -b -l" + buf.length + " -v\"" + value + "\"");
        return out.toString();
    }
}
