package fec_util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class BlockDigestInputStream extends FilterInputStream
{
  protected MessageDigest md;
  protected int blockSize;
  protected int byteCount;
  ArrayList digestList;
  Buffer[] digests;

  public int read()
    throws IOException
  {
    byte[] b = new byte[1];
    if (read(b, 0, 1) == -1) {
      return -1;
    }
    return b[0] & 0xFF;
  }

  public long skip(long n) throws IOException {
    byte[] b = new byte[n < 1024L ? (int)n : 1024];
    long l = n;

    while (l > 0L)
    {
      int c;
      if ((c = read(b, 0, l < 1024L ? (int)l : 1024)) == -1) {
        break;
      }
      l -= c;
    }
    return n - l;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    int left = this.blockSize - this.byteCount;
    int c;
    if ((c = this.in.read(b, off, len < left ? len : left)) == -1) {
      return -1;
    }
    this.md.update(b, off, c);
    this.byteCount += c;

    if (this.byteCount == this.blockSize) {
      this.digestList.add(new Buffer(this.md.digest()));
      this.byteCount = 0;
    }
    return c;
  }

  public void finish() {
    if (this.byteCount != 0) {
      this.digestList.add(new Buffer(this.md.digest()));
    }
    this.digests = ((Buffer[])this.digestList.toArray(new Buffer[0]));
    this.digestList = null;
  }

  public void close() throws IOException {
    if (this.digestList != null) {
      finish();
    }
    this.in.close();
  }

  public Buffer[] getBlockDigests() {
    if (this.digests == null) {
      throw new IllegalStateException("Must call finish or close first");
    }
    return this.digests;
  }

  public BlockDigestInputStream(InputStream is, String algorithm, int blockSize)
    throws NoSuchAlgorithmException
  {
    super(is); this.digestList = new ArrayList(); this.digests = null;
    if (blockSize <= 0) {
      throw new IllegalArgumentException("blockSize must be > 0");
    }
    this.md = MessageDigest.getInstance(algorithm);
    this.blockSize = blockSize;
  }
}

/* Location:           C:\Users\ken\temp\
 * Qualified Name:     com.onionnetworks.util.BlockDigestInputStream
 * JD-Core Version:    0.6.0
 */