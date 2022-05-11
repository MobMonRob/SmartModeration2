package dhbw.smartmoderation.moderationCard;

import android.content.Context;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplicationImpl;

public class ModerationCardColorImporter {
    private static ModerationCardColorImporter moderationCardColorImporter;

    private final HashMap<Integer, Integer> textColorOfBackground;
    private final int[] backgroundColors;


    public static ModerationCardColorImporter getInstance() {
        if (moderationCardColorImporter == null) {
            moderationCardColorImporter = new ModerationCardColorImporter();
        }
        return moderationCardColorImporter;
    }

    public ModerationCardColorImporter() {
        Context context = SmartModerationApplicationImpl.getApp().getApplicationContext();
        textColorOfBackground = new HashMap<>();
        ArrayList<String> backgroundColorSet = new ArrayList<>();
        Collections.addAll(backgroundColorSet,  context.getResources().getStringArray(R.array.moderationCardBackgroundColorset));

        ArrayList<String> fontColorSet = new ArrayList<>();
        Collections.addAll(fontColorSet, context.getResources().getStringArray(R.array.moderationCardFontColorset));

        backgroundColors = new int[backgroundColorSet.size()];
        for (int i = 0; i <backgroundColorSet.size(); i++) {
            backgroundColors[i] = Color.parseColor(backgroundColorSet.get(i));
            textColorOfBackground.put(backgroundColors[i], Color.parseColor(fontColorSet.get(i)));
        }
    };

    public int[] getBackgroundColors() {
        return backgroundColors;
    }

    public int getFontColor(int backgroundColor) {
        try {
            return textColorOfBackground.get(backgroundColor);
        } catch (NullPointerException e) {
            if (backgroundColor >= Color.BLACK && backgroundColor < Color.GRAY) {
                return Color.WHITE;
            } else {
                return  Color.BLACK;
            }
        }
    }
}
