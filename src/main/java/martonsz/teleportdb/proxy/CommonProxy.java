package martonsz.teleportdb.proxy;

import java.io.File;

import martonsz.teleportdb.ClientEventHandler;
import martonsz.teleportdb.command.CommandTpd;
import martonsz.teleportdb.wrapper.PositionDatabase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
		File modConfigurationDirectory = event.getModConfigurationDirectory();
		File dbPath = new File(modConfigurationDirectory.getPath(), "teleport_database.db");
		new PositionDatabase().createNewDatabase(dbPath);
	}

	public void init(FMLInitializationEvent event) {

	}

	public void postInit(FMLPostInitializationEvent event) {

	}

}
