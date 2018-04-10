package martonsz.teleportdb.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import martonsz.teleportdb.wrapper.PositionDatabase;
import martonsz.teleportdb.wrapper.SavedPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;

public class CommandTpd extends CommandBase {

	private static Logger logger = LogManager.getLogger(CommandTpd.class);
	private PositionDatabase database;

	public CommandTpd() {
		this.database = new PositionDatabase();
	}

	@Override
	public String getName() {
		return "tpd";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		StringBuffer sb = new StringBuffer();
		sb.append("/tpd save [-f] [name] [public]	- Save your current position as 'name'. \n");
		sb.append("									  Use -f to force save and overwrite existing name\n");
		sb.append("								      Ff no named is specifed the 'name' is used.\n");
		sb.append("/tpd list [public/all] 			- List saved positions\n");
		sb.append("/tpd name [public]				- Teleport to named position.\n");
		return sb.toString();
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if (sender instanceof EntityPlayer) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer | sender instanceof EntityPlayerMP) {

			EntityPlayer player = (EntityPlayer) sender;
			int currentDimension = player.getEntityWorld().provider.getDimension();

			if (args.length == 0) {
				player.sendMessage(new TextComponentString(getUsage(sender)));
				return;
			}
			
			String username = sender.getName();
			if (args[0].equalsIgnoreCase("save")) {
				BlockPos blockPos = sender.getPosition();
				float pitch = player.rotationPitch;
				float rotationYaw = player.rotationYaw;
				String positionName = "home";
				SavedPosition position = new SavedPosition(username, positionName, blockPos, pitch, rotationYaw,
						currentDimension, true);
				database.savePosition(position);
				player.sendMessage(new TextComponentString("Saved your position: " + position));
			} else if (args[0].equalsIgnoreCase("tp")) {

				String positionName = "home";
				SavedPosition position = database.getPosition(username, positionName);
				BlockPos pos = position.getPosition();
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				float pitch = position.getPitch();
				float rotation = position.getRotationYaw();
				int destDimension = position.getDimension();
				WorldServer destWorld = server.getWorld(destDimension);
				if (!(destWorld == server.getWorld(currentDimension))) {
					// list.transferPlayerToDimension(senderMP, destWorldId, new
					// HomeTeleporter(destWorld, true));
					player.sendMessage(new TextComponentString("Error. Can't teleport between dimensions yet."));
					return;
				}

				// player.setPositionAndRotation(x, y, z, rotation, pitch);
				// player.setLocationAndAngles(x + 0.5, y, z + 0.5, rotation, pitch); //Doesn't
				// work all the time
				player.setPositionAndUpdate(x + 0.5, y, z + 0.5);
				player.motionX = 0;
				player.motionY = 0;
				player.motionZ = 0;
				player.sendMessage(new TextComponentString("Teleported to: " + position));
			}

		}
	}

}
