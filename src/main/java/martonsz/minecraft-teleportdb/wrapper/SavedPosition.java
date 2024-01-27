package martonsz.teleportdb.wrapper;

import net.minecraft.util.math.BlockPos;

public class SavedPosition {

	private String username;
	private String positionName;
	private BlockPos blockPos;
	private float pitch;
	private float rotationYaw;
	private int dimension;
	private boolean isPublic;

	/**
	 * @param username
	 * @param positionName
	 * @param blockPos
	 * @param pitch
	 * @param rotationYaw
	 * @param dimension
	 * @param isPublic
	 */
	public SavedPosition(String username, String positionName, BlockPos blockPos, float pitch, float rotationYaw,
			int dimension, boolean isPublic) {
		this.username = username;
		this.positionName = positionName;
		this.blockPos = blockPos;
		this.pitch = pitch;
		this.rotationYaw = rotationYaw;
		this.dimension = dimension;
		this.isPublic = isPublic;
	}

	// public SavedPosition(String username, String positionName, BlockPos blockPos,
	// float pitch2, float rotationYaw2,
	// int dimension2, boolean isPublic) {
	// // TODO Auto-generated constructor stub
	// }

	public String getUsername() {
		return username;
	}

	public String getPositionName() {
		return positionName;
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public BlockPos getPosition() {
		return blockPos;
	}

	public float getPitch() {
		return pitch;
	}

	public float getRotationYaw() {
		return rotationYaw;
	}

	public int getDimension() {
		return dimension;
	}

	@Override
	public String toString() {
		return getPositionName() + ": " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ();
	}

}
