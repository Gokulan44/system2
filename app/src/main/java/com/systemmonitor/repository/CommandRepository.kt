package com.systemmonitor.repository

import com.systemmonitor.data.network.ConnectionManager
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.CommandType
import com.systemmonitor.domain.model.Laptop
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.Locale
import javax.inject.Singleton

@Singleton
class CommandRepository @Inject constructor(
    private val connectionManager: ConnectionManager
) {
    suspend fun sendPowerCommand(
        laptop: Laptop,
        commandType: CommandType,
        pin: String?
    ): NetworkResult<String> {
        if (commandType == CommandType.ON) {
            val mac = laptop.macAddress
            if (mac.isNullOrEmpty()) {
                return NetworkResult.Error("MAC Address is not registered for this laptop. Please re-pair the device to enable Wake-on-LAN.")
            }
            return withContext(Dispatchers.IO) {
                try {
                    val macBytes = getMacBytes(mac)
                    val bytes = ByteArray(6 + 16 * macBytes.size)
                    for (i in 0..5) {
                        bytes[i] = 0xff.toByte()
                    }
                    for (i in 6 until bytes.size step macBytes.size) {
                        System.arraycopy(macBytes, 0, bytes, i, macBytes.size)
                    }

                    // Send magic packet to global broadcast address on ports 7 & 9
                    val address = InetAddress.getByName("255.255.255.255")
                    val socket = DatagramSocket()
                    socket.broadcast = true

                    val packet9 = DatagramPacket(bytes, bytes.size, address, 9)
                    socket.send(packet9)

                    val packet7 = DatagramPacket(bytes, bytes.size, address, 7)
                    socket.send(packet7)

                    socket.close()
                    NetworkResult.Success("Wake-on-LAN magic packet broadcasted successfully to $mac")
                } catch (e: Exception) {
                    NetworkResult.Error("Failed to send Wake-on-LAN magic packet: ${e.message}", e)
                }
            }
        }
        return connectionManager.executePowerCommand(laptop, commandType, pin)
    }

    private fun getMacBytes(macStr: String): ByteArray {
        val cleanMac = macStr.replace("-", ":").uppercase(Locale.US)
        val hex = cleanMac.split(":")
        if (hex.size != 6) {
            throw IllegalArgumentException("Invalid MAC address format: $macStr")
        }
        val bytes = ByteArray(6)
        for (i in 0..5) {
            bytes[i] = hex[i].toInt(16).toByte()
        }
        return bytes
    }
}
