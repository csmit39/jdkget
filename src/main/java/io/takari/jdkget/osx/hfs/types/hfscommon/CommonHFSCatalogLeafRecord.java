/*-
 * Copyright (C) 2008 Erik Larsson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.takari.jdkget.osx.hfs.types.hfscommon;

import io.takari.jdkget.osx.csjc.StructElements;
import io.takari.jdkget.osx.hfs.types.hfs.CatDataRec;
import io.takari.jdkget.osx.hfs.types.hfs.CatKeyRec;
import io.takari.jdkget.osx.hfs.types.hfs.CdrDirRec;
import io.takari.jdkget.osx.hfs.types.hfs.CdrFThdRec;
import io.takari.jdkget.osx.hfs.types.hfs.CdrFilRec;
import io.takari.jdkget.osx.hfs.types.hfs.CdrThdRec;
import io.takari.jdkget.osx.hfs.types.hfsplus.HFSPlusCatalogFile;
import io.takari.jdkget.osx.hfs.types.hfsplus.HFSPlusCatalogFolder;
import io.takari.jdkget.osx.hfs.types.hfsplus.HFSPlusCatalogKey;
import io.takari.jdkget.osx.hfs.types.hfsplus.HFSPlusCatalogLeafRecordData;
import io.takari.jdkget.osx.hfs.types.hfsplus.HFSPlusCatalogThread;
import io.takari.jdkget.osx.hfs.types.hfsx.HFSXCatalogKey;
import io.takari.jdkget.osx.util.Util;

/**
 * @author <a href="http://www.catacombae.org/" target="_top">Erik Larsson</a>
 */
public abstract class CommonHFSCatalogLeafRecord
  extends CommonBTLeafRecord<CommonHFSCatalogKey>
  implements StructElements {

  public static CommonHFSCatalogLeafRecord createHFS(byte[] data, int offset, int length) {
    final CatKeyRec key;
    final CatDataRec recordData;

    key = new CatKeyRec(data, offset);

    int recordOffset = offset + key.occupiedSize();
    // Align to word boundary (primitive...)
    if (recordOffset % 2 != 0)
      recordOffset++;

    // Peek at known 8-bit value indicating the record type
    byte recordType = data[recordOffset];
    switch (recordType) {
      case CatDataRec.HFS_DIRECTORY_RECORD:
        recordData = new CdrDirRec(data, recordOffset);
        break;
      case CatDataRec.HFS_FILE_RECORD:
        recordData = new CdrFilRec(data, recordOffset);
        break;
      case CatDataRec.HFS_DIRECTORY_THREAD_RECORD:
        recordData = new CdrThdRec(data, recordOffset);
        break;
      case CatDataRec.HFS_FILE_THREAD_RECORD:
        recordData = new CdrFThdRec(data, recordOffset);
        break;
      default:
        System.err.println("key:");
        key.print(System.err, " ");
        System.err.println("data: " + Util.byteArrayToHexString(data, offset, length));
        throw new RuntimeException("Invalid HFS record type: " + recordType);
    }

    return create(key, recordData);
  }

  public static CommonHFSCatalogLeafRecord createHFSPlus(byte[] data, int offset, int length) {
    HFSPlusCatalogKey key = new HFSPlusCatalogKey(data, offset);
    final HFSPlusCatalogLeafRecordData recordData;

    // Peek at known 16-bit value to determine proper subtype
    short recordType = Util.readShortBE(data, offset + key.length());
    switch (recordType) {
      case HFSPlusCatalogLeafRecordData.RECORD_TYPE_FOLDER:
        recordData = new HFSPlusCatalogFolder(data, offset + key.length());
        break;
      case HFSPlusCatalogLeafRecordData.RECORD_TYPE_FILE:
        recordData = new HFSPlusCatalogFile(data, offset + key.length());
        break;
      case HFSPlusCatalogLeafRecordData.RECORD_TYPE_FILE_THREAD:
      case HFSPlusCatalogLeafRecordData.RECORD_TYPE_FOLDER_THREAD:
        recordData = new HFSPlusCatalogThread(data, offset + key.length());
        break;
      default:
        throw new RuntimeException("Invalid record type!");
    }
    return create(key, recordData);
  }

  public static CommonHFSCatalogLeafRecord createHFSX(byte[] data, int offset,
    int length, byte keyCompareType) {
    final HFSXCatalogKey key =
      new HFSXCatalogKey(data, offset, keyCompareType);
    final HFSPlusCatalogLeafRecordData recordData;

    // Peek at known 16-bit value to determine proper subtype
    short recordType = Util.readShortBE(data, offset + key.length());
    switch (recordType) {
      case HFSPlusCatalogLeafRecordData.RECORD_TYPE_FOLDER:
        recordData = new HFSPlusCatalogFolder(data, offset + key.length());
        break;
      case HFSPlusCatalogLeafRecordData.RECORD_TYPE_FILE:
        recordData = new HFSPlusCatalogFile(data, offset + key.length());
        break;
      case HFSPlusCatalogLeafRecordData.RECORD_TYPE_FILE_THREAD:
      case HFSPlusCatalogLeafRecordData.RECORD_TYPE_FOLDER_THREAD:
        recordData = new HFSPlusCatalogThread(data, offset + key.length());
        break;
      default:
        throw new RuntimeException("Invalid record type!");
    }
    return create(key, recordData);
  }

  @Override
  public abstract CommonHFSCatalogKey getKey();

  public static CommonHFSCatalogLeafRecord create(HFSPlusCatalogKey key,
    HFSPlusCatalogLeafRecordData data) {
    if (data instanceof HFSPlusCatalogFolder) {
      return CommonHFSCatalogFolderRecord.create(key, (HFSPlusCatalogFolder) data);
    } else if (data instanceof HFSPlusCatalogFile) {
      return CommonHFSCatalogFileRecord.create(key, (HFSPlusCatalogFile) data);
    } else if (data instanceof HFSPlusCatalogThread) {
      if (data.getRecordType() == HFSPlusCatalogLeafRecordData.RECORD_TYPE_FILE_THREAD) {
        return CommonHFSCatalogFileThreadRecord.create(key, (HFSPlusCatalogThread) data);
      }

      else if (data.getRecordType() == HFSPlusCatalogLeafRecordData.RECORD_TYPE_FOLDER_THREAD) {
        return CommonHFSCatalogFolderThreadRecord.create(key, (HFSPlusCatalogThread) data);
      } else
        throw new RuntimeException("Unknown catalog thread type: " + data.getRecordType());
    } else
      throw new RuntimeException("Unknown type of HFSPlusCatalogLeafRecordData: " + data.getClass());
  }

  public static CommonHFSCatalogLeafRecord create(CatKeyRec key,
    CatDataRec data) {
    if (data instanceof CdrDirRec) {
      return CommonHFSCatalogFolderRecord.create(key, (CdrDirRec) data);
    } else if (data instanceof CdrFilRec) {
      return CommonHFSCatalogFileRecord.create(key, (CdrFilRec) data);
    } else if (data instanceof CdrFThdRec) {
      return CommonHFSCatalogFileThreadRecord.create(key, (CdrFThdRec) data);
    } else if (data instanceof CdrThdRec) {
      return CommonHFSCatalogFolderThreadRecord.create(key, (CdrThdRec) data);
    } else
      throw new RuntimeException("Unknown type of CatDataRec: " + data.getClass());
  }
}
