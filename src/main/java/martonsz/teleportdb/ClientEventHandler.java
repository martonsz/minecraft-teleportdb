package martonsz.teleportdb;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class ClientEventHandler {

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		EntityPlayer player = event.player;
		World world = player.getEntityWorld();
		if (!world.isRemote) {
			player.sendMessage(
					new TextComponentString("This server is running Teleport Database " + Ref.VERSION + " by marton.sz!"));
			player.sendMessage(new TextComponentString("Type /tpd help in the console for more info."));
		}
	}
}
