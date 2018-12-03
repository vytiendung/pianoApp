package com.piano.pianoapp;

import com.piano.pianoapp.util.Config;
import com.piano.pianoapp.util.Constant;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.nodes.CCSpriteFrameCache;
import org.cocos2d.types.CGSize;

public class NoteObject extends CCLayer {
    public static final String NOTE_FRAME_NAME_TINY = "guide_note_tiny.png";
    public static final String NOTE_FRAME_NAME_SMALL = "guide_note_small.png";
    public static final String NOTE_FRAME_NAME_DEFAULT = "guide_note.png";

    public int border = 0;

    private String frameName;
    private MidiNote midiNote;
    public float distance;
    private UserConfig userConfig;

    public NoteObject(MidiNote midiNote) {
        super();
        this.midiNote = midiNote;
        distance = (midiNote.duration / 1000) * Config.getInstance().speed;
        userConfig = UserConfig.getInstance();
        render();
    }

    private void render() {
        if (midiNote == null) return;
        buildFrameNameByDuration();
        CCSprite sprite = createSprite();
        border = 0;
        if (midiNote.name.contains("m")) {
            sprite.setColor(userConfig.getBlackNoteGuideColor());
            sprite.setScale(Config.getInstance().keyWidthBlack / sprite.getContentSizeRef().width,
                    distance / sprite.getContentSizeRef().height);
        } else {
            sprite.setColor(userConfig.getWhiteNoteGuideColor());
            sprite.setScale(Config.getInstance().keyWidthWhite / sprite.getContentSizeRef().width,
                    distance / sprite.getContentSizeRef().height);
        }

        addChild(sprite);
    }

    private void buildFrameNameByDuration() {
        if (midiNote.duration < 200) {
            frameName = NOTE_FRAME_NAME_TINY;
        } else if (midiNote.duration < 700) {
            frameName = NOTE_FRAME_NAME_SMALL;
        } else {
            frameName = NOTE_FRAME_NAME_DEFAULT;
        }
    }

    private CCSprite createSprite() {
        CCSpriteFrame sf = CCSpriteFrameCache.sharedSpriteFrameCache().spriteFrameByName(frameName);
        CCSprite sprite = CCSprite.sprite(sf);
        sprite.setAnchorPoint(0.5f, 0f);
        return sprite;
    }

    public void glow() {
        removeAllChildren(true);
        frameName = getFrameNameForGlowingNote();
        CCSprite sprite = createSprite();
        border = (Config.getInstance().deviceType == Constant.SMALL ? 15 : 22) * 2;
	    CGSize size = sprite.getContentSizeRef();
	    if (midiNote.name.contains("m")) {
		    sprite.setColor(userConfig.getBlackNoteGuideColor());
		    sprite.setScale(Config.getInstance().keyWidthBlack / (size.width - border), distance / (size.height - border));
	    } else {
		    sprite.setColor(userConfig.getWhiteNoteGuideColor());
		    size = sprite.getContentSizeRef();
		    sprite.setScale(Config.getInstance().keyWidthWhite / (size.width - border), distance / (size.height - border));
	    }

	    addChild(sprite);
	    sprite.setPosition(0, -border*sprite.getScaleY()/2);
    }

	public void release() {
		removeAllChildren(true);
		buildFrameNameByDuration();
		CCSprite sprite = createSprite();
		border = 0;
		if (midiNote.name.contains("m")) {
			sprite.setScale(Config.getInstance().keyWidthBlack / sprite.getContentSizeRef().width,
					distance / sprite.getContentSizeRef().height);
		} else {
			sprite.setScale(Config.getInstance().keyWidthWhite / sprite.getContentSizeRef().width,
					distance / sprite.getContentSizeRef().height);
		}
		addChild(sprite);
	}

	public String getNoteName(){
		return midiNote.name;
	}

    private String getFrameNameForGlowingNote() {
        return frameName.replace(".png", "_glow.png");
    }
}
