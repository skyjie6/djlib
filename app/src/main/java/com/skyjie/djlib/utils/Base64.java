package com.skyjie.djlib.utils;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by dengyuanting on 16-7-12.
 */
public class Base64 {
    static final byte[] CHUNK_SEPARATOR = new byte[]{(byte)13, (byte)10};
    private static final byte[] STANDARD_ENCODE_TABLE = new byte[]{(byte)65, (byte)66, (byte)67, (byte)68, (byte)69, (byte)70, (byte)71, (byte)72, (byte)73, (byte)74, (byte)75, (byte)76, (byte)77, (byte)78, (byte)79, (byte)80, (byte)81, (byte)82, (byte)83, (byte)84, (byte)85, (byte)86, (byte)87, (byte)88, (byte)89, (byte)90, (byte)97, (byte)98, (byte)99, (byte)100, (byte)101, (byte)102, (byte)103, (byte)104, (byte)105, (byte)106, (byte)107, (byte)108, (byte)109, (byte)110, (byte)111, (byte)112, (byte)113, (byte)114, (byte)115, (byte)116, (byte)117, (byte)118, (byte)119, (byte)120, (byte)121, (byte)122, (byte)48, (byte)49, (byte)50, (byte)51, (byte)52, (byte)53, (byte)54, (byte)55, (byte)56, (byte)57, (byte)43, (byte)47};
    private static final byte[] URL_SAFE_ENCODE_TABLE = new byte[]{(byte)65, (byte)66, (byte)67, (byte)68, (byte)69, (byte)70, (byte)71, (byte)72, (byte)73, (byte)74, (byte)75, (byte)76, (byte)77, (byte)78, (byte)79, (byte)80, (byte)81, (byte)82, (byte)83, (byte)84, (byte)85, (byte)86, (byte)87, (byte)88, (byte)89, (byte)90, (byte)97, (byte)98, (byte)99, (byte)100, (byte)101, (byte)102, (byte)103, (byte)104, (byte)105, (byte)106, (byte)107, (byte)108, (byte)109, (byte)110, (byte)111, (byte)112, (byte)113, (byte)114, (byte)115, (byte)116, (byte)117, (byte)118, (byte)119, (byte)120, (byte)121, (byte)122, (byte)48, (byte)49, (byte)50, (byte)51, (byte)52, (byte)53, (byte)54, (byte)55, (byte)56, (byte)57, (byte)45, (byte)95};
    private static final byte[] DECODE_TABLE = new byte[]{(byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)62, (byte)-1, (byte)62, (byte)-1, (byte)63, (byte)52, (byte)53, (byte)54, (byte)55, (byte)56, (byte)57, (byte)58, (byte)59, (byte)60, (byte)61, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)0, (byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8, (byte)9, (byte)10, (byte)11, (byte)12, (byte)13, (byte)14, (byte)15, (byte)16, (byte)17, (byte)18, (byte)19, (byte)20, (byte)21, (byte)22, (byte)23, (byte)24, (byte)25, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)63, (byte)-1, (byte)26, (byte)27, (byte)28, (byte)29, (byte)30, (byte)31, (byte)32, (byte)33, (byte)34, (byte)35, (byte)36, (byte)37, (byte)38, (byte)39, (byte)40, (byte)41, (byte)42, (byte)43, (byte)44, (byte)45, (byte)46, (byte)47, (byte)48, (byte)49, (byte)50, (byte)51};
    private byte[] encodeTable;
    private byte[] decodeTable;
    private byte[] lineSeparator;
    private int decodeSize;
    private int encodeSize;
    private int unencodedBlockSize;
    private int encodedBlockSize;
    protected int lineLength;
    private int chunkSeparatorLength;
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    protected Base64(int unencodedBlockSize, int encodedBlockSize, int lineLength, int chunkSeparatorLength) {
        this.unencodedBlockSize = unencodedBlockSize;
        this.encodedBlockSize = encodedBlockSize;
        boolean useChunking = lineLength > 0 && chunkSeparatorLength > 0;
        this.lineLength = useChunking?lineLength / encodedBlockSize * encodedBlockSize:0;
        this.chunkSeparatorLength = chunkSeparatorLength;
    }

    public Base64() {
        this(0);
    }

    public Base64(boolean urlSafe) {
        this(76, CHUNK_SEPARATOR, urlSafe);
    }

    public Base64(int lineLength) {
        this(lineLength, CHUNK_SEPARATOR);
    }

    public Base64(int lineLength, byte[] lineSeparator) {
        this(lineLength, lineSeparator, false);
    }

    public Base64(int lineLength, byte[] lineSeparator, boolean urlSafe) {
        this(3, 4, lineLength, lineSeparator == null?0:lineSeparator.length);
        this.decodeTable = DECODE_TABLE;
        if(lineSeparator != null) {
            if(this.containsAlphabetOrPad(lineSeparator)) {
                String sep = newStringUtf8(lineSeparator);
                throw new IllegalArgumentException("lineSeparator must not contain base64 characters: [" + sep + "]");
            }

            if(lineLength > 0) {
                this.encodeSize = 4 + lineSeparator.length;
                this.lineSeparator = new byte[lineSeparator.length];
                System.arraycopy(lineSeparator, 0, this.lineSeparator, 0, lineSeparator.length);
            } else {
                this.encodeSize = 4;
                this.lineSeparator = null;
            }
        } else {
            this.encodeSize = 4;
            this.lineSeparator = null;
        }

        this.decodeSize = this.encodeSize - 1;
        this.encodeTable = urlSafe?URL_SAFE_ENCODE_TABLE:STANDARD_ENCODE_TABLE;
    }


    public static String newStringUtf8(byte[] bytes) {
        return bytes == null?null:new String(bytes, UTF_8);
    }

    public static String encodeBase64URLSafeString(byte[] binaryData) {
        return newStringUtf8(encodeBase64(binaryData, false, true));
    }

    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked, boolean urlSafe) {
        return encodeBase64(binaryData, isChunked, urlSafe, 2147483647);
    }

    protected boolean containsAlphabetOrPad(byte[] arrayOctet) {
        if(arrayOctet == null) {
            return false;
        } else {
            byte[] arr$ = arrayOctet;
            int len$ = arrayOctet.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                byte element = arr$[i$];
                if(61 == element || this.isInAlphabet(element)) {
                    return true;
                }
            }

            return false;
        }
    }

    protected boolean isInAlphabet(byte octet) {
        return octet >= 0 && octet < this.decodeTable.length && this.decodeTable[octet] != -1;
    }

    public static byte[] encodeBase64(byte[] binaryData, boolean isChunked, boolean urlSafe, int maxResultSize) {
        if(binaryData != null && binaryData.length != 0) {
            Base64 b64 = isChunked?new Base64(urlSafe):new Base64(0, CHUNK_SEPARATOR, urlSafe);
            long len = b64.getEncodedLength(binaryData);
            if(len > (long)maxResultSize) {
                throw new IllegalArgumentException("Input array too big, the output array would be bigger (" + len + ") than the specified maximum size of " + maxResultSize);
            } else {
                return b64.encode(binaryData);
            }
        } else {
            return binaryData;
        }
    }

    protected byte[] ensureBufferSize(int size, Context context) {
        return context.buffer != null && context.buffer.length >= context.pos + size?context.buffer:this.resizeBuffer(context);
    }

    private byte[] resizeBuffer(Context context) {
        if(context.buffer == null) {
            context.buffer = new byte[this.getDefaultBufferSize()];
            context.pos = 0;
            context.readPos = 0;
        } else {
            byte[] b = new byte[context.buffer.length * 2];
            System.arraycopy(context.buffer, 0, b, 0, context.buffer.length);
            context.buffer = b;
        }

        return context.buffer;
    }

    protected int getDefaultBufferSize() {
        return 8192;
    }

    void encode(byte[] in, int inPos, int inAvail, Context context) {
        if(!context.eof) {
            if(inAvail < 0) {
                context.eof = true;
                if(0 == context.modulus && this.lineLength == 0) {
                    return;
                }

                byte[] i = this.ensureBufferSize(this.encodeSize, context);
                int buffer = context.pos;
                switch(context.modulus) {
                    case 0:
                        break;
                    case 1:
                        i[context.pos++] = this.encodeTable[context.ibitWorkArea >> 2 & 63];
                        i[context.pos++] = this.encodeTable[context.ibitWorkArea << 4 & 63];
                        if(this.encodeTable == STANDARD_ENCODE_TABLE) {
                            i[context.pos++] = 61;
                            i[context.pos++] = 61;
                        }
                        break;
                    case 2:
                        i[context.pos++] = this.encodeTable[context.ibitWorkArea >> 10 & 63];
                        i[context.pos++] = this.encodeTable[context.ibitWorkArea >> 4 & 63];
                        i[context.pos++] = this.encodeTable[context.ibitWorkArea << 2 & 63];
                        if(this.encodeTable == STANDARD_ENCODE_TABLE) {
                            i[context.pos++] = 61;
                        }
                        break;
                    default:
                        throw new IllegalStateException("Impossible modulus " + context.modulus);
                }

                context.currentLinePos += context.pos - buffer;
                if(this.lineLength > 0 && context.currentLinePos > 0) {
                    System.arraycopy(this.lineSeparator, 0, i, context.pos, this.lineSeparator.length);
                    context.pos += this.lineSeparator.length;
                }
            } else {
                for(int var8 = 0; var8 < inAvail; ++var8) {
                    byte[] var9 = this.ensureBufferSize(this.encodeSize, context);
                    context.modulus = (context.modulus + 1) % 3;
                    int b = in[inPos++];
                    if(b < 0) {
                        b += 256;
                    }

                    context.ibitWorkArea = (context.ibitWorkArea << 8) + b;
                    if(0 == context.modulus) {
                        var9[context.pos++] = this.encodeTable[context.ibitWorkArea >> 18 & 63];
                        var9[context.pos++] = this.encodeTable[context.ibitWorkArea >> 12 & 63];
                        var9[context.pos++] = this.encodeTable[context.ibitWorkArea >> 6 & 63];
                        var9[context.pos++] = this.encodeTable[context.ibitWorkArea & 63];
                        context.currentLinePos += 4;
                        if(this.lineLength > 0 && this.lineLength <= context.currentLinePos) {
                            System.arraycopy(this.lineSeparator, 0, var9, context.pos, this.lineSeparator.length);
                            context.pos += this.lineSeparator.length;
                            context.currentLinePos = 0;
                        }
                    }
                }
            }

        }
    }

    public byte[] encode(byte[] pArray) {
        if(pArray != null && pArray.length != 0) {
            Context context = new Context();
            this.encode(pArray, 0, pArray.length, context);
            this.encode(pArray, 0, -1, context);
            byte[] buf = new byte[context.pos - context.readPos];
            this.readResults(buf, 0, buf.length, context);
            return buf;
        } else {
            return pArray;
        }
    }

    int available(Context context) {
        return context.buffer != null?context.pos - context.readPos:0;
    }

    int readResults(byte[] b, int bPos, int bAvail, Context context) {
        if(context.buffer != null) {
            int len = Math.min(this.available(context), bAvail);
            System.arraycopy(context.buffer, context.readPos, b, bPos, len);
            context.readPos += len;
            if(context.readPos >= context.pos) {
                context.buffer = null;
            }

            return len;
        } else {
            return context.eof?-1:0;
        }
    }

    public long getEncodedLength(byte[] pArray) {
        long len = (long)((pArray.length + this.unencodedBlockSize - 1) / this.unencodedBlockSize) * (long)this.encodedBlockSize;
        if(this.lineLength > 0) {
            len += (len + (long)this.lineLength - 1L) / (long)this.lineLength * (long)this.chunkSeparatorLength;
        }

        return len;
    }


    static class Context {
        int ibitWorkArea;
        long lbitWorkArea;
        byte[] buffer;
        int pos;
        int readPos;
        boolean eof;
        int currentLinePos;
        int modulus;

        Context() {
        }

        public String toString() {
            return String.format("%s[buffer=%s, currentLinePos=%s, eof=%s, ibitWorkArea=%s, lbitWorkArea=%s, modulus=%s, pos=%s, readPos=%s]", new Object[]{this.getClass().getSimpleName(), Arrays.toString(this.buffer), Integer.valueOf(this.currentLinePos), Boolean.valueOf(this.eof), Integer.valueOf(this.ibitWorkArea), Long.valueOf(this.lbitWorkArea), Integer.valueOf(this.modulus), Integer.valueOf(this.pos), Integer.valueOf(this.readPos)});
        }
    }
}
