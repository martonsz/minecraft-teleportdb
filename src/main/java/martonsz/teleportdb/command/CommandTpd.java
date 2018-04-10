package martonsz.teleportdb.command;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import martonsz.teleportdb.wrapper.PositionDatabase;
import martonsz.teleportdb.wrapper.SavedPosition;
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
	// private PositionDatabase database;

	// public CommandTpd() {
	// // database = new PositionDatabase();
	// }

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

			PositionDatabase database = PositionDatabase.get();
			EntityPlayer player = (EntityPlayer) sender;
			try {
				int currentDimension = player.getEntityWorld().provider.getDimension();

				Set<String> commands = parseArgs(args);
				boolean isPublic = commands.remove("public");
				String username = sender.getName();

				if (commands.remove("save")) {
					/*
					 * save
					 */
					boolean force = commands.remove("-f");
					Optional<String> findFirst = commands.stream().findFirst();
					String positionName = findFirst.orElse("home");

					SavedPosition position = savePosition(sender, player, currentDimension, isPublic, username, force,
							positionName);
					if (position == null) {
						player.sendMessage(
								new TextComponentString("Position name already exists. Use -f to force an overwrite"));
					} else {
						player.sendMessage(new TextComponentString(
								"Saved your position: " + position + ", " + (isPublic ? "public" : "private")));
					}
				} else if (commands.remove("list")) {
					/*
					 * list
					 */
					List<String> positions = null;
					if (isPublic) {
						positions = database.listPositionsPublic();
					} else {
						positions = database.listPositionsPrivate(username);
					}
					player.sendMessage(new TextComponentString("--- " + (isPublic ? "Public" : "Private") + " list  ---"));
					for (String positionString : positions) {
						player.sendMessage(new TextComponentString(positionString));
					}
				} else if (commands.remove("remove")) {
					/*
					 * remove
					 */
					Optional<String> findFirst = commands.stream().findFirst();
					String positionName = findFirst.orElse("home");
					boolean removed = database.remove(username, positionName, isPublic);
					if (removed) {
						player.sendMessage(new TextComponentString("Deleted : " + positionName));
					} else {
						player.sendMessage(new TextComponentString("No position named : " + positionName));
					}
				} else {
					/*
					 * teleport (empty command)
					 */

					Optional<String> findFirst = commands.stream().findFirst();
					String positionName = findFirst.orElse("home");

					SavedPosition position = database.getPosition(username, positionName, isPublic);
					if (position == null) {
						player.sendMessage(new TextComponentString("Can't find position: " + positionName));
						return;
					}
					BlockPos pos = position.getPosition();
					int x = pos.getX();
					int y = pos.getY();
					int z = pos.getZ();
					// float pitch = position.getPitch();
					// float rotation = position.getRotationYaw();
					int destDimension = position.getDimension();
					WorldServer destWorld = server.getWorld(destDimension);
					if (!(destWorld == server.getWorld(currentDimension))) {
						// list.transferPlayerToDimension(senderMP, destWorldId, new
						// HomeTeleporter(destWorld, true));
						player.sendMessage(new TextComponentString("Error. Can't teleport between dimensions yet."));
						return;
					}

					SavedPosition fromPosition = savePosition(sender, player, currentDimension, false, username, true,
							"_from");
					player.sendMessage(new TextComponentString("Saved your from position to " + fromPosition));

					// player.setPositionAndRotation(x, y, z, rotation, pitch);
					// player.setLocationAndAngles(x + 0.5, y, z + 0.5, rotation, pitch); //Doesn't
					// work all the time
					player.setPositionAndUpdate(x + 0.5, y, z + 0.5);
					player.motionX = 0;
					player.motionY = 0;
					player.motionZ = 0;
					player.sendMessage(new TextComponentString("Teleported to " + position));
				}

			} catch (SQLException e) {
				player.sendMessage(new TextComponentString("Error. Se log for more details" + e.getMessage()));
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public String getName() {
		return "tpd";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		StringBuffer sb = new StringBuffer();
		sb.append("/tpd save [-f] [name] [public] - Save your current position as 'name'. \n");
		sb.append("                                 Use -f to force save and overwrite existing name\n");
		sb.append("                                 Ff no named is specifed the 'name' is used.\n");
		sb.append("/tpd list [public]             - List saved positions\n");
		sb.append("/tpd name [public]             - Teleport to named position.\n");
		sb.append("/tpd delete name [public]      - Delete saved position.\n");
		return sb.toString();
	}

	private Set<String> parseArgs(String[] args) {
		Set<String> argsSet = new TreeSet<>();
		for (String arg : args) {
			argsSet.add(arg.toLowerCase());
		}
		return argsSet;
	}

	/**
	 * @param sender
	 * @param player
	 * @param currentDimension
	 * @param isPublic
	 * @param username
	 * @param force
	 * @param positionName
	 * @return
	 * @throws SQLException
	 */
	private SavedPosition savePosition(ICommandSender sender, EntityPlayer player, int currentDimension,
			boolean isPublic, String username, boolean force, String positionName) throws SQLException {
		BlockPos blockPos = sender.getPosition();
		float pitch = player.rotationPitch;
		float rotationYaw = player.rotationYaw;
		SavedPosition position = new SavedPosition(username, positionName, blockPos, pitch, rotationYaw, currentDimension,
				isPublic);

		boolean success = PositionDatabase.get().savePosition(position, force);
		if (success) {
			return position;
		} else {
			return null;
		}
	}

}
