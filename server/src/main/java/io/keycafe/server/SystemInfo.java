package io.keycafe.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class SystemInfo {
    private static final Logger logger = LogManager.getLogger(SystemInfo.class);
    public static Inet4Address defaultNonLoopbackIpV4Address() {
        return DefaultNonLoopbackIPv4Address.defaultNonLoopbackIpV4Address;
    }
    private SystemInfo() {}

    private static final class DefaultNonLoopbackIPv4Address {

        // Forked from InetUtils in spring-cloud-common 3.0.0.M1 at e7bb7ed3ae19a91c6fa7b3b698dd9788f70df7d4
        static final Inet4Address defaultNonLoopbackIpV4Address;

        static {
            Inet4Address result = null;
            String nicDisplayName = null;
            try {
                int lowest = Integer.MAX_VALUE;
                for (final Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                     nics.hasMoreElements();) {
                    final NetworkInterface nic = nics.nextElement();
                    if (!nic.isUp()) {
                        logger.debug("{} is down. Trying next.", nic.getDisplayName());
                        continue;
                    }

                    // The NIC whose index is the lowest will be likely the valid IPv4 address.
                    // See https://github.com/spring-cloud/spring-cloud-commons/issues/82.
                    if (nic.getIndex() < lowest || result == null) {
                        lowest = nic.getIndex();
                    } else {
                        logger.debug("{} has higher index({}) than {}. Skip.",
                                nic.getDisplayName(), nic.getIndex(), result);
                        continue;
                    }

                    for (final Enumeration<InetAddress> addrs = nic.getInetAddresses();
                         addrs.hasMoreElements();) {
                        final InetAddress address = addrs.nextElement();
                        if (!(address instanceof Inet4Address)) {
                            logger.debug("{} of {} is not an Inet4Address. Trying next.",
                                    address, nic.getDisplayName());
                            continue;
                        }
                        if (address.isLoopbackAddress()) {
                            logger.debug("{} of {} is a loopback address. Trying next.",
                                    address, nic.getDisplayName());
                            continue;
                        }
                        result = (Inet4Address) address;
                        nicDisplayName = nic.getDisplayName();
                    }
                }
            } catch (IOException ex) {
                logger.warn("Could not get a non-loopback IPv4 address:", ex);
            }

            if (result != null) {
                defaultNonLoopbackIpV4Address = result;
                logger.info("defaultNonLoopbackIpV4Address: {} (from: {})",
                        defaultNonLoopbackIpV4Address, nicDisplayName);
            } else {
                Inet4Address temp = null;
                try {
                    final InetAddress localHost = InetAddress.getLocalHost();
                    if (localHost instanceof Inet4Address) {
                        temp = (Inet4Address) localHost;
                        logger.info("defaultNonLoopbackIpV4Address: {} (from: InetAddress.getLocalHost())",
                                temp);
                    } else {
                        logger.warn("Could not get a non-loopback IPv4 address. " +
                                "defaultNonLoopbackIpV4Address is set to null.");
                    }
                } catch (UnknownHostException e) {
                    logger.warn("Unable to retrieve the localhost address. " +
                            "defaultNonLoopbackIpV4Address is set to null.", e);
                }
                defaultNonLoopbackIpV4Address = temp;
            }
        }
    }
}
