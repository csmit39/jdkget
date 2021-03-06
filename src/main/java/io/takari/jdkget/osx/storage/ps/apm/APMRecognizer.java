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

package io.takari.jdkget.osx.storage.ps.apm;

import io.takari.jdkget.osx.io.ReadableRandomAccessStream;
import io.takari.jdkget.osx.storage.ps.PartitionSystemRecognizer;
import io.takari.jdkget.osx.storage.ps.apm.types.ApplePartitionMap;
import io.takari.jdkget.osx.storage.ps.apm.types.DriverDescriptorRecord;

/**
 * @author <a href="http://www.catacombae.org/" target="_top">Erik Larsson</a>
 */
public class APMRecognizer implements PartitionSystemRecognizer {

  @Override
  public boolean detect(ReadableRandomAccessStream fsStream, long offset, long length) {
    try {
      //ReadableRandomAccessStream llf = data.createReadOnlyFile();
      byte[] firstBlock = new byte[512];

      fsStream.seek(0);
      fsStream.readFully(firstBlock);

      // Look for APM
      DriverDescriptorRecord ddr = new DriverDescriptorRecord(firstBlock, 0);
      if (ddr.isValid()) {
        int blockSize = ddr.getSbBlkSize();
        //long numberOfBlocksOnDevice = Util.unsign(ddr.getSbBlkCount());
        //bitStream.seek(blockSize*1); // second block, first partition in list
        ApplePartitionMap apm = new ApplePartitionMap(fsStream, blockSize * 1, blockSize);
        if (apm.getUsedPartitionCount() > 0) {
          return true;
        }
      }
    } catch (Exception e) {
    }
    return false;
  }
}
