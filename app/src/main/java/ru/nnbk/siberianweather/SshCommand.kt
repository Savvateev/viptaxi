package ru.nnbk.siberianweather

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

suspend fun sshCommand(command: String)
        : String = withContext(Dispatchers.IO) {
    val host = "18.242.118.175"
    val username = "admin"
    val password = "728178@support"
    val jsch = JSch()
    var session: Session? = null
    var channel: ChannelExec? = null
    try {
        session = jsch.getSession(username, host, 22).apply {
            setPassword(password)
            setConfig("StrictHostKeyChecking", "no")
            connect(10000)
        }
        channel = session.openChannel("exec") as ChannelExec
        channel.setCommand(command)
        channel.inputStream = null
        val input = channel.inputStream
        channel.connect(5000)
        val reader = BufferedReader(InputStreamReader(input))
        val output = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            output.append(line).append("\n")
            line = reader.readLine()
        }
        output.toString().trim()
    } finally {
        channel?.disconnect()
        session?.disconnect()
    }
}