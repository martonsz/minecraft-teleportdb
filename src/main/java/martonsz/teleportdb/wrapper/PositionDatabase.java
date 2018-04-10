package martonsz.teleportdb.wrapper;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.math.BlockPos;

public class PositionDatabase {

	private static Logger logger = LogManager.getLogger(PositionDatabase.class);
	private static String dbPath;

	public void createNewDatabase(File dbPath) {
		this.dbPath = dbPath.getAbsolutePath();

		StringBuffer checkSQL = new StringBuffer();
		checkSQL.append("SELECT name ");
		checkSQL.append("FROM   sqlite_master ");
		checkSQL.append("WHERE  TYPE = 'table' ");
		checkSQL.append("       AND name = 'tbl_public_position';");

		StringBuffer publicSQL = new StringBuffer();
		publicSQL.append("CREATE TABLE tbl_public_position ( ");
		publicSQL.append("    user_name TEXT NOT NULL, ");
		publicSQL.append("	position_name TEXT ");
		publicSQL.append("	block_position REAL NOT NULL, ");
		publicSQL.append("	pitch REAL NOT NULL, ");
		publicSQL.append("	dimension INTEGER NOT NULL, ");
		publicSQL.append("    UNIQUE ( ");
		publicSQL.append("        user_name, ");
		publicSQL.append("        position_name ");
		publicSQL.append("    ) ");
		publicSQL.append("    ON CONFLICT FAIL ");
		publicSQL.append("); ");

		StringBuffer privateSQL = new StringBuffer();
		privateSQL.append("CREATE TABLE tbl_private_position ( ");
		privateSQL.append("    user_name TEXT NOT NULL, ");
		privateSQL.append("	position_name TEXT ");
		privateSQL.append("	block_position REAL NOT NULL, ");
		privateSQL.append("	pitch REAL NOT NULL, ");
		privateSQL.append("	dimension INTEGER NOT NULL, ");
		privateSQL.append("    UNIQUE ( ");
		privateSQL.append("        user_name, ");
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
				logger.info("Created new teleport database in " + dbPath.getAbsolutePath());
			} else {
				logger.info("Db already exists");
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Connection getConnection() throws SQLException {
		String url = "jdbc:sqlite:" + dbPath;
		return DriverManager.getConnection(url);
	}

	public void savePosition(SavedPosition position) {
		// TODO Auto-generated method stub
		
	}

	public SavedPosition getPosition(String username, String positionName) {
		// TODO Auto-generated method stub
		return null;
	}
}
