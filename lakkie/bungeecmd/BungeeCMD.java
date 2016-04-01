package lakkie.bungeecmd;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

public class BungeeCMD extends JavaPlugin implements PluginMessageListener {

	private Permission use = new Permission("bungeecmd.use");

	public void onEnable() {
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
	}

	public void onPluginMessageReceived(String channel, Player p, byte[] message) {
		if (!channel.equals("BungeeCord"))
			return;
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		Bukkit.getLogger().info(ChatColor.GREEN + "Received a command from BungeeCMD saying: " + in.readUTF());
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), in.readUTF());
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		boolean bypass = false;
		if (!(sender instanceof Player))
			bypass = true;

		if (sender.isOp())
			return true;

		if (label.equalsIgnoreCase("bungeecmd")) {
			if (!bypass) {
				Player p = (Player) sender;
				if (!p.hasPermission(use)) {
					sender.sendMessage(ChatColor.RED + "You do not have permission!");
					return false;
				}
			}
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "Usage: /bungeecmd <command>");
				return false;
			}
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);
			String msg = "";
			for (int i = 0; i < args.length; i++) {
				if (i == 0) {
					msg = msg + args[i];
					continue;
				} else {
					msg = msg + " " + args[i];
				}
			}
			try {
				out.writeUTF("Forward");
				out.writeUTF("ALL");
				out.writeUTF(msg);

				ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
				DataOutputStream msgout = new DataOutputStream(msgbytes);

				msgout.writeUTF(msg);
				msgout.writeShort(123);

				out.write(msgbytes.toByteArray());
				out.writeShort(msgbytes.toByteArray().length);

				Bukkit.getServer().sendPluginMessage(this, "BungeeCord", b.toByteArray());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

}
