package com.example.cabinetconfigurator.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PricingDao_Impl implements PricingDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ParameterDefinitionEntity> __insertionAdapterOfParameterDefinitionEntity;

  private final EntityInsertionAdapter<PricingProfileEntity> __insertionAdapterOfPricingProfileEntity;

  private final EntityInsertionAdapter<PricingParameterValueEntity> __insertionAdapterOfPricingParameterValueEntity;

  public PricingDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfParameterDefinitionEntity = new EntityInsertionAdapter<ParameterDefinitionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `parameter_definitions` (`key`,`label`,`category`,`valueType`,`unit`,`sortOrder`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ParameterDefinitionEntity entity) {
        statement.bindString(1, entity.getKey());
        statement.bindString(2, entity.getLabel());
        statement.bindString(3, entity.getCategory());
        statement.bindString(4, entity.getValueType());
        if (entity.getUnit() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getUnit());
        }
        statement.bindLong(6, entity.getSortOrder());
      }
    };
    this.__insertionAdapterOfPricingProfileEntity = new EntityInsertionAdapter<PricingProfileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `pricing_profiles` (`id`,`name`,`isActive`,`updatedAt`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PricingProfileEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getUpdatedAt());
      }
    };
    this.__insertionAdapterOfPricingParameterValueEntity = new EntityInsertionAdapter<PricingParameterValueEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `pricing_parameter_values` (`profileId`,`parameterKey`,`value`,`updatedAt`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PricingParameterValueEntity entity) {
        statement.bindLong(1, entity.getProfileId());
        statement.bindString(2, entity.getParameterKey());
        statement.bindString(3, entity.getValue());
        statement.bindLong(4, entity.getUpdatedAt());
      }
    };
  }

  @Override
  public Object insertDefinitions(final List<ParameterDefinitionEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfParameterDefinitionEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertProfile(final PricingProfileEntity profile,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPricingProfileEntity.insertAndReturnId(profile);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertParameterValues(final List<PricingParameterValueEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPricingParameterValueEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDefinitions(
      final Continuation<? super List<ParameterDefinitionEntity>> $completion) {
    final String _sql = "SELECT * FROM parameter_definitions ORDER BY sortOrder";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ParameterDefinitionEntity>>() {
      @Override
      @NonNull
      public List<ParameterDefinitionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfKey = CursorUtil.getColumnIndexOrThrow(_cursor, "key");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfValueType = CursorUtil.getColumnIndexOrThrow(_cursor, "valueType");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final List<ParameterDefinitionEntity> _result = new ArrayList<ParameterDefinitionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ParameterDefinitionEntity _item;
            final String _tmpKey;
            _tmpKey = _cursor.getString(_cursorIndexOfKey);
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpValueType;
            _tmpValueType = _cursor.getString(_cursorIndexOfValueType);
            final String _tmpUnit;
            if (_cursor.isNull(_cursorIndexOfUnit)) {
              _tmpUnit = null;
            } else {
              _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            }
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            _item = new ParameterDefinitionEntity(_tmpKey,_tmpLabel,_tmpCategory,_tmpValueType,_tmpUnit,_tmpSortOrder);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getActiveProfile(final Continuation<? super PricingProfileWithValues> $completion) {
    final String _sql = "SELECT * FROM pricing_profiles WHERE isActive = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, true, _cancellationSignal, new Callable<PricingProfileWithValues>() {
      @Override
      @Nullable
      public PricingProfileWithValues call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
            final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final LongSparseArray<ArrayList<PricingParameterValueEntity>> _collectionValues = new LongSparseArray<ArrayList<PricingParameterValueEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionValues.containsKey(_tmpKey)) {
                _collectionValues.put(_tmpKey, new ArrayList<PricingParameterValueEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshippricingParameterValuesAscomExampleCabinetconfiguratorDataLocalPricingParameterValueEntity(_collectionValues);
            final PricingProfileWithValues _result;
            if (_cursor.moveToFirst()) {
              final PricingProfileEntity _tmpProfile;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpName;
              _tmpName = _cursor.getString(_cursorIndexOfName);
              final boolean _tmpIsActive;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfIsActive);
              _tmpIsActive = _tmp != 0;
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              _tmpProfile = new PricingProfileEntity(_tmpId,_tmpName,_tmpIsActive,_tmpUpdatedAt);
              final ArrayList<PricingParameterValueEntity> _tmpValuesCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpValuesCollection = _collectionValues.get(_tmpKey_1);
              _result = new PricingProfileWithValues(_tmpProfile,_tmpValuesCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
            _statement.release();
          }
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<PricingProfileWithValues> observeActiveProfile() {
    final String _sql = "SELECT * FROM pricing_profiles WHERE isActive = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"pricing_parameter_values",
        "pricing_profiles"}, new Callable<PricingProfileWithValues>() {
      @Override
      @Nullable
      public PricingProfileWithValues call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
            final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
            final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
            final LongSparseArray<ArrayList<PricingParameterValueEntity>> _collectionValues = new LongSparseArray<ArrayList<PricingParameterValueEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionValues.containsKey(_tmpKey)) {
                _collectionValues.put(_tmpKey, new ArrayList<PricingParameterValueEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshippricingParameterValuesAscomExampleCabinetconfiguratorDataLocalPricingParameterValueEntity(_collectionValues);
            final PricingProfileWithValues _result;
            if (_cursor.moveToFirst()) {
              final PricingProfileEntity _tmpProfile;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpName;
              _tmpName = _cursor.getString(_cursorIndexOfName);
              final boolean _tmpIsActive;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfIsActive);
              _tmpIsActive = _tmp != 0;
              final long _tmpUpdatedAt;
              _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
              _tmpProfile = new PricingProfileEntity(_tmpId,_tmpName,_tmpIsActive,_tmpUpdatedAt);
              final ArrayList<PricingParameterValueEntity> _tmpValuesCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpValuesCollection = _collectionValues.get(_tmpKey_1);
              _result = new PricingProfileWithValues(_tmpProfile,_tmpValuesCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object countProfiles(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM pricing_profiles";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshippricingParameterValuesAscomExampleCabinetconfiguratorDataLocalPricingParameterValueEntity(
      @NonNull final LongSparseArray<ArrayList<PricingParameterValueEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshippricingParameterValuesAscomExampleCabinetconfiguratorDataLocalPricingParameterValueEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `profileId`,`parameterKey`,`value`,`updatedAt` FROM `pricing_parameter_values` WHERE `profileId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "profileId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfProfileId = 0;
      final int _cursorIndexOfParameterKey = 1;
      final int _cursorIndexOfValue = 2;
      final int _cursorIndexOfUpdatedAt = 3;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<PricingParameterValueEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final PricingParameterValueEntity _item_1;
          final long _tmpProfileId;
          _tmpProfileId = _cursor.getLong(_cursorIndexOfProfileId);
          final String _tmpParameterKey;
          _tmpParameterKey = _cursor.getString(_cursorIndexOfParameterKey);
          final String _tmpValue;
          _tmpValue = _cursor.getString(_cursorIndexOfValue);
          final long _tmpUpdatedAt;
          _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
          _item_1 = new PricingParameterValueEntity(_tmpProfileId,_tmpParameterKey,_tmpValue,_tmpUpdatedAt);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
