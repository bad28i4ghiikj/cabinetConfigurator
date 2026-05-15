package com.example.cabinetconfigurator.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
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
public final class QuoteDao_Impl implements QuoteDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<QuoteEntity> __insertionAdapterOfQuoteEntity;

  private final EntityInsertionAdapter<QuoteZoneEntity> __insertionAdapterOfQuoteZoneEntity;

  private final EntityInsertionAdapter<QuotePricingSnapshotEntity> __insertionAdapterOfQuotePricingSnapshotEntity;

  public QuoteDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfQuoteEntity = new EntityInsertionAdapter<QuoteEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `quotes` (`id`,`name`,`cabinetType`,`elementType`,`widthMm`,`heightMm`,`depthMm`,`totalNet`,`totalGross`,`pricingProfileId`,`pricingProfileName`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final QuoteEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getCabinetType());
        statement.bindString(4, entity.getElementType());
        statement.bindLong(5, entity.getWidthMm());
        statement.bindLong(6, entity.getHeightMm());
        statement.bindLong(7, entity.getDepthMm());
        statement.bindDouble(8, entity.getTotalNet());
        statement.bindDouble(9, entity.getTotalGross());
        if (entity.getPricingProfileId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getPricingProfileId());
        }
        if (entity.getPricingProfileName() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getPricingProfileName());
        }
        statement.bindLong(12, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfQuoteZoneEntity = new EntityInsertionAdapter<QuoteZoneEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `quote_zones` (`id`,`quoteId`,`name`,`orderIndex`,`quantity`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final QuoteZoneEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getQuoteId());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getOrderIndex());
        statement.bindLong(5, entity.getQuantity());
      }
    };
    this.__insertionAdapterOfQuotePricingSnapshotEntity = new EntityInsertionAdapter<QuotePricingSnapshotEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `quote_pricing_snapshot` (`quoteId`,`parameterKey`,`value`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final QuotePricingSnapshotEntity entity) {
        statement.bindLong(1, entity.getQuoteId());
        statement.bindString(2, entity.getParameterKey());
        statement.bindString(3, entity.getValue());
      }
    };
  }

  @Override
  public Object insertQuote(final QuoteEntity entity,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfQuoteEntity.insertAndReturnId(entity);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertZones(final List<QuoteZoneEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfQuoteZoneEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSnapshot(final List<QuotePricingSnapshotEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfQuotePricingSnapshotEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<QuoteAggregate>> observeAllQuotes() {
    final String _sql = "SELECT * FROM quotes ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"quote_zones",
        "quote_pricing_snapshot", "quotes"}, new Callable<List<QuoteAggregate>>() {
      @Override
      @NonNull
      public List<QuoteAggregate> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
            final int _cursorIndexOfCabinetType = CursorUtil.getColumnIndexOrThrow(_cursor, "cabinetType");
            final int _cursorIndexOfElementType = CursorUtil.getColumnIndexOrThrow(_cursor, "elementType");
            final int _cursorIndexOfWidthMm = CursorUtil.getColumnIndexOrThrow(_cursor, "widthMm");
            final int _cursorIndexOfHeightMm = CursorUtil.getColumnIndexOrThrow(_cursor, "heightMm");
            final int _cursorIndexOfDepthMm = CursorUtil.getColumnIndexOrThrow(_cursor, "depthMm");
            final int _cursorIndexOfTotalNet = CursorUtil.getColumnIndexOrThrow(_cursor, "totalNet");
            final int _cursorIndexOfTotalGross = CursorUtil.getColumnIndexOrThrow(_cursor, "totalGross");
            final int _cursorIndexOfPricingProfileId = CursorUtil.getColumnIndexOrThrow(_cursor, "pricingProfileId");
            final int _cursorIndexOfPricingProfileName = CursorUtil.getColumnIndexOrThrow(_cursor, "pricingProfileName");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final LongSparseArray<ArrayList<QuoteZoneEntity>> _collectionZones = new LongSparseArray<ArrayList<QuoteZoneEntity>>();
            final LongSparseArray<ArrayList<QuotePricingSnapshotEntity>> _collectionPricingSnapshot = new LongSparseArray<ArrayList<QuotePricingSnapshotEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionZones.containsKey(_tmpKey)) {
                _collectionZones.put(_tmpKey, new ArrayList<QuoteZoneEntity>());
              }
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionPricingSnapshot.containsKey(_tmpKey_1)) {
                _collectionPricingSnapshot.put(_tmpKey_1, new ArrayList<QuotePricingSnapshotEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipquoteZonesAscomExampleCabinetconfiguratorDataLocalQuoteZoneEntity(_collectionZones);
            __fetchRelationshipquotePricingSnapshotAscomExampleCabinetconfiguratorDataLocalQuotePricingSnapshotEntity(_collectionPricingSnapshot);
            final List<QuoteAggregate> _result = new ArrayList<QuoteAggregate>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final QuoteAggregate _item;
              final QuoteEntity _tmpQuote;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpName;
              _tmpName = _cursor.getString(_cursorIndexOfName);
              final String _tmpCabinetType;
              _tmpCabinetType = _cursor.getString(_cursorIndexOfCabinetType);
              final String _tmpElementType;
              _tmpElementType = _cursor.getString(_cursorIndexOfElementType);
              final int _tmpWidthMm;
              _tmpWidthMm = _cursor.getInt(_cursorIndexOfWidthMm);
              final int _tmpHeightMm;
              _tmpHeightMm = _cursor.getInt(_cursorIndexOfHeightMm);
              final int _tmpDepthMm;
              _tmpDepthMm = _cursor.getInt(_cursorIndexOfDepthMm);
              final double _tmpTotalNet;
              _tmpTotalNet = _cursor.getDouble(_cursorIndexOfTotalNet);
              final double _tmpTotalGross;
              _tmpTotalGross = _cursor.getDouble(_cursorIndexOfTotalGross);
              final Long _tmpPricingProfileId;
              if (_cursor.isNull(_cursorIndexOfPricingProfileId)) {
                _tmpPricingProfileId = null;
              } else {
                _tmpPricingProfileId = _cursor.getLong(_cursorIndexOfPricingProfileId);
              }
              final String _tmpPricingProfileName;
              if (_cursor.isNull(_cursorIndexOfPricingProfileName)) {
                _tmpPricingProfileName = null;
              } else {
                _tmpPricingProfileName = _cursor.getString(_cursorIndexOfPricingProfileName);
              }
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              _tmpQuote = new QuoteEntity(_tmpId,_tmpName,_tmpCabinetType,_tmpElementType,_tmpWidthMm,_tmpHeightMm,_tmpDepthMm,_tmpTotalNet,_tmpTotalGross,_tmpPricingProfileId,_tmpPricingProfileName,_tmpCreatedAt);
              final ArrayList<QuoteZoneEntity> _tmpZonesCollection;
              final long _tmpKey_2;
              _tmpKey_2 = _cursor.getLong(_cursorIndexOfId);
              _tmpZonesCollection = _collectionZones.get(_tmpKey_2);
              final ArrayList<QuotePricingSnapshotEntity> _tmpPricingSnapshotCollection;
              final long _tmpKey_3;
              _tmpKey_3 = _cursor.getLong(_cursorIndexOfId);
              _tmpPricingSnapshotCollection = _collectionPricingSnapshot.get(_tmpKey_3);
              _item = new QuoteAggregate(_tmpQuote,_tmpZonesCollection,_tmpPricingSnapshotCollection);
              _result.add(_item);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshipquoteZonesAscomExampleCabinetconfiguratorDataLocalQuoteZoneEntity(
      @NonNull final LongSparseArray<ArrayList<QuoteZoneEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipquoteZonesAscomExampleCabinetconfiguratorDataLocalQuoteZoneEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`quoteId`,`name`,`orderIndex`,`quantity` FROM `quote_zones` WHERE `quoteId` IN (");
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
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "quoteId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfQuoteId = 1;
      final int _cursorIndexOfName = 2;
      final int _cursorIndexOfOrderIndex = 3;
      final int _cursorIndexOfQuantity = 4;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<QuoteZoneEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final QuoteZoneEntity _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final long _tmpQuoteId;
          _tmpQuoteId = _cursor.getLong(_cursorIndexOfQuoteId);
          final String _tmpName;
          _tmpName = _cursor.getString(_cursorIndexOfName);
          final int _tmpOrderIndex;
          _tmpOrderIndex = _cursor.getInt(_cursorIndexOfOrderIndex);
          final int _tmpQuantity;
          _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
          _item_1 = new QuoteZoneEntity(_tmpId,_tmpQuoteId,_tmpName,_tmpOrderIndex,_tmpQuantity);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }

  private void __fetchRelationshipquotePricingSnapshotAscomExampleCabinetconfiguratorDataLocalQuotePricingSnapshotEntity(
      @NonNull final LongSparseArray<ArrayList<QuotePricingSnapshotEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipquotePricingSnapshotAscomExampleCabinetconfiguratorDataLocalQuotePricingSnapshotEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `quoteId`,`parameterKey`,`value` FROM `quote_pricing_snapshot` WHERE `quoteId` IN (");
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
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "quoteId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfQuoteId = 0;
      final int _cursorIndexOfParameterKey = 1;
      final int _cursorIndexOfValue = 2;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<QuotePricingSnapshotEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final QuotePricingSnapshotEntity _item_1;
          final long _tmpQuoteId;
          _tmpQuoteId = _cursor.getLong(_cursorIndexOfQuoteId);
          final String _tmpParameterKey;
          _tmpParameterKey = _cursor.getString(_cursorIndexOfParameterKey);
          final String _tmpValue;
          _tmpValue = _cursor.getString(_cursorIndexOfValue);
          _item_1 = new QuotePricingSnapshotEntity(_tmpQuoteId,_tmpParameterKey,_tmpValue);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
