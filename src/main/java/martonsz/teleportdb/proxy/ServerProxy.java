package martonsz.teleportdb.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import martonsz.teleportdb.Ref;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

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
