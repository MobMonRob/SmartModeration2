package dhbw.smartmoderation;

import org.briarproject.bramble.api.crypto.KeyStrengthener;
import org.briarproject.bramble.api.db.DatabaseConfig;

import java.io.File;

import javax.annotation.Nullable;

class SmartModerationDatabaseConfig implements DatabaseConfig {

	private final File dbDir;
	private final File dbKeyDir;

	SmartModerationDatabaseConfig(File dbDir, File dbKeyDir) {
		this.dbDir = dbDir;
		this.dbKeyDir = dbKeyDir;
	}

	@Override
	public File getDatabaseDirectory() {
		return dbDir;
	}

	@Override
	public File getDatabaseKeyDirectory() {
		return dbKeyDir;
	}

	@Nullable
	@Override
	public KeyStrengthener getKeyStrengthener() {
		return null;
	}

}
