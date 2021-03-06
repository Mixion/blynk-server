package cc.blynk.integration.tcp;

import cc.blynk.integration.SingleServerInstancePerTest;
import cc.blynk.server.core.model.device.Device;
import cc.blynk.server.core.model.device.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static cc.blynk.integration.TestUtil.createDevice;
import static cc.blynk.integration.TestUtil.illegalCommandBody;
import static cc.blynk.integration.TestUtil.ok;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/2/2015.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceCommandsTest extends SingleServerInstancePerTest {

    @Test
    public void testAddNewDevice() throws Exception {
        Device device0 = new Device(0, "My Dashboard", "UNO");
        device0.status = Status.ONLINE;
        Device device1 = new Device(1, "My Device", "ESP8266");
        device1.status = Status.OFFLINE;

        clientPair.appClient.createDevice(1, device1);
        Device device = clientPair.appClient.parseDevice();
        assertNotNull(device);
        assertNotNull(device.token);
        clientPair.appClient.verifyResult(createDevice(1, device));

        clientPair.appClient.reset();

        clientPair.appClient.send("getDevices 1");

        Device[] devices = clientPair.appClient.parseDevices();
        assertNotNull(devices);
        assertEquals(2, devices.length);

        assertEqualDevice(device0, devices[0]);
        assertEqualDevice(device1, devices[1]);
    }

    @Test
    public void testUpdateExistingDevice() throws Exception {
        Device device0 = new Device(0, "My Dashboard Updated", "UNO");
        device0.status = Status.ONLINE;

        clientPair.appClient.updateDevice(1, device0);
        clientPair.appClient.verifyResult(ok(1));

        clientPair.appClient.reset();

        clientPair.appClient.send("getDevices 1");
        Device[] devices = clientPair.appClient.parseDevices();

        assertNotNull(devices);
        assertEquals(1, devices.length);

        assertEqualDevice(device0, devices[0]);
    }

    @Test
    public void testUpdateNonExistingDevice() throws Exception {
        Device device = new Device(100, "My Dashboard Updated", "UNO");

        clientPair.appClient.updateDevice(1, device);
        clientPair.appClient.verifyResult(illegalCommandBody(1));
    }

    @Test
    public void testGetDevices() throws Exception {
        Device device0 = new Device(0, "My Dashboard", "UNO");
        device0.status = Status.ONLINE;

        clientPair.appClient.send("getDevices 1");

        Device[] devices = clientPair.appClient.parseDevices();
        assertNotNull(devices);
        assertEquals(1, devices.length);

        assertEqualDevice(device0, devices[0]);
    }

    @Test
    public void testTokenNotUpdatedForExistingDevice() throws Exception {
        Device device0 = new Device(0, "My Dashboard", "UNO");
        device0.status = Status.ONLINE;

        clientPair.appClient.send("getDevices 1");

        Device[] devices = clientPair.appClient.parseDevices();
        assertNotNull(devices);
        assertEquals(1, devices.length);

        assertEqualDevice(device0, devices[0]);
        String token = devices[0].token;

        device0.name = "My Dashboard UPDATED";
        device0.token = "123";

        clientPair.appClient.updateDevice(1, device0);
        clientPair.appClient.verifyResult(ok(2));

        clientPair.appClient.reset();

        clientPair.appClient.send("getDevices 1");
        devices = clientPair.appClient.parseDevices();

        assertNotNull(devices);
        assertEquals(1, devices.length);

        assertEqualDevice(device0, devices[0]);
        assertEquals("My Dashboard UPDATED", devices[0].name);
        assertEquals(token, devices[0].token);
    }


    @Test
    public void testDeletedNewlyAddedDevice() throws Exception {
        Device device0 = new Device(0, "My Dashboard", "UNO");
        device0.status = Status.ONLINE;
        Device device1 = new Device(1, "My Device", "ESP8266");
        device1.status = Status.OFFLINE;

        clientPair.appClient.createDevice(1, device1);
        Device device = clientPair.appClient.parseDevice();
        assertNotNull(device);
        assertNotNull(device.token);
        clientPair.appClient.verifyResult(createDevice(1, device));

        clientPair.appClient.reset();

        clientPair.appClient.send("getDevices 1");

        Device[] devices = clientPair.appClient.parseDevices();
        assertNotNull(devices);
        assertEquals(2, devices.length);

        assertEqualDevice(device0, devices[0]);
        assertEqualDevice(device1, devices[1]);

        clientPair.appClient.send("deleteDevice 1\0" + device1.id);
        clientPair.appClient.verifyResult(ok(2));

        clientPair.appClient.reset();

        clientPair.appClient.send("getDevices 1");
        devices = clientPair.appClient.parseDevices();

        assertNotNull(devices);
        assertEquals(1, devices.length);

        assertEqualDevice(device0, devices[0]);
    }

    private static void assertEqualDevice(Device expected, Device real) {
        assertEquals(expected.id, real.id);
        //assertEquals(expected.name, real.name);
        assertEquals(expected.boardType, real.boardType);
        assertNotNull(real.token);
        assertEquals(expected.status, real.status);
    }

}
