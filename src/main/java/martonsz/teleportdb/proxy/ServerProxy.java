package martonsz.teleportdb.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import martonsz.teleportdb.ClientEventHandler;
import martonsz.teleportdb.Ref;
import martonsz.teleportdb.command.CommandTpd;
import martonsz.teleportdb.wrapper.PositionDatabase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public class ServerProxy extends CommonProxy {

	private static Logger logger = LogManager.getLogger(ServerProxy.class);

	@Override
	public void init(FMLInitializationEvent event) {
		logger.info("Starting Teleport Database mod version " + Ref.VERSION);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		logger.info("Teleport Database mod version " + Ref.VERSION + " loaded!");
	}

}
