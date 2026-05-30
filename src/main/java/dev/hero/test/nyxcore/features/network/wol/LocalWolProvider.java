package dev.hero.test.nyxcore.features.network.wol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import dev.hero.test.nyxcore.config.ProviderConstants;
import dev.hero.test.nyxcore.exceptions.ActionExecutionException;
import dev.hero.test.nyxcore.dto.ProviderResult;

@Component
@ConditionalOnProperty(
        name = ProviderConstants.Network.WOL_MODE,
        havingValue = ProviderConstants.Values.LOCAL,
        matchIfMissing = true
)
public class LocalWolProvider implements WolProvider {

    @Override
    public ProviderResult wake(String macStr) {
        byte[] macBytes = getMacBytes(macStr);
        byte[] bytes = new byte[6 + 16 * macBytes.length];

        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }

        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        try {
            InetAddress address = InetAddress.getByName("255.255.255.255");
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);

            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                socket.send(packet);
            }
            return ProviderResult.success("Wake-on-LAN magic packet sent to " + macStr, -1, "");
        } catch (IOException e) {
            throw new ActionExecutionException("Failed to send Wake-on-LAN packet.", e);
        }
    }

    private byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");

        if (hex.length != 6) {
            throw new IllegalArgumentException("Format must be AA:BB:CC:DD:EE:FF");
        }

        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit.");
        }
        return bytes;
    }
}