package com.example.cabinetconfigurator.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile PricingDao _pricingDao;

  private volatile QuoteDao _quoteDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `parameter_definitions` (`key` TEXT NOT NULL, `label` TEXT NOT NULL, `category` TEXT NOT NULL, `valueType` TEXT NOT NULL, `unit` TEXT, `sortOrder` INTEGER NOT NULL, PRIMARY KEY(`key`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pricing_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pricing_parameter_values` (`profileId` INTEGER NOT NULL, `parameterKey` TEXT NOT NULL, `value` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`profileId`, `parameterKey`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `quotes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `cabinetType` TEXT NOT NULL, `elementType` TEXT NOT NULL, `widthMm` INTEGER NOT NULL, `heightMm` INTEGER NOT NULL, `depthMm` INTEGER NOT NULL, `totalNet` REAL NOT NULL, `totalGross` REAL NOT NULL, `pricingProfileId` INTEGER, `pricingProfileName` TEXT, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `quote_zones` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `quoteId` INTEGER NOT NULL, `name` TEXT NOT NULL, `orderIndex` INTEGER NOT NULL, `quantity` INTEGER NOT NULL, FOREIGN KEY(`quoteId`) REFERENCES `quotes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_quote_zones_quoteId` ON `quote_zones` (`quoteId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `quote_pricing_snapshot` (`quoteId` INTEGER NOT NULL, `parameterKey` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`quoteId`, `parameterKey`), FOREIGN KEY(`quoteId`) REFERENCES `quotes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_quote_pricing_snapshot_quoteId` ON `quote_pricing_snapshot` (`quoteId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'a0960bf8b8c51685545cc5f4b6c4d503')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `parameter_definitions`");
        db.execSQL("DROP TABLE IF EXISTS `pricing_profiles`");
        db.execSQL("DROP TABLE IF EXISTS `pricing_parameter_values`");
        db.execSQL("DROP TABLE IF EXISTS `quotes`");
        db.execSQL("DROP TABLE IF EXISTS `quote_zones`");
        db.execSQL("DROP TABLE IF EXISTS `quote_pricing_snapshot`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsParameterDefinitions = new HashMap<String, TableInfo.Column>(6);
        _columnsParameterDefinitions.put("key", new TableInfo.Column("key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParameterDefinitions.put("label", new TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParameterDefinitions.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParameterDefinitions.put("valueType", new TableInfo.Column("valueType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParameterDefinitions.put("unit", new TableInfo.Column("unit", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParameterDefinitions.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysParameterDefinitions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesParameterDefinitions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoParameterDefinitions = new TableInfo("parameter_definitions", _columnsParameterDefinitions, _foreignKeysParameterDefinitions, _indicesParameterDefinitions);
        final TableInfo _existingParameterDefinitions = TableInfo.read(db, "parameter_definitions");
        if (!_infoParameterDefinitions.equals(_existingParameterDefinitions)) {
          return new RoomOpenHelper.ValidationResult(false, "parameter_definitions(com.example.cabinetconfigurator.data.local.ParameterDefinitionEntity).\n"
                  + " Expected:\n" + _infoParameterDefinitions + "\n"
                  + " Found:\n" + _existingParameterDefinitions);
        }
        final HashMap<String, TableInfo.Column> _columnsPricingProfiles = new HashMap<String, TableInfo.Column>(4);
        _columnsPricingProfiles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPricingProfiles.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPricingProfiles.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPricingProfiles.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPricingProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPricingProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPricingProfiles = new TableInfo("pricing_profiles", _columnsPricingProfiles, _foreignKeysPricingProfiles, _indicesPricingProfiles);
        final TableInfo _existingPricingProfiles = TableInfo.read(db, "pricing_profiles");
        if (!_infoPricingProfiles.equals(_existingPricingProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "pricing_profiles(com.example.cabinetconfigurator.data.local.PricingProfileEntity).\n"
                  + " Expected:\n" + _infoPricingProfiles + "\n"
                  + " Found:\n" + _existingPricingProfiles);
        }
        final HashMap<String, TableInfo.Column> _columnsPricingParameterValues = new HashMap<String, TableInfo.Column>(4);
        _columnsPricingParameterValues.put("profileId", new TableInfo.Column("profileId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPricingParameterValues.put("parameterKey", new TableInfo.Column("parameterKey", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPricingParameterValues.put("value", new TableInfo.Column("value", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPricingParameterValues.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPricingParameterValues = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPricingParameterValues = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPricingParameterValues = new TableInfo("pricing_parameter_values", _columnsPricingParameterValues, _foreignKeysPricingParameterValues, _indicesPricingParameterValues);
        final TableInfo _existingPricingParameterValues = TableInfo.read(db, "pricing_parameter_values");
        if (!_infoPricingParameterValues.equals(_existingPricingParameterValues)) {
          return new RoomOpenHelper.ValidationResult(false, "pricing_parameter_values(com.example.cabinetconfigurator.data.local.PricingParameterValueEntity).\n"
                  + " Expected:\n" + _infoPricingParameterValues + "\n"
                  + " Found:\n" + _existingPricingParameterValues);
        }
        final HashMap<String, TableInfo.Column> _columnsQuotes = new HashMap<String, TableInfo.Column>(12);
        _columnsQuotes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotes.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotes.put("cabinetType", new TableInfo.Column("cabinetType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotes.put("elementType", new TableInfo.Column("elementType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotes.put("widthMm", new TableInfo.Column("widthMm", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotes.put("heightMm", new TableInfo.Column("heightMm", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotes.put("depthMm", new TableInfo.Column("depthMm", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotes.put("totalNet", new TableInfo.Column("totalNet", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotes.put("totalGross", new TableInfo.Column("totalGross", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotes.put("pricingProfileId", new TableInfo.Column("pricingProfileId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotes.put("pricingProfileName", new TableInfo.Column("pricingProfileName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotes.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysQuotes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesQuotes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoQuotes = new TableInfo("quotes", _columnsQuotes, _foreignKeysQuotes, _indicesQuotes);
        final TableInfo _existingQuotes = TableInfo.read(db, "quotes");
        if (!_infoQuotes.equals(_existingQuotes)) {
          return new RoomOpenHelper.ValidationResult(false, "quotes(com.example.cabinetconfigurator.data.local.QuoteEntity).\n"
                  + " Expected:\n" + _infoQuotes + "\n"
                  + " Found:\n" + _existingQuotes);
        }
        final HashMap<String, TableInfo.Column> _columnsQuoteZones = new HashMap<String, TableInfo.Column>(5);
        _columnsQuoteZones.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuoteZones.put("quoteId", new TableInfo.Column("quoteId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuoteZones.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuoteZones.put("orderIndex", new TableInfo.Column("orderIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuoteZones.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysQuoteZones = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysQuoteZones.add(new TableInfo.ForeignKey("quotes", "CASCADE", "NO ACTION", Arrays.asList("quoteId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesQuoteZones = new HashSet<TableInfo.Index>(1);
        _indicesQuoteZones.add(new TableInfo.Index("index_quote_zones_quoteId", false, Arrays.asList("quoteId"), Arrays.asList("ASC")));
        final TableInfo _infoQuoteZones = new TableInfo("quote_zones", _columnsQuoteZones, _foreignKeysQuoteZones, _indicesQuoteZones);
        final TableInfo _existingQuoteZones = TableInfo.read(db, "quote_zones");
        if (!_infoQuoteZones.equals(_existingQuoteZones)) {
          return new RoomOpenHelper.ValidationResult(false, "quote_zones(com.example.cabinetconfigurator.data.local.QuoteZoneEntity).\n"
                  + " Expected:\n" + _infoQuoteZones + "\n"
                  + " Found:\n" + _existingQuoteZones);
        }
        final HashMap<String, TableInfo.Column> _columnsQuotePricingSnapshot = new HashMap<String, TableInfo.Column>(3);
        _columnsQuotePricingSnapshot.put("quoteId", new TableInfo.Column("quoteId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotePricingSnapshot.put("parameterKey", new TableInfo.Column("parameterKey", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuotePricingSnapshot.put("value", new TableInfo.Column("value", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysQuotePricingSnapshot = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysQuotePricingSnapshot.add(new TableInfo.ForeignKey("quotes", "CASCADE", "NO ACTION", Arrays.asList("quoteId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesQuotePricingSnapshot = new HashSet<TableInfo.Index>(1);
        _indicesQuotePricingSnapshot.add(new TableInfo.Index("index_quote_pricing_snapshot_quoteId", false, Arrays.asList("quoteId"), Arrays.asList("ASC")));
        final TableInfo _infoQuotePricingSnapshot = new TableInfo("quote_pricing_snapshot", _columnsQuotePricingSnapshot, _foreignKeysQuotePricingSnapshot, _indicesQuotePricingSnapshot);
        final TableInfo _existingQuotePricingSnapshot = TableInfo.read(db, "quote_pricing_snapshot");
        if (!_infoQuotePricingSnapshot.equals(_existingQuotePricingSnapshot)) {
          return new RoomOpenHelper.ValidationResult(false, "quote_pricing_snapshot(com.example.cabinetconfigurator.data.local.QuotePricingSnapshotEntity).\n"
                  + " Expected:\n" + _infoQuotePricingSnapshot + "\n"
                  + " Found:\n" + _existingQuotePricingSnapshot);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "a0960bf8b8c51685545cc5f4b6c4d503", "638f89bc01a80fb83354db828f2a3364");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "parameter_definitions","pricing_profiles","pricing_parameter_values","quotes","quote_zones","quote_pricing_snapshot");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `parameter_definitions`");
      _db.execSQL("DELETE FROM `pricing_profiles`");
      _db.execSQL("DELETE FROM `pricing_parameter_values`");
      _db.execSQL("DELETE FROM `quotes`");
      _db.execSQL("DELETE FROM `quote_zones`");
      _db.execSQL("DELETE FROM `quote_pricing_snapshot`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(PricingDao.class, PricingDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(QuoteDao.class, QuoteDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public PricingDao pricingDao() {
    if (_pricingDao != null) {
      return _pricingDao;
    } else {
      synchronized(this) {
        if(_pricingDao == null) {
          _pricingDao = new PricingDao_Impl(this);
        }
        return _pricingDao;
      }
    }
  }

  @Override
  public QuoteDao quoteDao() {
    if (_quoteDao != null) {
      return _quoteDao;
    } else {
      synchronized(this) {
        if(_quoteDao == null) {
          _quoteDao = new QuoteDao_Impl(this);
        }
        return _quoteDao;
      }
    }
  }
}
