/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.clemensbartz.chattychimpchat.adb;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.TimeoutException;
import com.google.common.collect.Lists;

import com.android.SdkConstants;
import de.clemensbartz.chattychimpchat.core.IChimpBackend;
import de.clemensbartz.chattychimpchat.core.IChimpDevice;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Backend implementation that works over ADB to talk to the device.
 */
public class AdbBackend implements IChimpBackend {
    private static Logger LOG = Logger.getLogger(AdbBackend.class.getCanonicalName());
    // How long to wait each time we check for the device to be connected.
    private static final int CONNECTION_ITERATION_TIMEOUT_MS = 200;
    private final List<IChimpDevice> devices = Lists.newArrayList();
    private final AndroidDebugBridge bridge;
    private final boolean initAdb;

    /**
     * Constructs an AdbBackend with default options.
     */
    public AdbBackend() {
        this(null, false);
    }

    /**
     * Constructs an AdbBackend.
     *
     * @param adbLocation The location of the adb binary. If null, AdbBackend will
     *     attempt to find the binary by itself.
     * @param noInitAdb If true, AdbBackend will not initialize AndroidDebugBridge.
     */
    public AdbBackend(String adbLocation, boolean noInitAdb) {
        this.initAdb = !noInitAdb;

        // [try to] ensure ADB is running
        if (adbLocation == null) {
            adbLocation = findAdb();
        }

        if (initAdb) {
            AndroidDebugBridge.init(false /* debugger support */);
        }

        bridge = AndroidDebugBridge.createBridge(
                adbLocation, true /* forceNewBridge */);
    }

    private String findAdb() {
        String mrParentLocation =
            System.getProperty("com.android.monkeyrunner.bindir"); //$NON-NLS-1$


        // in the new SDK, adb is in the platform-tools, but when run from the command line
        // in the Android source tree, then adb is next to monkeyrunner.
        if (mrParentLocation != null && mrParentLocation.length() != 0) {
            // check if there's a platform-tools folder
            File platformTools = new File(new File(mrParentLocation).getParent(),
                    SdkConstants.FD_PLATFORM_TOOLS);
            if (platformTools.isDirectory()) {
                return platformTools.getAbsolutePath() + File.separator + SdkConstants.FN_ADB;
            }

            return mrParentLocation + File.separator + SdkConstants.FN_ADB;
        }

        return SdkConstants.FN_ADB;
    }

    /**
     * Checks the attached devices looking for one whose device id matches the specified regex.
     *
     * @param deviceIdRegex the regular expression to match against
     * @return the Device (if found), or null (if not found).
     */
    private IDevice findAttachedDevice(String deviceIdRegex) {
        Pattern pattern = Pattern.compile(deviceIdRegex);
        for (IDevice device : bridge.getDevices()) {
            String serialNumber = device.getSerialNumber();
            if (pattern.matcher(serialNumber).matches() || serialNumber.equals(deviceIdRegex)) {
                return device;
            }
        }
        return null;
    }

    public IChimpDevice waitForConnection() throws IOException,
            AdbCommandRejectedException, InterruptedException, TimeoutException {
        return waitForConnection(Integer.MAX_VALUE, ".*");
    }

    public IChimpDevice waitForConnection(long timeoutMs, String deviceIdRegex) throws IOException,
            AdbCommandRejectedException, InterruptedException, TimeoutException
    {
        // Check for default values to guarantee at least two device checking cycles
        if (timeoutMs < CONNECTION_ITERATION_TIMEOUT_MS) {
            timeoutMs = CONNECTION_ITERATION_TIMEOUT_MS + 1;
        }
        do {
            IDevice device = findAttachedDevice(deviceIdRegex);
            // Only return the device when it is online
            if (device != null && device.getState() == IDevice.DeviceState.ONLINE) {
                IChimpDevice chimpDevice = new AdbChimpDevice(device);
                devices.add(chimpDevice);
                return chimpDevice;
            }

            Thread.sleep(CONNECTION_ITERATION_TIMEOUT_MS);
            timeoutMs -= CONNECTION_ITERATION_TIMEOUT_MS;
        } while (timeoutMs > 0);

        // Timeout.  Give up.
        return null;
    }

    public void shutdown() throws IOException {
        for (IChimpDevice device : devices) {
            device.dispose();
        }
        if (initAdb) {
            AndroidDebugBridge.terminate();
        }
    }
}
