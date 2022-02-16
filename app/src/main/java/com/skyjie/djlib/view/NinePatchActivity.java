package com.skyjie.djlib.view;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;


import com.skyjie.djlib.R;
import com.skyjie.djlib.utils.IntReader;
import com.skyjie.djlib.utils.NinePatchUtils;
import com.skyjie.djlib.utils.ninepatch.NinePatchChunk;

import java.io.IOException;
import java.io.InputStream;


public class NinePatchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nine_patch);

        TextView textView1 = (TextView) findViewById(R.id.ninepatch_tv1);
        Drawable drawable = getDrawable(R.drawable.bg_enter_effect);
        textView1.setBackground(drawable);

        TextView textView2 = (TextView) findViewById(R.id.ninepatch_tv2);
        try {
            Drawable drawable2 = NinePatchUtils.decodeDrawableFromAsset(this, "bg_enter_effect.9.png");
            textView2.setBackground(drawable2);
        } catch (Exception mE) {
            mE.printStackTrace();
        }
    }

    private void testNinePatch() {
        String pngName = "bg_enter_effect.9.png";
        try {
            InputStream is = getResources().getAssets().open(pngName);
            // Bitmap bitmap = BitmapFactory.decodeStream(is);
            // byte[] chunkData1 = bitmap.getNinePatchChunk();
            byte[] chunkData2 = loadNinePatchChunk(is);
            NinePatchChunk mChunk = chunkData2 == null ? null : NinePatchChunk.deserialize(chunkData2);
            if (mChunk == null) {
                throw new RuntimeException("invalid nine-patch image: " + pngName);
            }
            System.out.println(mChunk.mPaddings.toShortString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * PNG Chunk struct
     * <a href="http://dev.exiv2.org/projects/exiv2/wiki/The_Metadata_in_PNG_files">The Metadata in PNG files</a>
     *
     *   +--------+---------+
     *   | Length | 4 bytes |
     *   +--------+---------+
     *   | Chunk  | 4 bytes |
     *   |  type  |         |
     *   +--------+---------+
     *   | Chunk  | Length  |
     *   |  data  |  bytes  |
     *   +--------+---------+
     *   | CRC    | 4 bytes |
     *   +--------+---------+
     * @return chunk
     * @throws IOException
     */
    private byte[] loadNinePatchChunk(InputStream is) throws IOException {
        IntReader reader = new IntReader(is, true);
        // check PNG signature
        // A PNG always starts with an 8-byte signature: 137 80 78 71 13 10 26 10 (decimal values).
        if (reader.readInt() != 0x89504e47 || reader.readInt() != 0x0D0A1A0A) {
            return null;
        }

        while (true) {
            int length = reader.readInt();
            int type = reader.readInt();
            // check for nine patch chunk type (npTc)
            if (type != 0x6E705463) {
                reader.skip(length + 4/*crc*/);
                continue;
            }
            return reader.readByteArray(length);
        }
    }
}
