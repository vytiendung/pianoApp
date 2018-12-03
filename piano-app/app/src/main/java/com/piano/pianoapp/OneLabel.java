package com.piano.pianoapp;

import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.types.ccColor3B;

public class OneLabel extends CCNode {
    private CCLabel label;
    private CCSprite bg;
    public OneLabel(String string, String fontname, float fontsize){
        super();
        label = CCLabel.makeLabel(string,fontname,fontsize);
        label.setPosition(0,0);
        addChild(label,2);
    }

    public void setLabelColor(ccColor3B color3)
    {
        label.setColor(color3);
    }

    public void setLabelScale(float scale)
    {
        label.setScale(scale);
    }

    public void setLabelPosition(float x, float y)
    {
        label.setPosition(x, y);
    }

    public void createBackgroundLabel(CCSpriteFrame spriteFrame){
        bg = CCSprite.sprite(spriteFrame);
        bg.setPosition(bg.getContentSize().width / 2, bg.getContentSize().height / 2);
        addChild(bg, 1);
    }

    public void setBgColor(ccColor3B color3){
        if(bg != null){
            bg.setColor(color3);
        }
    }

    public void setBgScale(float scale){
        if(bg != null){
            bg.setScale(scale);
            bg.setPosition(bg.getContentSize().width * scale / 2, bg.getContentSize().height * scale / 2);
        }
    }

	public void showLabel(boolean showBg)
	{
		label.setVisible(true);
		if(bg != null)
			bg.setVisible(showBg);
	}

	public void hideLabel()
	{
		label.setVisible(false);
		if(bg != null)
			bg.setVisible(false);
	}

    public CCSprite getBg()
    {
        return bg;
    }
}
