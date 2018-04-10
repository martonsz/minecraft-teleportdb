package martonsz.teleportdb.wrapper;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.server.FMLServerHandler;

public class PositionDatabase {

	private static PositionDatabase singleton;

	public PositionDatabase() {
		createNewDatabase();
	}

	public static synchronized PositionDatabase get() {
		if (singleton == null) {
			singleton = new PositionDatabase();
		}
		return singleton;
	}

	private static Logger logger = LogManager.getLogger(PositionDatabase.class);
	private static String dbPath;

	public synchronized void createNewDatabase() {
		if (PositionDatabase.dbPath != null)
			return;

		// Fungerar för klient
		// File worldDirectory = new File(Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + "/saves/"
		// + Minecraft.getMinecraft().getIntegratedServer().getFolderName());
		// logger.info("#################### " + worldDirectory.getAbsolutePath());
		// File dbPathFile = new File(worldDirectory.getPath(), "teleport_database.db");

		// För server
		MinecraftServer mc = FMLCommonHandler.instance().getMinecraftServerInstance();
		String worldName = mc.getFolderName();
		File saveDir = FMLServerHandler.instance().getSavesDirectory();
		File dbPathFile = new File(saveDir, worldName + "/teleport_database.db");

		PositionDatabase.dbPath = dbPathFile.getAbsolutePath();

		StringBuffer checkSQL = new StringBuffer();
		checkSQL.append("SELECT name ");
		checkSQL.append("FROM   sqlite_master ");
		checkSQL.append("WHERE  TYPE = 'table' ");
		checkSQL.append("       AND name = 'tbl_public_position';");

		StringBuffer publicSQL = new StringBuffer();
		publicSQL.append("CREATE TABLE tbl_public_position ( ");
		publicSQL.append("   username TEXT NOT NULL, ");
		publicSQL.append("	position_name TEXT, ");
		publicSQL.append("	block_position REAL NOT NULL, ");
		publicSQL.append("	pitch REAL NOT NULL, ");
		publicSQL.append("	rotation REAL NOT NULL, ");
		publicSQL.append("	dimension INTEGER NOT NULL, ");
		publicSQL.append("    UNIQUE ( ");
		publicSQL.append("        username, ");
		publicSQL.append("        position_name ");
		publicSQL.append("    ) ");
		publicSQL.append("    ON CONFLICT FAIL ");
		publicSQL.append("); ");

		StringBuffer privateSQL = new StringBuffer();
		privateSQL.append("CREATE TABLE tbl_private_position ( ");
		privateSQL.append("  username TEXT NOT NULL, ");
		privateSQL.append("	position_name TEXT, ");
		privateSQL.append("	block_position REAL NOT NULL, ");
		privateSQL.append("	pitch REAL NOT NULL, ");
		privateSQL.append("	rotation REAL NOT NULL, ");
		privateSQL.append("	dimension INTEGER NOT NULL, ");
		privateSQL.append("    UNIQUE ( ");
		privateSQL.append("        username, ");
		privateSQL.append("        position_name ");
		privateSQL.append("    ) ");
		privateSQL.append("    ON CONFLICT FAIL ");
		privateSQL.append(");");

		try (Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(checkSQL.toString());
			if (!rs.next()) {
				stmt.execute(publicSQL.toString());
				stmt.execute(privateSQL.toString());
				logger.info("Created new teleport database in " + dbPath);
			} else {
				logger.info("DB already exists");
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Connection getConnection() throws SQLException {
		String url = "jdbc:sqlite:" + dbPath;
		return DriverManager.getConnection(url);
	}

	public SavedPosition getPosition(String username, String positionName, boolean isPublic) throws SQLException {
		String table = getTable(isPublic);
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM " + table + " WHERE username = ? AND position_name = ?");

		try (Connection conn = getConnection()) {
			PreparedStatement pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, username);
			pstmt.setString(2, positionName);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				long blockLong = rs.getLong("block_position");
				float pitch = rs.getFloat("pitch");
				float rotation = rs.getFloat("rotation");
				int dimension = rs.getInt("dimension");
				BlockPos blockPosition = BlockPos.fromLong(blockLong);
				return new SavedPosition(username, positionName, blockPosition, pitch, rotation, dimension, isPublic);
			}
		}
		return null;
	}

	private String getTable(boolean isPublic) {
		return isPublic ? "tbl_public_position" : "tbl_private_position";
	}

	/**
	 * @param isPublic
	 * @return
	 * @throws SQLException
	 */
	public List<String> listPositionsPublic() throws SQLException {
		String sql = "SELECT * FROM tbl_public_position ORDER BY username, position_name";

		ArrayList<String> list = new ArrayList<>();
		try (Connection conn = getConnection()) {
			ResultSet rs = conn.createStatement().executeQuery(sql.toString());
			while (rs.next()) {
				String savedUsername = rs.getString("username");
				String positionName = rs.getString("position_name");
				list.add(positionName + " - " + savedUsername);
			}
		}
		return list;
	}

	/**
	 * @param isPublic
	 * @return
	 * @throws SQLException
	 */
	public List<String> listPositionsPrivate(String username) throws SQLException {
		String sql = "SELECT * FROM tbl_private_position WHERE username = ? ORDER BY position_name";

		ArrayList<String> list = new ArrayList<>();
		try (Connection conn = getConnection()) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String positionName = rs.getString("position_name");
				list.add(positionName);
			}
		}
		return list;
	}

	/**
	 * @param positionName
	 * @param isPublic
	 * @return true if a position was removed
	 * @throws SQLException
	 */
	public boolean remove(String username, String positionName, boolean isPublic) throws SQLException {
		String table = getTable(isPublic);
		StringBuffer sql = new StringBuffer();
		sql.append("DELETE FROM " + table + " WHERE username = ? AND position_name = ?");

		try (Connection conn = getConnection()) {
			PreparedStatement pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, username);
			pstmt.setString(2, positionName);
			int rowCount = pstmt.executeUpdate();
			return rowCount > 0 ? true : false;
		}
	}

	/**
	 * @param position
	 * @param force
	 * @return false if position name already exists
	 * @throws SQLException
	 */
	public boolean savePosition(SavedPosition position, boolean force) throws SQLException {
		String table = getTable(position.isPublic());
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO " + table + " (username, position_name, block_position, pitch, rotation, dimension)");
		sql.append(" VALUES ");
		sql.append("(?, ?, ?, ?, ?, ?)");

		if (force) {
			remove(position.getUsername(), position.getPositionName(), position.isPublic());
		} else {
			SavedPosition savedPosition = getPosition(position.getUsername(), position.getPositionName(),
					position.isPublic());
			if (savedPosition != null)
				return false;
		}

		try (Connection conn = getConnection()) {
			PreparedStatement pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, position.getUsername());
			pstmt.setString(2, position.getPositionName());
			pstmt.setLong(3, position.getBlockPos().toLong());
			pstmt.setFloat(4, position.getPitch());
			pstmt.setFloat(5, position.getRotationYaw());
			pstmt.setInt(6, position.getDimension());
			pstmt.executeUpdate();
			return true;
		}
	}
}
