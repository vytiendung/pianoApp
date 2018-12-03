package com.piano.pianoapp;


import org.cocos2d.layers.CCLayer;
import org.cocos2d.nodes.CCNode;
import java.util.List;


public class LabelLayer extends CCLayer {
    public LabelLayer(){
        super();
        setIsTouchEnabled(false);
        setAnchorPoint(0f, 0f);
    }

    public CCNode addChild(CCNode child, int z, int flag){
        float sx = getScaleX();
        float sy = getScaleY();
        float tempSx = 1/sx;
        float tempSy = 1/sy;
        child.setScale(tempSx,tempSy);
        return super.addChild(child,z,flag);
    }

    public void updateChildPosWithoutScale(IKeyboard keyboard){
        keyboard.onPosResizeKeyboard();
        this.setPosition(((CCLayer)keyboard).getPosition().x,this.getPosition().y);
    }

    public void updateChildPos(IKeyboard keyboard) {
        keyboard.onPosResizeKeyboard();
        float sx = ((CCLayer)keyboard).getScaleX();
        float sy = ((CCLayer)keyboard).getScaleY();
        float tempSx = 1/sx;
        float tempSy = 1/sy;

        this.setPosition(((CCLayer)keyboard).getPosition().x,this.getPosition().y);
        this.setScale(sx, sy);

        List<CCNode> nodes = getChildren();
        if(nodes !=null) {
            for (CCNode label : nodes) {
                label.setScale(tempSx,tempSy);
            }
        }
    }
}
