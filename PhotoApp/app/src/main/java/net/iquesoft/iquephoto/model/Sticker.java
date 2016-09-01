package net.iquesoft.iquephoto.model;

import net.iquesoft.iquephoto.R;

import java.util.Arrays;
import java.util.List;

/**
 * @author Sergey Belenkiy
 *         Stiker - model class how used in {@link net.iquesoft.iquephoto.view.fragment.StickersFragment} class.
 */
public class Sticker {
    private int title;
    private int image;

    /**
     * Array with "Flag" stickers;
     */
    public static Sticker flagStickers[] = {
            new Sticker(R.string.flag_ukraine, R.drawable.flag_ukraine),
            new Sticker(R.string.flag_russia, R.drawable.flag_russia),
            new Sticker(R.string.flag_germany, R.drawable.flag_germany),
            new Sticker(R.string.flag_brazil, R.drawable.flag_brazil)
    };

    /**
     * Array with "Emoticons" stickers;
     */
    public static Sticker emoticonsStickers[] = {
            new Sticker(R.string.emoticon_happy, R.drawable.emoticon_happy),
            new Sticker(R.string.emoticon_in_love, R.drawable.emoticon_in_love),
            new Sticker(R.string.emoticon_smile, R.drawable.emoticon_smile),
            new Sticker(R.string.emoticon_tongue_out, R.drawable.emoticon_tongue_out),
            new Sticker(R.string.emoticon_sad, R.drawable.emoticon_sad),
            new Sticker(R.string.emoticon_mad, R.drawable.emoticon_mad),


    };

    public Sticker(int title, int image) {
        this.title = title;
        this.image = image;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    /**
     * @return list with "Flag" stickers;
     */
    public static List<Sticker> getFlagStickersList() {
        return Arrays.asList(flagStickers);
    }

    /**
     * @return list with "Emoticons" stickers;
     */
    public static List<Sticker> getEmoticonsStickersList() {
        return Arrays.asList(emoticonsStickers);
    }
}